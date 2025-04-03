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

            BufferManagerImpl bufferManager = new BufferManagerImpl(1000 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            // Change the enum to change what file you want to write to
            BTreeImpl testBTreeIndexTitle = new BTreeImpl(bufferManager, 51, File.MOVIE_TITLE_IDX);

            BTreeImpl testBTreeIndexMovieId = new BTreeImpl(bufferManager, 51, File.MOVIE_ID_IDX);

            Utilities utilities = new Utilities(mainFileName, diskFileName);

            utilities.testP1(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            utilities.testC3(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);
            utilities.testC4(false, false, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName);

            // Iterator<Rid> ridIterator = testBTreeIndexMovieId.rangeSearch("s", "u");

//            int i = 0;
//            Rid nextRid;
//            while (ridIterator.hasNext()){
//                nextRid = ridIterator.next();
//                if (i % 574428 == 0) {
//                    Page nextPage = bufferManager.getPage(nextRid.getPageId(), File.DISK);
//                    bufferManager.unpinPage(nextPage.getPid(), File.DISK);
//                    Row curRow = nextPage.getRow(nextRid.getSlotId());
//                    System.out.println(curRow.ge);
//                }
//            }

//            bufferManager.populateDisk(-1, filePath);
//
//            System.out.println("Disk populated");
//
//            testBTreeIndexMovieId.bulkLoad();
//
//            System.out.println("MovieID Index Populated");
//
//            testBTreeIndexTitle.populateIndex();
//
//            System.out.println("Movie Title Index Populated");

            // testBTreeIndex.bulkLoad();
            // Iterator<Rid> returnRIDs = testBTreeIndexMovieId.rangeSearch("s", "u");

//            for (Iterator<Rid> it = returnRIDs; it.hasNext(); ) {
//                Rid rid = it.next();
//                Page page = bufferManager.getPage(rid.getPageId(), File.DISK);
//                bufferManager.unpinPage(page.getPid(), File.DISK);
//                Row record = page.getRow(rid.getSlotId());
//                System.out.println(new String(record.getTitle()).trim());
//            }
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