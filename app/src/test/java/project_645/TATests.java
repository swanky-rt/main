package project_645;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TATests {

    // Verify that all creations are written to disk if marked dirty
    String workingDirectory = System.getProperty("user.dir");
    String testFileDirectory = "/src/test/java/project_645/DB files/";

    String fileName = "testdbfile.dat";
    String movieIdIndexFileName = "movieIdIndex.dat";
    String movieIdTitleFileName = "movieTitleIndex.dat";

    private BufferManagerImpl bufferManager;
    private RandomAccessFile randomAccessFile;
    private Path path1;
    private Page page;
    String path = "/src/main/java/project_645/DB files/";
    String movieTitleIndexFileName = "movieTitleIndex.dat";
    String filePath = System.getProperty("user.dir") + path;

    String testFilePath = System.getProperty("user.dir") + testFileDirectory;

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

        Path movieIdPath = Paths.get(workingDirectory + testFileDirectory + movieIdIndexFileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieIdPath);
            System.out.println("File created: " + movieIdPath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieIdPath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }

        Path movieTitlePath = Paths.get(workingDirectory + testFileDirectory + movieTitleIndexFileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieTitlePath);
            System.out.println("File created: " + movieTitlePath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieTitlePath)) {
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

        Path movieIdPath = Paths.get(workingDirectory, testFileDirectory, movieIdIndexFileName);
        try {
            Files.deleteIfExists(movieIdPath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path movieTitlePath = Paths.get(workingDirectory, testFileDirectory, movieTitleIndexFileName);
        try {
            Files.deleteIfExists(movieTitlePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }
    }

    @Test
    void testCreationAndEviction() throws Exception {
        BufferManager bf = new BufferManagerImpl(100 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieIdTitleFileName);
        int counter = 0;
        for (int i = 0; i < 10000; ++i) {
            Page page = bf.createPage(File.DISK);
            // bf.getPage(page.getPid());
            while (! page.isFull()) {
                Row row = new Row(("Movie " + counter).getBytes(), ("Title " + counter).getBytes(), null, null, null);
                page.insertRow(row);
                counter += 1;
            }
            // From Piazza post @50:
            // https://piazza.com/class/m69unvy0rxi3uh/post/50
            // The buffer manager increments the pin count when a page is requested through the getPage or
            // createPage method. It decrements the pin count when the caller invokes unpinPage,
            // which indicates that the caller does not need the page any longer.
            // Therefore, since both createPage and getPage is called on this page, we need to decrement
            // the pin count twice for each page to be elegible for eviction
            bf.markDirty(i, File.DISK);
            bf.unpinPage(page.getPid(), File.DISK);
            bf.unpinPage(page.getPid(), File.DISK);
        }
        counter = 0;
        for (int i = 0; i < 10000; ++i) {
            Page page = bf.getPage(i, File.DISK);
            try {
                int j = 0;
                byte[] movieIdByteArr = ("Movie " + counter).getBytes(StandardCharsets.US_ASCII);
                byte[] movieTitleByteArr = ("Title " + counter).getBytes(StandardCharsets.US_ASCII);
                byte[] movieIdFixedLength = new byte[9];
                byte[] movieTitleFixedLength = new byte[30];
                System.arraycopy(movieIdByteArr, 0, movieIdFixedLength, 0, Math.min(movieIdByteArr.length, movieIdFixedLength.length));
                System.arraycopy(movieTitleByteArr, 0, movieTitleFixedLength, 0, Math.min(movieTitleByteArr.length, movieTitleFixedLength.length));
                assertArrayEquals(movieIdFixedLength, page.getRow(j).getMovieId());
                assertArrayEquals(movieTitleFixedLength, page.getRow(j).getTitle());
                bf.unpinPage(page.getPid(), File.DISK);
                counter += 105;
            }
            catch (Exception e) {

            }
        }
    }

    @Test
    void testLRUEviction() throws Exception {
        BufferManager bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieIdTitleFileName);

        for (int i = 0; i < 5; ++i) {
            Page page = bf.createPage(File.DISK);
            bf.unpinPage(page.getPid(), File.DISK);
        }
        long startTime = System.nanoTime();
        bf.getPage(0, File.DISK);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long startTime1 = System.nanoTime();
        bf.getPage(-5, File.DISK);
        long endtime1 = System.nanoTime();
        long duration1 = endtime1 - startTime1;

        assert((duration) > duration1);
    }

    @Test
    void testPinnedLRUEviction() throws Exception {
        BufferManager bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieIdTitleFileName);
        Page page = bf.createPage(File.DISK);
        Page tempPage = bf.createPage(File.DISK);
        bf.unpinPage(tempPage.getPid(), File.DISK);
        for (int i = 0; i < 5; ++i) {
            Page curPage = bf.createPage(File.DISK);
            bf.unpinPage(curPage.getPid(), File.DISK);
        }
        long startTime = System.nanoTime();
        // get time of first evicted page, assigned id 0 at write time
        bf.getPage(0, File.DISK);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        long startTime1 = System.nanoTime();
        bf.getPage(page.getPid(), File.DISK);
        long endTime1 = System.nanoTime();
        long duration1 = endTime1 - startTime1;
        assert(duration1 < duration);
    }

    @Test
    void testEditNotWrittenIfNotMarkedDirty() throws Exception {
        BufferManager bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieIdTitleFileName);
        Page page = bf.createPage(File.DISK);
        page.insertRow(new Row("Movie1".getBytes(StandardCharsets.US_ASCII), "Title1".getBytes(StandardCharsets.US_ASCII), null, null, null));
        page.markNotDirty();
        bf.unpinPage(page.getPid(), File.DISK);
        for (int i = 0; i < 5; ++i) {
            Page page1 = bf.createPage(File.DISK);
            page1.insertRow(new Row("Movie2".getBytes(StandardCharsets.US_ASCII), "Title2".getBytes(StandardCharsets.US_ASCII), null, null, null));
            bf.unpinPage(page1.getPid(), File.DISK);
        }
        byte[] movieIdByteArr = ("Movie2").getBytes(StandardCharsets.US_ASCII);
        byte[] movieIdFixedLength = new byte[9];
        System.arraycopy(movieIdByteArr, 0, movieIdFixedLength, 0, Math.min(movieIdByteArr.length, movieIdFixedLength.length));

        // Check that the first inserted page into the buffer manager is that marked not dirty.
        assertNull(bf.getPage(0, File.DISK).getRow(0));

    }

}
