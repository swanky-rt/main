package project_645;

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

            BufferManagerImpl indexBufferManagerTest = new BufferManagerImpl(4*4096, filePath, movieIdIndexFileName, movieIdIndexFileName, movieTitleIndexFileName);

            BTreeImpl testBTreeIndex = new BTreeImpl(indexBufferManagerTest, 1, true);

            testBTreeIndex.insert("00000002", new Rid(2, 6));
            testBTreeIndex.insert("00000001", new Rid(2, 5));
            testBTreeIndex.insert("00000003", new Rid(2, 7));
            testBTreeIndex.insert("00000002", new Rid(2, 6));

            testBTreeIndex.search("00000002");


            // bufferManager.populateDisk(700, filePath);
            // Utilities utilities = new Utilities(mainFileName, diskFileName);
            // utilities.loadDataset(bufferManager, filePath);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}