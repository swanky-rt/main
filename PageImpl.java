import java.util.ArrayList;
import java.util.List;

public class PageImpl implements Page{
    private static final int PAGE_SIZE = 4096; // 4kb
    private static final int ROW_SIZE = 39; //column 1 is 9 char, and column 2 is 30 char
    private static final int MAX_TUPLES = PAGE_SIZE/ROW_SIZE;
    private final List<Row> rowList;

    public PageImpl(){
        this.rowList = new ArrayList<>();
    }
    @Override
    public Row getRow(int rowId) {
        return (rowId >=0 && rowId < rowList.size()) ? rowList.get(rowId) : null;
    }

    @Override
    public int insertRow(Row row) {
        if(isFull()){
            return -1;
        }
        rowList.add(row);;
        return rowList.size() - 1;

    }

    @Override
    public boolean isFull() {
        return rowList.size() == MAX_TUPLES;
    }
}
