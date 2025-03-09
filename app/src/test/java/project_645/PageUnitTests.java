package project_645;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

//Testing all non-getters and setters in a unit testing environment
//Also excluding deserialized methods as that isn't a requirement for this lab
public class PageUnitTests {

    @Test
    void TestGetRow() {
        Page page = new PageImpl(0);

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
        Page testPage = new PageImpl(0);

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
        Page page = new PageImpl(0);

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
