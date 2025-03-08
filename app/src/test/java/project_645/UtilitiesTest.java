package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class UtilitiesTest {

    @Mock
    private BufferManagerImpl bufferManagerMock;

    @Mock
    private PageImpl pageMock;

    @InjectMocks
    private Utilities utilities;

    private final String testFilePath = "main/app/src/main/java/project_645/DB_files/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadDataset() throws IOException {
        when(bufferManagerMock.isBufferPoolFull()).thenReturn(false);
        when(bufferManagerMock.createPage()).thenReturn(pageMock);
        when(bufferManagerMock.getPage(anyInt())).thenReturn(null);

        utilities.loadDataset(bufferManagerMock, testFilePath);

        verify(bufferManagerMock, atLeastOnce()).createPage();
        verify(bufferManagerMock, atLeastOnce()).isBufferPoolFull();
    }

    @Test
    void testWritePageToDisk() throws IOException {
        when(pageMock.getAllRows()).thenReturn(new Row[]{new Row("123".getBytes(), "Movie".getBytes())});
        
        utilities.writePageToDisk(1, pageMock);

        verify(pageMock, atLeastOnce()).getAllRows();
    }

    @Test
    void testLoadPageFromDisk() throws IOException {
        Path testPath = Paths.get(testFilePath + "testdb.dat");
        Files.createFile(testPath);

        Page loadedPage = utilities.loadPageFromDisk(1);
        assertNotNull(loadedPage);

        Files.deleteIfExists(testPath);
    }

    @Test
    void testGetNextPageId() {
        int initialPageId = utilities.currentPageID;
        int pageId = utilities.getNextPageId();
        assertEquals(initialPageId + 1, pageId);
    }

    @Test
    void testPopulateDisk() throws IOException {
        doNothing().when(utilities).writePageToDisk(0, any(Page.class));
        
        utilities.populateDisk(100);
        
        verify(utilities, times(1)).writePageToDisk(0, any(Page.class));
    }
}
