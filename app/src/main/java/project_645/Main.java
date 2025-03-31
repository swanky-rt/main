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

            BufferManagerImpl indexBufferManagerTest = new BufferManagerImpl(5*4096, filePath, movieIdIndexFileName, movieIdIndexFileName, movieTitleIndexFileName);

            BTreeImpl testBTreeIndex = new BTreeImpl(indexBufferManagerTest, 1, true, File.MOVIE_ID_IDX);

            int pageId = 0;
            int slotId = 0;
            for (int i = 0; i < 1000; ++i) {
                testBTreeIndex.insert(String.format("%09d", i), new Rid(pageId, slotId));
                slotId += 1;
                if (slotId == 105) {
                    slotId = 0;
                    pageId += 1;
                }
            }
            indexBufferManagerTest.force();

//            testBTreeIndex.insert("00000002", new Rid(2, 6));
//            testBTreeIndex.insert("00000001", new Rid(2, 5));
//            testBTreeIndex.insert("00000002", new Rid(2, 6));
//            testBTreeIndex.insert("00000003", new Rid(2, 7));
//            testBTreeIndex.insert("00000002", new Rid(2, 9));
//            testBTreeIndex.insert("00000002", new Rid(2, 6));
//            testBTreeIndex.insert("00000001", new Rid(2, 5));
//            testBTreeIndex.insert("00000002", new Rid(2, 6));
//            testBTreeIndex.insert("00000003", new Rid(2, 7));
//            testBTreeIndex.insert("00000002", new Rid(2, 9));

//            testBTreeIndex.search("Popular Science, Featuring Moon Rocket");
            Iterator<Rid> returnRIDs = testBTreeIndex.rangeSearch("000000513", "000000892");

//            Utilities utilities = new Utilities(mainFileName, diskFileName);
//            utilities.populateIndex(testBTreeIndex, filePath, 50000);
//            indexBufferManagerTest.force();

            int test = 2;


            // bufferManager.populateDisk(700, filePath);
            // Utilities utilities = new Utilities(mainFileName, diskFileName);
            // utilities.loadDataset(bufferManager, filePath);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}