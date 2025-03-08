package project_645;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities implements Serializable {

    //private static final int BUFFER_SIZE = 4 * 4096;
    private static final int PAGE_SIZE = 4096;
    public int currentPageIDToWrite;
    public int currentPageID = -1;
    public int currentUnwrittenPageId = -1;
    private String filepath;
    private String filename = "testdb.dat";



    public Utilities() {
    }

    // Loads the buffer manager with the imdb dataset
    public void loadDataset(BufferManager bf, String filepath) throws Exception {
         Path dbFilePath = Paths.get( filepath + filename).toAbsolutePath();
        // create page
        Page newPage = bf.createPage();
        int pageId = newPage.getPid();
        System.out.print(pageId+ "the page number");
        newPage = bf.getPage(pageId);
        BufferedReader reader = new BufferedReader(new FileReader(filepath + "title.basics.tsv"));
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
        bf.unpinPage(pageId);
        // sequence of getPage calls that causes P to be evicted

        int diskSizeInPages = (int)Files.size(dbFilePath) / PAGE_SIZE;
        if (diskSizeInPages < bf.bufferSize / 4096) {
            throw new Exception("Disk does not have sufficiently many unique pages to fill up the buffer manager");
        }
        for (int i = 0; i < diskSizeInPages; ++i) {
            bf.getPage(i);
            if (i >= 5) {
                bf.unpinPage(pageId);
            }
            bf.unpinPage(i);
        }
        //get page back
        pageId = newPage.getPid();
        newPage = bf.getPage(pageId);
        bf.markDirty(pageId);

        // add some more pages using p.insertRow();
        for (int i = 0; i < 20; ++i) {
            String curLine = reader.readLine();
            String[] columns = curLine.split("\t");
            byte[] movieId = columns[0].getBytes();
            byte[] titleId = columns[2].getBytes();
            Row row = new Row(movieId, titleId);
            newPage.insertRow(row);
        }
        bf.unpinPage(pageId);
    }
}
