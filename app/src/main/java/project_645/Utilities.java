package project_645;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

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

    public void testC1(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName) throws Exception {
        BufferManager c1BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName);

        // Create the indexes
        BTreeImpl bTreeMovieId = new BTreeImpl(c1BufferManager, 51, File.MOVIE_ID_IDX);

        if (populateMovieIdIndexFile) {
            bTreeMovieId.bulkLoad();
        }
    }

    public void testC2(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName) throws Exception {
        BufferManager c2BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName);
        BTreeImpl bTreeTitleId = new BTreeImpl(c2BufferManager, 51, File.MOVIE_TITLE_IDX);

        if (populateMovieTitleIndexFile) {
            bTreeTitleId.populateIndex();
        }
    }

    public void testC3(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName) throws Exception {
        // Search by one key on b+ tree index:
        BufferManager c3BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName);

        // Create the indexes
        BTreeImpl bTreeMovieId = new BTreeImpl(c3BufferManager, 51, File.MOVIE_ID_IDX);
        BTreeImpl bTreeTitleId = new BTreeImpl(c3BufferManager, 51, File.MOVIE_TITLE_IDX);


        Iterator<Rid> rids = bTreeMovieId.search("tt0000811");
        Iterator<Rid> ridsCopy = bTreeTitleId.search("The Blind Man");

        // verify that the RIDs returned are the same
        if (!rids.hasNext() || !ridsCopy.hasNext()) {
            System.out.println("Could not find any RIDS associated with the passed keys.");
            return;
        }

        while (rids.hasNext()) {
            Rid curMovieIdRID = rids.next();

            Page curRIDPage = c3BufferManager.getPage(curMovieIdRID.getPageId(), File.DISK);
            c3BufferManager.unpinPage(curMovieIdRID.getPageId(), File.DISK);

            Row testRow = curRIDPage.getRow(curMovieIdRID.getSlotId());
            if (!new String(testRow.getMovieId()).trim().equals("tt0000811")) {
                System.out.println("A movie ID in the index does not correspond to the queried movie ID on disk");
                return;
            }
        }

        while (ridsCopy.hasNext()) {
            Rid curMovieIdRID = ridsCopy.next();

            Page curRIDPage = c3BufferManager.getPage(curMovieIdRID.getPageId(), File.DISK);
            c3BufferManager.unpinPage(curMovieIdRID.getPageId(), File.DISK);

            Row testRow = curRIDPage.getRow(curMovieIdRID.getSlotId());
            if (!new String(testRow.getTitle()).trim().equals("The Blind Man")) {
                System.out.println("A movie ID in the title index does not correspond to the queried RID on disk");
                return;
            }
        }

        System.out.println("The expected movie RIDs were found by traversing the requested index!");
    }

    public void testC4(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName) throws Exception {
        BufferManager c4BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName);

        // Create the indexes
        BTreeImpl bTreeMovieId = new BTreeImpl(c4BufferManager, 51, File.MOVIE_ID_IDX);
        BTreeImpl bTreeTitleId = new BTreeImpl(c4BufferManager, 51, File.MOVIE_TITLE_IDX);

        Iterator<Rid> rids = bTreeMovieId.rangeSearch("tt0000811", "tt0032549");

        Iterator<Rid> titleRids = bTreeTitleId.rangeSearch("b", "e");

        while (rids.hasNext()) {
            Rid curRID = rids.next();

            Page curPage = c4BufferManager.getPage(curRID.getPageId(), File.DISK);

            Row curRow = curPage.getRow(curRID.getSlotId());

            c4BufferManager.unpinPage(curPage.getPid(), File.DISK);

            String curMovieId = new String(curRow.getMovieId()).trim();

            if (!(curMovieId.compareTo("tt0000811") >= 0 && curMovieId.compareTo("tt0032549") <= 0)) {
                System.out.println("Queried movie IDs on disk do not match the RIDs in the index");
                return;
            }
        }

        while (titleRids.hasNext()) {
            Rid curRID = titleRids.next();

            Page curPage = c4BufferManager.getPage(curRID.getPageId(), File.DISK);

            Row curRow = curPage.getRow(curRID.getSlotId());

            c4BufferManager.unpinPage(curPage.getPid(), File.DISK);

            String curMovieTitle = new String(curRow.getTitle()).trim();

            if (!(curMovieTitle.compareTo("b") >= 0 && curMovieTitle.compareTo("e") <= 0)) {
                System.out.println("Queried movie IDs on disk do not match the RIDs in the index");
                return;
            }
        }

        System.out.println("All tested RIDs found in range query do fall in the range denoted by the index!");
    }

    public void testP1(String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName) throws Exception {
        // initialize buffer manager to perform the range query
        BufferManager p1BufferManager = new BufferManagerImpl(500*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName);

        BTreeImpl bTreeMovieId = new BTreeImpl(p1BufferManager, 51, File.MOVIE_ID_IDX);

        // Increased selectivities


    }

}
