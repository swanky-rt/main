package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectionOperatorTest {

    private Operator childOperator;
    private BufferManagerImpl bufferManager;
    private SelectionOperator selectionOperator;

    @BeforeEach
    void setUp() {
        childOperator = mock(Operator.class);
        bufferManager = mock(BufferManagerImpl.class);
        selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);
    }

    @Test
    void openTest() {
        doNothing().when(childOperator).open();
        selectionOperator.open();
        verify(childOperator, times(1)).open();
    }

    @Test
    void hasNextTest() throws IOException {
        when(childOperator.hasNext()).thenReturn(true);
        assertTrue(selectionOperator.hasNext());

        when(childOperator.hasNext()).thenReturn(false);
        assertFalse(selectionOperator.hasNext());
    }

    @Test
    void nextTest_matchingRecord() throws Exception {
        Row dummyRow = mock(Row.class);
        Rid dummyRid = new Rid(0, 0);

        Record matchingRecord = mock(Record.class);
        when(matchingRecord.getMovieTitleBytes()).thenReturn("TestTitle".getBytes());

        when(childOperator.next())
                .thenReturn(matchingRecord)
                .thenReturn(null);

        selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);

        Record result = selectionOperator.next();

        assertNotNull(result);  // should now pass
        assertArrayEquals("TestTitle".getBytes(), result.getMovieTitleBytes());  // use assertArrayEquals for bytes
    }


    @Test
    void nextTest_nonMatchingRecord() throws Exception {
        Row dummyRow = new Row("dummy".getBytes(), "dummyTitle".getBytes());
        Rid dummyRid = new Rid(0, 0);
        Record nonMatchingRecord = new Record(
                dummyRow,
                "movieId456".getBytes(),
                "DifferentTitle".getBytes(),
                "something".getBytes(),
                dummyRid
        );

        when(childOperator.next())
                .thenReturn(nonMatchingRecord)
                .thenReturn(null);

        selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);

        Record result = selectionOperator.next();
        assertNull(result);
    }

    @Test
    void closeTest() {
        doNothing().when(childOperator).close();
        selectionOperator.close();
        verify(childOperator, times(1)).close();
    }

    @Test
    void getRelationTest() {
        when(childOperator.getRelation()).thenReturn(File.DISK);
        assertEquals(File.DISK, selectionOperator.getRelation());
    }

    @Test
    void makeResetOperatorTrueTest() {
        assertDoesNotThrow(() -> selectionOperator.makeResetOperatorTrue());
    }
}
