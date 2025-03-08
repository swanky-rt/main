package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PageImplTest {

    @Mock
    private PageImpl pageImpl;

    @Mock
    private Row row1;

    @Mock
    private Row row2;

    @BeforeEach
    public void setUp() {
        lenient().when(row1.getMovieId()).thenReturn("tt0000499".getBytes());
        lenient().when(row1.getTitle()).thenReturn("An Impossible Voyage".getBytes());

        lenient().when(row2.getMovieId()).thenReturn("tt0000500".getBytes());
        lenient().when(row2.getTitle()).thenReturn("The Abductors".getBytes());
        MockitoAnnotations.openMocks(this); 
    }

    @Test
    public void testInsertRow_ShouldInsertRowCorrectly() {
        when(pageImpl.insertRow(row1)).thenReturn(0);
        int rowId = pageImpl.insertRow(row1);
        assertEquals(0, rowId, "Row should be inserted at position 0");
    }


    @Test
    public void testFullPageHandling_ShouldReturnErrorWhenPageIsFull() {
        when(pageImpl.insertRow(any(Row.class))).thenReturn(-1);
        assertEquals(-1, pageImpl.insertRow(row1), "Should return -1 when the page is full");
    }

    @Test
    public void testGetRow() {
        when(pageImpl.getRow(0)).thenReturn(row1);
        Row retrievedRow = pageImpl.getRow(0);
        assertNotNull(retrievedRow, "Retrieved row should not be null");
        assertArrayEquals(row1.getMovieId(), retrievedRow.getMovieId(), "Movie ID should match");
        assertArrayEquals(row1.getTitle(), retrievedRow.getTitle(), "Title should match");
    }

    @Test
    public void testGetRowOutOfBounds() {
        doReturn(null).when(pageImpl).getRow(anyInt());
        assertNull(pageImpl.getRow(-1), "Should return null for negative index");
        assertNull(pageImpl.getRow(100), "Should return null for out-of-bounds index");
    }


    
    @Test
    public void testDeserializeRow() {
        
        when(pageImpl.getRow(0)).thenReturn(row1);

        // Call the method under test
        pageImpl.deserializeRows();

        // Retrieve the deserialized rows
        String[][] deserializedRows = pageImpl.getDeserializedRows();

        // Assert that the deserializedRows is not null
        assertNotNull(deserializedRows); // Ensuring it's not null

        // Assert that the deserializedRows array is populated correctly
        assertEquals("tt0000499", deserializedRows[0][0]);  // Expected value at [0][0]
        assertEquals("An Impossible Voyage", deserializedRows[0][1]);  // Expected value at [0][1]
        assertEquals("tt0000500", deserializedRows[1][0]);  // Expected value at [1][0]
        assertEquals("The Abductors", deserializedRows[1][1]);  // Expected value at [1][1]

        // Verify that getAllRows() and deserializeRows() were called on pageImpl
        verify(pageImpl).getAllRows();
        verify(pageImpl).deserializeRows();
    }


    
    
    @Test
    public void testDeserializeRows() {
    // Mock the behavior of getAllRows to return an array of row1 and row2
    Row[] rowsArray = new Row[]{row1, row2};
    doReturn(rowsArray).when(pageImpl).getAllRows();

    // Call the method under test
    pageImpl.deserializeRows();

    // Retrieve the deserialized rows from the method's output
    String[][] deserializedRows = pageImpl.getDeserializedRows();

    // Debugging: Output deserializedRows to help identify the issue
    System.out.println("Deserialized Rows: " + Arrays.deepToString(deserializedRows));

    // Assert that deserializedRows is not null
    assertNotNull(deserializedRows, "Deserialized rows should not be null");

    // Assert that the deserializedRows array is populated correctly
    assertEquals("tt0000499", deserializedRows[0][0], "Movie ID for row 1 should match");
    assertEquals("An Impossible Voyage", deserializedRows[0][1], "Movie Title for row 1 should match");
    assertEquals("tt0000500", deserializedRows[1][0], "Movie ID for row 2 should match");
    assertEquals("The Abductors", deserializedRows[1][1], "Movie Title for row 2 should match");

    // Verify that getAllRows() and deserializeRows() were called
    verify(pageImpl).getAllRows(); // Ensure getAllRows was called
    verify(pageImpl).deserializeRows(); // Ensure deserializeRows was called
}



    
    
    @Test
    public void testPageFullStatus() {
        when(pageImpl.isFull()).thenReturn(false).thenReturn(true);
        assertFalse(pageImpl.isFull(), "Page should not be full initially");
        assertTrue(pageImpl.isFull(), "Page should be full after max rows are inserted");
        verify(pageImpl, times(2)).isFull();
    }
    
}
