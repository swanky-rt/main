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

            BufferManagerImpl bufferManager = new BufferManagerImpl(100000 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

//            bufferManager.populateDisk(50000, filePath);
//            bufferManager.force();

            // BufferManagerImpl indexBufferManagerTest = new BufferManagerImpl(5000*4096, filePath, movieIdIndexFileName, movieIdIndexFileName, movieTitleIndexFileName);

            // Change the enum to change what file you want to write to
            BTreeImpl testBTreeIndex = new BTreeImpl(bufferManager, 51, File.MOVIE_TITLE_IDX);

            // Uncomment out the following 3 lines to write the tsv file to the index.
            // The third parameter is the number of entries to get. If you want to get all entries, pass -1.
            // Running this code will cause them to exist in the index but not on disk. To load on disk, use the "populate disk" method.
            // I plan on having these more closely linked later tonight.
//
//            Utilities utilities = new Utilities(mainFileName, diskFileName);
//            utilities.populateIndex(testBTreeIndex, filePath, -1, File.MOVIE_TITLE_IDX);
//            bufferManager.force();

            // Iterator<Rid> test1 = testBTreeIndex.search("Carmencita");

//            int test2 = 2;
//
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
//            bufferManager.force();
//
            Iterator<Rid> returnRIDs = testBTreeIndex.rangeSearch("ba", "be");

            for (Iterator<Rid> it = returnRIDs; it.hasNext(); ) {
                Rid rid = it.next();
                Page page = bufferManager.getPage(rid.getPageId(), File.DISK);
                bufferManager.unpinPage(page.getPid(), File.DISK);
                Row record = page.getRow(rid.getSlotId());
                System.out.println(new String(record.getTitle()).trim());
            }
//
//            System.out.println(returnRIDs.hasNext());


//            testBTreeIndex.search("Popular Science, Featuring Moon Rocket");

            int test = 2;


//             bufferManager.populateDisk(-1, filePath);
//             bufferManager.force();

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}