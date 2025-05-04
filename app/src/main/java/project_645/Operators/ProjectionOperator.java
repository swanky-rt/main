package project_645.Operators;

import project_645.*;
import project_645.Record;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ProjectionOperator implements Operator {
    private Operator child;
    private final File relation;
    private final BufferManagerImpl bufferManager;
    private boolean firstNext;
    private Page currentPage;
    private final boolean prematerialized;
    private boolean resetOperator = false;
    private ColumnNames[] columnNames;

    public ProjectionOperator(Operator child, ColumnNames[] columns, File relation, BufferManagerImpl bufferManager, boolean prematerialized) {
        this.child = child;
        this.relation = relation;
        this.bufferManager = bufferManager;
        this.firstNext = true;
        this.currentPage = null;
        this.prematerialized = prematerialized;
        this.columnNames = columns;
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
        if (firstNext && relation == File.TEMPORARY && !prematerialized) {
            materializeTable();
            bufferManager.unpinPage(currentPage.getPid(), File.TEMPORARY);
        }

        if ((firstNext || resetOperator) && relation == File.TEMPORARY) {
            firstNext = false;
            resetOperator = false;
            this.child = new TableScanOperator(bufferManager, relation);
            this.child.open();
        }
        Record input;
        while ((input = child.next()) != null) {
            return createNewRecord(input, false);
        }
        // unpin the last page in the relation after all records are exhausted
        return null;
    }

    @Override
    public void close() {

        bufferManager.deleteTemporaryTable();
        child.close();
    }

    @Override
    public void makeResetOperatorTrue() {
        this.resetOperator = true;
    }

    public void materializeTable() throws Exception {
        currentPage = bufferManager.createPage(File.TEMPORARY);

        int curMaterializedRecord = 0;
        Record input;
        while ((input = child.next()) != null) {
            createNewRecord(input, true);
            curMaterializedRecord += 1;
//            if (curMaterializedRecord % 100000 == 0) {
//                // System.out.println("" + curMaterializedRecord + " records materialized");
//            }
        }
        if (relation == File.TEMPORARY) {
            bufferManager.unpinPage(currentPage.getPid(), File.TEMPORARY);
        }
    }

    // helper method to construct record/row
    private Record createNewRecord(Record input, boolean materialize) throws Exception {
        byte[] newMovieId = new byte[9];
        byte[] newTitle = new byte[30];

        // Optional fields (set to empty or null)
        byte[] newPersonId = new byte[10];
        byte[] newCategory = new byte[20];
        byte[] newName = new byte[105];

        // Loop through the columns being projected
        // Create Row with the relevant fields (movieId, title) and use dummy values for the others
        // Create Row with movieId and title
        Row newRow = new Row(null, null, null, null, null);

        List<ColumnNames> columnsList = Arrays.asList(columnNames);
        if (columnsList.contains(ColumnNames.NAME)) {
            newRow.name = input.getNameBytes();
        }
        if (columnsList.contains(ColumnNames.CATEGORY)) {
            newRow.category = input.getCategoryBytes();
        }
        if (columnsList.contains(ColumnNames.MOVIEID)) {
            newRow.movieId = input.getMovieIdBytes();
        }
        if (columnsList.contains(ColumnNames.PERSONID)) {
            newRow.personId = input.getPersonIdBytes();
        }
        if (columnsList.contains(ColumnNames.TITLE)) {
            newRow.title = input.getMovieTitleBytes();
        }



        // Create a Record object and pass the Row along with the other fields (personId, category, name)
        if (relation == File.TEMPORARY && materialize) {
            currentPage.insertRow(newRow);
            if (currentPage.isFull()) {
                bufferManager.unpinPage(currentPage.getPid(), File.TEMPORARY);
                currentPage = bufferManager.createPage(File.TEMPORARY);
            }
        }
        return new Record(newRow, newPersonId, newCategory, newName, input.getRid());
    }

    @Override
    public File getRelation() {
        return child.getRelation();
    }

    // Helper method to convert byte array to int
    private int byteArrayToInt(byte[] byteArray, int offset) {
        return (byteArray[offset] & 0xFF) << 24
                | (byteArray[offset + 1] & 0xFF) << 16
                | (byteArray[offset + 2] & 0xFF) << 8
                | (byteArray[offset + 3] & 0xFF);
    }
}
