package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectionOperatorTest {

    private ProjectionOperator projectionOperator;
    private Operator mockChild;
    private BufferManagerImpl mockBufferManager;
    private Page mockPage;
    private Record sampleRecord;

    @BeforeEach
    void setUp() throws Exception {
        mockChild = mock(Operator.class);
        mockBufferManager = mock(BufferManagerImpl.class);
        mockPage = mock(Page.class);

        // Sample data for the Row
        byte[] movieId = Arrays.copyOf("tt0012345".getBytes(), 9);
        byte[] title = Arrays.copyOf("Sample Title".getBytes(), 30);
        byte[] personId = Arrays.copyOf("nm1234567".getBytes(), 10);
        byte[] category = Arrays.copyOf("director".getBytes(), 20);
        byte[] name = Arrays.copyOf("Sample Name".getBytes(), 105);

        Row sampleRow = new Row(movieId, title, personId, category, name);
        sampleRecord = new Record(sampleRow, personId, category, name, new Rid(0, 0));

        // Mock child operator behavior
        when(mockChild.hasNext()).thenReturn(true, false);
        when(mockChild.next()).thenReturn(sampleRecord).thenReturn(null);
        when(mockChild.getRelation()).thenReturn(File.TEMPORARY);

        // Mock buffer manager and page
        when(mockBufferManager.createPage(File.TEMPORARY)).thenReturn(mockPage);
        when(mockPage.insertRow(any(Row.class))).thenReturn(0);
        when(mockPage.isFull()).thenReturn(false);
        when(mockPage.getPid()).thenReturn(999L);
        doNothing().when(mockBufferManager).unpinPage(anyLong(), eq(File.TEMPORARY));
        doNothing().when(mockBufferManager).deleteTemporaryTable();

        projectionOperator = new ProjectionOperator(
                mockChild,
                new ColumnNames[]{ColumnNames.MOVIEID, ColumnNames.TITLE, ColumnNames.PERSONID, ColumnNames.CATEGORY, ColumnNames.NAME},
                File.TEMPORARY,
                mockBufferManager,
                false
        );
    }



    @Test
    void testCloseDeletesTemporaryTable() {
        projectionOperator.close();
        verify(mockBufferManager).deleteTemporaryTable();
        verify(mockChild).close();
    }

    @Test
    void testGetRelationDelegatesToChild() {
        when(mockChild.getRelation()).thenReturn(File.DISK);
        assertEquals(File.DISK, projectionOperator.getRelation());
    }



    @Test
    void testCreateNewRecordMaterializesSelectedColumns() throws Exception {
        // Configure mocked page behavior
        when(mockPage.insertRow(any(Row.class))).thenReturn(0);
        when(mockPage.isFull()).thenReturn(false);
        when(mockPage.getPid()).thenReturn(999L);
        when(mockBufferManager.createPage(File.TEMPORARY)).thenReturn(mockPage);

        // Reinitialize ProjectionOperator with full projection
        projectionOperator = new ProjectionOperator(
                mockChild,
                new ColumnNames[]{ColumnNames.MOVIEID, ColumnNames.TITLE, ColumnNames.PERSONID, ColumnNames.CATEGORY, ColumnNames.NAME},
                File.TEMPORARY,
                mockBufferManager,
                false
        );

        // Inject mockPage into private `currentPage` using reflection
        Field currentPageField = ProjectionOperator.class.getDeclaredField("currentPage");
        currentPageField.setAccessible(true);
        currentPageField.set(projectionOperator, mockPage);

        // Proceed with normal execution
        projectionOperator.open();
        projectionOperator.materializeTable(); // will use already-set mock `currentPage`
        Record result = projectionOperator.next();

        // Assertions on the returned record
        assertNotNull(result);
        assertEquals("tt0012345", new String(result.getRow().movieId).trim());
        assertEquals("Sample Title", new String(result.getRow().title).trim());
        assertEquals("nm1234567", new String(result.getRow().personId).trim());
        assertEquals("director", new String(result.getRow().category).trim());
        assertEquals("Sample Name", new String(result.getRow().name).trim());

        // Verify interactions
        verify(mockPage).insertRow(any(Row.class));
        verify(mockPage).isFull();
        verify(mockBufferManager, never()).createPage(File.TEMPORARY); // no new page needed
    }





}
