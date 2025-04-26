package project_645;

public class SelectionOperator implements Operator {
    private final Operator child;
    private final String columnName;
    private final String value;

    public SelectionOperator(Operator child, String columnName, String value) {
        this.child = child;
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public void open() {
        child.open();
    }

    @Override
    public boolean hasNext() {
        return child.hasNext();
    }

    @Override
    public Record next() {
        Record input;
        while ((input = child.next()) != null) {
            // Check if the value matches the column for filtering
            if (columnName.equals("movieId") && new String(input.getRawKey()).trim().equals(value)) {
                return input;  // Return record if it matches the condition
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
