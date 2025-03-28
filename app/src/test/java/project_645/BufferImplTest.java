package project_645;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

public class BufferImplTest {
    private BufferManagerImpl bufferManager;
    private RandomAccessFile randomAccessFile;
    private Path path1;
    private Page page;
    String path = "/src/main/java/project_645/DB files/";
    String mainFileName = "title.basics.tsv";
    String diskFileName = "testdb.dat";
    String movieIdIndexFileName = "movieIdIndex.dat";
    String movieTitleIndexFileName = "movieTitleIndex.dat";
    String filePath = System.getProperty("user.dir") + path;

    String workingDirectory = System.getProperty("user.dir");
    String testFileDirectory = "/src/test/java/project_645/DB files/";
    String testFilePath = System.getProperty("user.dir") + testFileDirectory;

    String fileName = "testdbfile.dat";

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

    @BeforeEach
    void setUp() throws IOException {
        bufferManager = mock(BufferManagerImpl.class);
        randomAccessFile = mock(RandomAccessFile.class);
        path1 = mock(Path.class);
        page = mock(Page.class);

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

        bufferManager = new BufferManagerImpl(2, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName);

    }

    @Test
    public void testCreatePage(){
        bufferManager.bufferPool.clear();
        Page page1 = bufferManager.createPage(File.DISK);
        assertNull(bufferManager.createPage(File.DISK));
    }

