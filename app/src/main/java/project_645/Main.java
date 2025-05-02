package project_645;

import javax.management.Query;
import java.util.Iterator;

import static project_645.Utilities.measureDirectorSelectivity;

public class Main {
    public static void main(String[] args) {
        try {
            // define parameters to our various objects
            String path = "/app/src/main/java/project_645/DB files/";
            String mainFileName = "title.basics.csv";
            String workedOnTSVFileName = "title.principals.csv";
            String peopleTSVFileName = "name.basics.csv";
            String diskFileName = "testdb.dat";
            String movieIdIndexFileName = "movieIdIndex.dat";
            String movieTitleIndexFileName = "movieTitleIndex.dat";
            String workedOnFileName = "workedOnTable.dat";
            String peopleFileName = "peopleTable.dat";
            String filePath = System.getProperty("user.dir") + path;
//
//            // NOTE: RUNNING MAIN WILL DELETE AND RECREATE THE INDEX FILES SO THAT ALL TESTS CAN BE RUN IN SUCCESSION
//            // PLEASE MAKE SURE TO SAVE THE INDEX FILES BEFORE RUNNING MAIN.
//            // IF YOU DO LOSE THE INDEX FILES, PLEASE SEE OUR DOCUMENTATION FOR A LINK TO A BACKUP VERSION WE CREATED
//
            Utilities utilities = new Utilities(mainFileName, diskFileName);
            // utilities.deleteAndRecreateIndexFiles(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);

            BufferManagerImpl bufferManager = new BufferManagerImpl(1000 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);

            // Creating the index on movie title should take 2-3 hours.
            // bufferManager.populateDisk(10000, filePath, "A", "z");

            // utilities.populateAllDiskFiles(filePath, mainFileName, workedOnTSVFileName, peopleTSVFileName, bufferManager);

            QueryExecutor testExecutor = new QueryExecutor();

            // testExecutor.prematerializeTable(1000 * 4096);
           // utilities.queryPlanCorrectnessTest1();

//            System.out.println("--------------------------------------------");

           // utilities.queryPlanCorrectnessTest2();

//            System.out.println("--------------------------------------------");

//           utilities.queryPlanCorrectnessTest3(filePath);
//
            long totalMovies   = bufferManager.getCurrentMovieIdPage();
            long totalWorkedOn = bufferManager.getNextWorkedOnPageId();
            long totalPeople   = bufferManager.getCurrentPeoplePageId();
            double sigmaP      = 7936128.0 / 92201673.0;

            String[] starts = {"a", "a", "a", "a", "a"};
            String[] ends   = {"a pirate arrives: loyal guidance",
                    "addicted to plastic surgery",
                    "alibreze",
                    "anal initiation",
                    "arri arri tatanet"};
            double[] selectivity = {1.0, 2.0, 3.0, 4.0, 5.0};
            utilities.testQueryPerformance(
                    filePath,
                    diskFileName, movieIdIndexFileName, movieTitleIndexFileName,workedOnFileName,peopleFileName,
                    starts, ends,
                    500 * 4096,  // buffer size in frames (pages)
                    totalMovies, totalWorkedOn, totalPeople,
                    sigmaP,
                    selectivity,
                    true
            );
            utilities.testQueryPerformance(
                    filePath,
                    diskFileName, movieIdIndexFileName, movieTitleIndexFileName,workedOnFileName,peopleFileName,
                    starts, ends,
                    1000 * 4096,  // buffer size in frames (pages)
                    totalMovies, totalWorkedOn, totalPeople,
                    sigmaP,
                    selectivity,
                    true
            );
            utilities.testQueryPerformance(
                    filePath,
                    diskFileName, movieIdIndexFileName, movieTitleIndexFileName,workedOnFileName,peopleFileName,
                    starts, ends,
                    5000 * 4096,  // buffer size in frames (pages)
                    totalMovies, totalWorkedOn, totalPeople,
                    sigmaP,
                    selectivity,
                    true
            );




            // Note: The index objects are defined within the utilities methods themselves. If you want to create
             //an index object that matches the attribute/file, read the following instructions.
             //Example instantiations are included
//             Change the enum to change what file you want to write to
//             The following two lines are an example of how to create the index.
//             Note that C1 and C2 create the index with order 51, as that is the maximum possible order possible for this index
            // BTreeImpl testBTreeIndexTitle = new BTreeImpl(bufferManager, 51, File.MOVIE_TITLE_IDX);
//
//            BTreeImpl testBTreeIndexMovieId = new BTreeImpl(bufferManager, 51, File.MOVIE_ID_IDX);
//
//             // The following creates/populates testdb.dat with every entry from the imdb file.
//            bufferManager.populateDisk(-1, filePath);
//            System.out.println("Successfully populate the disk file with all entries from the imdb dataset");
//
//            // comment out the following lines to not create/populate the two index files.
//            utilities.testC1(true, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);
//            System.out.println("Successfully bulk loaded the movie ID index with all records on disk");
            // Creating the index on movie title should take 2-3 hours.
//            utilities.testC2(true, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);
//            System.out.println("Successfully created the index on movie title");
//
//             // search and range search
//             utilities.testC3(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);
//             utilities.testC4(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);
//
//            // generate the plots (each takes 1-2 hours)
//            // plot on the clustered index checks all selectivities in increments of 5%
//            utilities.testP1(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, false, workedOnFileName, peopleFileName);
//            // plot on the unclustered index checks selectivities from 1%-10% in 1% increments
//            utilities.testP2(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, false, workedOnFileName, peopleFileName);
//            // performance test on both
//            utilities.testP3(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}