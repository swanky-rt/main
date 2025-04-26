package project_645;

public class TableScanOperator implements Operator {
    private final File tableFile;
    private final BufferManager bufferManager;
    private final String[] columns;
    private int currentPageIndex = 0;
    private Page currentPage;

    public TableScanOperator(BufferManager bufferManager, File tableName, String[] columns) {
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
    public boolean hasNext() {
        return currentPage != null && currentPage.getRowCount() > currentPageIndex;  // Check if there are more rows
    }

    @Override
    public Record next() {
        if (hasNext()) {
            Row row = currentPage.getRow(currentPageIndex);  // Get the next row from the current page
            currentPageIndex++;  // Move to the next row

            // For Movies table, we only need movieId and title.
            // Pass null for personId, category, and name.
            return new Record(row, null, null, null);  // Pass null for unused fields (personId, category, name)
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
}
