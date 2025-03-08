package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BufferManagerImplTest2 {
    private BufferManagerImpl bufferManager;
    private Utilities utilities;

    @BeforeEach
    void setUp() throws IOException {
        bufferManager = new BufferManagerImpl(40960); // 10 pages
        utilities = mock(Utilities.class);
    }

    @Test
    void testCreatePage() throws Exception {
        Page page = bufferManager.createPage();
        assertNotNull(page);
        assertEquals(0, page.getPid());
    }

    @Test
    void testGetPage() throws Exception {
        Page page = bufferManager.createPage();
        Page retrievedPage = bufferManager.getPage(page.getPid());
        assertEquals(page, retrievedPage);
    }

    @Test
    void testEvictPage() throws Exception {
        for (int i = 0; i < bufferManager.MAX_PAGE; i++) {
            bufferManager.createPage();
        }
        assertTrue(bufferManager.isBufferPoolFull());
        assertDoesNotThrow(() -> bufferManager.evictPage());
    }

    @Test
    void testMarkDirty() {
        PageImpl page = new PageImpl(1);
        bufferManager.bufferPool.put(1, page);
        bufferManager.markDirty(1);
        assertTrue(page.getDirtyStatus());
    }

    @Test
    void testUnpinPage() throws Exception {
        Page page = bufferManager.createPage();
        bufferManager.unpinPage(page.getPid());
        assertEquals(0, page.getPinCount());
    }

    @Test
    void testPinPage() throws Exception {
        Page page = bufferManager.createPage();
        bufferManager.pinPage(page.getPid());
        assertEquals(1, page.getPinCount());
    }

    @Test
    void testIsBufferPoolFull() throws Exception {
        for (int i = 0; i < bufferManager.MAX_PAGE; i++) {
            bufferManager.createPage();
        }
        assertTrue(bufferManager.isBufferPoolFull());
    }
}
