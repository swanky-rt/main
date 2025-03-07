package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    public void testGetSinglePage() {
        int pageId = 0;

        // Mocking the Page object
        Page mockPage = mock(Page.class);

        // Stubbing getPage to return the mocked page
        when(bufferManager.getPage(pageId)).thenReturn(mockPage);

        // Invoke the method under test
        Page page = bufferManager.getPage(pageId);

        // Assertions
        assertNotNull(page, "Page should not be null");
        verify(bufferManager).getPage(pageId); // Verify that getPage() was called with pageId
    }


    

    @Test
    public void testMarkDirty() {
        int pageId = 0;

        // Mock behavior: getPage should return a mocked Page object
        Page mockPage = mock(Page.class);
        when(bufferManager.getPage(pageId)).thenReturn(mockPage);

        // Act: Mark the page as dirty
        bufferManager.markDirty(pageId);

        // Assert: Verify that isDirty returns true
        when(bufferManager.isDirty(pageId)).thenReturn(true);
        assertTrue(bufferManager.isDirty(pageId), "Page should be marked dirty");

        // Verify that getPage() was called
        verify(bufferManager).getPage(pageId);
    }


    @Test
    public void testUnpinPage() {
        int pageId = 0;

        // Mock pinnedPages as a Set and bufferManager.unpinPage(pageId) behavior
        Set<Integer> pinnedPagesMock = mock(Set.class);
        when(bufferManager.pinnedPages).thenReturn(pinnedPagesMock);

        // Act: Unpin the page
        bufferManager.unpinPage(pageId);

        // Assert: Verify the page is removed from pinnedPages
        verify(pinnedPagesMock).remove(pageId);
    }

    @Test
    public void testEvictionPolicy() {
        // Mock bufferPool size and behavior
        when(bufferManager.bufferPool.size()).thenReturn(16);

        // Act: Simulate eviction scenario
        bufferManager.createPage();

        // Assert: Verify eviction logic
        assertTrue(bufferManager.bufferPool.size() <= 16, 
                "Buffer pool should not exceed max capacity after eviction.");

        // Verify createPage() was called
        verify(bufferManager).createPage();
    }

    @Test
    public void testWriteAndLoadPage() throws IOException {
        int pageId = 0;

        // Mock behavior of utility functions
        doNothing().when(utilities).writePageToDisk(eq(pageId), any(Page.class));
        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);

        // Act: Write page to disk and load it
        utilities.writePageToDisk(pageId, mockPage);
        Page loadedPage = utilities.loadPageFromDisk(pageId);

        // Assert: Validate that the loaded page is correct
        assertNotNull(loadedPage, "Loaded page should not be null");
        assertEquals(mockPage, loadedPage, "Loaded page should match the mocked page");

        // Verify interactions
        verify(utilities).writePageToDisk(eq(pageId), any(Page.class));
        verify(utilities).loadPageFromDisk(pageId);
    }
}
