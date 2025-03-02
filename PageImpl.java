import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageImpl implements Page, Serializable {
    private static final int PAGE_SIZE = 4096; // 4kb
    private static final int ROW_SIZE = 39; //column 1 is 9 char, and column 2 is 30 char
    private static final int MAX_TUPLES = PAGE_SIZE/ROW_SIZE;
    private Row[] rowList = new Row[MAX_TUPLES];
    private int bytesToPad = PAGE_SIZE - (MAX_TUPLES * ROW_SIZE);
    private int curRowCount = 0;

    public PageImpl(){

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
        return curRowCount - 1;
    }

    @Override
    public boolean isFull() {
        return curRowCount == MAX_TUPLES;
    }

    @Override
    public Row[] getAllRows() {
        return rowList;
    }

    @Override
    public int getBytesToPad() {
        return bytesToPad;
    }
}
