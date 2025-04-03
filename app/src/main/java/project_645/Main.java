package project_645;

import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        try {
            // define parameters to our various objects
            String path = "/app/src/main/java/project_645/DB files/";
            String mainFileName = "title.basics.tsv";
            String diskFileName = "testdb.dat";
            String movieIdIndexFileName = "movieIdIndex.dat";
            String movieTitleIndexFileName = "movieTitleIndex.dat";
            String filePath = System.getProperty("user.dir") + path;

            // NOTE: RUNNING MAIN WILL DELETE AND RECREATE THE INDEX FILES SO THAT ALL TESTS CAN BE RUN IN SUCCESSION
            // PLEASE MAKE SURE TO SAVE THE INDEX FILES BEFORE RUNNING MAIN.
            // IF YOU DO LOSE THE INDEX FILES, PLEASE SEE OUR DOCUMENTATION FOR A LINK TO A BACKUP VERSION WE CREATED

            Utilities utilities = new Utilities(mainFileName, diskFileName);
            utilities.deleteAndRecreateIndexFiles(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            BufferManagerImpl bufferManager = new BufferManagerImpl(1000 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            // Change the enum to change what file you want to write to
            // The following two lines are an example of how to create the index.
            // Note that C1 and C2 create the index with order 51, as that is the maximum possible order possible for this index
//            BTreeImpl testBTreeIndexTitle = new BTreeImpl(bufferManager, 51, File.MOVIE_TITLE_IDX);
//
//            BTreeImpl testBTreeIndexMovieId = new BTreeImpl(bufferManager, 51, File.MOVIE_ID_IDX);

            // The following creates/populates testdb.dat with every entry from the imdb file.
            bufferManager.populateDisk(-1, filePath);
            System.out.println("Successfully populate the disk file with all entries from the imdb dataset");

            // comment out the following lines to not create/populate the two index files.
            utilities.testC1(true, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);
            System.out.println("Successfully bulk loaded the movie ID index with all records on disk");
            // Creating the index on movie title should take 2-3 hours.
            utilities.testC2(true, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);
            System.out.println("Successfully created the index on movie title");

            // search and range search
            utilities.testC3(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);
            utilities.testC4(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            // generate the plots (each takes 1-2 hours)
            // plot on the clustered index checks all selectivities in increments of 5%
            utilities.testP1(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);
            // plot on the unclustered index checks selectivities from 1%-10% in 1% increments
            utilities.testP2(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}