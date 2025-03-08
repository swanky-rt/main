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
    public Map<Integer, Page> bufferPool;
    //public Set<Integer> dirtyPages;
    public Map<Page, Integer> pageMap;
    public LinkedList<Integer> lru;
    public Map<Integer, Integer> pinnedPages;
    private int currentPageID = -1;
    private String filepath;
    private String diskFileName;
    private String csvName;


    public BufferManagerImpl(int bufferSize, String filepath, String diskFileName, String csvName) throws IOException {
        super(bufferSize);
        this.PAGE_SIZE = 4096;
        this.MAX_PAGE = bufferSize/PAGE_SIZE;
        this.bufferPool = new HashMap<>();
        this.lru = new LinkedList<>();
       // this.dirtyPages = new HashSet<>();
        this.pageMap = new HashMap<>();
        this.pinnedPages = new HashMap<>();
        this.filepath = filepath;
        this.diskFileName = diskFileName;
        this.csvName = csvName;

    }
    Utilities utilities = new Utilities();

//This method gets the page from buffer pool and disk(if not present in buffer pool)

    @Override
    public Page getPage(int pageId) throws Exception {
        Page page= null;
        try {
            if (bufferPool.containsKey(pageId)) {
                lru.remove((Integer) pageId);
                lru.addFirst(pageId);
                this.pinPage(pageId);
                return bufferPool.get(pageId);
            }
            if (bufferPool.size() >= MAX_PAGE) {
                try {
                    evictPage();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            page = loadPageFromDisk(pageId);
            bufferPool.put(pageId, page);
            lru.addFirst(pageId);
            pageMap.put(page, pageId);
            this.pinPage(pageId);
            System.out.println("the page " + pageId + " is pinned");

        } catch (Exception e) {
            System.out.println("issue in eviction as all pages currently in buffer pool marked pinned");
        }
        return page;
    }

//This method evicts the page when buffer pool is full

    public void evictPage() throws Exception {
        for (Integer curPageId : lru.reversed()) {
            Page removedPage = bufferPool.get(curPageId);
            if (!pinnedPages.containsKey(curPageId)) {
                lru.remove(curPageId);
                try {
                    if (removedPage.getDirtyStatus()) {
                        writePageToDisk(curPageId, removedPage);
                        //dirtyPages.remove(curPageId);
                        removedPage.markNotDirty();
                    }
                    bufferPool.remove(curPageId);
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
    public Page createPage(){
        Page page = null;
        try {
            int pageId = getNextPageId();
            if (this.bufferPool.size() >= this.MAX_PAGE) {
                try {
                    evictPage();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
            page = new PageImpl(pageId);
            lru.addFirst(pageId);
            bufferPool.put(pageId, page);
            pageMap.put(page, pageId);
            this.pinPage(pageId);
            this.markDirty(pageId);
        } catch (Exception e) {
            System.out.println("buffer pool is full, eviction is not happening as all pages are pinned");
        }
        return page;
    }

//This method marks the page dirty

    @Override
    public void markDirty(int pageId) {
//        if(isDirty(pageId).equals(Boolean.FALSE)){
//            dirtyPages.add(pageId);
//        }

        if (bufferPool.containsKey(pageId)) {
            Page page = bufferPool.get(pageId);
            page.markDirty();
        }
    }

    public Boolean isDirty(int pageId){
        return bufferPool.containsKey(pageId) && bufferPool.get(pageId).getDirtyStatus();
    }

//This method pins the page using pageID

    @Override
    public void unpinPage(int pageId) {
        if(bufferPool.containsKey(pageId)){
            if(pinnedPages.containsKey(pageId)){
                int count = pinnedPages.get(pageId);
                if(count >0){
                    Page pageToUnpin = bufferPool.get(pageId);
                    pageToUnpin.decrementPinCount();
                    pinnedPages.put(pageId, pageToUnpin.getPinCount());
                }

            }
            if(pinnedPages.get(pageId) == 0) {
                pinnedPages.remove(pageId);
            }
        }
    }

// This method unpins the page using pageID

    public void pinPage(int pageId) {
        if(bufferPool.containsKey(pageId)){
           //pinnedPages.add(pageId);
            Page pageToPin = bufferPool.get(pageId);
            pageToPin.incrementPinCount();
            //int pageCount = bufferPool.get
            if(!pinnedPages.containsKey(pageId)){
                pinnedPages.put(pageId, pageToPin.getPinCount());
            }
            if(lru.contains(pageId)){
                System.out.println("the page id is" + pageId + "the size is " + lru.size());
            }
        }
    }

    // This method writes pages to disk

    public void writePageToDisk(int pageId, Page page) throws IOException {
        Path curPath = Paths.get( filepath + diskFileName).toAbsolutePath();
        try  {
            long fileSize = Files.size(curPath);
            long numPages = fileSize / PAGE_SIZE;
            if (pageId < 0) {
                pageId = (int)Files.size(curPath) / PAGE_SIZE;
            }
            if (numPages < pageId) {
                throw new IOException("There are not sufficiently many pages for this pageId to be valid," +
                        " or the pageID is invalid otherwise.");
            }
            int startByte = pageId * PAGE_SIZE;

            page.reassignPageId(pageId);

            RandomAccessFile curFile = new RandomAccessFile(filepath + diskFileName,"rw");
            curFile.seek(startByte);

            for (Row row : page.getAllRows()) {
                if (row == null) {
                    row = new Row(new byte[9], new byte[30]);
                }
                curFile.write(row.movieId);
                curFile.write(row.title);
            }

            curFile.write(new byte[page.getBytesToPad() - 1]);

            curFile.write((byte)page.getRowCount());

            curFile.close();

        } catch (IOException e) {
            System.out.println("Writing to the disk is failing due to this error" + e.toString());
        }
    }

    // This method loads pages from disk

    public Page loadPageFromDisk(int pageId) {
        Path curPath = Paths.get(filepath + diskFileName);
        Charset charset = StandardCharsets.US_ASCII;
        Page pageToPopulate = new PageImpl(pageId);
        pageToPopulate.markNotDirty();
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filepath + diskFileName))) {
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
            }
            reader.skip(pageToPopulate.getBytesToPad() - 1);
            curRowCount = reader.read();
            pageToPopulate.setAllRows(curPageRows);
            pageToPopulate.setRowCount(curRowCount);
        } catch (Exception e) {
            System.out.println("Writing to the disk is failing due to this error" + e.getMessage());
        }
        return pageToPopulate;
    }

    // meant to be run separately from the buffer manager, helper utility to initially populate the disk.
    public void populateDisk(int numRecords) throws IOException {
        int curPageId = 0;
        BufferedReader reader = new BufferedReader(new FileReader(this.filepath + "title.basics.tsv"));
        reader.readLine();
        boolean justFlushedPage = true;
        PageImpl newPage = new PageImpl(curPageId);
        for (int i = 0; i < numRecords; ++i) {
            if (newPage.isFull()) {
                writePageToDisk(curPageId++, newPage);
                newPage = new PageImpl(curPageId);
            }
            String line = reader.readLine();

            String[] columns = line.split("\t");
            byte[] title = columns[2].getBytes();
            byte[] movieId = columns[0].getBytes();
            Row row = new Row(movieId, title);
            newPage.insertRow(row);
        }
        writePageToDisk(curPageId, newPage);
    }

    public int getNextPageId() {
        return currentPageID--;
    }
}
