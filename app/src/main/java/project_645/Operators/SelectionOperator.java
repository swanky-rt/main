package project_645.Operators;

import project_645.ColumnNames;
import project_645.Record;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class SelectionOperator implements Operator {
    private final Operator child;
    private final ColumnNames columnName;
    private final String value;
    private byte[] key;
    private HashMap<ColumnNames, Integer> sizeMap = new HashMap<>() {{
        put(ColumnNames.MOVIEID, 9);
        put(ColumnNames.TITLE, 30);
        put(ColumnNames.PERSONID, 10);
        put(ColumnNames.CATEGORY, 20);
        put(ColumnNames.NAME, 105);
    }};

    public SelectionOperator(Operator child, ColumnNames columnName, String value) {
        this.child = child;
        this.columnName = columnName;
        this.value = value;
        this.key = new byte[sizeMap.get(columnName)];
        System.arraycopy(value.getBytes(), 0, this.key, 0, Math.min(value.length(), this.key.length));
    }

    @Override
    public void open() {
        child.open();
    }

    @Override
    public boolean hasNext() throws IOException {
        return child.hasNext();
    }

    @Override
    public Record next() throws Exception {
        Record input;
        while ((input = child.next()) != null) {
            // Check if the value matches the column for filtering
            switch (this.columnName) {
                case ColumnNames.TITLE:
                    if (Arrays.compare(this.key, input.getMovieTitleBytes()) == 0) {
                        return input;
                    }
                case ColumnNames.MOVIEID:
                    if (Arrays.compare(this.key, input.getMovieIdBytes()) == 0) {
                        return input;
                    }
                case ColumnNames.PERSONID:
                    if (Arrays.compare(this.key, input.getPersonIdBytes()) == 0) {
                        return input;
                    }
                case ColumnNames.NAME:
                    if (Arrays.compare(this.key, input.getNameBytes()) == 0) {
                        return input;
                    }
                case ColumnNames.CATEGORY:
                    if (Arrays.compare(this.key, input.getCategoryBytes()) == 0) {
                        return input;
                    }
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
}
