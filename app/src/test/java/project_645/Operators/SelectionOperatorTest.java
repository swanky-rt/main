package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SelectionOperatorTest {

    private Operator childOperator;
    private BufferManagerImpl bufferManager;

    @BeforeEach
    void setUp() {
        childOperator = mock(Operator.class);
        bufferManager = mock(BufferManagerImpl.class);
    }

    @Test
    void openTest() {
        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);
        selectionOperator.open();
        verify(childOperator, times(1)).open();
    }

    @Test
    void hasNextTest() throws Exception {
        when(childOperator.hasNext()).thenReturn(true, false);
        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);
        assertTrue(selectionOperator.hasNext());
        assertFalse(selectionOperator.hasNext());
    }

    @Test
    void nextTest_matchingTitle() throws Exception {
        Row dummyRow = new Row("dummy".getBytes(), "TestTitle".getBytes());
        Rid dummyRid = new Rid(0, 0);
        Record matchingRecord = new Record(dummyRow, "id".getBytes(), "TestTitle".getBytes(), "cat".getBytes(), dummyRid);

        when(childOperator.next()).thenReturn(matchingRecord, null);

        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.TITLE, "TestTitle", bufferManager);
        Record result = selectionOperator.next();
        assertNotNull(result);
        assertEquals("TestTitle", new String(result.getMovieTitleBytes()).trim());
        assertNull(selectionOperator.next()); // second call should return null
    }

    @Test
    void nextTest_nonMatchingMovieId() throws Exception {
        Row dummyRow = new Row("dummy".getBytes(), "title".getBytes());
        Rid dummyRid = new Rid(0, 0);
        Record record = mock(Record.class);

        when(record.getMovieIdBytes()).thenReturn("wrongId".getBytes());
        when(record.getRow()).thenReturn(dummyRow);
        when(record.getRid()).thenReturn(dummyRid);

        when(childOperator.next()).thenReturn(record, null);

        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.MOVIEID, "expectedId", bufferManager);
        Record result = selectionOperator.next();
        assertNull(result);
    }




    @Test
    void nextTest_rangeSelectionPersonId() throws Exception {
        Row dummyRow = new Row("movie123".getBytes(), "SomeTitle".getBytes());
        Rid dummyRid = new Rid(0, 0);
        Record record = new Record(dummyRow, "movie123".getBytes(), "SomeTitle".getBytes(), "director".getBytes(), dummyRid) {
            @Override
            public byte[] getPersonIdBytes() {
                return "person150".getBytes();
            }
        };

        when(childOperator.next()).thenReturn(record, null);

        SelectionOperator selectionOperator = new SelectionOperator(
                childOperator, ColumnNames.PERSONID, "person100", "person200", bufferManager
        );

        Record result = selectionOperator.next();
        assertNotNull(result);
    }






    @Test
    void nextTest_rangeSelectionName() throws Exception {
        Row dummyRow = new Row("movie123".getBytes(), "SomeTitle".getBytes());
        Rid dummyRid = new Rid(0, 0);
        Record record = new Record(dummyRow, "movie123".getBytes(), "SomeTitle".getBytes(), "director".getBytes(), dummyRid) {
            @Override
            public byte[] getNameBytes() {
                return "Brando".getBytes();
            }
        };

        when(childOperator.next()).thenReturn(record, null);

        SelectionOperator selectionOperator = new SelectionOperator(
                childOperator, ColumnNames.NAME, "A", "Z", bufferManager
        );

        Record result = selectionOperator.next();
        assertNotNull(result);
    }


    @Test
    void nextTest_resetOperatorTrue() throws Exception {
        TableScanOperator tableScanMock = mock(TableScanOperator.class);
        when(tableScanMock.next()).thenReturn(null);
        when(tableScanMock.getRelation()).thenReturn(File.DISK);

        SelectionOperator selectionOperator = new SelectionOperator(tableScanMock, ColumnNames.TITLE, "X", bufferManager);
        selectionOperator.makeResetOperatorTrue(); // sets flag
        Record result = selectionOperator.next();
        assertNull(result); // should not throw
    }

    @Test
    void getRelationTest() {
        when(childOperator.getRelation()).thenReturn(File.WORKEDON);
        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.CATEGORY, "director", bufferManager);
        assertEquals(File.WORKEDON, selectionOperator.getRelation());
    }

    @Test
    void closeTest() {
        SelectionOperator selectionOperator = new SelectionOperator(childOperator, ColumnNames.CATEGORY, "director", bufferManager);
        selectionOperator.close();
        verify(childOperator).close();
    }
}
