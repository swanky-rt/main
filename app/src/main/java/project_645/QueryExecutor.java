package project_645;

import project_645.Operators.SelectionOperator;
import project_645.Operators.TableScanOperator;

import java.io.IOException;
import java.util.ArrayList;

public class QueryExecutor {

    public void executeQuery(String startRange, String endRange, int bufferSize) throws Exception {
        // File paths (based on your structure and Utilities.java)
        String filePath = System.getProperty("user.dir") + "/app/src/main/java/project_645/DB files/"; // Correct path to your DB files
        String diskFileName = "testdb.dat";  // Assuming your disk file is named testdb.dat
        String movieIdIndexFileName = "movieIdIndex.dat";  // Movie ID index file
        String movieTitleIndexFileName = "movieTitleIndex.dat"; // Movie Title index file
        String workedOnTableFileName = "workedOnTable.dat";
        String peopleTableFileName = "peopleTable.dat";

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
        TableScanOperator moviesScan = new TableScanOperator(bufferManager, File.DISK, new String[] {"movieId", "title"});
        // moviesScan.open();
        ArrayList<Record> records = new ArrayList<Record>();
        Record result1;
//        while ((result1 = moviesScan.next()) != null) {
//            records.add(result1);  // CSV format without spaces
//        }
        TableScanOperator workedOnScan = new TableScanOperator(bufferManager, File.WORKEDON, new String[] {"movieId", "personId", "category"});
        Record result2;
//        while ((result2 = workedOnScan.next()) != null) {
//            System.out.println(result2.getMovieId() + "," + result2.getPersonId() + "," + result2.getCategory());  // CSV format without spaces
//        }
//        TableScanOperator peopleScan = new TableScanOperator(bufferManager, File.PEOPLE, new String[] {"personId", "name"});
//        Record result3;
//        while ((result3 = peopleScan.next()) != null) {
//            System.out.println(result3.getPersonId() + "," + result3.getName());  // CSV format without spaces
//        }
//        SelectionOperator movieSelection = new SelectionOperator(
//                moviesScan,
//                "title",
//                startRange  // Filtering based on startRange for "title"
//        );
//
//        // Step 2: WorkedOn (selection: category = "director")

        SelectionOperator workedOnSelection = new SelectionOperator(
                workedOnScan,
                ColumnNames.CATEGORY,
                "director"  // Filtering based on category being "director"
        );

        workedOnSelection.open();
        Record curResult;
        long curRecordCount = 0;
        while ((curResult = workedOnSelection.next()) != null) {
            curRecordCount += 1;
        }

        int test3 = 2;



//        // Step 3: WorkedOn projection (keep only movieId and personId)
//        ProjectionOperator workedOnProject = new ProjectionOperator(
//                workedOnSelection,
//                new String[] {"movieId", "personId"}
//        );
//
//        // Step 4: First Join — Movies ⨝ WorkedOn on movieId
//        BNLJoinOperator join1 = new BNLJoinOperator(
//                movieSelection,
//                workedOnProject,
//                "movieId",
//                "movieId"
//        );
//
//        // Step 5: People scan
//
//
//        // Step 6: Second Join — result of join1 ⨝ People on personId
//        BNLJoinOperator join2 = new BNLJoinOperator(
//                join1,
//                peopleScan,
//                "personId",
//                "personId"
//        );
//
//        // Step 7: Final projection: title, name
//        ProjectionOperator finalProjection = new ProjectionOperator(
//                join2,
//                new String[] {"title", "name"}
//        );
//
//        // Step 8: Execute the plan and output the result
//        finalProjection.open();
//        Record result;
//        while ((result = finalProjection.next()) != null) {
//            System.out.println(result.getTitle() + "," + result.getName());  // CSV format without spaces
//        }
//
//        finalProjection.close();
    }
}
