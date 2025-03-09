package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class BufferImplTest {
    private BufferManagerImpl bufferManager;
    private RandomAccessFile randomAccessFile;
    private Path path1;
    private Page page;
    String path = "/app/src/main/java/project_645/DB files/";
    String mainFileName = "title.basics.tsv";
    String diskFileName = "testdb.dat";
    String filePath = System.getProperty("user.dir") + path;

    @BeforeEach
    void setUp() throws IOException {
        bufferManager = mock(BufferManagerImpl.class);
        randomAccessFile = mock(RandomAccessFile.class);
        path1 = mock(Path.class);
        page = mock(Page.class);
        bufferManager = new BufferManagerImpl(2, filePath, diskFileName);
    }
    @Test
    public void testCreatePage(){
        bufferManager.createPage();
    }

    @Test
    public void testGetPageTest() throws Exception {
        Page page = new PageImpl(1);
        bufferManager.bufferPool.put(1, page);
        bufferManager.getPage(1);
    }

    @Test
    public void testGetPageFromDisk() throws Exception {
        bufferManager.getPage(1);
    }

//    @Test
//    public void testEvict() throws Exception {
//        Page page1 = new PageImpl(1);
//        Page page2 = new PageImpl(2);
//        Page page3 = new PageImpl(3);
//        bufferManager.bufferPool.put(1, page1);
//        bufferManager.bufferPool.put(2, page2);
//        bufferManager.bufferPool.put(3, page3);
//
//        bufferManger.bufferPool.getPid()
//
//       // doNothing().when(bufferManager).unpinPage(anyInt());
//        bufferManager.getPage(1);
//    }

    @Test
    public void testUnpinPage(){
        Page page1 = new PageImpl(1);
        Page page2 = new PageImpl(2);
        bufferManager.bufferPool.put(1, page1);
        bufferManager.bufferPool.put(2, page2);
        bufferManager.pinnedPages.put(1, 1);
        bufferManager.unpinPage(1);
    }

    @Test
    public void testUnpinPageifNoPinPage(){
        Page page1 = new PageImpl(1);
        Page page2 = new PageImpl(2);
        bufferManager.bufferPool.put(1, page1);
        bufferManager.bufferPool.put(2, page2);
        bufferManager.pinnedPages.put(1, 1);
        bufferManager.unpinPage(3);
    }

    @Test
    public void testPinPage(){
        Page page = new PageImpl(1);
        bufferManager.bufferPool.put(1, page);
        bufferManager.pinPage(1);
    }

    @Test
    public void testWritePageToDisk() throws IOException {
        Page page = new PageImpl(1);
        bufferManager.bufferPool.put(1, page);
        bufferManager.writePageToDisk(1, page);
    }

//    @Test
//    public void testWritePageToDisk_ValidPage() throws IOException {
//        // Arrange
//        int pageId = 1;
//        Page page = new PageImpl(pageId);
//        bufferManager.bufferPool.put(pageId, page);
//
//        Path mockPath = mock(Path.class);
//        RandomAccessFile mockRandomAccessFile = mock(RandomAccessFile.class);
//        when(Files.size(mockPath)).thenReturn(4096L);
//        when(page.getAllRows()).thenReturn(new Row[105]);
//        when(page.getBytesToPad()).thenReturn(100);
//        when(page.getRowCount()).thenReturn(1);
//
//        // Act
//        bufferManager.writePageToDisk(pageId, page);
//    }

    @Test
    public void testgetNextPageId(){
        bufferManager.getNextPageId();

    }

    @Test
     public void testsomething(){

    }


}
