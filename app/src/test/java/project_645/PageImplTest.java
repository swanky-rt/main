package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PageImplTest {
    private PageImpl page;
    private Row row;

    @BeforeEach
    public void setUp() {
        page = mock(PageImpl.class);
        row = mock(Row.class);
        page = new PageImpl(1, File.DISK);
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

    @Test
    void TestGetRow() {
        Page page = new PageImpl(0, File.DISK);

        // current row count of the page is 0, attempting to get a row should return null
        Row testRowOne = page.getRow(3);
        // attempting to reference an element out of bounds in either direction should result
        // in null being returned as well
        Row testRowTwo = page.getRow(-2);
        // only 105 rows per page
        Row testRowThree = page.getRow(210);

        assertNull(testRowOne);
        assertNull(testRowTwo);
        assertNull(testRowThree);

        // we now insert a row into the page object, this should be accessible
        Row row = new Row("moveId0".getBytes(StandardCharsets.US_ASCII), "movie title 0".getBytes());
        page.insertRow(row);
        // we should be able to access this row now
        Row testRowFour = page.getRow(0);

        assertEquals(row, testRowFour);

        // verify that we still cannot get any rows in bounds past expected row count and out of bounds
        Row testRowFive = page.getRow(1);
        Row testRowSix = page.getRow(-2);
        Row testRowSeven = page.getRow(210);
        assertNull(testRowFive);
        assertNull(testRowSix);
        assertNull(testRowSeven);

        // run these tests for each iteration of filling up the page
        for (int i = 1; i < page.getAllRows().length; ++i) {
            Row loopRow = new Row(("movieId" + i).getBytes(StandardCharsets.US_ASCII),
                    ("movie Title" + i).getBytes(StandardCharsets.US_ASCII));
            page.insertRow(loopRow);
            // check not possible + out of bounds, except for the last iteration of the loop
            // where null reference should be impossible
            Row testRowLoop1 = page.getRow(i + 1);
            Row testRowLoop2 = page.getRow(-2);
            Row testRowLoop3 = page.getRow(210);
            assertNull(testRowLoop1);
            assertNull(testRowLoop2);
            assertNull(testRowLoop3);

            // check if the gotten row is the same as the inserted row
            Row testRowLoop4 = page.getRow(i);
            assertEquals(testRowLoop4, loopRow);
        }
    }

    @Test
    void testInsertRow() {
        Page testPage = new PageImpl(0, File.DISK);

        // make sure the row count is 0
        assertEquals(testPage.getRowCount(), 0);
        for (int i = 0; i < testPage.getAllRows().length; ++i) {
            Row loopRow = new Row(("movieId" + i).getBytes(StandardCharsets.US_ASCII),
                    ("movie Title" + i).getBytes(StandardCharsets.US_ASCII));
            testPage.insertRow(loopRow);
            //make sure count properly updates
            assertEquals(testPage.getRowCount(), i + 1);
            //make sure newly inserted row is at the most recent position
            assertEquals(loopRow, testPage.getRow(i));
            //make sure that isFull is false unless i = 104
            if (i < 104) {
                assertFalse(testPage.isFull());
            }
            else {
                assertTrue(testPage.isFull());
            }
        }
        // attempt to insert a row after the page is full
        int status = testPage.insertRow(new Row("aefawf".getBytes(StandardCharsets.US_ASCII), "awefawef".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(status, -1);
        assertEquals(105, testPage.getRowCount());
    }

    @Test
    void testPinMethods() {
        Page page = new PageImpl(0, File.DISK);

        // When explicitly creating a page object, the pin count should start at 0
        // Only when "CreatePage" in the buffer manager is called should the pinned count not be 0
        // upon page creation.
        assertEquals(page.getPinCount(), 0);

        // increment pin count once
        page.incrementPinCount();
        //check that page pin count is 1
        assertEquals(page.getPinCount(), 1);
        // increment three more times
        page.incrementPinCount();
        page.incrementPinCount();
        page.incrementPinCount();
        //ensure pin count is 4
        assertEquals(page.getPinCount(), 4);
        //decrement pin count once
        page.decrementPinCount();
        //ensure pin count is 3
        assertEquals(page.getPinCount(), 3);
        //decrement pin count to 0
        page.decrementPinCount();
        page.decrementPinCount();
        page.decrementPinCount();
        //ensure count is 0
        assertEquals(page.getPinCount(), 0);
        //attempt to decrement pin count below 0, pin count should remain 0
        page.decrementPinCount();
        assertEquals(page.getPinCount(), 0);
    }
}

