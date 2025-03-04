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
    public void testGetPage() {
        Page page = bufferManager.getPage(1);
        assertNotNull(page, "Page should not be null");
    }

    @Test
    public void testCreatePage() {
        Page page = bufferManager.createPage();
        assertNotNull(page, "Created page should not be null");
    }

    @Test
    public void testMarkDirty() {
        int pageId = 1;
        bufferManager.getPage(pageId);
        bufferManager.markDirty(pageId);
        assertTrue(bufferManager.isDirty(pageId), "Page should be marked dirty");
    }

    @Test
    public void testUnpinPage() {
        int pageId = 2;
        bufferManager.getPage(pageId);
        bufferManager.unpinPage(pageId);
        assertFalse(bufferManager.pinnedPages.contains(pageId), "Page should not be pinned");
    }

    @Test
    public void testEvictPage() {
        for (int i = 0; i < bufferManager.MAX_PAGE; i++) {
            bufferManager.getPage(i);
        }
        int newPageId = bufferManager.MAX_PAGE + 1;
        bufferManager.getPage(newPageId);
        assertTrue(bufferManager.bufferPool.size() <= bufferManager.MAX_PAGE, "LRU should have evicted a page");
    }

    @Test
    public void testWriteAndLoadPage() {
        int pageId = 3;
        Page page = bufferManager.getPage(pageId);
        utilities.writePageToDisk(pageId, page);
        Page loadedPage = utilities.loadPageFromDisk(pageId);
        assertNotNull(loadedPage, "Loaded page should not be null");
    }

    @Test
    public void testLoadDataset() throws IOException {
        utilities.loadDataset(bufferManager, "main/app/src/main/java/project_645/DB files");
        assertFalse(bufferManager.bufferPool.isEmpty(), "Buffer pool should not be empty");
    }
}
