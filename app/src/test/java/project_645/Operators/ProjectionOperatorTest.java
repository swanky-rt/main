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

    private Record createDummyRecord(String movieId, String personId) {
        Row row = new Row(movieId.getBytes(), personId.getBytes());
        return new Record(row, movieId.getBytes(), null, personId.getBytes(), new Rid(0, 0));
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
    void closeTest() {
        projectionOperatorPrematerialized.close();
        verify(bufferManager).deleteTemporaryTable();
        verify(childOperator).close();
    }

    @Test
    void makeResetOperatorTrueTest() throws Exception {
        projectionOperatorPrematerialized.makeResetOperatorTrue();

        Operator scanOperator = mock(TableScanOperator.class);
        when(bufferManager.createPage(File.TEMPORARY)).thenReturn(mock(Page.class));
        when(scanOperator.hasNext()).thenReturn(false);
        when(scanOperator.next()).thenReturn(null);

        Record result = projectionOperatorPrematerialized.next();
        assertNull(result);
    }

    @Test
    void materializeTableTest() throws Exception {
        ProjectionOperator projection = new ProjectionOperator(
                childOperator, new String[]{"movieId", "personId"}, File.TEMPORARY, bufferManager, false);

        Record dummy1 = createDummyRecord("m1", "p1");
        Record dummy2 = createDummyRecord("m2", "p2");

        when(bufferManager.createPage(File.TEMPORARY)).thenReturn(mock(Page.class));
        when(childOperator.next()).thenReturn(dummy1).thenReturn(dummy2).thenReturn(null);

        projection.materializeTable();
        verify(bufferManager, atLeastOnce()).createPage(File.TEMPORARY);
    }

    @Test
    void getRelationTest() {
        assertNull( projectionOperatorPrematerialized.getRelation());
    }
}