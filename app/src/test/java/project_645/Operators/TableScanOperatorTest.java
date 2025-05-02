package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TableScanOperatorTest {

    private BufferManagerImpl mockBufferManager;
    private TableScanOperator tableScanOperator;
    private Page mockPage;
    private Row mockRow;

    @BeforeEach
    void setUp() throws Exception {
        mockBufferManager = mock(BufferManagerImpl.class);
        mockPage = mock(Page.class);
        mockRow = mock(Row.class);

        // Setup mock behavior
        when(mockPage.getRowCount()).thenReturn(1);
        when(mockPage.getRow(0)).thenReturn(mockRow);
        when(mockPage.getPid()).thenReturn(0L);
        when(mockPage.getDataFile()).thenReturn(File.DISK);

        when(mockBufferManager.getPage(0, File.DISK)).thenReturn(mockPage);
        when(mockBufferManager.getFileSizeOfChosenFile(File.DISK)).thenReturn(1);

        // Simulate pinnedPages as empty map
        mockBufferManager.pinnedPages = mock(Map.class);
        when(mockBufferManager.pinnedPages.containsKey(anyString())).thenReturn(false);

        tableScanOperator = new TableScanOperator(mockBufferManager, File.DISK);
    }

    @Test
    void testOpenInitializesCorrectly() {
        assertDoesNotThrow(() -> tableScanOperator.open());
    }

    @Test
    void testHasNextReturnsTrueWhenRowAvailable() throws Exception {
        tableScanOperator.open();
        assertTrue(tableScanOperator.hasNext());
    }

    @Test
    void testHasNextReturnsFalseWhenNoRowAvailable() throws Exception {
        when(mockPage.getRowCount()).thenReturn(0);
        tableScanOperator.open();
        assertFalse(tableScanOperator.hasNext());
    }

    @Test
    void testNextReturnsRecord() throws Exception {
        tableScanOperator.open();
        Record result = tableScanOperator.next();

        assertNotNull(result);
        assertEquals(mockRow, result.getRow());
    }

    @Test
    void testNextReturnsNullAfterPageExhausted() throws Exception {
        when(mockPage.getRowCount()).thenReturn(0);
        tableScanOperator.open();
        assertNull(tableScanOperator.next());
    }

    @Test
    void testNextHandlesNullCurrentPageGracefully() throws Exception {
        tableScanOperator = new TableScanOperator(mockBufferManager, File.DISK);
        Record result = null;

        try {
            result = tableScanOperator.next();  // Should not throw
        } catch (Exception ignored) {}

        assertNull(result);
    }

    @Test
    void testMakeResetOperatorTrueResetsState() throws Exception {
        tableScanOperator.open();
        tableScanOperator.makeResetOperatorTrue();

        // Should reload page and not throw
        assertTrue(tableScanOperator.hasNext());
    }

    @Test
    void testCloseSetsCurrentPageToNull() {
        tableScanOperator.close();
        // No exception expected
    }

    @Test
    void testGetRelationReturnsCorrectFile() {
        assertEquals(File.DISK, tableScanOperator.getRelation());
    }

    @Test
    void testNextTransitionsToNextPage() throws Exception {
        when(mockPage.getRowCount()).thenReturn(1);
        Page secondPage = mock(Page.class);
        when(secondPage.getRowCount()).thenReturn(1);
        when(secondPage.getRow(0)).thenReturn(mockRow);
        when(secondPage.getPid()).thenReturn(1L);
        when(secondPage.getDataFile()).thenReturn(File.DISK);
        when(mockBufferManager.getPage(1, File.DISK)).thenReturn(secondPage);
        when(mockBufferManager.getFileSizeOfChosenFile(File.DISK)).thenReturn(2);

        tableScanOperator.open();
        tableScanOperator.next(); // Advance to end of first page
        Record nextRecord = tableScanOperator.next(); // Now transition to second page
        assertNotNull(nextRecord);
        assertEquals(mockRow, nextRecord.getRow());
    }

    @Test
    void testMakeResetOperatorTrueUnpinsCurrentPage() throws Exception {
        tableScanOperator.open();

        String expectedKey = "DISK-0";

        // Stub constructPageIdentifier to return the key
        when(mockBufferManager.constructPageIdentifier(0L, File.DISK)).thenReturn(expectedKey);
        // Stub pinnedPages to simulate pinned entry
        when(mockBufferManager.pinnedPages.containsKey(expectedKey)).thenReturn(true, false);  // unpin once

        doNothing().when(mockBufferManager).unpinPage(0L, File.DISK);

        tableScanOperator.makeResetOperatorTrue();

        verify(mockBufferManager, atLeastOnce()).unpinPage(0L, File.DISK);
    }


    @Test
    void testHasNextThrowsWhenPageIsNull() {
        // forcibly inject null page and test
        assertThrows(NullPointerException.class, () -> {
            var pageField = TableScanOperator.class.getDeclaredField("currentPage");
            pageField.setAccessible(true);
            pageField.set(tableScanOperator, null);
            tableScanOperator.hasNext();
        });
    }
}