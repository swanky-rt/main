package project_645;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Step 1:


public class ClosedTest {


    String workingDirectory = System.getProperty("user.dir");
    String testFileDirectory = "/src/test/java/project_645/DB files/";

    String fileName = "testdbfile.dat";
    @BeforeEach
    public void setUp() {


        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(path);
            System.out.println("File created: " + path.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(path)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
    }

    @AfterEach
    public void teardown() {
        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
        try {
            Files.deleteIfExists(path); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }
    }


    //     Test1:
//     For a buffer size of 1, Continuosly insert, evict and check
//     Evicted page must be written to disk.
    @Test
    void testCreationAndEviction() throws Exception {
        // Create a buffer of size 2 pages
        int bufferSize = 10 * 4096;
        BufferManagerImpl bf = new BufferManagerImpl(bufferSize, workingDirectory + testFileDirectory, "testdbfile.dat");
        int counter = 0;
        int maxPages = 1000;
        int rows_per_page = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.createPage();
            while(! page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            bf.markDirty(page.getPid());
            bf.unpinPage(page.getPid());
        }
        int maxRows = counter;
        // System.out.print("max rows" +  maxRows + " " + rows_per_page);
        counter = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.getPage(i);
            bf.unpinPage(i);
            for(int localRow = 0; localRow < rows_per_page; localRow ++ ){
                Row row = page.getRow(localRow);
                String movString = "movie" + counter;
                if (movString.length()>9){
                    movString = movString.substring(0, 9);
                }
                String refmovie =  StringUtils.rightPad(movString, 9, '*');
                String reftitle = StringUtils.rightPad("title" + counter, 30, '*');
                assertEquals(new String(row.movieId), refmovie);
                assertEquals(new String(row.title), reftitle);
                counter = counter + 1;
            }
        }
    }


    // Updates not marked dirty are not persisted.
     @Test
    void testMarkDirty() throws Exception {
        // Create a buffer of size 2 pages
        int bufferSize = 2;
        BufferManagerImpl bf = new BufferManagerImpl(bufferSize * 4096, workingDirectory + testFileDirectory, "testdbfile.dat");
        int counter = 0;
        int maxPages = 4;
        int rows_per_page = 0;

        for(int i = -1; i >=-5; i--){
            Page page = bf.createPage();
            bf.unpinPage(i);
        }
        int[] pageIdxList = {0, 1, 2, 3};
        for(int i : pageIdxList){
            Page page = bf.getPage(i);
            bf.unpinPage(page.getPid());
            while(! page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                page.markNotDirty();
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            if(i %2 == 0) bf.markDirty(i);
            bf.unpinPage(i);
        }

        for(int i = 0; i < maxPages; i ++ ){
            Page pg = bf.getPage(i);
            bf.unpinPage(pg.getPid());
            if (i % 2 == 0){
                assertTrue(pg.isFull());
            }else{
                assertFalse(pg.isFull());
            }
        }
    }

//    // LRU eviction of pages.
    @Test
    void testLRUEviction() throws Exception {
        // Create a buffer of size 2 pages
        int bufferSize = 5;
        BufferManagerImpl bf = new BufferManagerImpl(bufferSize * 4096, workingDirectory + testFileDirectory, "testdbfile.dat");
        int counter = 0;
        int maxPages = 1000;
        int rows_per_page = 0;
        for(int i = 0; i < maxPages; i ++){
            Page page = bf.createPage();
            while(! page.isFull()){
                String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                String title = StringUtils.rightPad("title" + counter, 50, '*');
                Row row =new Row(movie.getBytes(), title.getBytes());
                page.insertRow(row);
                counter = counter + 1;
            }
            if (i == 0) rows_per_page =counter;
            bf.unpinPage(page.getPid());
            if (i >= 5) {
                bf.getPage(0);
                bf.unpinPage(0);
            }
        }
        long s1 = System.nanoTime();
        bf.getPage(0);
        long e1 = System.nanoTime();
        long d1 = e1 - s1;

        long s2 = System.nanoTime();
        bf.getPage(-999);
        long e2 = System.nanoTime();
        long d2 = e2 - s2;


        long s3 = System.nanoTime();
        bf.getPage(1);
        long e3 = System.nanoTime();
        long d3 = e3 - s3;
        System.out.println("" + d3 + " " + d1 + " " + d2);
        assertTrue(d3 > d1 * 2);
        assertTrue(d3 > d2 * 2);
    }
//
//    // pinned pages are not evicted, creating more than buffer manager size causes exception
    @Test
    void testPinnedEviction() throws IOException {
        // Create a buffer of size 2 pages
        int bufferSize = 3;
        BufferManagerImpl bf = new BufferManagerImpl(bufferSize * 4096, workingDirectory + testFileDirectory, "testdbfile.dat");
        int counter = 0;
        int maxPages = 10;
        int rows_per_page = 0;
        try{
            for(int i = 0; i < maxPages; i ++){
                Page page = bf.createPage();
                while(! page.isFull()){
                    String movie =  StringUtils.rightPad("movie" + counter, 9, '*');
                    String title = StringUtils.rightPad("title" + counter, 50, '*');
                    Row row =new Row(movie.getBytes(), title.getBytes());
                    page.insertRow(row);
                    counter = counter + 1;
                }

            }
            assertTrue(false);
        }catch (Exception e){
            assertTrue(true);
        }
    }
//
//    // test loading of imdb dataset
    @Test
    void testImdbDataset() throws Exception {
        // Create a buffer of size 2 pages
        int bufferSize1 = 3;
        BufferManagerImpl bf1 = new BufferManagerImpl(bufferSize1 * 4096, workingDirectory + testFileDirectory, "testdbfile.dat");

        String filepath = "title.basics.tsv";
        bf1.populateDisk(700, workingDirectory + testFileDirectory);
        Utilities ut = new Utilities(filepath, "testdb.dat");
        ut.loadDataset(bf1, workingDirectory + testFileDirectory);
        Row row1 = bf1.getPage(0).getRow(19);

        int bufferSize2 = 30;
        BufferManagerImpl bf2 = new BufferManagerImpl(bufferSize1 * 4096, workingDirectory + testFileDirectory, "testdbfile.dat");
        ut.loadDataset(bf2, workingDirectory + testFileDirectory);
        Row row2 = bf2.getPage(0).getRow(19);

        assertEquals(new String(row1.movieId),new String(row2.movieId));
    }


}