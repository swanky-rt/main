//package project_645;
//
//
//
//import org.junit.jupiter.api.BeforeEach;
//
//import org.junit.jupiter.api.Test;
//
//import org.junit.jupiter.api.extension.ExtendWith;
//
////import org.mockito.InjectMocks;
//
//import org.mockito.Mock;
//
//import org.mockito.Spy;
//
//import org.mockito.junit.jupiter.MockitoExtension;
//
//
//
//import java.io.IOException;
//
////import java.util.HashMap;
//
//import java.util.Map;
//
//
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import static org.mockito.ArgumentMatchers.any;
//
//import static org.mockito.ArgumentMatchers.eq;
//
//import static org.mockito.Mockito.*;
//
//
//
//@ExtendWith(MockitoExtension.class)
//
//public class BufferManagerImplTest {
//
//
//
//    @Mock private Utilities utilities;
//
//
//
//    @Mock private Page mockPage;
//
//
//
//    private Map<Integer, Page> bufferPoolMock;
//
//
//    @BeforeEach
//        void setUp() throws IOException {
//            bufferManager = mock(BufferManagerImpl.class);
//            utilities = new Utilities("title.basics.tsv", "testdb.dat");
//            utilitiesEmptyFile = new Utilities("title.basics.tsv", "testdb2.dat");
//           // bufferPoolMock = mock(Map.class);
//        }
//
//
//
//
//
//        // Use Reflection to set the private bufferPool field if necessary
//
//        try {
//
//            java.lang.reflect.Field bufferPoolField = BufferManagerImpl.class.getDeclaredField("bufferPool");
//
//            bufferPoolField.setAccessible(true);
//
//            bufferPoolField.set(bufferManager, bufferPoolMock);
//
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//
//            throw new RuntimeException(e);
//
//        }
//
//    }
//
//
//
//    @Test
//
//    public void testLoadDataset() throws IOException {
//
//        String datasetPath = "app/src/main/java/project_645/DB files";
//
//
//
//        try {
//
//            doNothing().when(utilities).loadDataset(any(BufferManagerImpl.class), eq(datasetPath));
//
//
//
//        } catch (Exception e) {
//
//
//
//            e.printStackTrace();
//
//        }
//
//        try {
//
//            utilities.loadDataset(bufferManager, datasetPath);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        try {
//
//            verify(utilities, times(1)).loadDataset(bufferManager, datasetPath);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//    }
//
//
//
//    @Test
//
//    public void testCreateSinglePage() {
//
//        Page page = null;
//
//        try {
//
//            page = bufferManager.createPage();
//
//            assertNotNull(page);
//
//        } catch (Exception e) {
//
//
//
//            e.printStackTrace();
//
//        }
//
//        //assertNotNull(page, "Created page should not be null");
//
//
//
//    }
//
//
//
//    @Test
//
//    public void testGetSinglePage() {
//
//        int pageId = 0;
//
//        Page page = bufferManager.getPage(pageId);
//
//        assertNotNull(page);
//
//        //assertNotNull(page, "Page should be null if it doesn't exist in bufferPool");
//
//    }
//
//
//
//    @Test
//
//    public void testMarkDirty() {
//
//        int pageId = 0;
//
//
//
//        bufferManager.markDirty(pageId);
//
//        verify(bufferManager).markDirty(pageId);
//
//    }
//
//
//
//    @Test
//
//    public void testUnpinPage() {
//
//        int pageId = 0;
//
//        bufferManager.unpinPage(pageId);
//
//        verify(bufferManager).unpinPage(pageId);
//
//    }
//
//
//
//    @Test
//
//    public void testEvictionPolicy() {
//
//        // Mock bufferPool size
//
//        when(bufferPoolMock.size()).thenReturn(16);
//
//
//
//        // Call createPage() which interacts with bufferPool
//
//        bufferManager.createPage();
//
//
//
//        // Assert that the buffer pool size does not exceed 16
//
//        assertTrue(bufferPoolMock.size() <= 16, "Buffer pool should not exceed max capacity.");
//
//    }
//
//
//
//    @Test
//
//    public void testWriteAndLoadPage() throws IOException {
//
//        int pageId = 0;
//
//        doNothing().when(utilities).writePageToDisk(eq(pageId), any(Page.class));
//
//        when(utilities.loadPageFromDisk(pageId)).thenReturn(mockPage);
//
//
//
//        utilities.writePageToDisk(pageId, mockPage);
//
//        Page loadedPage = utilities.loadPageFromDisk(pageId);
//
//
//
//        assertNotNull(loadedPage);
//
//        assertEquals(mockPage, loadedPage);
//
//        verify(utilities).writePageToDisk(eq(pageId), any(Page.class));
//
//        verify(utilities).loadPageFromDisk(pageId);
//
//    }
//
//}