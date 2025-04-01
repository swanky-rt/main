package project_645;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities implements Serializable {

    private static final int PAGE_SIZE = 4096;
    private final String filename;
    private final String diskFileName;



    public Utilities(String filename, String diskFileName) {
        this.filename = filename;
        this.diskFileName = diskFileName;
    }

    // Loads the buffer manager with the imdb dataset
    public void loadDataset(BufferManager bf, String filepath) throws Exception {
         Path dbFilePath = Paths.get( filepath + diskFileName).toAbsolutePath();
        // create page
        Page newPage = bf.createPage(File.DISK);
        int pageId = newPage.getPid();
        System.out.print(pageId+ "the page number");
        newPage = bf.getPage(pageId, File.DISK);
        BufferedReader reader = new BufferedReader(new FileReader(filepath + this.filename));
        reader.readLine();
        // add rows using p.insertRow without filling p up
        for (int i = 0; i < 80; ++i) {
            String curLine = reader.readLine();
            String[] columns = curLine.split("\t");
            byte[] movieId = columns[0].getBytes();
            byte[] titleId = columns[2].getBytes();
            Row row = new Row(movieId, titleId);
            newPage.insertRow(row);
        }
        bf.unpinPage(pageId, File.DISK);
        // sequence of getPage calls that causes P to be evicted

        int diskSizeInPages = (int)Files.size(dbFilePath) / PAGE_SIZE;
        if (diskSizeInPages < bf.getBufferSize() / 4096) {
            throw new Exception("Disk does not have sufficiently many unique pages to fill up the buffer manager");
        }
        for (int i = 0; i < diskSizeInPages; ++i) {
            bf.getPage(i, File.DISK);
            if (i >= 5) {
                bf.unpinPage(pageId, File.DISK);
            }
            bf.unpinPage(i, File.DISK);
        }
        //get page back
        pageId = newPage.getPid();
        newPage = bf.getPage(pageId, File.DISK);
        bf.markDirty(pageId, File.DISK);

        // add some more pages using p.insertRow();
        for (int i = 0; i < 20; ++i) {
            String curLine = reader.readLine();
            String[] columns = curLine.split("\t");
            byte[] movieId = columns[0].getBytes();
            byte[] titleId = columns[2].getBytes();
            Row row = new Row(movieId, titleId);
            newPage.insertRow(row);
        }
        bf.unpinPage(pageId, File.DISK);
    }


    public void populateIndex(BTreeImpl curIndex, String filePath, int numRecords) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath + "title.basics.tsv"));
        reader.readLine();
        String curLine;
        int testPage = 0;
        int testSlot = 0;
        long count = 0;
        while ((curLine = reader.readLine()) != null && (numRecords == -1 || count < numRecords)) {
                if (testPage == 93 && testSlot == 58) {
                    int breakpoint = 1;
                }
                if (testSlot == 11) {
                    int breakpoint = 2;
                }
                try {
                    String[] columns = curLine.split("\t");
                    String movieId = columns[0];
                    String titleId = columns[2];
                    byte[] testBytes = titleId.getBytes();
                    curIndex.insert(movieId, new Rid(testPage, testSlot));
                    ++testSlot;
                    if (testSlot >= 105) {
                        testSlot = 0;
                        testPage += 1;
                    }
                    count += 1;
                } catch (Exception e) {
                    int breakpoint = 2;
                }
        }
    }

}
