package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PageImplTest {

    @InjectMocks
    private PageImpl pageImpl;

    @Mock
    private Row row1;

    @Mock
    private Row row2;

    // Removed unnecessary mock rows (row3 and row4)
    
    @BeforeEach
    public void setUp() {
        // Use lenient stubbing for the mocks that are not always used in tests
        lenient().when(row1.getMovieId()).thenReturn("tt0000499".getBytes());
        lenient().when(row1.getTitle()).thenReturn("An Impossible Voyage".getBytes());

        lenient().when(row2.getMovieId()).thenReturn("tt0000500".getBytes());
        lenient().when(row2.getTitle()).thenReturn("The Abductors".getBytes());
    }

    

    @Test
    public void testInsertRow_ShouldInsertRowCorrectly() {
        int rowId = pageImpl.insertRow(row1);
        assertEquals(0, rowId, "Row should be inserted at position 0");
        assertEquals(1, pageImpl.getRowCount(), "Row count should be 1 after insertion");
    }

    @Test
    public void testInsertRow_ShouldInsertMultipleRows() {
        pageImpl.insertRow(row1);
        pageImpl.insertRow(row2);
        assertEquals(2, pageImpl.getRowCount(), "Row count should be 2 after inserting two rows");
    }

    @Test
    public void testFullPageHandling_ShouldReturnErrorWhenPageIsFull() {
    // Inserting rows until the page is full
    for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
        Row mockRow = mock(Row.class); // Create mock Row object
        pageImpl.insertRow(mockRow); // Insert the mock row into the page
    }

    // Create a row that will fail to insert when the page is full
    Row row1 = mock(Row.class);
    
    // Call insertRow and check that it returns -1 (page is full)
    int rowId = pageImpl.insertRow(row1);
    
    // Assert that the return value is -1, indicating the page is full
    assertEquals(-1, rowId, "Should return -1 when the page is full");
}

    @Test
    public void testGetRow() {
        pageImpl.insertRow(row1);
        Row retrievedRow = pageImpl.getRow(0);
        assertNotNull(retrievedRow, "Retrieved row should not be null");
        assertArrayEquals(row1.getMovieId(), retrievedRow.getMovieId(), "Movie ID should match");
        assertArrayEquals(row1.getTitle(), retrievedRow.getTitle(), "Title should match");
    }

    @Test
    public void testGetRowOutOfBounds() {
        assertNull(pageImpl.getRow(-1), "Should return null for negative index");
        assertNull(pageImpl.getRow(100), "Should return null for out-of-bounds index");
    }

    @Test
public void testDeserializeRow() {
    
    
    row1.movieId = "tt0000499".getBytes(StandardCharsets.US_ASCII); // Correct byte array for movieId
    row1.title = "An Impossible Voyage".getBytes(StandardCharsets.US_ASCII); // Correct byte array for title

    
    

    
    pageImpl.insertRow(row1);
    

    
    pageImpl.deserializeRows();

    
    String[][] deserializedRows = pageImpl.getDeserializedRows();

    
    String deserializedMovieId = deserializedRows[0][0];
    String deserializedTitle = deserializedRows[0][1];

    assertEquals("tt0000499", deserializedMovieId );
    assertEquals("An Impossible Voyage", deserializedTitle );


        
        
        
    }


    @Test
    public void testDeserializeRows() {
    
    
    row1.movieId = "tt0000499".getBytes(StandardCharsets.US_ASCII);
    row1.title = "An Impossible Voyage".getBytes(StandardCharsets.US_ASCII);

    
    row2.movieId = "tt0000500".getBytes(StandardCharsets.US_ASCII);
    row2.title = "The Abductors".getBytes(StandardCharsets.US_ASCII);

    

    
    pageImpl.insertRow(row1);
    pageImpl.insertRow(row2);
    

   
    pageImpl.deserializeRows();

    
    String[][] deserializedRows = pageImpl.getDeserializedRows();

    
    for (int i = 0; i < pageImpl.getAllRows().length; i++) {
        
        String originalMovieId = new String(pageImpl.getAllRows()[i].movieId, StandardCharsets.US_ASCII).trim();
        String originalTitle = new String(pageImpl.getAllRows()[i].title, StandardCharsets.US_ASCII).trim();

        
        String deserializedMovieId = deserializedRows[i][0].trim();
        String deserializedTitle = deserializedRows[i][1].trim();

        
        System.out.println("Original - Movie ID: " + originalMovieId + ", Title: " + originalTitle);
        System.out.println("Deserialized - Movie ID: " + deserializedMovieId + ", Title: " + deserializedTitle);

        
        assertEquals("Movie ID mismatch at row " + i, originalMovieId, deserializedMovieId);
        assertEquals("Title mismatch at row " + i, originalTitle, deserializedTitle);
    }
}








    

    

@Test
public void testPageFullStatus() {
    assertFalse(pageImpl.isFull(), "Page should not be full initially");

    // Insert rows while mocking only if necessary
    for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
        
        pageImpl.insertRow(row1);  
    }

    assertTrue(pageImpl.isFull(), "Page should be full after max rows are inserted");
}


}
