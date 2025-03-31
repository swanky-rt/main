package project_645;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BtreeTest {
    @Mock
    private BufferManagerImpl bufferManager;
    private BTreeImpl btree;

    private final int TEST_ORDER = 3;
    private AutoCloseable closeable;
    private Path path;
    private String fileName = "testdbfile.dat";
    private String testFileDirectory = "/src/test/java/project_645/DB files/";
    private String testFilePath;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        bufferManager = mock(BufferManagerImpl.class);
        Page mockPage = mock(Page.class);
        when(mockPage.getPid()).thenReturn(1);
        btree = new BTreeImpl(bufferManager, TEST_ORDER, true, File.MOVIE_ID_IDX);

        testFilePath = System.getProperty("user.dir") + testFileDirectory + fileName;
        path = Paths.get(testFilePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        when(bufferManager.createPage(File.DISK)).thenReturn(mockPage);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(path);
        closeable.close();
    }

    @Test
    void testSingleInsert() throws Exception {
        Page page = mock(Page.class);
        when(bufferManager.createPage(File.DISK)).thenReturn(page);
        when(page.getPid()).thenReturn(1);

        Rid testRid = new Rid(1, 1);
        btree.insert("key1", testRid);

        verify(bufferManager).createPage(File.DISK);
        verify(bufferManager).markDirty(1, File.DISK);
        verify(bufferManager, atLeastOnce()).unpinPage(1, File.DISK);

        Iterator<Rid> searchResults = btree.search("key1");
        assertTrue(searchResults.hasNext());
        assertEquals(testRid, searchResults.next());
    }

    @Test
    void testSearchInEmptyTree() throws Exception {
        Page mockRoot = mock(Page.class);
        when(bufferManager.getPage(0, File.DISK)).thenReturn(mockRoot);
        when(mockRoot.getRow(0)).thenReturn(createMetaRow('L', -1, -1, 0));

        Iterator<Rid> results = btree.search("anykey");
        assertFalse(results.hasNext());

        verify(bufferManager).getPage(0, File.DISK);
        verify(bufferManager).unpinPage(0, File.DISK);
    }

    @Test
    void testLeafNodeSplit() throws Exception {
        Page mockRoot = mock(Page.class);
        Page mockNewLeaf = mock(Page.class);

        when(bufferManager.createPage(File.DISK)).thenReturn(mockNewLeaf);
        when(mockRoot.getPid()).thenReturn(0);
        when(mockNewLeaf.getPid()).thenReturn(1);

        when(bufferManager.getPage(0, File.DISK)).thenReturn(mockRoot);
        when(mockRoot.getRow(0)).thenReturn(createMetaRow('L', -1, -1, 7));

        List<Row> mockRows = new ArrayList<>();
        mockRows.add(createMetaRow('L', -1, -1, 7));
        for (int i = 1; i <= 7; i++) {
            mockRows.add(createDataRow("key" + i, new Rid(i, i)));
        }
        when(mockRoot.getAllRows()).thenReturn(mockRows.toArray(new Row[0]));

        btree.insert("key8", new Rid(8, 8));

        verify(bufferManager, times(2)).createPage(File.DISK);
        verify(bufferManager).markDirty(0, File.DISK);
        verify(bufferManager).markDirty(1, File.DISK);

        Iterator<Rid> searchResults = btree.search("key8");
        assertTrue(searchResults.hasNext());
    }

    @Test
    void testRangeSearchAcrossLeaves() throws Exception {
        Page mockLeaf1 = mock(Page.class);
        Page mockLeaf2 = mock(Page.class);

        when(bufferManager.getPage(1, File.DISK)).thenReturn(mockLeaf1);
        when(mockLeaf1.getRow(0)).thenReturn(createMetaRow('L', 0, 2, 3));
        when(mockLeaf1.getAllRows()).thenReturn(new Row[]{
                createMetaRow('L', 0, 2, 3),
                createDataRow("apple", new Rid(1, 1)),
                createDataRow("banana", new Rid(1, 2)),
                createDataRow("cherry", new Rid(1, 3))
        });

        when(bufferManager.getPage(2, File.DISK)).thenReturn(mockLeaf2);
        when(mockLeaf2.getRow(0)).thenReturn(createMetaRow('L', 0, -1, 2));
        when(mockLeaf2.getAllRows()).thenReturn(new Row[]{
                createMetaRow('L', 0, -1, 2),
                createDataRow("date", new Rid(2, 1)),
                createDataRow("fig", new Rid(2, 2))
        });

        Iterator<Rid> results = btree.rangeSearch("b", "e");
        List<String> found = new ArrayList<>();
        while (results.hasNext()) {
            found.add(results.next().toString());
        }

        assertEquals(3, found.size());
        assertTrue(found.get(0).contains("banana"));
        assertTrue(found.get(1).contains("cherry"));
        assertTrue(found.get(2).contains("date"));
    }

    // Helper methods to create mock rows
    private Row createMetaRow(char type, int parent, int next, int numKeys) {
        Row row = mock(Row.class);
        when(row.movieId).thenReturn(new byte[]{(byte)type});
        when(row.title).thenReturn(new byte[30]);
        // Simplified - in real impl you'd need to properly encode these values
        return row;
    }
    private Row createDataRow(String key, Rid rid) {
        Row row = mock(Row.class);
        when(row.movieId).thenReturn(key.getBytes());
        // Simplified - in real impl you'd need to properly encode the Rid
        return row;
    }

    private Row createInternalRow(int childPid, String key) {
        Row row = mock(Row.class);
        // Simplified - in real impl you'd need proper encoding
        when(row.movieId).thenReturn(intToBytes(childPid));
        when(row.title).thenReturn(key.getBytes());
        return row;
    }

    private byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value
        };
    }
}
