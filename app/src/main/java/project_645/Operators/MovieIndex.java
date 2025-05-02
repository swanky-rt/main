package project_645.Operators;

import project_645.*;
import project_645.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MovieIndex implements Operator {
    private final File tableFile;
    private final BufferManagerImpl bufferManager;
//    private int curRowIndex = 0;
//    private int currentPageIndex=0;
    private int currentRecordIndex = 0;
    private Page currentPage;
    private String start;
    private String end;
    private List<Record> currentRecords = new ArrayList<>(); // Holds matching records

    public MovieIndex(BufferManagerImpl bufferManager, File tableName, String start, String end) {
        this.bufferManager = bufferManager;
        this.tableFile = tableName;
        this.start = start;
        this.end = end;
    }

    @Override
    public void open() {
        try {
            int currentPageIndex = 0;

            // Use index for querying movie titles
                currentRecords = getTitleRange(bufferManager, start, end);
                System.out.println("Indexed title range scan returned: " + currentRecords.size() + " records.");
            // You could add logic here for indexing on movieId in future
        } catch (Exception e) {
            System.err.println("Error opening MovieIndex: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return currentRecordIndex < currentRecords.size();
        //return currentPageIndex < bufferManager.getFileSizeOfChosenFile(tableFile) && curRowIndex < currentPage.getRowCount();  // Check if there are more rows
    }

    @Override
    public Record next() throws Exception {
        if (!hasNext()) {
            return null;
        }
        return currentRecords.get(currentRecordIndex++);
    }

    @Override
    public void close() {
        if (currentPage != null) {
            bufferManager.unpinPage(currentPage.getPid(), tableFile);  //  Safe unpin
        }
        // Close the operator, releasing any resources if needed
            currentRecords.clear();
            currentRecordIndex = 0;
    }

    @Override
    public File getRelation() {
        return tableFile;
    }

    @Override
    public void makeResetOperatorTrue() {
    }

    /**
     * Returns a list of Records with titles in the given range [start, end]
     */
    public List<Record> getTitleRange(BufferManagerImpl bufferManager, String start, String end) throws Exception {
        List<Record> resultRecords = new ArrayList<>();
        BTreeImpl bTreeTitleId = new BTreeImpl(bufferManager, 51, File.MOVIE_TITLE_IDX);  // B+ Tree for titles
        Iterator<Rid> titleRids = bTreeTitleId.rangeSearch(start, end);

        while (titleRids.hasNext()) {
            Rid curRID = titleRids.next();

            Page curPage = bufferManager.getPage(curRID.getPageId(), File.DISK);
            Row curRow = curPage.getRow(curRID.getSlotId());

            Record record = new Record(curRow, null, null, null, curRID);

            bufferManager.unpinPage(curPage.getPid(), File.DISK);

            String curMovieTitle = new String(curRow.getTitle()).trim();
            if (curMovieTitle.compareTo(start) >= 0 && curMovieTitle.compareTo(end) <= 0) {
                resultRecords.add(record);
            }
        }

        return resultRecords;
    }
}
