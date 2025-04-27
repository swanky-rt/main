package project_645.Operators;

import project_645.BufferManagerImpl;
import project_645.File;
import project_645.Record;
import project_645.Row;


import java.io.IOException;

public class ProjectionOperator implements Operator {
    private final Operator child;
    // private final String[] columns;
    private final File relation;
    private final BufferManagerImpl bufferManager;

    public ProjectionOperator(Operator child, String[] columns, File relation, BufferManagerImpl bufferManager) {
        this.child = child;
        // this.columns = columns;
        this.relation = relation;
        this.bufferManager = bufferManager;
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
        if (relation == File.TEMPORARY) {

        }
        Record input;
        while ((input = child.next()) != null) {
            byte[] newMovieId = new byte[9];
            byte[] newTitle = new byte[30];

            // Optional fields (set to empty or null)
            byte[] newPersonId = new byte[10];   // Empty for non-relevant fields
            byte[] newCategory = new byte[20];   // Empty for non-relevant fields
            byte[] newName = new byte[105];      // Empty for non-relevant fields

            // Loop through the columns being projected

            Row newRow;
            switch (this.relation) {
                case File.DISK:
                    newMovieId = input.getMovieIdBytes();
                    newTitle = input.getMovieTitleBytes();
                    newRow = new Row(newMovieId, newTitle);
                    break;
                case File.WORKEDON:
                    newMovieId = input.getMovieIdBytes();
                    newPersonId = input.getPersonIdBytes();
                    newCategory = input.getCategoryBytes();
                    newRow = new Row(newMovieId, newPersonId, newCategory);
                    break;
                case File.PEOPLE:
                    newPersonId = input.getPersonIdBytes();
                    newName = input.getNameBytes();
                    newRow = new Row(newPersonId, newName, true);
                    break;
                case File.TEMPORARY:
                    newMovieId = input.getMovieIdBytes();
                    newPersonId = input.getPersonIdBytes();
                    newRow = new Row(newMovieId, newPersonId);
                    break;
                default:
                    throw new Exception();
            }

            // Create Row with the relevant fields (movieId, title) and use dummy values for the others
              // Create Row with movieId and title

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
