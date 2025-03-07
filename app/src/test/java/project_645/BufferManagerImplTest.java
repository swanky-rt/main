package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BufferManagerImplTest {

    private BufferManagerImpl bufferManager; // Manually instantiated

    @Mock
    private Utilities utilities; // Mocked dependency

    @Mock
    private Page mockPage; // Mocked Page object

    @BeforeEach
    public void setUp() {
        bufferManager = new BufferManagerImpl(16 * 4096); // Provide required constructor argument
    }

    @Test
    public void testLoadDataset() throws IOException {
        String datasetPath = "./DB_files/";

        // Mocking utilities behavior
        doNothing().when(utilities).loadDataset(any(BufferManagerImpl.class), eq(datasetPath));

        // Calling the method under test
        utilities.loadDataset(bufferManager, datasetPath);

        // Verifying the interaction
        verify(utilities, times(1)).loadDataset(bufferManager, datasetPath);
    }

    @Test
    public void testCreateSinglePage() {
        Page page = bufferManager.createPage();
        assertNotNull(page, "Created page should not be null");
    }

    @Test
    public void testCreateMultiplePages() {
        for (int i = 0; i < 8; i++) {
            Page page = bufferManager.createPage();
            assertNotNull(page, "Page " + i + " should not be null");
        }
    }

    @Test
    public void testGetSinglePage() {
        int pageId = 0;
        bufferManager.createPage(); // Ensuring a page exists

        Page page = bufferManager.getPage(pageId);
        assertNotNull(page, "Page should not be null");
    }

    @Test
    public void testGetMultiplePages() {
        for (int i = 0; i < 8; i++) {
            bufferManager.createPage();
            Page page = bufferManager.getPage(i);
            assertNotNull(page, "Page " + i + " should not be null");
        }
    }

    @Test
    public void testMarkDirty() {
        int pageId = 0;
        bufferManager.createPage(); // Ensure a page exists

        bufferManager.markDirty(pageId);
        assertTrue(bufferManager.isDirty(pageId), "Page should be marked dirty");
    }

    @Test
    public void testUnpinPage() {
        int pageId = 0;
        bufferManager.createPage(); // Ensure a page exists

        bufferManager.unpinPage(pageId);
        assertFalse(bufferManager.pinnedPages.contains(pageId), "Page should not be pinned");
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
    public void testWriteAndLoadPage() throws IOException {
        int pageId = 0;
        bufferManager.createPage(); // Ensure a page exists

        doNothing().when(utilities).writePageToDisk(eq(pageId), any(Page.class));
        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);

        Page page = bufferManager.getPage(pageId);
        utilities.writePageToDisk(pageId, page);
        Page loadedPage = utilities.loadPageFromDisk(pageId);

        assertNotNull(loadedPage, "Loaded page should not be null");
        assertEquals(mockPage, loadedPage, "Loaded page should match the mocked page");
    }
}
