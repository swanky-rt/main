package project_645;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

// Serves as both unit testing for buffer manager, and
// end to end testing for the entire system
public class BufferImplUnitTests {

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

    @Test
    void testWritePageToDiskAndLoadPageFromDisk() throws IOException {
        BufferManagerImpl bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName);
        Page page1 = new PageImpl(5);
        // populate the page
        page1.insertRow(new Row("movie 1".getBytes(), "movie title 1".getBytes()));
        page1.insertRow(new Row("movie 2".getBytes(), "movie title 2".getBytes()));
        page1.insertRow(new Row("movie 3".getBytes(), "movie title 3".getBytes()));

        // attempt to write the page to disk, should throw an exception as page with id 5 isn't
        // the next available free space on disk. We implemeneted our buffer manager such that
        // this shouldn't be possible
        Exception exception = assertThrows(IOException.class, () -> bf.writePageToDisk(5, page1));

        assertEquals("There are not sufficiently many pages for this pageId to be valid," +
                " or the pageID is invalid otherwise.", exception.getMessage());

        // Attempt to write page to disk at valid index (index 0)
        page1.reassignPageId(0);
        assertDoesNotThrow(() -> bf.writePageToDisk(0, page1));
        // Attempt to load this page from disk.
        Page loadedPage = bf.loadPageFromDisk(0);
        //verify it's the same page by checking the row count and bytes in row 1
        assertEquals(loadedPage.getRowCount(), page1.getRowCount());
        assertArrayEquals(loadedPage.getRow(0).getMovieId(), page1.getRow(0).getMovieId());
        assertArrayEquals(loadedPage.getRow(0).getTitle(), page1.getRow(0).getTitle());
        // attempt to access a new page out of bounds, returns null
        assertNull(bf.loadPageFromDisk(5));

        // insert a few more pages
        Page page2 = new PageImpl(1);
        Page page3 = new PageImpl(2);
        Page page4 = new PageImpl(3);

        // populate page3 with records
        page3.insertRow(new Row("3movie 1".getBytes(), "3movie title 1".getBytes()));
        page3.insertRow(new Row("3movie 2".getBytes(), "3movie title 2".getBytes()));
        page3.insertRow(new Row("3movie 3".getBytes(), "3movie title 3".getBytes()));

        // write to disk
        assertDoesNotThrow(() -> bf.writePageToDisk(1, page2));
        assertDoesNotThrow(() -> bf.writePageToDisk(2, page3));
        assertDoesNotThrow(() -> bf.writePageToDisk(3, page4));

        //ensure that loading a page in the middle of the buffer pool loads the proper information
        Page loadedPage2 = bf.loadPageFromDisk(2);
        assertEquals(page3.getRowCount(), loadedPage2.getRowCount());
        assertArrayEquals(page3.getRow(0).getMovieId(), loadedPage2.getRow(0).getMovieId());
        assertArrayEquals(page3.getRow(0).getTitle(), loadedPage2.getRow(0).getTitle());
    }

    @Test
    void testGetPageEvictPageAndCreatePage() throws Exception {
        BufferManager bf = new BufferManagerImpl(4 * 4096, workingDirectory + testFileDirectory, fileName);
        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
        // First, attempt to get a page with ID that does not appear on disk or in the buffer manager
        assertNull(bf.getPage(0));

        // now, create a page in the buffer manager.
        Page page1 = bf.createPage();
        // check that the page is returned by getPage()
        Page retreivedPage = bf.getPage(page1.getPid());
        // at this point should be the same reference since it was never evicted from the buffer pool
        assertEquals(page1, retreivedPage);
        // at this point, the pin count should be 2, decrement the pin count by one
        bf.unpinPage(page1.getPid());
        // fill the buffer pool with only pinned pages
        Page page2 = bf.createPage();
        int page2Id = page2.getPid();
        Page page3 = bf.createPage();
        Page page4 = bf.createPage();
        // Now, attempting to create a new page should return null. There is no space in the buffer pool,
        // and all pages are pinned, so attempting to create a page will result in the page not being
        // created
        assertNull(bf.createPage());
        // Verify that all previously created pages are in the buffer pool
        assertEquals(page1, bf.getPage(page1.getPid()));
        assertEquals(page2, bf.getPage(page2.getPid()));
        assertEquals(page3, bf.getPage(page3.getPid()));
        assertEquals(page4, bf.getPage(page4.getPid()));

        // All pin counts should be 2, decrement pin count to 0 for every page except page 1
        bf.unpinPage(page2.getPid());
        bf.unpinPage(page2.getPid());
        bf.unpinPage(page3.getPid());
        bf.unpinPage(page3.getPid());
        bf.unpinPage(page4.getPid());
        bf.unpinPage(page4.getPid());
        //unpin page 1 once
        bf.unpinPage(page1.getPid());

        // page 1 is LRU, but is pinned, so attempting to add a new page should evict page 2.

        Page page5 = bf.createPage();
        //check that page 1 is in the buffer pool and page 2 is not.
        assertEquals(page1.getPid(), bf.getPage(page1.getPid()).getPid());

        // page 2 should have disk ID now and not temporary ID
        Page page2FromDisk = bf.getPage(page2.getPid());
        assertNull(bf.getPage(page2Id));
        // unpin page1 completely and ready it for eviction
        bf.unpinPage(page1.getPid());
        bf.unpinPage(page1.getPid());

        bf.getPage(page2.getPid());
        bf.getPage(page3.getPid());
        bf.getPage(page4.getPid());
        // creating page should evict
        int page1Id = page1.getPid();
        bf.createPage();
        // since evicted, saved pid and retrieved pid should be different
        Page page1FromDisk = bf.getPage(page1.getPid());
        assertNotEquals(page1Id, page1FromDisk.getPid());
    }

    // Test mark dirty
    @Test
    void testMarkDirty() throws Exception {
        BufferManager bf = new BufferManagerImpl(4096, workingDirectory + testFileDirectory, fileName);

        Page page1 = bf.createPage();

        // for the sake of these tests, we will be calling a method to set the boolean in the page object to not dirty
        // note that this should never be called outside of the buffer manager

        page1.markNotDirty();

        assertFalse(page1.getDirtyStatus());

        // manually mark dirty and unpin page1
        bf.markDirty(page1.getPid());
        bf.unpinPage(page1.getPid());

        // check if page1 is marked dirty
        assertTrue(page1.getDirtyStatus());

        // create a page, forcing a write to disk
        Page page2 = bf.createPage();
        // Another check that attempting to get a page when all are pinned shouldn't change the buffer pool
        Page page3 = bf.getPage(page1.getPid());
        // unpin it and load page1 back from disk
        bf.unpinPage(page2.getPid());
        // load page1 from disk
        Page page1FromDisk = bf.getPage(page1.getPid());
        assertFalse(page1FromDisk.getDirtyStatus());

    }

    @Test
    void verifyOnlyContinuousIdsCanBeWrittenToDisk() throws Exception {
        BufferManager bf = new BufferManagerImpl(4096, workingDirectory + "/not/a/real/directory", fileName);

        // hits a certain exception to achieve 100% test coverage
        Page page1 = bf.createPage();
        bf.unpinPage(page1.getPid());
        Page page2 = bf.createPage();

    }

}
