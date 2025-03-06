package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class BufferManagerImplTest {
    private BufferManagerImpl bufferManager;
    private Utilities utilities;

    @BeforeEach
    public void setUp() {
        bufferManager = new BufferManagerImpl(16 * 4096); // 16 pages buffer pool
        utilities = new Utilities();
    }

    @Test
    public void testLoadDataset() throws IOException {
        String datasetPath = "main/app/src/main/java/project_645/DB files/";
        utilities.loadDataset(bufferManager, datasetPath);
        assertTrue(bufferManager.bufferPool.size() > 0, "Buffer pool should not be empty after loading dataset");
    }

    @Test
    public void testCreateSinglePage() {
        Page page = bufferManager.createPage();
        assertNotNull(page, "Created page should not be null");
        System.out.println("Created Page: " + page);
    }

    @Test
    public void testCreateMultiplePages() {
        for (int i = 0; i < 8; i++) {
            Page page = bufferManager.createPage();
            assertNotNull(page, "Page " + i + " should not be null");
            System.out.println("Created Page " + i + ": " + page);
        }
    }

    @Test
    public void testGetSinglePage() {
        int pageId = 0;

        Page page = bufferManager.getPage(pageId);
        assertNotNull(page, "Page should not be null");
        System.out.println("Retrieved Page with ID: " + pageId + " -> " + page);
    }

    @Test
    public void testGetMultiplePages() {
        
        for (int i = 0; i < 8; i++) {
            Page page = bufferManager.getPage(i);
            assertNotNull(page, "Page " + i + " should not be null");
            System.out.println("Retrieved Page " + i + ": " + page);
        }
    }

    @Test
    public void testMarkDirty() {
        int pageId = 0;
        bufferManager.getPage(pageId);
        bufferManager.markDirty(pageId);
        assertTrue(bufferManager.isDirty(pageId), "Page should be marked dirty");
        System.out.println("Page " + pageId + " marked as dirty.");
    }

    @Test
    public void testUnpinPage() {
        int pageId = 0;
        bufferManager.getPage(pageId);
        bufferManager.unpinPage(pageId);
        assertFalse(bufferManager.pinnedPages.contains(pageId), "Page should not be pinned");
        System.out.println("Page " + pageId + " unpinned.");
    }

    @Test
    public void testEvictionPolicy() {
        for (int i = 0; i < 16; i++) { 
            bufferManager.createPage();
        }
        bufferManager.createPage(); // This should trigger eviction
    
        assertTrue(bufferManager.bufferPool.size() <= 16, "Buffer pool should not exceed max capacity after eviction.");
    }
    

    @Test
    public void testWriteAndLoadPage() {
        try {
            int pageId = 0;
            Page page = bufferManager.getPage(pageId);
            utilities.writePageToDisk(pageId, page);
            Page loadedPage = utilities.loadPageFromDisk(pageId);
            assertNotNull(loadedPage, "Loaded page should not be null");
            System.out.println("Page " + pageId + " successfully written and loaded from disk.");
        } catch (IOException e) {
            fail("IOException occurred during test: " + e.getMessage());
        }
    }

    
}
