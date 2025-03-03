
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.*;

public class BufferManagerImplFT {
    private BufferManagerImpl bufferManager;
    private Utilities utilitiesMock;
    private Page mockPage1, mockPage2, mockPage3;

    @BeforeMethod
    public void setUp() {
        // Mock Utilities because i need to figure out how to send real data// this will change
        utilitiesMock = Mockito.mock(Utilities.class);
        bufferManager = new BufferManagerImpl(8192); // 2 pages of size 4096 bytes
        bufferManager.utilities = utilitiesMock;

        // Create mock Page objects
        mockPage1 = Mockito.mock(Page.class);
        mockPage2 = Mockito.mock(Page.class);
        mockPage3 = Mockito.mock(Page.class);

        // Mock behavior of utility methods
        Mockito.when(utilitiesMock.loadPageFromDisk(Mockito.anyInt()))
               .thenReturn(mockPage1)
               .thenReturn(mockPage2)
               .thenReturn(mockPage3);
        Mockito.when(utilitiesMock.getNextPageId()).thenReturn(1, 2, 3);
    }

    @Test
    public void testGetPage() {
        Page page = bufferManager.getPage(1);
        Assert.assertNotNull(page, "Page should be loaded successfully.");
        Assert.assertEquals(bufferManager.bufferPool.size(), 1, "Buffer pool should contain one page.");
    }

    @Test
    public void testCreatePage() {
        Page page = bufferManager.createPage();
        Assert.assertNotNull(page, "Created page should not be null.");
        Assert.assertEquals(bufferManager.pageMap.size(), 1, "Page map should contain one page.");
    }

    @Test
    public void testMarkDirty() {
        bufferManager.getPage(1);
        bufferManager.markDirty(1);
        Assert.assertTrue(bufferManager.isDirty(1), "Page should be marked dirty.");
    }

    @Test
    public void testEvictionPolicy() {
        bufferManager.getPage(1);
        bufferManager.getPage(2);
        Assert.assertTrue(bufferManager.isBufferPoolFull(), "Buffer pool should be full.");

        bufferManager.getPage(3); // This should trigger an eviction
        Assert.assertFalse(bufferManager.bufferPool.containsKey(1), "Page 1 should be evicted.");
        Assert.assertEquals(bufferManager.bufferPool.size(), 2, "Buffer pool should still contain only two pages.");
    }

    @Test
    public void testPinPage() {
        bufferManager.getPage(1);
        bufferManager.pinPage(1);
        Assert.assertTrue(bufferManager.pinnedPages.contains(1), "Page 1 should be pinned.");
    }

    @Test
    public void testUnpinPage() {
        bufferManager.getPage(1);
        bufferManager.pinPage(1);
        bufferManager.unpinPage(1);
        Assert.assertFalse(bufferManager.pinnedPages.contains(1), "Page 1 should be unpinned.");
    }

    @Test
    public void testBufferPoolFull() {
        bufferManager.getPage(1);
        bufferManager.getPage(2);
        Assert.assertTrue(bufferManager.isBufferPoolFull(), "Buffer pool should be full after adding two pages.");
    }

    
}
