package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.*;
import project_645.Record;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

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

        when(mockBufferManager.createPage(File.TEMPORARY)).thenReturn(mockPage);
        when(mockBufferManager.constructPageIdentifier(anyLong(), any(File.class))).thenReturn("mock_pid");
        when(mockPage.getPid()).thenReturn(123L);
        when(mockPage.isFull()).thenReturn(false);
        when(mockPage.insertRow(any(Row.class))).thenReturn(0);
        doNothing().when(mockBufferManager).unpinPage(anyLong(), eq(File.TEMPORARY));
        doNothing().when(mockBufferManager).deleteTemporaryTable();

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

        Field currentPageField = ProjectionOperator.class.getDeclaredField("currentPage");
        currentPageField.setAccessible(true);
        currentPageField.set(projectionOperator, mockPage);
    }


    @Test
    void testOpenDelegatesToChild() throws Exception {
        // Act
        projectionOperator.open();

        // Assert
        verify(mockChild, times(1)).open();
    }

    @Test
    void testHasNextDelegatesToChild() throws Exception {
        // Setup child mock to return true
        when(mockChild.hasNext()).thenReturn(true);
        assertTrue(projectionOperator.hasNext());

        // Setup child mock to return false
        when(mockChild.hasNext()).thenReturn(false);
        assertFalse(projectionOperator.hasNext());

        // Verify delegation
        verify(mockChild, times(2)).hasNext();
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
    void testMakeResetOperatorTrueSetsFlag() throws Exception {
        Field resetField = ProjectionOperator.class.getDeclaredField("resetOperator");
        resetField.setAccessible(true);
        assertFalse((boolean) resetField.get(projectionOperator));

        projectionOperator.makeResetOperatorTrue();
        assertTrue((boolean) resetField.get(projectionOperator));
    }

    @Test
    void testByteArrayToIntConversion() throws Exception {
        byte[] testBytes = new byte[]{0x00, 0x00, 0x01, 0x00};
        Method method = ProjectionOperator.class.getDeclaredMethod("byteArrayToInt", byte[].class, int.class);
        method.setAccessible(true);

        int result = (int) method.invoke(projectionOperator, testBytes, 0);
        assertEquals(256, result);
    }

    /*@Test
    void testCreateNewRecordMaterializesSelectedColumns() throws Exception {
        projectionOperator.open();
        projectionOperator.materializeTable();
        Record result = projectionOperator.next();

        assertNotNull(result);
        assertEquals("tt0012345", new String(result.getRow().movieId).trim());
        assertEquals("Sample Title", new String(result.getRow().title).trim());
        assertEquals("nm1234567", new String(result.getRow().personId).trim());
        assertEquals("director", new String(result.getRow().category).trim());
        assertEquals("Sample Name", new String(result.getRow().name).trim());

        verify(mockPage, atLeastOnce()).insertRow(any(Row.class));
        verify(mockPage, atLeastOnce()).isFull();
    }*/

   /* @Test
    void testNextReturnsNullAfterExhaustion() throws Exception {
        projectionOperator.open();
        projectionOperator.materializeTable();
        projectionOperator.next();
        assertNull(projectionOperator.next());
    }*/







}
