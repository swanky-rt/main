package project_645;

import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        try {
            String path = "/app/src/main/java/project_645/DB files/";
            String mainFileName = "title.basics.tsv";
            String diskFileName = "testdb.dat";
            String movieIdIndexFileName = "movieIdIndex.dat";
            String movieTitleIndexFileName = "movieTitleIndex.dat";
            String filePath = System.getProperty("user.dir") + path;

            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            BufferManagerImpl indexBufferManagerTest = new BufferManagerImpl(20*4096, filePath, movieIdIndexFileName, movieIdIndexFileName, movieTitleIndexFileName);

            BTreeImpl testBTreeIndex = new BTreeImpl(indexBufferManagerTest, 50, true, File.MOVIE_TITLE_IDX);

            // Uncomment out the following 3 lines to write the tsv file to the index.
            // Running this code will cause them to exist in the index but not on disk. To load on disk, use the "populate disk" method.
            // I plan on having these more closely linked later tonight.

            // Utilities utilities = new Utilities(mainFileName, diskFileName);
            // utilities.populateIndex(testBTreeIndex, filePath, 50000);
            // indexBufferManagerTest.force();

//            int pageId = 0;
//            int slotId = 0;
//            for (int i = 0; i < 1000; ++i) {
//                testBTreeIndex.insert(String.format("%09d", i), new Rid(pageId, slotId));
//                 // bufferManager.unpinPage(2, File.MOVIE_ID_IDX);
//                slotId += 1;
//                if (slotId == 105) {
//                    slotId = 0;
//                    pageId += 1;
//                }
//            }
//            indexBufferManagerTest.force();
//
//            Iterator<Rid> returnRIDs = testBTreeIndex.rangeSearch("000000513", "000000892");


//            testBTreeIndex.search("Popular Science, Featuring Moon Rocket");


//            Utilities utilities = new Utilities(mainFileName, diskFileName);
//            utilities.populateIndex(testBTreeIndex, filePath, 50000);
//            indexBufferManagerTest.force();

            int test = 2;


            // bufferManager.populateDisk(700, filePath);
//             Utilities utilities = new Utilities(mainFileName, diskFileName);
//             utilities.loadDataset(bufferManager, filePath);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}