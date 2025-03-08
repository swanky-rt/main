package project_645;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities implements Serializable {

    private static final int BUFFER_SIZE = 4 * 4096;
    private static final int PAGE_SIZE = 4096;
    public int currentPageID;
    private static String filepath = System.getProperty("user.dir") + "/src/main/java/project_645/DB files/";
    private static String filename = "testdb.dat";
    private final Path dbFilePath = Paths.get( filepath + filename).toAbsolutePath();


    public Utilities() throws IOException {
        this.currentPageID = (int)Files.size(dbFilePath) / PAGE_SIZE;
    }

    // Loads the buffer manager with the imdb dataset
    public void loadDataset(BufferManager bf, String filepath) throws Exception {
        // create page
        Page newPage = bf.createPage();
        int pageId = newPage.getPid();
        bf.getPage(pageId);
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

        int bufferManagerSize = bf.bufferSize;
        if (currentPageID < bf.bufferSize / 4096) {
            throw new Exception("Disk does not have sufficiently many unique pages to fill up the buffer manager");
        }
        for (int i = 0; i < pageId; ++i) {
            Page loopPage = bf.getPage(i);
            if (i >= 5) {
                bf.unpinPage(pageId);
            }
//            if (i >= 5) {
//                String curLine = reader.readLine();
//                String[] columns = curLine.split("\t");
//                byte[] movieId = columns[0].getBytes();
//                byte[] titleId = columns[2].getBytes();
//                Row row = new Row(movieId, titleId);
//                loopPage.insertRow(row);
//            }
            bf.unpinPage(i);
        }
        //get page back
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

    public void writePageToDisk(int pageId, Page page) throws IOException {
        Path curPath = Paths.get(filepath + filename);
        try  {
//            Path path = Paths.get(filepath + pageId + ".dat");
//            if (Files.exists(path)) {
//                System.out.println("file exists in the disk already, updating it.." + path);
//                try (ObjectOutputStream outputStream = new AppendableObjectOutputStream(Files.newOutputStream(path))) {
//                    outputStream.writeObject(page);
//                }
//            } else {
//                ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(filepath + pageId + ".dat")));
//                outputStream.writeObject(page);
//                outputStream.close();
//            }

            long fileSize = Files.size(curPath);
            long numPages = fileSize / PAGE_SIZE;
            if (numPages < pageId || pageId < 0) {
                throw new IOException("There are not sufficiently many pages for this pageId to be valid," +
                        " or the pageID is invalid otherwise.");
            }
            int startByte = pageId * PAGE_SIZE;

            RandomAccessFile curFile = new RandomAccessFile(filepath + filename,"rw");
            curFile.seek(startByte);

            for (Row row : page.getAllRows()) {
                if (row == null) {
                    row = new Row(new byte[9], new byte[30]);
                }
                curFile.write(row.movieId);
                curFile.write(row.title);
            }



            curFile.write(new byte[page.getBytesToPad() - 1]);

            int test = page.getRowCount();

            curFile.write((byte)page.getRowCount());

            curFile.close();

        } catch (IOException e) {
            System.out.println("Writing to the disk is failing due to this error" + e.toString());
        }
    }

    public Page loadPageFromDisk(int pageId) {
        Path curPath = Paths.get(filepath + filename);
        Charset charset = StandardCharsets.US_ASCII;
        Page pageToPopulate = new PageImpl(pageId);
        pageToPopulate.markNotDirty();
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filepath + filename))) {
//            ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(Paths.get(String.valueOf(filepath + pageId + ".dat"))));
//            Page page = (Page) inputStream.readObject();
//            inputStream.close();
//            return page;
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

            reader.close();

//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        } catch (Exception e) {
            System.out.println("Writing to the disk is failing due to this error" + e.getMessage());
        }
        return pageToPopulate;
    }

    public int getNextPageId() {
        return currentPageID++;
    }

    private static class AppendableObjectOutputStream extends ObjectOutputStream {
        public AppendableObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {

        }

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
}
