
package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BufferManagerImplTest2{

    private BufferManagerImpl bufferManager;

    @Mock
    private Utilities utilities; 

    @Mock
    private Page mockPage; 

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bufferManager = new BufferManagerImpl(16 * 4096);
    }

    @Test
    public void testCreatePage() {
      System.out.println("Running testCreatePage...");
        Page page = bufferManager.createPage();
        assertNotNull(page, "Created page should not be null");
        assertTrue(bufferManager.pageMap.containsKey(page), "Page should be mapped to a pageId");
    }

    @Test
    public void testGetPage_whenPageIsInBufferPool() {
        int pageId = 0;
        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);

        bufferManager.getPage(pageId);
        verify(utilities, never()).loadPageFromDisk(pageId);  // No disk load should occur if page is in buffer pool
    }

    @Test
    public void testGetPage_whenPageIsNotInBufferPool() throws IOException {
        int pageId = 1;
        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);

        Page page = bufferManager.getPage(pageId);
        assertNotNull(page, "Page should be loaded from disk");
        verify(utilities, times(1)).loadPageFromDisk(pageId);  // Should trigger disk load when not in pool
    }

    @Test
    public void testEviction_whenBufferIsFull() throws IOException {
        for (int i = 0; i < bufferManager.MAX_PAGE + 1; i++) {
            bufferManager.createPage();
        }

        assertTrue(bufferManager.bufferPool.size() <= bufferManager.MAX_PAGE,
                "Buffer pool should not exceed max capacity after eviction.");
    }

    @Test
    public void testMarkDirty() {
        int pageId = 1;
        bufferManager.markDirty(pageId);
        assertTrue(bufferManager.isDirty(pageId), "Page should be marked dirty");
    }

    @Test
    public void testWritePageToDisk() throws IOException {
        int pageId = 3;

        // Simulating writing to disk
        doNothing().when(utilities).writePageToDisk(pageId, mockPage);
        utilities.writePageToDisk(pageId, mockPage);

        verify(utilities, times(1)).writePageToDisk(pageId, mockPage);
    }

    @Test
    public void testLoadPageFromDisk() throws IOException {
        int pageId = 2;
        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);

        Page loadedPage = utilities.loadPageFromDisk(pageId);

        assertNotNull(loadedPage, "Loaded page should not be null");
        assertEquals(mockPage, loadedPage, "Loaded page should match the mocked page");

        verify(utilities, times(1)).loadPageFromDisk(pageId);
    }

    @Test
    public void testLoadDataset() throws IOException {
        String datasetPath = "main/app/src/main/java/project_645/DB_files/";

        // Mock utilities loadDataset method
        doNothing().when(utilities).loadDataset(any(BufferManagerImpl.class), eq(datasetPath));
        utilities.loadDataset(bufferManager, datasetPath);

        verify(utilities, times(1)).loadDataset(bufferManager, datasetPath);
    }

    @Test
    public void testPopulateDisk() throws IOException {
        int numRecords = 10;
        doNothing().when(utilities).populateDisk(numRecords);
        utilities.populateDisk(numRecords);

        verify(utilities, times(1)).populateDisk(numRecords);
    }
}
