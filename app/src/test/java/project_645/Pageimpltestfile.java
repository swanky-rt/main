package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class Pageimpltestfile {
    private PageImpl page;
    private Row row;

    @BeforeEach

    public void setUp() {
        page = mock(PageImpl.class);
        row = mock(Row.class);
        page = new PageImpl(1);
    }

    @Test
    public void testRow(){
        row = new Row(new byte[1], new byte[1]);
        page.insertRow(row);
        page.getAllRows();
    }

    @Test
    public void testRows(){
        row = new Row(new byte[1], new byte[1]);
        page.insertRow(row);
        page.getAllRows();
    }

    @Test
    public void testSingleRow(){
        row = new Row(new byte[1], new byte[1]);
        page.insertRow(row);
        page.getRow(1);
    }

    @Test
    public void testByteToPad(){
        Row row1 = new Row(new byte[1], new byte[1]);
        Row row2 = new Row(new byte[2], new byte[2]);
        List list = new ArrayList<>();
        list.add(row1);
        page.insertRow(row2);
        page.getBytesToPad();
    }

    @Test
    public void testSetAllRows(){
        page.setAllRows(new Row[2]);
    }

    @Test
    public void testSetRowCount(){
        page.setRowCount(2);
    }

    @Test
    public void testGetRowCount(){
        page.getRowCount();
    }

    @Test
    public void testGetDirtyStatus()
    {
        page.getDirtyStatus();
    }

    @Test
    public void testMarkDirtyStatus(){
        page.markDirty();
    }

    @Test
    public void testMarkNotDirty(){
        page.markNotDirty();
    }

    @Test
    public void testGetDeserializedMethod(){
        page.getDeserializedRows();
    }

    @Test
    public void testDeserializeRows(){
        Row row1 = new Row("tt01".getBytes(), "An impossible movie".getBytes());
        Row row2 = new Row("tt02".getBytes(), "Not impossible movie".getBytes());
        Row[] rows = new Row[]{row1, row2};
        page.setAllRows(rows);
        //page.insertRow(row1);
        page.deserializeRows();
    }

    @Test
    public void testDeserializeEmptyRows(){
        Row row = new Row("".getBytes(), "".getBytes());
        Row[] rows = new Row[]{row};
        page.setAllRows(rows);
        page.deserializeRows();
    }

    @Test
    public void testGetPid(){
        page.getPid();
    }

    @Test
    public void testIncrementPinCount(){
        page.incrementPinCount();
    }

    @Test
    public void testGetDecrementPinCount(){
        page.decrementPinCount();
    }

    @Test
    public void testReassignPageId(){
        page.reassignPageId(1);
    }

    @Test
    public void testGetPinCount(){
        page.getPinCount();
    }

    @Test
    public void testIsFull(){
        page.isFull();
    }

}
