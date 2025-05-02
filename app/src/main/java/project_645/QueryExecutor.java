package project_645;

import project_645.Operators.BNLJoinOperator;
import project_645.Operators.ProjectionOperator;
import project_645.Operators.SelectionOperator;
import project_645.Operators.TableScanOperator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class QueryExecutor {
    String filePath = System.getProperty("user.dir") + "/src/main/java/project_645/DB files/"; // Correct path to your DB files
    String diskFileName = "testdb.dat";  // Assuming your disk file is named testdb.dat
    String movieIdIndexFileName = "movieIdIndex.dat";  // Movie ID index file
    String movieTitleIndexFileName = "movieTitleIndex.dat"; // Movie Title index file
    String workedOnTableFileName = "workedOnTable.dat";
    String peopleTableFileName = "peopleTable.dat";

    // Returns the total number of I/Os made by the buffer manager
    public long executeQuery(String startRange, String endRange, int bufferSize, boolean usePrematerializedTempTable, String csvFileName) throws Exception {
        // File paths (based on your structure and Utilities.java)

        if (csvFileName != null) {
            // if results isn't null, save to it
            Path csvFilePath = Paths.get(filePath + csvFileName);
            try {
                Files.deleteIfExists(csvFilePath); // Deletes the file if it exists
                // System.out.println("File deleted successfully.");
            } catch (IOException e) {
                // System.err.println("An error occurred while deleting the file.");
                e.printStackTrace();
            }
            try {
                // Create an empty file if it doesn't exist
                Files.createFile(csvFilePath);
                // System.out.println("File created: " + csvFilePath.toAbsolutePath());
            } catch (IOException e) {
                if (Files.exists(csvFilePath)) {
                    // System.out.println("File already exists.");
                } else {
                    // System.err.println("An error occurred while creating the file.");
                    e.printStackTrace();
                }
            }
        }


        // Initialize BufferManagerImpl with the correct paths
        BufferManagerImpl bufferManager = new BufferManagerImpl(
                bufferSize,
                filePath,      // Correct file path
                diskFileName,  // Disk file name
                movieIdIndexFileName,
                movieTitleIndexFileName,
                workedOnTableFileName,
                peopleTableFileName
        );

        // Step 1: Movies scan + title range selection
        TableScanOperator moviesScan = new TableScanOperator(bufferManager, File.DISK);

        // Step 2: Selection on operator from chosen range
        SelectionOperator testRangeSelection = new SelectionOperator(moviesScan, ColumnNames.TITLE, startRange, endRange, bufferManager, false);

        // Step 3: workedOnScan operator
        TableScanOperator workedOnScan = new TableScanOperator(bufferManager, File.WORKEDON);

        // Step 4: peopleScan operator
        TableScanOperator peopleScan = new TableScanOperator(bufferManager, File.PEOPLE);


        // Step 5: selection on workedOn where value is director
        SelectionOperator workedOnSelection = new SelectionOperator(
                workedOnScan,
                ColumnNames.CATEGORY,
                "director",  // Filtering based on category being "director"
                bufferManager,
                false
        );

//        // Step 6: WorkedOn projection (keep only movieId and personId)
        ProjectionOperator workedOnProject = new ProjectionOperator(
                workedOnSelection,
                new ColumnNames[] {ColumnNames.MOVIEID, ColumnNames.PERSONID},
                File.TEMPORARY,
                bufferManager,
                usePrematerializedTempTable
        );


//        workedOnProject.open();
//        while (workedOnProject.next() != null) {
//           continue;
//        }
        // Step 7: join on materialized table and outer relation
        BNLJoinOperator bnlJoinOperator = new BNLJoinOperator(testRangeSelection, workedOnProject,
                ColumnNames.MOVIEID, ColumnNames.MOVIEID, bufferManager, File.BNL1);


        // bnlJoinOperator.open();

//        Record curRecord;
//        while ((curRecord = bnlJoinOperator.next()) != null) {
//            System.out.println(curRecord.getTitleDeserialized() + "," + curRecord.getPersonIdDeserialized() + "," + curRecord.getMovieIdDeserialized());
//        }
//        // Step 8: join on
         BNLJoinOperator bnlJoinOperator2 = new BNLJoinOperator(bnlJoinOperator, peopleScan,
                 ColumnNames.PERSONID, ColumnNames.PERSONID, bufferManager, File.BNL2);

////
//        // Step 7: Final projection: title, name
        ProjectionOperator finalProjection = new ProjectionOperator(
                bnlJoinOperator2,
                new ColumnNames[] {ColumnNames.TITLE, ColumnNames.NAME},
                File.BNL2,
                bufferManager,
                true
        );
        // Step 8: Execute the plan and output the result
        finalProjection.open();
        Record result;

        BufferedWriter writer  = null;
        if (csvFileName != null) {
            writer = new BufferedWriter(new FileWriter(filePath + csvFileName));
        }
        while ((result = finalProjection.next()) != null) {
            System.out.println(result.getTitleDeserialized() + "," + result.getName());  // CSV format without spaces
            if (writer != null) {
                writer.write(result.getTitleDeserialized() + "," + result.getName() +"\n");
            }
        }
        if (csvFileName != null) {
            writer.close();
        }
        if (!usePrematerializedTempTable) {
             finalProjection.close();
        }
        return bufferManager.getTotalIOs();
    }

    // prematerializes the table
    public long prematerializeTable(int bufferSize) throws Exception {
        // Initialize BufferManagerImpl with the correct paths
        BufferManagerImpl bufferManager = new BufferManagerImpl(
                bufferSize,
                filePath,      // Correct file path
                diskFileName,  // Disk file name
                movieIdIndexFileName,
                movieTitleIndexFileName,
                workedOnTableFileName,
                peopleTableFileName
        );

        TableScanOperator workedOnScan = new TableScanOperator(bufferManager, File.WORKEDON);

        SelectionOperator workedOnSelection = new SelectionOperator(
                workedOnScan,
                ColumnNames.CATEGORY,
                "director",  // Filtering based on category being "director"
                bufferManager,
                false
        );
        ProjectionOperator workedOnProject = new ProjectionOperator(
                workedOnSelection,
                new ColumnNames[] {ColumnNames.MOVIEID, ColumnNames.PERSONID},
                File.TEMPORARY,
                bufferManager,
                false
        );

        workedOnProject.open();

        int curRecordCount = 0;
        Record curRecord = workedOnProject.next();

        bufferManager.force();

        // minus 1 for scan operator
        // System.out.println("All records were materialized in " + (bufferManager.getTotalIOs() - 1) + " I/Os");
        return bufferManager.getTotalIOs() - 1;
    }
}
