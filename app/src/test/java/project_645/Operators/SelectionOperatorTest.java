package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SelectionOperatorTest {

    private Operator mockChild;
    private BufferManagerImpl mockBufferManager;
    private Record matchingRecord;
    private Record nonMatchingRecord;

    @BeforeEach
    void setUp() {
        mockChild = mock(Operator.class);
        mockBufferManager = mock(BufferManagerImpl.class);

        byte[] matchingTitle = "wayfarers".getBytes();
        byte[] matchingMovieId = "tt0754347".getBytes();
        Row matchingRow = new Row(matchingMovieId, matchingTitle, "p123456789".getBytes(), "director".getBytes(), "Some Name".getBytes());

        byte[] nonMatchingTitle = "zebra".getBytes();
        Row nonMatchingRow = new Row("tt9999999".getBytes(), nonMatchingTitle, "p987654321".getBytes(), "writer".getBytes(), "Another Name".getBytes());

        matchingRecord = new Record(matchingRow, matchingRow.getPersonId(), matchingRow.getCategory(), matchingRow.getName(), new Rid(0, 0));
        nonMatchingRecord = new Record(nonMatchingRow, nonMatchingRow.getPersonId(), nonMatchingRow.getCategory(), nonMatchingRow.getName(), new Rid(1, 0));
    }

    @Test
    void testTitleMatchAndMismatch() throws Exception {
        when(mockChild.next()).thenReturn(matchingRecord).thenReturn(nonMatchingRecord).thenReturn(null);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.TITLE, "wayfarers", mockBufferManager, false);
        selection.open();

        Record result = selection.next();
        assertNotNull(result);
        assertEquals("wayfarers", result.getTitleDeserialized());

        Record shouldBeNull = selection.next();
        assertNull(shouldBeNull);

        selection.close();
    }

    @Test
    void testMovieIdMatch() throws Exception {
        when(mockChild.next()).thenReturn(matchingRecord).thenReturn(null);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.MOVIEID, "tt0754347", mockBufferManager, false);
        selection.open();

        Record result = selection.next();
        assertNotNull(result);
        assertEquals("tt0754347", result.getMovieIdDeserialized());
    }

    @Test
    void testCategoryMatch() throws Exception {
        when(mockChild.next()).thenReturn(matchingRecord).thenReturn(null);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.CATEGORY, "director", mockBufferManager, false);
        selection.open();
        assertNotNull(selection.next());
    }

    @Test
    void testPersonIdMatch() throws Exception {
        when(mockChild.next()).thenReturn(matchingRecord).thenReturn(null);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.PERSONID, "p123456789", mockBufferManager, false);
        selection.open();
        assertNotNull(selection.next());
    }

    @Test
    void testNameMatch() throws Exception {
        when(mockChild.next()).thenReturn(matchingRecord).thenReturn(null);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.NAME, "Some Name", mockBufferManager, false);
        selection.open();
        assertNotNull(selection.next());
    }



    @Test
    void testHasNextDelegation() throws Exception {
        when(mockChild.hasNext()).thenReturn(true);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.TITLE, "wayfarers", mockBufferManager, false);
        assertTrue(selection.hasNext());
    }

    @Test
    void testGetRelationDelegation() {
        when(mockChild.getRelation()).thenReturn(File.DISK);
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.TITLE, "wayfarers", mockBufferManager, false);
        assertEquals(File.DISK, selection.getRelation());
    }

    @Test
    void testOpenAndCloseDelegation() throws Exception {
        doNothing().when(mockChild).open();
        doNothing().when(mockChild).close();

        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.TITLE, "wayfarers", mockBufferManager, false);
        selection.open();
        selection.close();

        verify(mockChild, times(1)).open();
        verify(mockChild, times(1)).close();
    }

    @Test
    void testConstructorWithEndKey() {
        SelectionOperator selection = new SelectionOperator(mockChild, ColumnNames.TITLE, "a", "z", mockBufferManager, false);
        assertNotNull(selection);
    }
}