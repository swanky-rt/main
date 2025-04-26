package project_645;

public class ProjectionOperator implements Operator {
    private final Operator child;
    private final String[] columns;

    public ProjectionOperator(Operator child, String[] columns) {
        this.child = child;
        this.columns = columns;
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
            byte[] newMovieId = new byte[9];
            byte[] newTitle = new byte[30];

            // Optional fields (set to empty or null)
            byte[] newPersonId = new byte[10];   // Empty for non-relevant fields
            byte[] newCategory = new byte[20];   // Empty for non-relevant fields
            byte[] newName = new byte[105];      // Empty for non-relevant fields

            // Loop through the columns being projected
            for (String col : columns) {
                switch (col) {
                    case "movieId":
                        byte[] rawKey = input.getRawKey();  // This is byte[]
                        System.arraycopy(rawKey, 0, newMovieId, 0, 9);  // Copy 9 bytes for movieId
                        break;
                    case "title":
                        byte[] titleBytes = input.getRawValue();  // This is byte[]
                        System.arraycopy(titleBytes, 0, newTitle, 0, 30);  // Copy 30 bytes for title
                        break;
                    case "personId":
                        System.arraycopy(input.getPersonId(), 0, newPersonId, 0, 10);  // Copy 10 bytes for personId
                        break;
                    case "category":
                        System.arraycopy(input.getCategory(), 0, newCategory, 0, 20);  // Copy 20 bytes for category
                        break;
                    case "name":
                        System.arraycopy(input.getName(), 0, newName, 0, 105);  // Copy 105 bytes for name
                        break;
                    default:
                        throw new RuntimeException("Unknown column: " + col);
                }
            }

            // Create Row with the relevant fields (movieId, title) and use dummy values for the others
            Row newRow = new Row(newMovieId, newTitle, null, null, null);  // Create Row with movieId and title

            // Create a Record object and pass the Row along with the other fields (personId, category, name)
            return new Record(newRow, newPersonId, newCategory, newName);
        }
        return null;
    }

    @Override
    public void close() {
        child.close();
    }

    // Helper method to convert byte array to int
    private int byteArrayToInt(byte[] byteArray, int offset) {
        return (byteArray[offset] & 0xFF) << 24
                | (byteArray[offset + 1] & 0xFF) << 16
                | (byteArray[offset + 2] & 0xFF) << 8
                | (byteArray[offset + 3] & 0xFF);
    }
}
