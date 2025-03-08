package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilitiesTest2 {
    private Utilities utilities;
    private BufferManagerImpl bufferManager;
    
    @BeforeEach
    void setUp() throws IOException {
        bufferManager = mock(BufferManagerImpl.class);
        utilities = new Utilities();
    }
    
    @Test
    void testGetNextPageId() {
        int initialPageId = utilities.currentPageID;
        assertEquals(initialPageId, utilities.getNextPageId());
    }
    
    @Test
    void testWriteAndLoadPage() throws IOException {
        PageImpl page = new PageImpl(1);
        utilities.writePageToDisk(1, page);
        Page loadedPage = utilities.loadPageFromDisk(1);
        assertNotNull(loadedPage);
        assertEquals(page.getPid(), loadedPage.getPid());
    }
    
    @Test
    void testLoadDataset() throws Exception {
        doNothing().when(bufferManager).unpinPage(anyInt());
        doReturn(new PageImpl(1)).when(bufferManager).createPage();
        
        assertDoesNotThrow(() -> utilities.loadDataset(bufferManager, "/src/main/java/project_645/DB_files/"));
    }
    
    @Test
    void testPopulateDisk() throws IOException {
        utilities.populateDisk(50);
        Path filePath = Paths.get(System.getProperty("user.dir") + "/src/main/java/project_645/DB_files/testdb.dat");
        assertTrue(Files.exists(filePath));
    }
}
