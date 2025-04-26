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
    private final Map<File, String> fileMap = new HashMap<>();
    private int currentPageID;
    private int currentMovieIdPage;
    private int currentMovieTitlePageId;
    private String filepath;
    private String diskFileName;
    private String movieIdIndexFileName;
    private String movieTitleIndexFileName;
    private static final int MAX_LINE_LENGTH = 10000;



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

//        this.diskFileName = diskFileName;
//        this.movieIdIndexFileName = movieIdIndexFileName;
//        this.movieTitleIndexFileName = movieTitleIndexFileName;
        // Define the file mappings
        fileMap.put(File.DISK, diskFileName);
        fileMap.put(File.MOVIE_ID_IDX, movieIdIndexFileName);
        fileMap.put(File.MOVIE_TITLE_IDX, movieTitleIndexFileName);
        fileMap.put(File.MOVIES, "movies.disk");
        fileMap.put(File.WORKEDON, "workedon.disk");
        fileMap.put(File.PEOPLE, "people.disk");
        this.currentPageID = (int)Files.size(Paths.get( filepath + diskFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieIdPage = (int)Files.size(Paths.get( filepath + movieIdIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
        this.currentMovieTitlePageId = (int)Files.size(Paths.get( filepath + movieTitleIndexFileName).toAbsolutePath()) / this.PAGE_SIZE;
    }

//This method gets the page from buffer pool and disk(if not present in buffer pool)

//    @Override
//    public Page getPage(int pageId, File dataFile) throws Exception {
//        String pageIdentifier = constructPageIdentifier(pageId, dataFile);
//        Page page= null;
//        try {
//            if (bufferPool.containsKey(pageIdentifier)) {
//                lru.remove(pageIdentifier);
//                lru.addFirst(pageIdentifier);
//                this.pinPage(pageIdentifier);
//                return bufferPool.get(pageIdentifier);
//            }
//            if (this.pinnedPages.size() == MAX_PAGE) {
//                // return null in the case that that all pages are pinned
//                return null;
//            }
//            Path curPath = Paths.get( filepath + diskFileName).toAbsolutePath();
//            long fileSize = Files.size(curPath);
//            long numPages = fileSize / PAGE_SIZE;
//            if (bufferPool.size() >= MAX_PAGE && pageId >= 0 && pageId < numPages) {
//                evictPage();
//            }
//            page = loadPageFromDisk(pageId, dataFile);
//            if (page == null) {
//                throw new Exception("page does not exist on disk");
//            }
//            bufferPool.put(pageIdentifier, page);
//            lru.addFirst(pageIdentifier);
//            pageMap.put(page, pageId);
//            this.pinPage(pageIdentifier);
//            // System.out.println("the page " + pageId + " is pinned");
//
//        } catch (Exception e) {
//            System.out.println("issue in eviction as all pages currently in buffer pool marked pinned");
//        }
//        return page;
//    }

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


// old code
//    public int getFileSizeOfChosenFile(File dataFile) throws IOException {
//        if (dataFile == File.DISK) {
//            return getNumPagesOnDisk();
//        }
//        else if (dataFile == File.MOVIE_ID_IDX) {
//            return (int)Files.size(Paths.get(filepath + movieIdIndexFileName)) / PAGE_SIZE;
//        }
//        else {
//            return (int)Files.size(Paths.get(filepath + movieTitleIndexFileName)) / PAGE_SIZE;
//        }
//    }

    public int getFileSizeOfChosenFile(File dataFile) throws IOException {
        String fileName = fileMap.get(dataFile);
        if (fileName == null) {
            throw new IllegalArgumentException("Unknown File enum: " + dataFile);
        }

        Path filePathObj = Paths.get(filepath + fileName).toAbsolutePath();
        return (int) Files.size(filePathObj) / PAGE_SIZE;
    }

    // This method writes pages to disk

    public void writePageToDisk(Page page, File dataFile) throws IOException {
        //String curDiskFileName = dataFile == File.DISK ? diskFileName : dataFile == File.MOVIE_ID_IDX ? this.movieIdIndexFileName : this.movieTitleIndexFileName;
        String curDiskFileName = fileMap.get(dataFile);
        if (curDiskFileName == null) {
            throw new IllegalArgumentException("Unknown File enum: " + dataFile);
        }
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
                row = new Row(new byte[9], new byte[30], null, null, null);
            }
            if(curDiskFileName.equals("people.disk")){
                curFile.write(row.personId);
                curFile.write(row.name);
            }
            if(curDiskFileName.equals("movies.disk")){
                curFile.write(row.movieId);
                curFile.write(row.title);
            }
            if(curDiskFileName.equals("workedon.disk")){
                curFile.write(row.movieId);
                curFile.write(row.personId);
                curFile.write(row.category);
            }

        }

        curFile.write(new byte[page.getBytesToPad() - 1]);

        curFile.write((byte)page.getRowCount());

        // curFile.getFD().sync();

        curFile.close();
    }

    // This method loads pages from disk

    public Page loadPageFromDisk(int pageId, File dataFile) {
        //String curDiskFileName = dataFile == File.DISK ? diskFileName : dataFile == File.MOVIE_ID_IDX ? this.movieIdIndexFileName : this.movieTitleIndexFileName;
        String curDiskFileName = fileMap.get(dataFile);
        if (curDiskFileName == null) {
            throw new IllegalArgumentException("Unknown File enum: " + dataFile);
        }
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
                        Row curRow = new Row(curMovieId, curMovieTitle, null, null, null);
                        curPageRows[i] = curRow;
                        break;
                    }
                }

                for (byte b : curMovieTitle) {
                    if (b != 0) {
                        Row curRow = new Row(curMovieId, curMovieTitle, null, null, null);
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
            Row row = new Row(movieId, title, null, null, null);
            newPage.insertRow(row);
            i += 1;
            line = reader.readLine();
        }
        writePageToDisk(newPage, File.DISK);
    }

    public void populateTable(File tableFile, String filepath, int numRecords) throws IOException {
        BufferedReader reader = null;
        String line;
        int curPageId = 0;
        PageImpl newPage = new PageImpl(curPageId, tableFile);
        int i = 0;

        switch (tableFile) {
            case MOVIES:
                reader = new BufferedReader(new FileReader(filepath + "title.basics.tsv"));
                reader.readLine(); // skip header
                while ((line = reader.readLine()) != null && (numRecords == -1 || i < numRecords)) {
                    String[] cols = line.split("\t");
                    byte[] movieId = padBytes(cols[0].getBytes(), 9);   // tconst
                    byte[] title = padBytes(cols[2].getBytes(), 30);    // primaryTitle

                    Row row = new Row(movieId, title, null, null, null);
                    if (newPage.isFull()) {
                        writePageToDisk(newPage, tableFile);
                        newPage = new PageImpl(++curPageId, tableFile);
                    }
                    newPage.insertRow(row);
                    i++;
                }
                break;

            case WORKEDON:
                try (FileInputStream fis = new FileInputStream(filepath + "title.principals.tsv");
                     InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                     BufferedReader readerWO = new BufferedReader(isr)) {

                    //readerWO.readLine(); // Skip header
                    String lineWO;
                    int lineNum = 0;

                    while ((lineWO = readSafeLine(readerWO)) != null && (numRecords == -1 || i < numRecords)) {
                        lineNum++;

                        // Skip ridiculously long lines
                        if (lineWO.length() > 10000) {
                            System.err.println("Skipping long line at row " + lineNum);
                            continue;
                        }

                        String[] cols = lineWO.split("\t");
                        if (cols.length < 4) {
                            System.err.println("Skipping malformed line at row " + lineNum);
                            continue;
                        }

                        String movieIdRaw = cols[0];
                        String personIdRaw = cols[2];
                        String categoryRaw = cols[3];

                        if (movieIdRaw.length() > 20 || categoryRaw.length() > 100) {
                            System.err.println("Skipping too long field at row " + lineNum);
                            continue;
                        }

                        byte[] movieId = padBytes(movieIdRaw.getBytes(), 9);
                        byte[] personId = padBytes(personIdRaw.getBytes(), 10);
                        byte[] category = padBytes(categoryRaw.getBytes(), 20);

                        Row row = new Row(movieId, null, personId, category, null);
                        if (newPage.isFull()) {
                            writePageToDisk(newPage, tableFile);
                            newPage = new PageImpl(++curPageId, tableFile);
                        }

                        newPage.insertRow(row);
                        i++;
                    }

                } catch (Exception e) {
                    System.err.println("Error parsing WORKEDON file: " + e.getMessage());
                    e.printStackTrace();
                }
                break;

            case PEOPLE:
                reader = new BufferedReader(new FileReader(filepath + "name.basics.tsv"));
                reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null && (numRecords == -1 || i < numRecords)) {
                    String[] cols = line.split("\t");

                    String movieIdRaw = cols[0];
                    String personIdRaw = cols[1];

                    byte[] personId = padBytes(movieIdRaw.getBytes(), 10);  // nconst
                    byte[] name = padBytes(personIdRaw.getBytes(), 105);     // primaryName

                    Row row = new Row(null, null, personId, null, name);  // Construct row for People
                    if (newPage.isFull()) {
                        writePageToDisk(newPage, tableFile);  // Write to disk when full
                        newPage = new PageImpl(++curPageId, tableFile);  // Create a new page
                    }
                    newPage.insertRow(row);  // Insert row into page
                    i++;
                }
                break;
        }

        if (newPage.getRowCount() > 0) {
            writePageToDisk(newPage, tableFile);
        }

        if (reader != null) {
            reader.close();
        }

        System.out.println("Populated table: " + tableFile + " with " + i + " rows.");
    }

    // creates buffer pool map descriptors based on the data file containing the page
    public String constructPageIdentifier(int pageId, File dataFile) {
        return dataFile.toString() + "-" + Integer.toString(pageId);
    }

    public static byte[] padBytes(byte[] input, int length) {
        byte[] result = new byte[length];
        int copyLength = Math.min(input.length, length);
        System.arraycopy(input, 0, result, 0, copyLength);
        return result;
    }

    private String readSafeLine(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        int count = 0;

        while ((c = reader.read()) != -1) {
            if (c == '\n') break;
            sb.append((char) c);
            count++;

            if (count > MAX_LINE_LENGTH) {
                System.err.println("Line exceeded max length, skipping...");
                // skip to end of line
                while (c != '\n' && (c = reader.read()) != -1) {}
                return null; // signal to skip this line
            }
        }

        if (count == 0 && c == -1) return null; // EOF

        return sb.toString();
    }
    private Page createPageInstance(int pageId, File dataFile) {
        switch (dataFile) {
            case MOVIES:
                return new MoviesPage(pageId, dataFile);
            case WORKEDON:
                return new WorkedOnPage(pageId, dataFile);
            case PEOPLE:
                return new PeoplePage(pageId, dataFile);
            default:
                return new PageImpl(pageId, dataFile);  // Default page for unknown files
        }
    }

    @Override
    public Page getPage(int pageId, File dataFile) throws Exception {
        // Construct the unique identifier for the page
        String pageIdentifier = constructPageIdentifier(pageId, dataFile);
        Page page = null;

        // Step 1: Check if the page is already in the buffer pool
        if (bufferPool.containsKey(pageIdentifier)) {
            // Move the page to the front of the LRU list (most recently used)
            lru.remove(pageIdentifier);
            lru.addFirst(pageIdentifier);
            this.pinPage(pageIdentifier); // Pin the page to prevent eviction
            return bufferPool.get(pageIdentifier); // Return the page from the buffer pool
        }

        // Step 2: If the buffer pool is full, evict a page
        if (this.pinnedPages.size() == MAX_PAGE) {
            // No space in buffer pool, return null indicating that page couldn't be loaded
            return null;
        }
        if(dataFile.equals(File.MOVIES)) {
            List<String> movieColumns = Arrays.asList("movieId", "title");
            List<Integer> movieColumnSizes = Arrays.asList(9, 30);
            page = loadPageFromDiskForJoins(pageId, File.MOVIES, movieColumns, movieColumnSizes);
        }

// For WorkedOn

        if(dataFile.equals(File.WORKEDON)) {
            List<String> workedOnColumns = Arrays.asList("movieId", "personId", "category");
            List<Integer> workedOnColumnSizes = Arrays.asList(9, 10, 20);
            page = loadPageFromDiskForJoins(pageId, File.WORKEDON, workedOnColumns, workedOnColumnSizes);
        }

// For People
        if(dataFile.equals(File.PEOPLE)) {
            List<String> peopleColumns = Arrays.asList("personId", "name");
            List<Integer> peopleColumnSizes = Arrays.asList(10, 105);
            page = loadPageFromDiskForJoins(pageId, File.PEOPLE, peopleColumns, peopleColumnSizes);
        }
        if (page == null) {
            throw new Exception("Page does not exist on disk or could not be loaded.");
        }

        // Step 4: Add the newly loaded page to the buffer pool
        bufferPool.put(pageIdentifier, page);
        lru.addFirst(pageIdentifier); // Mark the page as most recently used
        pageMap.put(page, pageId);    // Map the page to its page ID
        this.pinPage(pageIdentifier); // Pin the page to prevent eviction

        return page;
    }

    public Page loadPageFromDiskForJoins(int pageId, File dataFile, List<String> columnNames, List<Integer> columnSizes) throws IOException {
        // Get the appropriate file name based on the table

            // Get the appropriate file name based on the table
            String curDiskFileName = fileMap.get(dataFile);
            if (curDiskFileName == null) {
                throw new IllegalArgumentException("Unknown File enum: " + dataFile);
            }
        Page pageToPopulate= null;

            // Resolve the full path to the file
            Path curPath = Paths.get(filepath + curDiskFileName).toAbsolutePath();
            if(dataFile.equals(File.WORKEDON)){
                pageToPopulate = new WorkedOnPage(pageId, dataFile);
            }
        if(dataFile.equals(File.PEOPLE)){
           pageToPopulate = new PeoplePage(pageId, dataFile);
        }
        if(dataFile.equals(File.MOVIES)){
            pageToPopulate = new MoviesPage(pageId, dataFile);
        }
        else {
            pageToPopulate = new PageImpl(pageId, dataFile);
        }
            pageToPopulate.markNotDirty();  // Initially mark the page as not dirty

            // Read the page data from the file
            try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(curPath.toFile()))) {
                long fileSize = Files.size(curPath);
                long numPages = fileSize / PAGE_SIZE;

                // Ensure the pageId is valid
                if (numPages - 1 < pageId || pageId < 0) {
                    throw new IOException("Invalid pageId: " + pageId + " or not enough pages in the file");
                }

                int startByte = pageId * PAGE_SIZE;
                reader.skip(startByte);  // Skip to the position of the page in the file

                int curRowCount = 0;
                Row[] curPageRows = new Row[PageImpl.MAX_TUPLES];

                // Read the rows in the page based on column sizes
                for (int i = 0; i < PageImpl.PAGE_SIZE / PageImpl.ROW_SIZE; ++i) {
                    byte[] rowBytes = new byte[PAGE_SIZE];  // Row buffer

                    // Read the row data based on column sizes
                    int currentOffset = 0;
                    List<byte[]> columnValues = new ArrayList<>();
                    for (int j = 0; j < columnSizes.size(); j++) {
                        byte[] columnData = new byte[columnSizes.get(j)];
                        reader.read(columnData, 0, columnData.length);
                        columnValues.add(columnData);
                        currentOffset += columnSizes.get(j);
                    }

                    // Create a Row based on column data
                    Row row = new Row(
                            columnValues.get(0),  // First column (e.g., movieId)
                            columnValues.get(1),  // Second column (e.g., title or personId)
                            columnValues.size() > 2 ? columnValues.get(2) : null,  // Optional columns (category or name)
                            columnValues.size() > 3 ? columnValues.get(3) : null,
                            columnValues.size() > 4 ? columnValues.get(4) : null
                    );

                    curPageRows[i] = row;
                }

                reader.skip(pageToPopulate.getBytesToPad() - 1);  // Skip padding bytes
                curRowCount = reader.read();
                pageToPopulate.setAllRows(curPageRows);
                pageToPopulate.setRowCount(curRowCount);

            } catch (Exception e) {
                System.out.println("Failed to read from disk: " + e.getMessage());
                return null;
            }

            return pageToPopulate;
        }
}
