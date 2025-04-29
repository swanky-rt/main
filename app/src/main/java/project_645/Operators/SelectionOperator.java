package project_645.Operators;

import project_645.*;
import project_645.Record;

import java.io.IOException;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashMap;

public class SelectionOperator implements Operator {
    private Operator child;
    private final ColumnNames columnName;
    private final String value;
    private byte[] key;
    private byte[] endkey;
    private BufferManagerImpl bufferManager;
    private HashMap<ColumnNames, Integer> sizeMap = new HashMap<>() {{
        put(ColumnNames.MOVIEID, 9);
        put(ColumnNames.TITLE, 30);
        put(ColumnNames.PERSONID, 10);
        put(ColumnNames.CATEGORY, 20);
        put(ColumnNames.NAME, 105);
    }};

    private boolean resetOperator = false;

    public SelectionOperator(Operator child, ColumnNames columnName, String value, BufferManagerImpl bufferManager) {
        this.child = child;
        this.columnName = columnName;
        this.value = value;
        this.key = new byte[sizeMap.get(columnName)];
        this.endkey = new byte[sizeMap.get(columnName)];
        System.arraycopy(value.getBytes(), 0, this.key, 0, Math.min(value.length(), this.key.length));
        System.arraycopy(value.getBytes(), 0, this.endkey, 0, Math.min(value.length(), this.endkey.length));
        this.bufferManager = bufferManager;
    }

    public SelectionOperator(Operator child, ColumnNames columnName, String value, String endkey, BufferManagerImpl bufferManager) {
        this.child = child;
        this.columnName = columnName;
        this.value = value;
        this.key = new byte[sizeMap.get(columnName)];
        this.endkey = new byte[sizeMap.get(columnName)];
        System.arraycopy(value.getBytes(), 0, this.key, 0, Math.min(value.length(), this.key.length));
        System.arraycopy(endkey.getBytes(), 0, this.endkey, 0, Math.min(endkey.length(), this.endkey.length));
    }

    @Override
    public void open() throws Exception {
        child.open();
    }

    @Override
    public boolean hasNext() throws IOException {
        return child.hasNext();
    }

    @Override
    public Record next() throws Exception {
        Record input;
        int recordCount = 0;

        if (resetOperator) {
            resetOperator = false;
            this.child = new TableScanOperator(bufferManager, child.getRelation());
            this.child.open();
        }

        while ((input = child.next()) != null) {
            // Check if the value matches the column for filtering
            switch (this.columnName) {
                case ColumnNames.TITLE:
                    if (Arrays.compare(this.key, input.getMovieTitleBytes()) <= 0 &&
                            Arrays.compare(this.endkey, input.getMovieTitleBytes()) >= 0) {
                        return input;
                    }
                    break;
                case ColumnNames.MOVIEID:
                    if (Arrays.compare(this.key, input.getMovieIdBytes()) >= 0 &&
                            Arrays.compare(this.endkey, input.getMovieIdBytes()) >= 0) {
                        return input;
                    }
                    break;
                case ColumnNames.PERSONID:
                    if (Arrays.compare(this.key, input.getPersonIdBytes()) <= 0 &&
                            Arrays.compare(this.endkey, input.getPersonIdBytes()) >= 0) {
                        return input;
                    }
                    break;
                case ColumnNames.NAME:
                    if (Arrays.compare(this.key, input.getNameBytes()) <= 0 &&
                            Arrays.compare(this.endkey, input.getNameBytes()) >= 0) {
                        return input;
                    }
                    break;
                case ColumnNames.CATEGORY:
                    if (Arrays.compare(this.key, input.getCategoryBytes()) <= 0 &&
                            Arrays.compare(this.endkey, input.getCategoryBytes()) >= 0) {
                        return input;
                    }
                    break;
                default:
            }
            // Add other conditions for other columns like 'title', 'category', etc.
        }
        return null;
    }

    @Override
    public void close() {
        child.close();
    }

    @Override
    public File getRelation() {
        return child.getRelation();
    }

    @Override
    public void makeResetOperatorTrue() {
        this.resetOperator = true;
    }
}
