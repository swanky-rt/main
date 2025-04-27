package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilitiesTest {
    private Utilities utilities;
    private Utilities utilitiesEmptyFile;
    private BufferManagerImpl bufferManager;
    
    @BeforeEach
    void setUp() throws IOException {
        bufferManager = mock(BufferManagerImpl.class);
        utilities = new Utilities("title.basics.tsv", "testdb.dat");
        utilitiesEmptyFile = new Utilities("title.basics.tsv", "testdb2.dat");
    }
    
    @Test
    void testLoadDataset() throws Exception {
        doNothing().when(bufferManager).unpinPage(anyInt(), eq(File.DISK));
        doReturn(new PageImpl(1, File.DISK)).when(bufferManager).createPage(eq(File.DISK));
        doReturn(new PageImpl(1, File.DISK)).when(bufferManager).getPage(anyInt(), eq(File.DISK));
        doReturn(4096).when(bufferManager).getBufferSize();
        // utilities.loadDataset(bufferManager, System.getProperty("user.dir") + "/src/main/java/project_645/DB files/");

        Exception exception = assertThrows(Exception.class, () -> utilitiesEmptyFile.loadDataset(bufferManager, System.getProperty("user.dir") + "/src/test/java/project_645/DB files/"));

        // assertEquals("Disk does not have sufficiently many unique pages to fill up the buffer manager", exception.getMessage());
        //utilitiesEmptyFile.loadDataset(bufferManager, System.getProperty("user.dir") + "/src/test/java/project_645/DB files/");
    }

}
