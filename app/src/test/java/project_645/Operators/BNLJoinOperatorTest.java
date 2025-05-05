
package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
    void hasNextInitiallyTrue() {
        assertTrue(bnlJoinOperator.hasNext());
    }

    @Test
    void hasNextFalseAfterExhaustionViaReflection() throws Exception {
        Field outerExhaustedField = BNLJoinOperator.class.getDeclaredField("outerExhausted");
        outerExhaustedField.setAccessible(true);
        outerExhaustedField.set(bnlJoinOperator, true);
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
    void openCloseTest() throws Exception {
        bnlJoinOperator.open();
        verify(outer).open();
        verify(inner).open();

        bnlJoinOperator.close();
        verify(outer).close();
        verify(inner).close();
    }

    @Test
    void getRelationTest() {
        assertEquals(File.BNL1, bnlJoinOperator.getRelation());
    }

    @Test
    void makeResetOperatorTrueTest() {
        assertDoesNotThrow(() -> bnlJoinOperator.makeResetOperatorTrue());
    }

    @Test
    void byteArrayToIntConversionTest() throws Exception {
        byte[] array = {0x00, 0x00, 0x01, 0x00}; // should be 256
        Method method = BNLJoinOperator.class.getDeclaredMethod("byteArrayToInt", byte[].class, int.class);
        method.setAccessible(true);
        int result = (int) method.invoke(bnlJoinOperator, array, 0);
        assertEquals(256, result);
    }

    @Test
    void nextReturnsNullWhenOuterExhausted() throws Exception {
        Field outerExhaustedField = BNLJoinOperator.class.getDeclaredField("outerExhausted");
        outerExhaustedField.setAccessible(true);
        outerExhaustedField.set(bnlJoinOperator, true);
        assertNull(bnlJoinOperator.next());
    }

    @Test
    void getRecordKeyTestForAllFields() {
        Record mockRecord = mock(Record.class);
        when(mockRecord.getMovieIdBytes()).thenReturn("m1".getBytes());
        when(mockRecord.getNameBytes()).thenReturn("n1".getBytes());
        when(mockRecord.getCategoryBytes()).thenReturn("c1".getBytes());
        when(mockRecord.getMovieTitleBytes()).thenReturn("t1".getBytes());
        when(mockRecord.getPersonIdBytes()).thenReturn("p1".getBytes());

        assertEquals("m1", new String(bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.MOVIEID)).trim());
        assertEquals("n1", new String(bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.NAME)).trim());
        assertEquals("c1", new String(bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.CATEGORY)).trim());
        assertEquals("t1", new String(bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.TITLE)).trim());
        assertEquals("p1", new String(bnlJoinOperator.getRecordKey(mockRecord, ColumnNames.PERSONID)).trim());
    }

    @Test
    void createJoinedRecordReturnsNonNull() throws Exception {
        Record innerRecord = mock(Record.class);
        Page joinPage = mock(Page.class);
        Row row = mock(Row.class);

        when(innerRecord.getPersonIdBytes()).thenReturn("p1".getBytes());
        when(bufferManager.getPage(anyLong(), eq(File.BNL1))).thenReturn(joinPage);
        when(joinPage.getPid()).thenReturn(1L);
        when(joinPage.getRow(anyInt())).thenReturn(row);
        when(row.getMovieId()).thenReturn("m1".getBytes());
        when(row.getTitle()).thenReturn("Title".getBytes());

        Rid rid = new Rid(1, 0);
        Method method = BNLJoinOperator.class.getDeclaredMethod("createJoinedRecord", Record.class, Rid.class);
        method.setAccessible(true);
        Object out = method.invoke(bnlJoinOperator, innerRecord, rid);
        assertNotNull(out);
    }

    @Test
    void getNextOuterBlock_handlesNullFromOuter() throws Exception {
        when(outer.next()).thenReturn(null);
        Method method = BNLJoinOperator.class.getDeclaredMethod("getNextOuterBlock");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(bnlJoinOperator));
    }

    @Test
    void next_handlesFirstNextLogicGracefully() throws Exception {
        Field firstNext = BNLJoinOperator.class.getDeclaredField("firstNext");
        firstNext.setAccessible(true);
        firstNext.set(bnlJoinOperator, true);

        Field outerExhausted = BNLJoinOperator.class.getDeclaredField("outerExhausted");
        outerExhausted.setAccessible(true);
        outerExhausted.set(bnlJoinOperator, false);

        when(outer.next()).thenReturn(null);
        assertDoesNotThrow(() -> bnlJoinOperator.next());
    }

    @Test
    void next_returnsNullIfHashTableEmptyAndNotFirst() throws Exception {
        Field firstNext = BNLJoinOperator.class.getDeclaredField("firstNext");
        firstNext.setAccessible(true);
        firstNext.set(bnlJoinOperator, false);

        Field hashTable = BNLJoinOperator.class.getDeclaredField("hashTable");
        hashTable.setAccessible(true);
        hashTable.set(bnlJoinOperator, new java.util.HashMap<>());

        assertNull(bnlJoinOperator.next());
    }

    @Test
    void next_curInnerRidsPresentButCurRidNull_skipsToNextRecord() throws Exception {
        Record outerRecord = mock(Record.class);
        Record innerRecord = mock(Record.class);
        Row row = mock(Row.class);
        Page page = mock(Page.class);

        when(outerRecord.getRow()).thenReturn(row);
        when(outerRecord.getMovieIdDeserialized()).thenReturn("key1");
        when(outerRecord.getMovieIdBytes()).thenReturn("key1".getBytes());
        when(outer.next()).thenReturn(outerRecord).thenReturn(null);
        when(bufferManager.createPage(File.BNL1)).thenReturn(page);
        when(page.getPid()).thenReturn(1L);
        when(page.getRowCount()).thenReturn(0);
        when(page.isFull()).thenReturn(false);
        when(page.insertRow(any())).thenReturn(1);
        when(bufferManager.getPage(1L, File.BNL1)).thenReturn(page);
        when(page.getRow(0)).thenReturn(row);

        when(inner.next()).thenReturn(innerRecord).thenReturn(null);
        when(innerRecord.getMovieIdBytes()).thenReturn("key1".getBytes());
        when(innerRecord.getPersonIdBytes()).thenReturn("p1".getBytes());

        BNLJoinOperator op = new BNLJoinOperator(outer, inner, ColumnNames.MOVIEID, ColumnNames.MOVIEID, bufferManager, File.BNL1);
        op.open();

        // Inject simulated state after getNextOuterBlock
        Method m = BNLJoinOperator.class.getDeclaredMethod("getNextOuterBlock");
        m.setAccessible(true);
        m.invoke(op);

        Field hashTableField = BNLJoinOperator.class.getDeclaredField("hashTable");
        hashTableField.setAccessible(true);
        HashMap<String, List<Rid>> map = (HashMap<String, List<Rid>>) hashTableField.get(op);

        List<Rid> ridListWithNull = new ArrayList<>();
        ridListWithNull.add(null);  // simulate corrupt RID
        map.put("key1", ridListWithNull);

        // This should not throw even with null RID
        assertDoesNotThrow(op::next);
    }

    @Test
    void test_getNextOuterBlock_fullCoverage() throws Exception {
        Record outerRecord = mock(Record.class);
        Row row = mock(Row.class);
        Page page = mock(Page.class);

        // Setup for outer record
        when(outerRecord.getMovieIdDeserialized()).thenReturn("tt0754347");
        when(outerRecord.getMovieIdBytes()).thenReturn("tt0754347".getBytes());
        when(outerRecord.getRow()).thenReturn(row);

        // Setup for page
        when(page.getPid()).thenReturn(1L);
        when(page.getRowCount()).thenReturn(0);
        when(page.isFull()).thenReturn(false); // Ensure it won't toggle createPage again
        when(page.insertRow(row)).thenReturn(1);

        // BufferManager setup
        when(bufferManager.createPage(File.BNL1)).thenReturn(page);

        // Mocking outer.next() enough times to go through the full loop
        int loopLimit = 2; // simulate 2 iterations
        Record[] records = new Record[loopLimit + 1];
        for (int i = 0; i < loopLimit; i++) {
            records[i] = outerRecord;
        }
        records[loopLimit] = null; // to exit the loop

        when(outer.next()).thenReturn(records[0], java.util.Arrays.copyOfRange(records, 1, records.length));

        // Setup bnlJoinOperator to use a small numTuples to force loop entry
        Field frameField = BNLJoinOperator.class.getDeclaredField("freeBufferPoolFrames");
        frameField.setAccessible(true);
        frameField.set(bnlJoinOperator, 1); // small to make numTuples small

        // Invoke private method
        Method method = BNLJoinOperator.class.getDeclaredMethod("getNextOuterBlock");
        method.setAccessible(true);
        method.invoke(bnlJoinOperator);

        // Verify key presence
        Field hashField = BNLJoinOperator.class.getDeclaredField("hashTable");
        hashField.setAccessible(true);
        HashMap<String, List<Rid>> map = (HashMap<String, List<Rid>>) hashField.get(bnlJoinOperator);
        assertNotNull(map.get("tt0754347"), "Expected key in hash table");
        assertEquals(loopLimit, map.get("tt0754347").size());
    }

    @Test
    void createJoinedRecord_handlesBNL2Case() throws Exception {
        Record innerRecord = mock(Record.class);
        Page joinPage = mock(Page.class);
        Row row = mock(Row.class);

        // Mock behavior for BNL2-specific data
        when(innerRecord.getNameBytes()).thenReturn("name".getBytes());
        when(joinPage.getPid()).thenReturn(42L);
        when(joinPage.getRow(anyInt())).thenReturn(row);
        when(row.getMovieId()).thenReturn("m1".getBytes());
        when(row.getTitle()).thenReturn("title".getBytes());
        when(row.getPersonId()).thenReturn("p1".getBytes());

        BufferManagerImpl bufferManagerMock = mock(BufferManagerImpl.class);
        when(bufferManagerMock.getPage(42L, File.BNL2)).thenReturn(joinPage);

        BNLJoinOperator operator = new BNLJoinOperator(
                mock(Operator.class),
                mock(Operator.class),
                ColumnNames.MOVIEID,
                ColumnNames.PERSONID,
                bufferManagerMock,
                File.BNL2 // explicitly testing BNL2 case
        );

        Rid rid = new Rid(42L, 0);
        Method m = BNLJoinOperator.class.getDeclaredMethod("createJoinedRecord", Record.class, Rid.class);
        m.setAccessible(true);
        Record result = (Record) m.invoke(operator, innerRecord, rid);

        assertNotNull(result);
    }
}
