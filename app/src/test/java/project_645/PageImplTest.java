package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PageImplTest {

    private PageImpl pageImpl;
    private Row row1;
    private Row row2;
    private Row row3;
    private Row row4;

    @BeforeEach
    public void setUp() {
        pageImpl = new PageImpl();

        // Convert String to byte[] for the movieId and title
        row1 = new Row("tt0000499".getBytes(), "An Impossible Voyage".getBytes());
        row2 = new Row("tt0000500".getBytes(), "The Abductors".getBytes());
        row3 = new Row("tt0000501".getBytes(), "Adventures of Sherlock Holmes".getBytes());
        row4 = new Row("tt0000502".getBytes(), "Bohemios".getBytes());
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
        // Insert enough rows to fill the page
        for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
            pageImpl.insertRow(new Row(String.format("tt%07d", i).getBytes(), ("Movie " + i).getBytes()));
        }

        // Try to insert a row when the page is full
        int rowId = pageImpl.insertRow(row1);
        assertEquals(-1, rowId, "Should return -1 when the page is full");
    }

    

    @Test
    public void testInsertRow_ShouldNotInsertWhenPageIsFull() {
        // Insert enough rows to fill the page
        for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
            pageImpl.insertRow(new Row(String.format("tt%07d", i).getBytes(), ("Movie " + i).getBytes()));
        }

        // Try to insert a row when the page is full
        int rowId = pageImpl.insertRow(row1);
        assertEquals(-1, rowId, "Row should not be inserted when page is full");
        assertEquals(PageImpl.MAX_TUPLES, pageImpl.getRowCount(), "Row count should remain the same when insertion fails");
    }

    @Test
    public void testGetRow() {
        pageImpl.insertRow(row1);
        Row retrievedRow = pageImpl.getRow(0);
        assertNotNull(retrievedRow, "Retrieved row should not be null");
        assertArrayEquals(row1.movieId, retrievedRow.movieId, "Movie ID should match");
        assertArrayEquals(row1.title, retrievedRow.title, "Title should match");
    }

    @Test
    public void testGetRowOutOfBounds() {
        assertNull(pageImpl.getRow(-1), "Should return null for negative index");
        assertNull(pageImpl.getRow(100), "Should return null for out-of-bounds index");
    }

    @Test
    public void testGetRow_ShouldReturnNullForInvalidIndex() {
        Row retrievedRow = pageImpl.getRow(-1);
        assertNull(retrievedRow, "Row should be null for invalid index");

        retrievedRow = pageImpl.getRow(100);
        assertNull(retrievedRow, "Row should be null for out-of-bounds index");
    }

    @Test
    public void testDeserializeRows_ShouldHandleEmptyRowsGracefully() {
        pageImpl.deserializeRows();
        String[][] deserializedRows = pageImpl.getDeserializedRows();
        assertNull(deserializedRows[0][0], "Deserialization should not fail when no rows are inserted");
    }
    
    @Test
    public void testDeserializeRows() {
        pageImpl.insertRow(row1);
        pageImpl.insertRow(row2);
        pageImpl.deserializeRows();

        String[][] deserializedRows = pageImpl.getDeserializedRows();
        assertEquals("tt0000499", deserializedRows[0][0], "Movie ID should be deserialized correctly");
        assertEquals("An Impossible Voyage", deserializedRows[0][1], "Movie Title should be deserialized correctly");
        assertEquals("tt0000500", deserializedRows[1][0], "Movie ID should be deserialized correctly");
        assertEquals("The Abductors", deserializedRows[1][1], "Movie Title should be deserialized correctly");
    }

    @Test
    public void testDeserializeEmptyRows() {
        pageImpl.deserializeRows();
        String[][] deserializedRows = pageImpl.getDeserializedRows();
        assertNull(deserializedRows[0][0], "Should handle empty rows gracefully");
    }

    @Test
    public void testPageFullStatus() {
        assertFalse(pageImpl.isFull(), "Page should not be full initially");

        for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
            pageImpl.insertRow(new Row(String.format("tt%07d", i).getBytes(), ("Movie " + i).getBytes()));
        }

        assertTrue(pageImpl.isFull(), "Page should be full after max rows are inserted");
    }


}