    @Test
    public void testGetPageTest() throws Exception {
        Page page = new PageImpl(1, File.DISK);
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(page.getPid(), File.DISK), page);
        Page fetchedPage = bufferManager.getPage(1, File.DISK);
        assertNotNull(fetchedPage);
        assertEquals(page, fetchedPage);
    }

    @Test
    public void testUnpinPage(){
        Page page1 = new PageImpl(1, File.DISK);
        Page page2 = new PageImpl(2, File.DISK);
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(page1.getPid(), File.DISK), page1);
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(page2.getPid(), File.DISK), page2);
        bufferManager.pinnedPages.put(bufferManager.constructPageIdentifier(page1.getPid(), File.DISK), 1);
        bufferManager.unpinPage(1, File.DISK);
        assertFalse(bufferManager.pinnedPages.containsKey(Integer.toString(1)));
    }

    @Test
    public void testUnpinPageifNoPinPage(){
        Page page1 = new PageImpl(1, File.DISK);
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(page1.getPid(), File.DISK), page1);
        bufferManager.pinnedPages.put(bufferManager.constructPageIdentifier(page1.getPid(), File.DISK), 1);
        bufferManager.unpinPage(1, File.DISK);
        assertEquals(0, bufferManager.pinnedPages.size());
    }

    @Test
    public void testPinPage(){
        Page page = new PageImpl(1, File.DISK);
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(page.getPid(), File.DISK), page);
        bufferManager.pinPage(bufferManager.constructPageIdentifier(page.getPid(), File.DISK));
        assertTrue(bufferManager.pinnedPages.containsKey(bufferManager.constructPageIdentifier(page.getPid(), File.DISK)));
        assertEquals(1, page.getPinCount());
    }

    @Test
    public void testgetNextPageId(){
        int nextPageId = bufferManager.getNextPageId();
        assertEquals(0, nextPageId);
        nextPageId = bufferManager.getNextPageId();
        assertEquals(1, nextPageId);

    }
    @Test
    public void testMarkDirty(){
        bufferManager.bufferPool.put(bufferManager.constructPageIdentifier(1, File.DISK), page);
        bufferManager.markDirty(1, File.DISK);
        verify(page).markDirty();
    }


    @Test
    void testWritePageToDiskAndLoadPageFromDisk() throws IOException {
        BufferManagerImpl bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieTitleIndexFileName);
        Page page1 = new PageImpl(5, File.DISK);
        // populate the page
        page1.insertRow(new Row("movie 1".getBytes(), "movie title 1".getBytes()));
        page1.insertRow(new Row("movie 2".getBytes(), "movie title 2".getBytes()));
        page1.insertRow(new Row("movie 3".getBytes(), "movie title 3".getBytes()));

        // attempt to write the page to disk, should throw an exception as page with id 5 isn't
        // the next available free space on disk. This should now write 4 blank pages before writing the 5th.
        bf.writePageToDisk(page1, File.DISK);
        long numPages = Files.size(Paths.get( testFilePath + fileName).toAbsolutePath()) / 4096;
        assertEquals(6, numPages);



        // Attempt to write page to disk at valid index (index 0)
        page1.reassignPageId(0);
        assertDoesNotThrow(() -> bf.writePageToDisk(page1, File.DISK));
        // Attempt to load this page from disk.
        Page loadedPage = bf.loadPageFromDisk(page1.getPid(), File.DISK);
        //verify it's the same page by checking the row count and bytes in row 1
        assertEquals(loadedPage.getRowCount(), page1.getRowCount());
        assertArrayEquals(loadedPage.getRow(0).getMovieId(), page1.getRow(0).getMovieId());
        assertArrayEquals(loadedPage.getRow(0).getTitle(), page1.getRow(0).getTitle());
        // attempt to access a new page out of bounds, returns null
        assertNull(bf.loadPageFromDisk(10, File.DISK));

        // insert a few more pages
        Page page2 = new PageImpl(1, File.DISK);
        Page page3 = new PageImpl(2, File.DISK);
        Page page4 = new PageImpl(3, File.DISK);

        // populate page3 with records
        page3.insertRow(new Row("3movie 1".getBytes(), "3movie title 1".getBytes()));
        page3.insertRow(new Row("3movie 2".getBytes(), "3movie title 2".getBytes()));
        page3.insertRow(new Row("3movie 3".getBytes(), "3movie title 3".getBytes()));

        // write to disk
        assertDoesNotThrow(() -> bf.writePageToDisk(page2, File.DISK));
        assertDoesNotThrow(() -> bf.writePageToDisk(page3, File.DISK));
        assertDoesNotThrow(() -> bf.writePageToDisk(page4, File.DISK));

        //ensure that loading a page in the middle of the buffer pool loads the proper information
        Page loadedPage2 = bf.loadPageFromDisk(2, File.DISK);
        assertEquals(page3.getRowCount(), loadedPage2.getRowCount());
        assertArrayEquals(page3.getRow(0).getMovieId(), loadedPage2.getRow(0).getMovieId());
        assertArrayEquals(page3.getRow(0).getTitle(), loadedPage2.getRow(0).getTitle());
    }

    @Test
    void testGetPageEvictPageAndCreatePage() throws Exception {
        BufferManager bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieTitleIndexFileName);
        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
        // First, attempt to get a page with ID that does not appear on disk or in the buffer manager
        assertNull(bf.getPage(0, File.DISK));

        // now, create a page in the buffer manager.
        Page page1 = bf.createPage(File.DISK);
        int page1Id = page1.getPid();
        // check that the page is returned by getPage()
        Page retreivedPage = bf.getPage(page1.getPid(), File.DISK);
        // at this point should be the same reference since it was never evicted from the buffer pool
        assertEquals(page1, retreivedPage);
        // at this point, the pin count should be 2, decrement the pin count by one
        bf.unpinPage(page1.getPid(), File.DISK);
        // fill the buffer pool with only pinned pages
        Page page2 = bf.createPage(File.DISK);
        int page2Id = page2.getPid();
        Page page3 = bf.createPage(File.DISK);
        Page page4 = bf.createPage(File.DISK);
        // Now, attempting to create a new page should return null. There is no space in the buffer pool,
        // and all pages are pinned, so attempting to create a page will result in the page not being
        // created
        assertNull(bf.createPage(File.DISK));
        // Verify that all previously created pages are in the buffer pool
        assertEquals(page1, bf.getPage(page1.getPid(), File.DISK));
        assertEquals(page2, bf.getPage(page2.getPid(), File.DISK));
        assertEquals(page3, bf.getPage(page3.getPid(), File.DISK));
        assertEquals(page4, bf.getPage(page4.getPid(), File.DISK));

        // All pin counts should be 2, decrement pin count to 0 for every page except page 1
        bf.unpinPage(page2.getPid(), File.DISK);
        bf.unpinPage(page2.getPid(), File.DISK);
        bf.unpinPage(page3.getPid(), File.DISK);
        bf.unpinPage(page3.getPid(), File.DISK);
        bf.unpinPage(page4.getPid(), File.DISK);
        bf.unpinPage(page4.getPid(), File.DISK);
        //unpin page 1 once
        bf.unpinPage(page1.getPid(), File.DISK);

        // page 1 is LRU, but is pinned, so attempting to add a new page should evict page 2.

        Page page5 = bf.createPage(File.DISK);
        //check that page 1 is in the buffer pool and page 2 is not.
        assertEquals(page1.getPid(), bf.getPage(page1.getPid(), File.DISK).getPid());

        // page 2 should have disk ID now and not temporary ID
