package project_645;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import java.util.*;

public class BufferManagerImpFTest {
    private BufferManagerImpl bufferManager;
    private Utilities utilities;

    @BeforeMethod
    public void setUp() {
        utilities = new Utilities();
        bufferManager = new BufferManagerImpl(8192); // Buffer size of 8192 bytes
    }

    @Test
    public void testCreatePageFillBufferAndEvict() {
        // Create and load pages to fill the buffer pool
        for (int i = 1; i <= 2; i++) {
            Page page = bufferManager.createPage();
            assertNotNull(page, "Page should not be null");
            bufferManager.getPage(i);
        }

        // Verify buffer pool is full
        assertTrue(bufferManager.isBufferPoolFull(), "Buffer pool should be full");

        // Create and load an additional page to trigger eviction
        Page newPage = bufferManager.createPage();
        assertNotNull(newPage, "New page should not be null");
        bufferManager.getPage(3);

        // Verify eviction has occurred
        assertFalse(bufferManager.bufferPool.containsKey(1), "Buffer pool should not contain page 1 after eviction");
        assertTrue(bufferManager.bufferPool.containsKey(3), "Buffer pool should contain page 3");
    }

    
}
