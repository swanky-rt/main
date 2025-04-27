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
    private int currentWorkedOnPageId;
    private int currentPeoplePageId;
    private int curTempTableId;
    private String filepath;
    private String diskFileName;
    private String movieIdIndexFileName;
    private String movieTitleIndexFileName;
    private String workedOnFileName;
    private String peopleFileName;


    public BufferManagerImpl(int bufferSize, String filepath, String diskFileName, String movieIdIndexFileName,
                             String movieTitleIndexFileName, String workedOnFilename, String peopleFileName) throws IOException {
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
        this.workedOnFileName = workedOnFilename;
        this.peopleFileName = peopleFileName;
        this.currentPageID = (int)Files.size(Paths.get( filepath + diskFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieIdPage = (int)Files.size(Paths.get( filepath + movieIdIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieTitlePageId = (int)(int)Files.size(Paths.get( filepath + movieTitleIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentWorkedOnPageId = (int)(Files.size(Paths.get( filepath + workedOnFilename).toAbsolutePath()) / (long)this.PAGE_SIZE);
        this.currentPeoplePageId = (int)Files.size(Paths.get( filepath + peopleFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.curTempTableId = 0;
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
            int pageId = getNextCreatePageId(dataFile);
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

    // private helper method to get the next page ID for the appropriate page
    private int getNextCreatePageId(File dataFile) {
        return switch (dataFile) {
            case File.DISK -> getNextPageId();
            case File.MOVIE_ID_IDX -> getNextMovieIdIndexPage();
            case File.MOVIE_TITLE_IDX -> getNextMovieTitleIndexPage();
            case File.WORKEDON -> getNextWorkedOnPageId();
            case File.PEOPLE -> getNextPersonPageId();
            case File.TEMPORARY -> getNextTempTableId();
            default -> getNextPageId();
        };
    }

    // private helper method to get the corresponding file name for the correct data file
    private String getDataFileName(File dataFile) {
        return switch (dataFile) {
            case File.DISK -> diskFileName;
            case File.MOVIE_ID_IDX -> movieIdIndexFileName;
            case File.MOVIE_TITLE_IDX -> movieTitleIndexFileName;
            case File.WORKEDON -> workedOnFileName;
            case File.PEOPLE -> peopleFileName;
            case File.TEMPORARY -> File.TEMPORARY.toString() + ".dat";
            default -> diskFileName;
        };
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
        else if (dataFile == File.WORKEDON) {
            return (int)(Files.size(Paths.get(filepath + workedOnFileName)) / (long)PAGE_SIZE);
        }
        else if (dataFile == File.PEOPLE) {
            return (int)(Files.size(Paths.get(filepath + peopleFileName)) / (long)PAGE_SIZE);
        }
        else if (dataFile == File.MOVIE_ID_IDX) {
            return (int)(Files.size(Paths.get(filepath + movieIdIndexFileName)) / (long)PAGE_SIZE);
        }
        else if (dataFile == File.MOVIE_TITLE_IDX) {
            return (int)(Files.size(Paths.get(filepath + movieTitleIndexFileName)) / (long)PAGE_SIZE);
        }
        else if (dataFile == File.TEMPORARY) {
            return (int)(Files.size(Paths.get(filepath + File.TEMPORARY.toString() + ".dat")) / (long)PAGE_SIZE);
        }
        return -1;
    }

    // This method writes pages to disk

    public void writePageToDisk(Page page, File dataFile) throws IOException {
        String curDiskFileName = getDataFileName(dataFile);
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
                row = createNewRow(dataFile);
            }
            if (dataFile == File.DISK || dataFile == File.MOVIE_ID_IDX || dataFile == File.MOVIE_TITLE_IDX) {
                curFile.write(row.movieId);
                curFile.write(row.title);
            }
            else if (dataFile == File.WORKEDON) {
                curFile.write(row.movieId);
                curFile.write(row.personId);
                curFile.write(row.category);
            }
            else if (dataFile == File.PEOPLE) {
                curFile.write(row.personId);
                curFile.write(row.name);
            }
            else if (dataFile == File.TEMPORARY) {
                curFile.write(row.movieId);
                curFile.write(row.personId);
            }
            else {
                throw new IOException();
            }
        }

        curFile.write(new byte[page.getBytesToPad() - 1]);

        curFile.write((byte)page.getRowCount());

        // curFile.getFD().sync();

        curFile.close();
    }

    // helper method to create row object
    private Row createNewRow(File dataFile) {
        return switch (dataFile) {
            case File.DISK -> new Row(new byte[9], new byte[30]);
            case File.MOVIE_ID_IDX -> new Row(new byte[9], new byte[30]);
            case File.MOVIE_TITLE_IDX -> new Row(new byte[9], new byte[30]);
            case File.WORKEDON -> new Row(new byte[9], new byte[10], new byte[20]);
            case File.PEOPLE -> new Row(new byte[10], new byte[105], true);
            case File.TEMPORARY -> new Row(new byte[9], new byte[10]);
            default -> new Row(new byte[9], new byte[30]);
        };
    }

    // This method loads pages from disk

    public Page loadPageFromDisk(int pageId, File dataFile) {
        String curDiskFileName = getDataFileName(dataFile);
        Path curPath = Paths.get(filepath + curDiskFileName);
        Charset charset = StandardCharsets.US_ASCII;
        PageImpl pageToPopulate = new PageImpl(pageId, dataFile);
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

            Row[] curPageRows = new Row[pageToPopulate.MAX_TUPLES];

            if (dataFile == File.DISK || dataFile == File.MOVIE_ID_IDX || dataFile == File.MOVIE_TITLE_IDX) {
                populateDataRows(curPageRows, reader);
            }
            else if (dataFile == File.WORKEDON) {
                populateWorkedOnRows(curPageRows, reader);
            }
            else if (dataFile == File.PEOPLE) {
                populatePeopleRows(curPageRows, reader);
            }
            else if (dataFile == File.TEMPORARY) {
                populateTempTableRows(curPageRows, reader);
            }
            else {
                throw new Exception();
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


    // Helper method to load the data into a page in the case that it is from the disk, or index file
    private void populateDataRows(Row[] curPageRows, BufferedInputStream reader) throws IOException {
        for (int i = 0; i < curPageRows.length; ++i) {
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
    }

    // Helper method to load the data into a page object in the case it is from the WorkedOn table
    private void populateWorkedOnRows(Row[] curPageRows, BufferedInputStream reader) throws IOException {
        for (int i = 0; i < curPageRows.length; ++i) {
            byte[] curMovieId = new byte[Row.MOVIE_ID_SIZE];
            byte[] curPersonId = new byte[Row.PERSON_ID_SIZE];
            byte[] curCategory = new byte[Row.CATEGORY_SIZE];

            reader.read(curMovieId, 0, curMovieId.length);
            reader.read(curPersonId, 0, curPersonId.length);
            reader.read(curCategory, 0, curCategory.length);

            for (byte b : curMovieId) {
                if (b != 0) {
                    Row curRow = new Row(curMovieId, curPersonId, curCategory);
                    curPageRows[i] = curRow;
                    break;
                }
            }

            for (byte b : curPersonId) {
                if (b != 0) {
                    Row curRow = new Row(curMovieId, curPersonId, curCategory);
                    curPageRows[i] = curRow;
                    break;
                }
            }

            for (byte b : curCategory) {
                if (b != 0) {
                    Row curRow = new Row(curMovieId, curPersonId, curCategory);
                    curPageRows[i] = curRow;
                    break;
                }
            }
        }
    }

    // Helper method to load people rows into the page object
    private void populatePeopleRows(Row[] curPageRows, BufferedInputStream reader) throws IOException {
        for (int i = 0; i < curPageRows.length; ++i) {
            byte[] curPersonId = new byte[Row.PERSON_ID_SIZE];
            byte[] curName = new byte[Row.NAME_SIZE];

            reader.read(curPersonId, 0, curPersonId.length);

            reader.read(curName, 0, curName.length);

            for (byte b : curPersonId) {
                if (b != 0) {
                    Row curRow = new Row(curPersonId, curName);
                    curPageRows[i] = curRow;
                    break;
                }
            }

            for (byte b : curName) {
                if (b != 0) {
                    Row curRow = new Row(curPersonId, curName);
                    curPageRows[i] = curRow;
                    break;
                }
            }
        }
    }

    // Helper method to load the data into a page object in the case it is from the WorkedOn table
    private void populateTempTableRows(Row[] curPageRows, BufferedInputStream reader) throws IOException {
        for (int i = 0; i < curPageRows.length; ++i) {
            byte[] curMovieId = new byte[Row.MOVIE_ID_SIZE];
            byte[] curPersonId = new byte[Row.PERSON_ID_SIZE];

            reader.read(curMovieId, 0, curMovieId.length);
            reader.read(curPersonId, 0, curPersonId.length);

            for (byte b : curMovieId) {
                if (b != 0) {
                    Row curRow = new Row(curMovieId, curPersonId, File.TEMPORARY);
                    curPageRows[i] = curRow;
                    break;
                }
            }

            for (byte b : curPersonId) {
                if (b != 0) {
                    Row curRow = new Row(curMovieId, curPersonId, File.TEMPORARY);
                    curPageRows[i] = curRow;
                    break;
                }
            }
        }
    }


    @Override
    public void force() throws Exception {
        for (int i = lru.size() - 1; i >= 0; --i) {
            String[] key = lru.get(i).split("-");
            File dataFile = File.valueOf(key[0]);
            int pid = Integer.parseInt(key[1]);
            String fileKey = lru.get(i);
            while (pinnedPages.containsKey(fileKey)) {
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

    // Method to access next ID for the worked on table
    public int getNextWorkedOnPageId() {
        return currentWorkedOnPageId++;
    }

    // Method to access next ID for the person table
    public int getNextPersonPageId() {
        return currentPeoplePageId++;
    }

    public int getNextTempTableId() {
        return curTempTableId++;
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

    // creates buffer pool map descriptors based on the data file containing the page
    public String constructPageIdentifier(int pageId, File dataFile) {
        return dataFile.toString() + "-" + Integer.toString(pageId);
    }
}
