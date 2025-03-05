import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class BufferManagerImplTest {
    private BufferManagerImpl bufferManager;
    private Utilities utilities;

    @Before
    public void setUp() {
        bufferManager = new BufferManagerImpl(8192); // 8KB buffer pool
        utilities = new Utilities();
    }

    @Test
    public void testGetPage() {
        Page page = bufferManager.getPage(1);
        assertNotNull(page);
        assertTrue(bufferManager.bufferPool.containsKey(1));
    }

    @Test
    public void testCreatePage() {
        Page page = bufferManager.createPage();
        assertNotNull(page);
        assertTrue(bufferManager.pageMap.containsKey(page));
    }

    @Test
    public void testEvictPage() {
        bufferManager.getPage(1);
        bufferManager.getPage(2);
        bufferManager.getPage(3);
        bufferManager.getPage(4);
        bufferManager.getPage(5); // Should trigger eviction if buffer size is exceeded
        assertTrue(bufferManager.bufferPool.size() <= bufferManager.MAX_PAGE);
    }

    @Test
    public void testMarkDirty() {
        bufferManager.getPage(1);
        bufferManager.markDirty(1);
        assertTrue(bufferManager.isDirty(1));
    }

    @Test
    public void testUnpinPage() {
        bufferManager.getPage(1);
        bufferManager.unpinPage(1);
        assertFalse(bufferManager.pinnedPages.contains(1));
    }

    @Test
    public void testPinPage() {
        bufferManager.getPage(1);
        bufferManager.pinPage(1);
        assertTrue(bufferManager.pinnedPages.contains(1));
    }

    @Test
    public void testBufferPoolFull() {
        for (int i = 0; i < bufferManager.MAX_PAGE; i++) {
            bufferManager.getPage(i);
        }
        assertTrue(bufferManager.isBufferPoolFull());
    }
}
