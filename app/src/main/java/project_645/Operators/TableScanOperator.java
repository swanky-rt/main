package project_645.Operators;

import project_645.*;
import project_645.Record;

import java.io.IOException;

public class TableScanOperator implements Operator {
    private final File tableFile;
    private final BufferManagerImpl bufferManager;
    private int currentPageIndex = 0;
    private int curRowIndex = 0;
    private Page currentPage;
    private int maxPages;
   // private final String[] columns;

    public TableScanOperator(BufferManagerImpl bufferManager, File tableFile){//String[] columns) {
        this.bufferManager = bufferManager;
        this.tableFile = tableFile;
       // this.columns = columns;
    }

    @Override
    public void open() {
        try {
            currentPageIndex = 0;
            curRowIndex = 0;
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
                while (bufferManager.pinnedPages.containsKey(bufferManager.constructPageIdentifier(currentPage.getPid(), tableFile))) {
                    bufferManager.unpinPage(currentPage.getPid(), tableFile);
                }
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
        while (bufferManager.pinnedPages.containsKey(bufferManager.constructPageIdentifier(currentPage.getPid(), tableFile))) {
            bufferManager.unpinPage(currentPage.getPid(), tableFile);
        }
    return null;
}

    @Override
    public void close() {
        // Close the operator, releasing any resources if needed
        currentPage = null;
    }

    @Override
    public File getRelation() {
        return tableFile;
    }

    @Override
    public void makeResetOperatorTrue() throws Exception {
        if (currentPage != null) {
            while (bufferManager.pinnedPages.containsKey(bufferManager.constructPageIdentifier(currentPage.getPid(), tableFile))) {
                bufferManager.unpinPage(currentPage.getPid(), tableFile);
            }  //  Safe unpin
        }
        currentPageIndex = 0;
        curRowIndex = 0;
        currentPage = bufferManager.getPage(currentPageIndex, tableFile);
        return;
    }
}
