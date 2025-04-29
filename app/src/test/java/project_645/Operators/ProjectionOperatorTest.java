package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectionOperatorTest {

    private Operator childOperator;
    private BufferManagerImpl bufferManager;
    private ProjectionOperator projectionOperatorPrematerialized;
    private ProjectionOperator projectionOperatorNonPrematerialized;

    @BeforeEach
    void setUp() {
        childOperator = mock(Operator.class);
        bufferManager = mock(BufferManagerImpl.class);

        projectionOperatorPrematerialized = new ProjectionOperator(
                childOperator, new String[]{"movieId", "personId"}, File.TEMPORARY, bufferManager, true);

        projectionOperatorNonPrematerialized = new ProjectionOperator(
                childOperator, new String[]{"movieId", "personId"}, File.TEMPORARY, bufferManager, false);
    }

    @Test
    void openTest() {
        projectionOperatorPrematerialized.open();
        verify(childOperator).open();
    }

    @Test
    void hasNextTest() throws Exception {
        when(childOperator.hasNext()).thenReturn(true);
        assertTrue(projectionOperatorPrematerialized.hasNext());

        when(childOperator.hasNext()).thenReturn(false);
        assertFalse(projectionOperatorPrematerialized.hasNext());
    }

    @Test
    void nextTestPrematerialized() throws Exception {
        Record mockRecord = mock(Record.class);
        when(childOperator.next()).thenReturn(mockRecord).thenReturn(null);

        Record result = projectionOperatorPrematerialized.next();
        //assertNotNull(result);
    }

    @Test
    void nextTestNonPrematerialized() throws Exception {
        Record mockRecord = mock(Record.class);
        when(childOperator.next()).thenReturn(mockRecord).thenReturn(null);

        // Mock BufferManager createPage and unpinPage
        when(bufferManager.createPage(File.TEMPORARY)).thenReturn(mock(Page.class));

        Record result = projectionOperatorNonPrematerialized.next();
        //assertNotNull(result);
    }

    @Test
    void closeTest() {
        projectionOperatorPrematerialized.close();
        verify(bufferManager).deleteTemporaryTable();
        verify(childOperator).close();
    }

    @Test
    void makeResetOperatorTrueTest() throws Exception {
        // set reset operator
        projectionOperatorPrematerialized.makeResetOperatorTrue();

        // simulate child operator behavior after reset
        Operator scanOperator = mock(TableScanOperator.class);
        when(bufferManager.createPage(File.TEMPORARY)).thenReturn(mock(Page.class));
        when(scanOperator.hasNext()).thenReturn(false);
        when(scanOperator.next()).thenReturn(null);

        Record result = projectionOperatorPrematerialized.next();
        // Expect null because no records after reset
        assertNull(result);
    }

    @Test
    void materializeTableTest() throws Exception {
        ProjectionOperator projection = new ProjectionOperator(
                childOperator, new String[]{"movieId", "personId"}, File.TEMPORARY, bufferManager, false);

        Record mockRecord1 = mock(Record.class);
        Record mockRecord2 = mock(Record.class);

        when(bufferManager.createPage(File.TEMPORARY)).thenReturn(mock(Page.class));
        when(childOperator.next()).thenReturn(mockRecord1).thenReturn(mockRecord2).thenReturn(null);

        projection.materializeTable();

        //verify(bufferManager, atLeastOnce()).createPage(File.TEMPORARY);
    }

    @Test
    void getRelationTest() {
        assertEquals(File.TEMPORARY, projectionOperatorPrematerialized.getRelation());
    }
}
