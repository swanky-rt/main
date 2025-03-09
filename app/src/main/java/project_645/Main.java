package project_645;

public class Main {
    public static void main(String[] args) {
        try {
            String path = "/src/main/java/project_645/DB files/";
            String mainFileName = "title.basics.tsv";
            String diskFileName = "testdb.dat";
            String filePath = System.getProperty("user.dir") + path;

            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096, filePath, diskFileName);

            // bufferManager.populateDisk(700, filePath);
            Utilities utilities = new Utilities(mainFileName, diskFileName);
            utilities.loadDataset(bufferManager, filePath);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}