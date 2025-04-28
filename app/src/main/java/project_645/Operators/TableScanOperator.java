package project_645.Operators;

import project_645.*;
import project_645.Record;

import java.io.IOException;

public class TableScanOperator implements Operator {
    private final File tableFile;
    private final BufferManager bufferManager;
    private final String[] columns;
    private int currentPageIndex = 0;
    private int curRowIndex = 0;
    private Page currentPage;
    private int maxPages;

    public TableScanOperator(BufferManagerImpl bufferManager, File tableName, String[] columns) {
        this.bufferManager = bufferManager;
        this.tableFile = tableName;
        this.columns = columns;
    }

    @Override
    public void open() {
        try {
            currentPageIndex = 0;
            currentPage = bufferManager.getPage(currentPageIndex, tableFile);  // Correct method: getPage()

        } catch (Exception e) {
            System.err.println("Error opening TableScanOperator: " + e.getMessage());
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return currentPageIndex < bufferManager.getFileSizeOfChosenFile(tableFile) && curRowIndex < currentPage.getRowCount();  // Check if there are more rows
    }

    @Override
    public Record next() throws Exception {
        if (this.currentPage == null) {
            int test = 1;
        }
        if (hasNext()) {
            Row row = currentPage.getRow(curRowIndex);  // Get the next row from the current page
            curRowIndex++;  // Move to the next row

            if (curRowIndex >= currentPage.getRowCount()) {
                bufferManager.unpinPage(currentPageIndex, tableFile);
                curRowIndex = 0;
                ++currentPageIndex;
                if (hasNext()) {
                    currentPage = bufferManager.getPage(currentPageIndex, tableFile);
                }
            }
            // For Movies table, we only need movieId and title.
            // Pass null for personId, category, and name.
            return new Record(row, null, null, null, new Rid(currentPageIndex, curRowIndex));  // Pass null for unused fields (personId, category, name)
        }
    return null;
}

    @Override
    public void close() {
        if (currentPage != null) {
            bufferManager.unpinPage(currentPage.getPid(), tableFile);  //  Safe unpin
        }
        // Close the operator, releasing any resources if needed
        currentPage = null;
    }

    @Override
    public File getRelation() {
        return tableFile;
    }

    @Override
    public void makeResetOperatorTrue() {
        return;
    }
}
