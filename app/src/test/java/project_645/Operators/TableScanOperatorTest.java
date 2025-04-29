package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TableScanOperatorTest {

    private BufferManagerImpl bufferManager;
    private TableScanOperator tableScanOperator;
    private Page page;

    @BeforeEach
    void setUp() throws Exception {
        bufferManager = mock(BufferManagerImpl.class);
        page = mock(Page.class);

        // setup: create a table scan over mock buffer manager and table
        tableScanOperator = new TableScanOperator(bufferManager, File.DISK, new String[]{"movieId", "title"});
    }

    @Test
    void openTest() throws Exception {
        when(bufferManager.getPage(0, File.DISK)).thenReturn(page);

        tableScanOperator.open();

        assertNotNull(tableScanOperator);  // Simply ensure open() doesn't throw and operator is ready
    }

    @Test
    void hasNextTest() throws Exception {
        when(page.getRowCount()).thenReturn(1);
        when(bufferManager.getPage(0, File.DISK)).thenReturn(page);
        when(bufferManager.getFileSizeOfChosenFile(File.DISK)).thenReturn(1);

        tableScanOperator.open();

        assertTrue(tableScanOperator.hasNext());
    }

    @Test
    void nextTest_withRecords() throws Exception {
        Row mockRow = mock(Row.class);
        when(page.getRowCount()).thenReturn(1);
        when(page.getRow(0)).thenReturn(mockRow);
        when(bufferManager.getPage(0, File.DISK)).thenReturn(page);
        when(bufferManager.getFileSizeOfChosenFile(File.DISK)).thenReturn(1);

        tableScanOperator.open();
        Record result = tableScanOperator.next();

        assertNotNull(result);
        assertEquals(mockRow, result.getRow());
    }

    @Test
    void nextTest_noMoreRecords() throws Exception {
        when(bufferManager.getFileSizeOfChosenFile(File.DISK)).thenReturn(0);

        tableScanOperator.open();
        Record result = tableScanOperator.next();

        assertNull(result);
    }

    @Test
    void closeTest() throws Exception {
        when(bufferManager.getPage(0, File.DISK)).thenReturn(page);

        tableScanOperator.open();
        tableScanOperator.close();

        verify(bufferManager, atLeastOnce()).unpinPage(anyLong(), eq(File.DISK));
    }

    @Test
    void getRelationTest() {
        assertEquals(File.DISK, tableScanOperator.getRelation());
    }

    @Test
    void makeResetOperatorTrueTest() {
        // nothing to assert because makeResetOperatorTrue() is no-op for TableScanOperator
        // but we still need a test method for coverage
        assertDoesNotThrow(() -> tableScanOperator.makeResetOperatorTrue());
    }
}
