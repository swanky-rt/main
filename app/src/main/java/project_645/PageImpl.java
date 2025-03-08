package project_645;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class PageImpl implements Page, Serializable {
    static final int PAGE_SIZE = 4096;
    static final int ROW_SIZE = 39; //column 1 is 9 char, and column 2 is 30 char
    static final int MAX_TUPLES = PAGE_SIZE/ROW_SIZE;
    private Row[] rowList = new Row[MAX_TUPLES];
    static final int bytesToPad = PAGE_SIZE - (MAX_TUPLES * ROW_SIZE);
    private int curRowCount = 0;
    private boolean isDirty = true;
    private String[][] deserializedRows = new String[MAX_TUPLES][2];
    private int pageId;
    private int pinCount = 0;

    public PageImpl(int pageId){
        this.pageId = pageId;
    }
    @Override
    public Row getRow(int rowId) {
        return (rowId >=0 && rowId < curRowCount) ? rowList[rowId] : null;
    }

    @Override
    public int insertRow(Row row) {
        if(isFull()){
            return -1;
        }
        rowList[curRowCount] = row;
        curRowCount += 1;
        this.markDirty();
        return curRowCount - 1;
    }

    @Override
    public boolean isFull() {
        return curRowCount == MAX_TUPLES;
    }

    @Override
    public int getPid() {
        return this.pageId;
    }

    @Override
    public void incrementPinCount() {
        this.pinCount += 1;
    }

    @Override
    public void decrementPinCount() {
        --this.pinCount;
        this.pinCount -= Math.max(this.pinCount, 0);
    }

    @Override
    public int getPinCount() {
        return this.pinCount;
    }

    @Override
    public Row[] getAllRows() {
        return rowList;
    }

    @Override
    public int getBytesToPad() {
        return bytesToPad;
    }

    @Override
    public void setAllRows(Row[] rows) {
        this.rowList = rows;
    }

    @Override
    public void setRowCount(int rowCount) {
        this.curRowCount = rowCount;
    }

    @Override
    public int getRowCount() {
        return curRowCount;
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    @Override
    public void markNotDirty() {
        this.isDirty = false;
    }

    @Override
    public boolean getDirtyStatus() {
        return this.isDirty;
    }

    @Override
    public void deserializeRows() {
        for (int i = 0; i < getAllRows().length; ++i) {
            Row curRow = getAllRows()[i];
            if (curRow != null) {
                String movieId = new String(curRow.movieId, StandardCharsets.US_ASCII).replaceAll("\u0000", "");
                String movieTitle = new String(curRow.title, StandardCharsets.US_ASCII).replaceAll("\u0000", "");;
                deserializedRows[i][0] = movieId;
                deserializedRows[i][1] = movieTitle;
            }
        }
    }

    @Override
    public String[][] getDeserializedRows() {
        return this.deserializedRows;
    }



}
