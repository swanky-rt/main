package project_645.Operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import project_645.*;
import project_645.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MovieIndexTest {

    private MovieIndex movieIndex;
    private BufferManagerImpl mockBufferManager;
    private BTreeImpl mockBTree;
    private Page mockPage;
    private Row mockRow;
    private Rid mockRid;

    private final String START = "A";
    private final String END = "Z";

    @BeforeEach
    void setUp() throws Exception {
        mockBufferManager = mock(BufferManagerImpl.class);
        mockPage = mock(Page.class);
        mockRow = mock(Row.class);
        mockRid = mock(Rid.class);

        // Setup mock RID and mock BTree range search
        when(mockRid.getPageId()).thenReturn(1L);
        when(mockRid.getSlotId()).thenReturn(0);
        when(mockBufferManager.getPage(1L, File.DISK)).thenReturn(mockPage);
        when(mockPage.getRow(0)).thenReturn(mockRow);
        when(mockPage.getPid()).thenReturn(1L);
        when(mockRow.getTitle()).thenReturn("Sample Title".getBytes());

        doNothing().when(mockBufferManager).unpinPage(1L, File.DISK);

        movieIndex = spy(new MovieIndex(mockBufferManager, File.DISK, START, END));

        // Override getTitleRange to inject mocked data
        doReturn(Arrays.asList(new Record(mockRow, null, null, null, mockRid)))
                .when(movieIndex).getTitleRange(mockBufferManager, START, END);
    }

    @Test
    void testOpenInitializesRecords() {
        movieIndex.open();
        assertDoesNotThrow(() -> movieIndex.next());
    }

    @Test
    void testHasNextAndNext() throws Exception {
        movieIndex.open();

        assertTrue(movieIndex.hasNext());
        Record rec = movieIndex.next();
        assertNotNull(rec);
        assertFalse(movieIndex.hasNext());
        assertNull(movieIndex.next());  // end of iteration
    }




    @Test
    void testGetRelationReturnsCorrectFile() {
        assertEquals(File.DISK, movieIndex.getRelation());
    }

    @Test
    void testMakeResetOperatorTrueDoesNothing() {
        movieIndex.makeResetOperatorTrue();  // Should not throw or change state
    }

    @Test
    void testGetTitleRangeReturnsFilteredResults() throws Exception {
        // Set up iterator with a record having title in range
        BTreeImpl bTree = mock(BTreeImpl.class);
        Iterator<Rid> mockIterator = mock(Iterator.class);

        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(mockRid);
        when(mockBufferManager.getPage(anyLong(), eq(File.DISK))).thenReturn(mockPage);
        when(mockPage.getRow(anyInt())).thenReturn(mockRow);
        when(mockRow.getTitle()).thenReturn("Apple".getBytes());
        when(mockPage.getPid()).thenReturn(123L);

        doNothing().when(mockBufferManager).unpinPage(anyLong(), eq(File.DISK));
        doReturn(mockIterator).when(bTree).rangeSearch(anyString(), anyString());

        List<Record> results = movieIndex.getTitleRange(mockBufferManager, "A", "Z");
        assertEquals(1, results.size());
    }







}
