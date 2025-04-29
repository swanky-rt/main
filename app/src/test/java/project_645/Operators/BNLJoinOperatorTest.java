package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BNLJoinOperatorTest {

    private Operator outer;
    private Operator inner;
    private BufferManagerImpl bufferManager;
    private BNLJoinOperator bnlJoinOperator;

    @BeforeEach
    void setUp() {
        outer = mock(Operator.class);
        inner = mock(Operator.class);
        bufferManager = mock(BufferManagerImpl.class);
        bnlJoinOperator = new BNLJoinOperator(outer, inner, ColumnNames.MOVIEID, ColumnNames.MOVIEID, bufferManager, File.BNL1);
    }

    @Test
    void hasNextInitiallyTrue() throws Exception {
        assertTrue(bnlJoinOperator.hasNext());
    }

    @Test
    void hasNextFalseAfterExhaustionViaReflection() throws Exception {
        // Use reflection to access and modify the private field 'outerExhausted'
        Field outerExhaustedField = BNLJoinOperator.class.getDeclaredField("outerExhausted");
        outerExhaustedField.setAccessible(true);  // Bypass private access
        outerExhaustedField.set(bnlJoinOperator, true);  // Set value to true

        // Verify that hasNext now returns false
        assertFalse(bnlJoinOperator.hasNext());
    }

    @Test
    void getRecordKeyTest() {
        Record mockRecord = mock(Record.class);
        when(mockRecord.getMovieIdBytes()).thenReturn("tt001".getBytes());

        byte[] result = bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.MOVIEID);
        assertNotNull(result);
        assertEquals("tt001", new String(result).trim());
    }

    @Test
    void openCloseTest() {
        bnlJoinOperator.open();
        verify(outer, times(1)).open();
        verify(inner, times(1)).open();

        bnlJoinOperator.close();
        verify(outer, times(1)).close();
        verify(inner, times(1)).close();
    }

    @Test
    void getRelationTest() {
        assertEquals(File.BNL1, bnlJoinOperator.getRelation());
    }

    @Test
    void makeResetOperatorTrueTest() {
        // Just to verify the method exists and doesn't throw
        bnlJoinOperator.makeResetOperatorTrue();
    }
}
