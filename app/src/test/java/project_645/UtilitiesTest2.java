package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class UtilitiesTest2 {

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
        // Mock method behavior
        when(bufferManagerMock.isBufferPoolFull()).thenReturn(false);
        when(bufferManagerMock.createPage()).thenReturn(pageMock);
        when(bufferManagerMock.getPage(anyInt())).thenReturn(null);

        // Calling the method under test
        utilities.loadDataset(bufferManagerMock, testFilePath);

        // Verify interaction
        verify(bufferManagerMock, atLeastOnce()).createPage();
        verify(bufferManagerMock, atLeastOnce()).isBufferPoolFull();
    }

    @Test
    void testWritePageToDisk() throws IOException {
        // Setup
        when(pageMock.getAllRows()).thenReturn(new Row[]{new Row("123".getBytes(), "Movie".getBytes())});

        // Calling method under test
        utilities.writePageToDisk(1, pageMock);

        // Verifying interactions
        verify(pageMock, atLeastOnce()).getAllRows();
    }

    @Test
    void testLoadPageFromDisk() throws IOException {
        // Mock file creation
        Path testPath = Paths.get(testFilePath + "testdb.dat");
        Files.createFile(testPath);

        // Calling method under test
        Page loadedPage = utilities.loadPageFromDisk(1);
        
        // Verify the result
        assertNotNull(loadedPage);

        // Clean up
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
        // Set up mocking for writing to disk
        doNothing().when(utilities).writePageToDisk(anyInt(), any(Page.class));
        
        // Calling the method under test
        utilities.populateDisk(100);
        
        // Verifying that writePageToDisk was called exactly once
        verify(utilities, times(1)).writePageToDisk(anyInt(), any(Page.class));
    }

    // Optional additional test to verify file system interaction
    @Test
    void testFileNotFoundInLoadPage() {
        // Setup mock for file not found
        Path invalidPath = Paths.get(testFilePath + "nonexistent.dat");

        // Test exception handling when file doesn't exist
        assertThrows(IOException.class, () -> {
            utilities.loadPageFromDisk(1);
        });
    }
}
