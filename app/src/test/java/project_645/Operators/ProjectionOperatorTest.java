package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectionOperatorTest {

    private ProjectionOperator projectionOperator;
    private Operator mockChild;
    private BufferManagerImpl mockBufferManager;
    private Page mockPage;
    private Record sampleRecord;
    private Row sampleRow;

    @BeforeEach
    void setUp() throws Exception {
        mockChild = mock(Operator.class);
        mockBufferManager = mock(BufferManagerImpl.class);
        mockPage = mock(Page.class);

        // Configure BufferManager + Page mocks
        when(mockBufferManager.createPage(File.TEMPORARY)).thenReturn(mockPage);
        when(mockBufferManager.constructPageIdentifier(anyLong(), any(File.class))).thenReturn("mock_pid");
        when(mockPage.getPid()).thenReturn(123L);
        when(mockPage.isFull()).thenReturn(false);
        when(mockPage.insertRow(any(Row.class))).thenReturn(0);
        doNothing().when(mockBufferManager).unpinPage(anyLong(), eq(File.TEMPORARY));
        doNothing().when(mockBufferManager).deleteTemporaryTable();

        // Sample data
        byte[] movieId = Arrays.copyOf("tt0012345".getBytes(), 9);
        byte[] title = Arrays.copyOf("Sample Title".getBytes(), 30);
        byte[] personId = Arrays.copyOf("nm1234567".getBytes(), 10);
        byte[] category = Arrays.copyOf("director".getBytes(), 20);
        byte[] name = Arrays.copyOf("Sample Name".getBytes(), 105);

        sampleRow = new Row(movieId, title, personId, category, name);
        sampleRecord = new Record(sampleRow, personId, category, name, new Rid(0, 0));

        when(mockChild.hasNext()).thenReturn(true, false);
        when(mockChild.next()).thenReturn(sampleRecord);
        when(mockChild.getRelation()).thenReturn(File.TEMPORARY);

        projectionOperator = new ProjectionOperator(
                mockChild,
                new ColumnNames[]{ColumnNames.MOVIEID, ColumnNames.TITLE, ColumnNames.PERSONID, ColumnNames.CATEGORY, ColumnNames.NAME},
                File.TEMPORARY,
                mockBufferManager,
                false
        );

        // Inject mockPage into private currentPage field using reflection
        Field currentPageField = ProjectionOperator.class.getDeclaredField("currentPage");
        currentPageField.setAccessible(true);
        currentPageField.set(projectionOperator, mockPage);
    }

    @Test
    void testCreateNewRecordMaterializesSelectedColumns() throws Exception {
        projectionOperator.open();
        projectionOperator.materializeTable(); // Triggers createNewRecord
        Record result = projectionOperator.next();

        assertNotNull(result);
        assertEquals("tt0012345", new String(result.getRow().movieId).trim());
        assertEquals("Sample Title", new String(result.getRow().title).trim());
        assertEquals("nm1234567", new String(result.getRow().personId).trim());
        assertEquals("director", new String(result.getRow().category).trim());
        assertEquals("Sample Name", new String(result.getRow().name).trim());

        verify(mockPage, atLeastOnce()).insertRow(any(Row.class));
        verify(mockPage, atLeastOnce()).isFull();
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
    void testNextReturnsNullAfterExhaustion() throws Exception {
        projectionOperator.open();
        projectionOperator.materializeTable();
        projectionOperator.next(); // consume one
        assertNull(projectionOperator.next()); // next should be null
    }
}