//        Page page2FromDisk = bf.getPage(page2.getPid());
//        assertNull(bf.getPage(page2Id));
        // unpin page1 completely and ready it for eviction
        bf.unpinPage(page1.getPid(), File.DISK);
        bf.unpinPage(page1.getPid(), File.DISK);

        bf.getPage(page2.getPid(), File.DISK);
        bf.getPage(page3.getPid(), File.DISK);
        bf.getPage(page4.getPid(), File.DISK);
        bf.unpinPage(page2.getPid(), File.DISK);
        bf.unpinPage(page2.getPid(), File.DISK);
        // creating page should evict
        bf.createPage(File.DISK);
        bf.unpinPage(page3.getPid(), File.DISK);
        // Returned ID and stored ID should be the same
        Page page1FromDisk = bf.getPage(page1.getPid(), File.DISK);
        assertEquals(page1Id, page1FromDisk.getPid());
    }

    // Test mark dirty
    @Test
    void testMarkDirty2() throws Exception {
        BufferManager bf = new BufferManagerImpl(4096, workingDirectory + testFileDirectory, fileName,
                movieIdIndexFileName, movieTitleIndexFileName);

        Page page1 = bf.createPage(File.DISK);

        // for the sake of these tests, we will be calling a method to set the boolean in the page object to not dirty
        // note that this should never be called outside of the buffer manager

        page1.markNotDirty();

        assertFalse(page1.getDirtyStatus());

        // manually mark dirty and unpin page1
        bf.markDirty(page1.getPid(), File.DISK);
        bf.unpinPage(page1.getPid(), File.DISK);

        // check if page1 is marked dirty
        assertTrue(page1.getDirtyStatus());

        // create a page, forcing a write to disk
        Page page2 = bf.createPage(File.DISK);
        // Another check that attempting to get a page when all are pinned shouldn't change the buffer pool
        Page page3 = bf.getPage(page1.getPid(), File.DISK);
        // unpin it and load page1 back from disk
        bf.unpinPage(page2.getPid(), File.DISK);
        // load page1 from disk
        Page page1FromDisk = bf.getPage(page1.getPid(), File.DISK);
        assertFalse(page1FromDisk.getDirtyStatus());

    }

    @Test
    void verifyOnlyContinuousIdsCanBeWrittenToDisk() throws Exception {
//        BufferManager bf = new BufferManagerImpl(4096, workingDirectory + "/not/a/real/directory", fileName,
//                movieIdIndexFileName, movieTitleIndexFileName);

//        // hits a certain exception to achieve 100% test coverage
//        Page page1 = bf.createPage();
//        bf.unpinPage(page1.getPid());
//        Page page2 = bf.createPage();
//        bf.unpinPage(page2.getPid());
//        Page page3 = bf.getPage(0);

    }
}
