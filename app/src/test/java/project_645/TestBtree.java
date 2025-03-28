package project_645;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

class TestBtree {
    
    @Mock
    private BufferManager bufferMgr;
    
    @InjectMocks
    private BTreeImpl btree;
    
    private final int TEST_ORDER = 3;
    private AutoCloseable closeable;
    
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        btree = new BTreeImpl(bufferMgr, TEST_ORDER, true);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSingleInsert() throws Exception {
        // Mock page creation
        Page mockPage = mock(Page.class);
        when(bufferMgr.createPage()).thenReturn(mockPage);
        when(mockPage.getPid()).thenReturn(1);
        
        // Test insert
        Rid testRid = new Rid(1, 1);
        btree.insert("key1", testRid);
        
        // Verify buffer manager interactions
        verify(bufferMgr).createPage();
        verify(bufferMgr).markDirty(1);
        verify(bufferMgr, atLeastOnce()).unpinPage(1);
    }

    @Test
    void testSearchInEmptyTree() throws Exception {
        // Mock root page as empty leaf
        Page mockRoot = mock(Page.class);
        when(bufferMgr.getPage(0)).thenReturn(mockRoot);
        when(mockRoot.getRow(0)).thenReturn(createMetaRow('L', -1, -1, 0));
        
        Iterator<Rid> results = btree.search("anykey");
        assertFalse(results.hasNext());
        
        verify(bufferMgr).getPage(0);
        verify(bufferMgr).unpinPage(0);
    }

    @Test
    void testLeafNodeSplit() throws Exception {
        // Setup mock pages
        Page mockRoot = mock(Page.class);
        Page mockNewLeaf = mock(Page.class);
        
        when(bufferMgr.createPage()).thenReturn(mockNewLeaf);
        when(mockRoot.getPid()).thenReturn(0);
        when(mockNewLeaf.getPid()).thenReturn(1);
        
        // Mock root page as full leaf
        when(bufferMgr.getPage(0)).thenReturn(mockRoot);
        when(mockRoot.getRow(0)).thenReturn(createMetaRow('L', -1, -1, 7)); // Overfull
        
        // Mock rows in leaf
        List<Row> mockRows = new ArrayList<>();
        mockRows.add(createMetaRow('L', -1, -1, 7));
        for (int i = 1; i <= 7; i++) {
            mockRows.add(createDataRow("key" + i, new Rid(i, i)));
        }
        when(mockRoot.getAllRows()).thenReturn(mockRows.toArray(new Row[0]));
        
        // Trigger split by inserting one more key
        btree.insert("key8", new Rid(8, 8));
        
        // Verify split occurred
        verify(bufferMgr, times(2)).createPage(); // One for new leaf, one for new root
        verify(bufferMgr).markDirty(0); // Original leaf
        verify(bufferMgr).markDirty(1); // New leaf
    }

    @Test
    void testRangeSearchAcrossLeaves() throws Exception {
        // Setup mock pages
        Page mockLeaf1 = mock(Page.class);
        Page mockLeaf2 = mock(Page.class);
        
        // Mock leaf 1
        when(bufferMgr.getPage(1)).thenReturn(mockLeaf1);
        when(mockLeaf1.getRow(0)).thenReturn(createMetaRow('L', 0, 2, 3));
        when(mockLeaf1.getAllRows()).thenReturn(new Row[]{
            createMetaRow('L', 0, 2, 3),
            createDataRow("apple", new Rid(1, 1)),
            createDataRow("banana", new Rid(1, 2)),
            createDataRow("cherry", new Rid(1, 3))
        });
        
        // Mock leaf 2
        when(bufferMgr.getPage(2)).thenReturn(mockLeaf2);
        when(mockLeaf2.getRow(0)).thenReturn(createMetaRow('L', 0, -1, 2));
        when(mockLeaf2.getAllRows()).thenReturn(new Row[]{
            createMetaRow('L', 0, -1, 2),
            createDataRow("date", new Rid(2, 1)),
            createDataRow("fig", new Rid(2, 2))
        });
        
        // Mock root pointing to leaves
        Page mockRoot = mock(Page.class);
        when(bufferMgr.getPage(0)).thenReturn(mockRoot);
        when(mockRoot.getRow(0)).thenReturn(createMetaRow('I', -1, 1,1));
        when(mockRoot.getAllRows()).thenReturn(new Row[]{
            createMetaRow('I', -1, 1,1),
            createInternalRow(1, "cherry"),
            createInternalRow(2, "fig")
        });
        
        // Test range search
        Iterator<Rid> results = btree.rangeSearch("b", "e");
        List<String> found = new ArrayList<>();
        while(results.hasNext()) {
            found.add(results.next().toString());
        }
        
        assertEquals(3, found.size());
        assertTrue(found.get(0).contains("banana"));
        assertTrue(found.get(1).contains("cherry"));
        assertTrue(found.get(2).contains("date"));
        
        verify(bufferMgr, times(3)).getPage(anyInt());
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
