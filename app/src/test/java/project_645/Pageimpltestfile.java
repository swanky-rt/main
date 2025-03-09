package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Pageimpltestfile {
    private PageImpl page;
    private Row row;

    @BeforeEach
    public void setUp() {
        page = mock(PageImpl.class);
        row = mock(Row.class);
        page = new PageImpl(1);
    }

    @Test
    public void testRow() {
        page.insertRow(row);
        assertNotNull(page.getAllRows());
        assertEquals(1, page.getRowCount());  // Assert that a row has been added
    }



    @Test
    public void testSingleRow() {
        page.insertRow(row);
        Row fetchedRow = page.getRow(0);  // Corrected the index to 0, since rowId starts from 0
        assertNotNull(fetchedRow);
        assertEquals(row, fetchedRow);  // Assert that the inserted row is the same as the fetched row
    }

    @Test
    public void testBytesToPad() {
        Row row1 = new Row(new byte[1], new byte[1]);
        Row row2 = new Row(new byte[2], new byte[2]);
        page.insertRow(row1);
        page.insertRow(row2);
        assertEquals(PageImpl.bytesToPad, page.getBytesToPad());  // Assert the bytes to pad
    }

    @Test
    public void testSetAllRows() {
        Row[] rows = new Row[2];
        page.setAllRows(rows);
        assertArrayEquals(rows, page.getAllRows());  // Assert that the rows are set correctly
    }

    @Test
    public void testSetRowCount() {
        page.setRowCount(2);
        assertEquals(2, page.getRowCount());  // Assert that the row count is set correctly
    }

    @Test
    public void testGetRowCount() {
        page.setRowCount(5);
        assertEquals(5, page.getRowCount());  // Assert the correct row count is returned
    }

    @Test
    public void testGetDirtyStatus() {
        page.markDirty();
        assertTrue(page.getDirtyStatus());  // Assert that the page is marked as dirty
        page.markNotDirty();
        assertFalse(page.getDirtyStatus());  // Assert that the page is marked as not dirty
    }

    @Test
    public void testMarkDirtyStatus() {
        page.markDirty();
        assertTrue(page.getDirtyStatus());  // Assert that the page is dirty after marking
    }

    @Test
    public void testMarkNotDirty() {
        page.markNotDirty();
        assertFalse(page.getDirtyStatus());  // Assert that the page is not dirty after marking
    }

    @Test
    public void testGetDeserializedMethod() {
        String[][] deserializedRows = page.getDeserializedRows();
        assertNotNull(deserializedRows);  // Assert that the deserialized rows array is not null
    }

    @Test
    public void testDeserializeRows() {
        Row row1 = new Row("tt01".getBytes(), "An impossible movie".getBytes());
        Row row2 = new Row("tt02".getBytes(), "Not impossible movie".getBytes());
        Row[] rows = new Row[]{row1, row2};
        page.setAllRows(rows);
        page.deserializeRows();
        String[][] deserializedRows = page.getDeserializedRows();
        assertNotNull(deserializedRows);  // Assert deserialized rows are not null
        assertEquals("tt01", deserializedRows[0][0]);  // Assert deserialized movie ID
        assertEquals("An impossible movie", deserializedRows[0][1]);  // Assert deserialized movie title
    }

    @Test
    public void testDeserializeEmptyRows() {
        Row row = new Row("".getBytes(), "".getBytes());
        Row[] rows = new Row[]{row};
        page.setAllRows(rows);
        page.deserializeRows();
        String[][] deserializedRows = page.getDeserializedRows();
        assertNotNull(deserializedRows);  // Assert deserialized rows are not null
        assertEquals("", deserializedRows[0][0]);  // Assert empty movie ID
        assertEquals("", deserializedRows[0][1]);  // Assert empty movie title
    }

    @Test
    public void testGetPid() {
        assertEquals(1, page.getPid());  // Assert the page ID is correct
    }

    @Test
    public void testIncrementPinCount() {
        page.incrementPinCount();
        assertEquals(1, page.getPinCount());  // Assert that the pin count is incremented
    }

    @Test
    public void testDecrementPinCount() {
        page.incrementPinCount();
        page.decrementPinCount();
        assertEquals(0, page.getPinCount());  // Assert that the pin count is decremented
    }

    @Test
    public void testReassignPageId() {
        page.reassignPageId(10);
        assertEquals(10, page.getPid());  // Assert the page ID is reassigned correctly
    }

    @Test
    public void testIsFull() {
        for (int i = 0; i < PageImpl.MAX_TUPLES; i++) {
            page.insertRow(new Row(new byte[1], new byte[1]));
        }
        assertTrue(page.isFull());  // Assert that the page is full after inserting MAX_TUPLES rows
    }
}
