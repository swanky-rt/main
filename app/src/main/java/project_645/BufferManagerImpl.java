package project_645;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BufferManagerImpl extends BufferManager{
    public int MAX_PAGE;
    private int PAGE_SIZE;
    public Map<String, Page> bufferPool;
    public Map<Page, Integer> pageMap;
    public LinkedList<String> lru;
    public Map<String, Integer> pinnedPages;
    private int currentPageID;
    private int currentMovieIdPage;
    private int currentMovieTitlePageId;
    private String filepath;
    private String diskFileName;
    private String movieIdIndexFileName;
    private String movieTitleIndexFileName;


    public BufferManagerImpl(int bufferSize, String filepath, String diskFileName, String movieIdIndexFileName,
                             String movieTitleIndexFileName) throws IOException {
        super(bufferSize);
        this.PAGE_SIZE = 4096;
        this.MAX_PAGE = bufferSize/PAGE_SIZE;
        this.bufferPool = new HashMap<>();
        this.lru = new LinkedList<>();
        this.pageMap = new HashMap<>();
        this.pinnedPages = new HashMap<>();
        this.filepath = filepath;
        this.diskFileName = diskFileName;
        this.movieIdIndexFileName = movieIdIndexFileName;
        this.movieTitleIndexFileName = movieTitleIndexFileName;
        this.currentPageID = (int)Files.size(Paths.get( filepath + diskFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieIdPage = (int)Files.size(Paths.get( filepath + movieIdIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieTitlePageId = (int)(int)Files.size(Paths.get( filepath + movieTitleIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
    }

//This method gets the page from buffer pool and disk(if not present in buffer pool)

    @Override
    public Page getPage(int pageId, File dataFile) throws Exception {
        String pageIdentifier = constructPageIdentifier(pageId, dataFile);
        Page page= null;
        try {
            if (bufferPool.containsKey(pageIdentifier)) {
                lru.remove(pageIdentifier);
                lru.addFirst(pageIdentifier);
                this.pinPage(pageIdentifier);
                return bufferPool.get(pageIdentifier);
            }
            if (this.pinnedPages.size() == MAX_PAGE) {
                // return null in the case that that all pages are pinned
                return null;
            }
            Path curPath = Paths.get( filepath + diskFileName).toAbsolutePath();
            long fileSize = Files.size(curPath);
            long numPages = fileSize / PAGE_SIZE;
            if (bufferPool.size() >= MAX_PAGE && pageId >= 0 && pageId < numPages) {
                evictPage();
            }
            page = loadPageFromDisk(pageId, dataFile);
            if (page == null) {
                throw new Exception("page does not exist on disk");
            }
            bufferPool.put(pageIdentifier, page);
            lru.addFirst(pageIdentifier);
            pageMap.put(page, pageId);
            this.pinPage(pageIdentifier);
            // System.out.println("the page " + pageId + " is pinned");

        } catch (Exception e) {
            System.out.println("issue in eviction as all pages currently in buffer pool marked pinned");
        }
        return page;
    }

//This method evicts the page when buffer pool is full

    public void evictPage() throws Exception {
        for (String currentPageId : lru.reversed()) {
            Page curPage = bufferPool.get(currentPageId);
            int curPageId = curPage.getPid();
            File dataFile = curPage.getDataFile();
            Page removedPage = bufferPool.get(currentPageId);
            if (!pinnedPages.containsKey(currentPageId)) {
                lru.remove(currentPageId);
                try {
                    if (removedPage.getDirtyStatus()) {
                        writePageToDisk(removedPage, dataFile);
                        removedPage.markNotDirty();
                    }
                    bufferPool.remove(currentPageId);
                    pageMap.remove(removedPage);
                } catch (IOException e) {
                    // Handle the exception, e.g., log it or rethrow it
                    System.err.println("Failed to write page to disk: " + e.getMessage());
                }
                return;
            }
        }
        throw new Exception("Every page in the buffer pool is currently pinned");
    }

//This method creates the new page

    @Override
    public Page createPage(File dataFile){
        Page page = null;
        try {
            int pageId = dataFile == File.DISK ? getNextPageId() : dataFile == File.MOVIE_ID_IDX ?
                    getNextMovieIdIndexPage() : getNextMovieTitleIndexPage();
            String pageIdentifier = constructPageIdentifier(pageId, dataFile);
            if (this.bufferPool.size() >= this.MAX_PAGE) {
                evictPage();
            }
            page = new PageImpl(pageId, dataFile);
            lru.addFirst(pageIdentifier);
            bufferPool.put(pageIdentifier, page);
            pageMap.put(page, pageId);
            this.pinPage(pageIdentifier);
            this.markDirty(pageId, dataFile);
        } catch (Exception e) {
            System.out.println("buffer pool is full, eviction is not happening as all pages are pinned");
        }
        return page;
    }

//This method marks the page dirty

    @Override
    public void markDirty(int pageId, File dataFile) {
        String pageIdentifier = constructPageIdentifier(pageId, dataFile);
        if (bufferPool.containsKey(pageIdentifier)) {
            Page page = bufferPool.get(pageIdentifier);
            page.markDirty();
        }
    }

//This method pins the page using pageID

    @Override
    public void unpinPage(int pageId, File dataFile) {
        String pageIdentifier = constructPageIdentifier(pageId, dataFile);
        if(bufferPool.containsKey(pageIdentifier)){
            if(pinnedPages.containsKey(pageIdentifier)){
                int count = pinnedPages.get(pageIdentifier);
                if(count >0){
                    Page pageToUnpin = bufferPool.get(pageIdentifier);
                    pageToUnpin.decrementPinCount();
                    pinnedPages.put(pageIdentifier, pageToUnpin.getPinCount());
                }
                if(pinnedPages.get(pageIdentifier) == 0) {
                    pinnedPages.remove(pageIdentifier);
                }
            }
        }
    }

// This method unpins the page using pageID

    public void pinPage(String pageIdentifier) {
        if(bufferPool.containsKey(pageIdentifier)){
            Page pageToPin = bufferPool.get(pageIdentifier);
            pageToPin.incrementPinCount();
            pinnedPages.put(pageIdentifier, pageToPin.getPinCount());
//            if(lru.contains(pageId)){
//                System.out.println("the page id is" + pageId + "the size is " + lru.size());
//            }
        }
    }

    public int getNumPagesOnDisk() throws IOException {
        return (int)Files.size(Paths.get(filepath + diskFileName)) / PAGE_SIZE;
    }

    public int getFileSizeOfChosenFile(File dataFile) throws IOException {
        if (dataFile == File.DISK) {
            return getNumPagesOnDisk();
        }
        else if (dataFile == File.MOVIE_ID_IDX) {
            return (int)Files.size(Paths.get(filepath + movieIdIndexFileName)) / PAGE_SIZE;
        }
        else {
            return (int)Files.size(Paths.get(filepath + movieTitleIndexFileName)) / PAGE_SIZE;
        }
    }

    // This method writes pages to disk

    public void writePageToDisk(Page page, File dataFile) throws IOException {
        String curDiskFileName = dataFile == File.DISK ? diskFileName : dataFile == File.MOVIE_ID_IDX ? this.movieIdIndexFileName : this.movieTitleIndexFileName;
        int pageId = page.getPid();
        Path curPath = Paths.get( filepath + curDiskFileName).toAbsolutePath();
        long fileSize = Files.size(curPath);
        long numPages = fileSize / PAGE_SIZE;
        byte[] bytesToWrite = new byte[0];

        if (pageId < 0) {
            pageId = (int)Files.size(curPath) / PAGE_SIZE;
        }
        if (numPages < pageId) {
            // pad entries with bytes of value 0.
            bytesToWrite = new byte[4096 * (int)(pageId - numPages)];
        }
        long startByte = numPages >= pageId ? (long)pageId * PAGE_SIZE : numPages * PAGE_SIZE;

        RandomAccessFile curFile = new RandomAccessFile(filepath + curDiskFileName,"rw");
        curFile.seek(startByte);

        curFile.write(bytesToWrite);

        for (Row row : page.getAllRows()) {
            if (row == null) {
                row = new Row(new byte[9], new byte[30]);
            }
            curFile.write(row.movieId);
            curFile.write(row.title);
        }

        curFile.write(new byte[page.getBytesToPad() - 1]);

        curFile.write((byte)page.getRowCount());

        // curFile.getFD().sync();

        curFile.close();
    }

    // This method loads pages from disk

    public Page loadPageFromDisk(int pageId, File dataFile) {
        String curDiskFileName = dataFile == File.DISK ? diskFileName : dataFile == File.MOVIE_ID_IDX ? this.movieIdIndexFileName : this.movieTitleIndexFileName;
        Path curPath = Paths.get(filepath + curDiskFileName);
        Charset charset = StandardCharsets.US_ASCII;
        Page pageToPopulate = new PageImpl(pageId, dataFile);
        pageToPopulate.markNotDirty();
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filepath + curDiskFileName))) {
            long fileSize = Files.size(curPath);
            long numPages = fileSize / PAGE_SIZE;
            if (numPages - 1 < pageId || pageId < 0) {
                throw new IOException("There are not sufficiently many pages for this pageId to be valid," +
                        " or the pageId is otherwise invalid.");
            }
            int startByte = pageId * PAGE_SIZE;

            reader.skip(startByte);

            int curRowCount = 0;

            Row[] curPageRows = new Row[PageImpl.MAX_TUPLES];

            for (int i = 0; i < PageImpl.PAGE_SIZE / PageImpl.ROW_SIZE; ++i) {
                byte[] curMovieId = new byte[Row.MOVIE_ID_SIZE];
                byte[] curMovieTitle = new byte[Row.TITLE_SIZE];

                reader.read(curMovieId, 0, curMovieId.length);

                reader.read(curMovieTitle, 0, curMovieTitle.length);

                for (byte b : curMovieId) {
                    if (b != 0) {
                        Row curRow = new Row(curMovieId, curMovieTitle);
                        curPageRows[i] = curRow;
                        break;
                    }
                }

                for (byte b : curMovieTitle) {
                    if (b != 0) {
                        Row curRow = new Row(curMovieId, curMovieTitle);
                        curPageRows[i] = curRow;
                        break;
                    }
                }
            }
            reader.skip(pageToPopulate.getBytesToPad() - 1);
            curRowCount = reader.read();
            pageToPopulate.setAllRows(curPageRows);
            pageToPopulate.setRowCount(curRowCount);
        } catch (Exception e) {
            System.out.println("Writing to the disk is failing due to this error" + e.getMessage());
            return null;
        }
        return pageToPopulate;
    }

    @Override
    public void force() throws Exception {
        for (int i = lru.size() - 1; i >= 0; --i) {
            String[] key = lru.get(i).split("-");
            File dataFile = File.valueOf(key[0]);
            int pid = Integer.parseInt(key[1]);
            while (pinnedPages.containsKey(key)) {
                unpinPage(pid, dataFile);
            }
            evictPage();
        }
    }

    // IDs are now sequentially assigned starting from the most recent page on disk
    public int getNextPageId() {
        return currentPageID++;
    }

    // Method to access next ID for movie ID index page on disk.
    public int getNextMovieIdIndexPage() {
        return currentMovieIdPage++;
    }

    // Method to access next ID for movie title ID index page on disk
    public int getNextMovieTitleIndexPage() {
        return currentMovieTitlePageId++;
    }

//     meant to be run separately from the buffer manager, helper utility to initially populate the disk.
    public void populateDisk(int numRecords, String filepath) throws IOException {
        int curPageId = 0;
        BufferedReader reader = new BufferedReader(new FileReader(filepath + "title.basics.tsv"));
        reader.readLine();
        String line = reader.readLine();
        boolean justFlushedPage = true;
        PageImpl newPage = new PageImpl(curPageId, File.DISK);
        int i = 0;
        while ((numRecords == -1 || i < numRecords) && line != null) {
            if (newPage.isFull()) {
                writePageToDisk(newPage, File.DISK);
                newPage = new PageImpl(++curPageId, File.DISK);
            }
            String[] columns = line.split("\t");
            byte[] title = columns[2].getBytes();
            byte[] movieId = columns[0].getBytes();
            Row row = new Row(movieId, title);
            newPage.insertRow(row);
            i += 1;
            line = reader.readLine();
        }
        writePageToDisk(newPage, File.DISK);
    }

    // creates bufferpool map descriptors based on the data file containing the page
    public String constructPageIdentifier(int pageId, File dataFile) {
        return dataFile.toString() + "-" + Integer.toString(pageId);
    }
}
