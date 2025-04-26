package project_645;

public class WorkedOnPage implements Page{
    static final int PAGE_SIZE = 4096;
    static final int ROW_SIZE = 39; //column 1 is 9 char, and column 2 is 30 char
    static final int MAX_TUPLES = PAGE_SIZE/ROW_SIZE;
    private Row[] rowList = new Row[MAX_TUPLES];
    static final int bytesToPad = PAGE_SIZE - (MAX_TUPLES * ROW_SIZE);
    private int curRowCount = 0;
    private boolean isDirty = true;
    private String[][] deserializedRows = new String[MAX_TUPLES][3];
    private int pageId;
    private File dataFile;
    private int pinCount = 0;

    public WorkedOnPage(int pageId, File dataFile){
        this.pageId = pageId;
        this.dataFile = dataFile;
    }

    public int insertRow(Row row) {
        if (isFull()) {
            return -1;
        }
        rowList[curRowCount] = row;
        curRowCount++;
        return curRowCount - 1;
    }

    public Row getRow(int rowId) {
        if (rowId >= 0 && rowId < curRowCount) {
            return rowList[rowId];
        }
        return null;
    }

    public boolean isFull() {
        return curRowCount >= MAX_TUPLES;
    }

    @Override
    public int getPid() {
        return 0;
    }

    @Override
    public void reassignPageId(int pageId) {

    }

    public Row[] getAllRows() {
        return rowList;
    }

    @Override
    public int getBytesToPad() {
        return 0;
    }

    public void setAllRows(Row[] rows) {
        this.rowList = rows;
        this.curRowCount = rows.length;
    }

    @Override
    public void setRowCount(int rowCount) {

    }

    public int getRowCount() {
        return curRowCount;
    }

    @Override
    public void deserializeRows() {

    }

    @Override
    public String[][] getDeserializedRows() {
        return new String[0][];
    }

    public void markDirty() {
        // Mark page as dirty
    }

    public void markNotDirty() {
        // Mark page as not dirty
    }

    public boolean getDirtyStatus() {
        return false;  // Implement accordingly
    }

    @Override
    public void incrementPinCount() {

    }

    @Override
    public void decrementPinCount() {

    }

    @Override
    public int getPinCount() {
        return 0;
    }

    @Override
    public File getDataFile() {
        return null;
    }
}
