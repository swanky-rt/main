package project_645;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class Utilities {

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
        int pageId = (int)newPage.getPid();
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
            Row row = new Row(movieId, titleId, null, null, null);
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
        pageId = (int)newPage.getPid();
        newPage = bf.getPage(pageId, File.DISK);
        bf.markDirty(pageId, File.DISK);

        // add some more pages using p.insertRow();
        for (int i = 0; i < 20; ++i) {
            String curLine = reader.readLine();
            String[] columns = curLine.split("\t");
            byte[] movieId = columns[0].getBytes();
            byte[] titleId = columns[2].getBytes();
            Row row = new Row(movieId, titleId, null, null, null);
            newPage.insertRow(row);
        }
        bf.unpinPage(pageId, File.DISK);
    }

//This method to perform c1 test
    public void testC1(boolean populateMovieIdIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                       String workedOnTableFileName, String personTableFileName) throws Exception {

        BufferManager c1BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);

        // Create the indexes
        BTreeImpl bTreeMovieId = new BTreeImpl(c1BufferManager, 51, File.MOVIE_ID_IDX);

        if (populateMovieIdIndexFile) {
            bTreeMovieId.bulkLoad();
        }
    }

//This method to perform c2 test
    public void testC2(boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                       String workedOnTableFileName, String personTableFileName) throws Exception {
        BufferManager c2BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);
        BTreeImpl bTreeTitleId = new BTreeImpl(c2BufferManager, 51, File.MOVIE_TITLE_IDX);

        if (populateMovieTitleIndexFile) {
            bTreeTitleId.populateIndex();
        }
    }

 //This method to perform c3 test
    public void testC3(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                       String workedOnTableFileName, String personTableFileName) throws Exception {
        // Search by one key on b+ tree index:
        BufferManager c3BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);

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

//This method to perform c4 test
    public void testC4(boolean populateMovieIdIndexFile, boolean populateMovieTitleIndexFile, String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                       String workedOnTableFileName, String personTableFileName) throws Exception {
        BufferManager c4BufferManager = new BufferManagerImpl(5000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);

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

//This method to perform P1 test
    public void testP1(String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName, boolean pinPages,
                       String workedOnTableFileName, String personTableFileName) throws Exception {
        // initialize buffer manager to perform the range query
        BufferManager p1BufferManager = new BufferManagerImpl(10000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);

        BTreeImpl bTreeMovieId = new BTreeImpl(p1BufferManager, 51, File.MOVIE_ID_IDX);

        int numPages = p1BufferManager.getNumPagesOnDisk();
        long startTimeScan = System.nanoTime();
        for (int i = 0; i < numPages; ++i) {
            p1BufferManager.getPage(i, File.DISK);
            p1BufferManager.unpinPage(i, File.DISK);
        }
        long endTimeScan = System.nanoTime();
        System.out.println(endTimeScan-startTimeScan);
        p1BufferManager.force();

        Iterator<Rid> rids = bTreeMovieId.rangeSearch("s", "u");
        ArrayList<Integer> selectivityPercentage = new ArrayList<>();
        selectivityPercentage.add(0);
        ArrayList<String> keys = new ArrayList<>();
        keys.add("");
        ArrayList<Rid> ridsList = new ArrayList<>();
        ArrayList<Long> Times = new ArrayList<Long>();
        int size = 0;

        while (rids.hasNext()) {
            size += 1;
            ridsList.add(rids.next());
        }
        rids = ridsList.iterator();
        double curTotal = size / 20.0;
        double curTotalAdd = size / 20.0;
        int percentage = 5;
        for (int i = 0; i < size; ++i) {
            Rid curRid = rids.next();
            if (i >= curTotal || i == size - 1) {
                Page curPage = p1BufferManager.getPage(curRid.getPageId(), File.DISK);
                Row curRow = curPage.getRow(curRid.getSlotId());
                String key = new String(curRow.getMovieId()).trim();
                p1BufferManager.unpinPage(curPage.getPid(), File.DISK);
                selectivityPercentage.add(percentage);
                keys.add(key);
                percentage += 5;
                curTotal += curTotalAdd;
            }
        }

        int rootId = bTreeMovieId.findRoot(0);
        p1BufferManager.force();
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < selectivityPercentage.size(); ++i) {
            if (pinPages) {
                pinPages(rootId, p1BufferManager, File.MOVIE_ID_IDX);
            }
            String startKey = "";
            String endKey = keys.get(i);
            long startTime = System.nanoTime();
            Iterator<Rid> curRangeRIDs = bTreeMovieId.rangeSearch(startKey, endKey);
            while (curRangeRIDs.hasNext()) {
                Rid curRID = curRangeRIDs.next();
                Page curPage = p1BufferManager.getPage(curRID.getPageId(), File.DISK);
                p1BufferManager.unpinPage(curPage.getPid(), File.DISK);
            }
            long endTime = System.nanoTime();
            p1BufferManager.force();
            times.add(endTime - startTime);
        }

        for (int i = 0; i < selectivityPercentage.size(); ++i) {
            System.out.println(keys.get(i));
            System.out.println(selectivityPercentage.get(i));
            System.out.println(times.get(i));
        }

        XYSeries indexQueries = new XYSeries("Index Queries");
        XYSeries scanQuery = new XYSeries("Direct Scan");
        XYSeries ratioSeries = new XYSeries("Ratio of index time to full scan");


        for (int i = 0; i < keys.size(); ++i) {
            indexQueries.add(selectivityPercentage.get(i), times.get(i));
            scanQuery.add(selectivityPercentage.get(i), (Number)(endTimeScan - startTimeScan));
            ratioSeries.add(selectivityPercentage.get(i), (Number)(times.get(i)/(endTimeScan - startTimeScan)));
        }

        XYSeriesCollection plot1 = new XYSeriesCollection();
        XYSeriesCollection plot2 = new XYSeriesCollection();
        plot1.addSeries(indexQueries);
        plot1.addSeries(scanQuery);
        plot2.addSeries(ratioSeries);


       // QueryPerformancePlot.plotChart(selectivityPercentage, times, endTimeScan - startTimeScan);

        JFreeChart chart1 = ChartFactory.createXYLineChart(
                "Time/Selectivity (Clustered)",
                "Selectivity Percentage",
                "Execution Time (nano seconds)",
                plot1,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "Time/Selectivity Ratio (Clustered)",
                "Selectivity Percentage",
                "Ratio of Execution Time (index/scan) (nano seconds)",
                plot2,
                PlotOrientation.VERTICAL,
                true, true, false
        );


        ChartPanel chartPanel = new ChartPanel(chart1);
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        frame.setContentPane(chartPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        ChartPanel chartPanel2 = new ChartPanel(chart2);
        JFrame frame2 = new JFrame();
        frame2.setSize(800, 800);
        frame2.setContentPane(chartPanel2);
        frame2.setLocationRelativeTo(null);
        frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame2.setVisible(true);
    }

//This method to perform P2 test
    public void testP2(String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName, boolean pinPages,
                       String workedOnTableFileName, String personTableFileName) throws Exception {
        // initialize buffer manager to perform the range query
        BufferManager p2BufferManager = new BufferManagerImpl(10000*4096, filePath, diskFileName,
                movieIdIndexFileName, movieTitleIndexFileName, workedOnTableFileName, personTableFileName);

        BTreeImpl bTreeMovieId = new BTreeImpl(p2BufferManager, 51, File.MOVIE_TITLE_IDX);

        int numPages = p2BufferManager.getNumPagesOnDisk();
        long startTimeScan = System.nanoTime();
        for (int i = 0; i < numPages; ++i) {
            Page curPage = p2BufferManager.getPage(i, File.DISK);
            p2BufferManager.unpinPage(i, File.DISK);
        }
        long endTimeScan = System.nanoTime();
        System.out.println(endTimeScan-startTimeScan);
        p2BufferManager.force();


        byte[] startKeyArr = new byte[] { -62, -95, -62, -95, -62, -95, -62, -65, 51, 48, 48, 46, 48, 48, 48, 32, 66, 111, 116, 101, 108, 108, 105, 110, 101, 115, 63, 33, 33, 33 };
        byte[] endKeyArr = new byte[] { 126, 121, 101, 115, 126, 32, 80, 82, 79, 88, 73, 77, 73, 84, 89, 32, 67, 72, 65, 84, 32, 126, 121, 101, 115, 126, 32, 45, 32, 65 };
        Iterator<Rid> rids = bTreeMovieId.rangeSearch(new String(startKeyArr), new String(endKeyArr));
        ArrayList<Integer> selectivityPercentage = new ArrayList<>();
        selectivityPercentage.add(0);
        ArrayList<String> keys = new ArrayList<>();
        keys.add("");
        ArrayList<Rid> ridsList = new ArrayList<>();
        ArrayList<Long> Times = new ArrayList<Long>();

        int size = 0;

        while (rids.hasNext()) {
            size += 1;
            ridsList.add(rids.next());
        }

        rids = ridsList.iterator();

        double curTotal = size / 100.0;
        double curTotalAdd = size / 100.0;
        int percentage = 1;
        for (int i = 0; i < size; ++i) {
            Rid curRid = rids.next();
            if (i >= curTotal || i == size - 1) {
                Page curPage = p2BufferManager.getPage(curRid.getPageId(), File.DISK);
                Row curRow = curPage.getRow(curRid.getSlotId());
                String key = new String(curRow.getTitle()).trim();
                p2BufferManager.unpinPage(curPage.getPid(), File.DISK);
                selectivityPercentage.add(percentage);
                System.out.println(percentage);
                System.out.println(key);
                keys.add(key);
                percentage += 1;
                curTotal += curTotalAdd;
                if (percentage == 11){
                    break;
                }
            }
        }

        int rootPid = bTreeMovieId.findRoot(0);
        p2BufferManager.force();
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < selectivityPercentage.size(); ++i) {
            if (pinPages) {
                pinPages(rootPid, p2BufferManager, File.MOVIE_TITLE_IDX);
            }
            String startKey = new String(startKeyArr).trim();
            String endKey = keys.get(i);
            long startTime = System.nanoTime();
            Iterator<Rid> curRangeRIDs = bTreeMovieId.rangeSearch(startKey, endKey);
            while (curRangeRIDs.hasNext()) {
                Rid curRID = curRangeRIDs.next();
                Page curPage = p2BufferManager.getPage(curRID.getPageId(), File.DISK);
                p2BufferManager.unpinPage(curPage.getPid(), File.DISK);
            }
            long endTime = System.nanoTime();
            System.out.println(keys.get(i));
            System.out.println(endTime - startTime);
            p2BufferManager.force();
            times.add(endTime - startTime);
        }

        for (int i = 0; i < selectivityPercentage.size(); ++i) {
            System.out.println(keys.get(i));
            System.out.println(selectivityPercentage.get(i));
            System.out.println(times.get(i));
        }

        XYSeries indexQueries = new XYSeries("Index Queries");
        XYSeries scanQuery = new XYSeries("Direct Scan");
        XYSeries ratioSeries = new XYSeries("Ratio of index time to full scan");


        for (int i = 0; i < keys.size(); ++i) {
            indexQueries.add(selectivityPercentage.get(i), times.get(i));
            scanQuery.add(selectivityPercentage.get(i), (Number)(endTimeScan - startTimeScan));
            ratioSeries.add(selectivityPercentage.get(i), (Number)(times.get(i)/(endTimeScan - startTimeScan)));
        }

        XYSeriesCollection plot1 = new XYSeriesCollection();
        XYSeriesCollection plot2 = new XYSeriesCollection();
        plot1.addSeries(indexQueries);
        plot1.addSeries(scanQuery);
        plot2.addSeries(ratioSeries);

        // QueryPerformancePlot.plotChart(selectivityPercentage, times, endTimeScan - startTimeScan);

        JFreeChart chart1 = ChartFactory.createXYLineChart(
                "Time/Selectivity (Unclustered)",
                "Selectivity Percentage",
                "Execution Time (nano seconds)",
                plot1,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        ChartFrame graphFrame = new ChartFrame("XYLine Chart", chart1);

        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "Time/Selectivity Ratio (Unclustered)",
                "Selectivity Percentage",
                "Ratio of Execution Time (index/scan) (nano seconds)",
                plot2,
                PlotOrientation.VERTICAL,
                true, true, false
        );


        ChartPanel chartPanel = new ChartPanel(chart1);
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        frame.setContentPane(chartPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        ChartPanel chartPanel2 = new ChartPanel(chart2);
        JFrame frame2 = new JFrame();
        frame2.setSize(800, 800);
        frame2.setContentPane(chartPanel2);
        frame2.setLocationRelativeTo(null);
        frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame2.setVisible(true);
    }

    public void testP3(String filePath,
                       String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                       String workedOnTableFileName, String personTableFileName) throws Exception {

        testP1(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, true, workedOnTableFileName, personTableFileName);
        testP2(filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, true, workedOnTableFileName, personTableFileName);

    }

    public void queryPlanCorrectnessTest1() throws Exception {

        QueryExecutor queryExecutor = new QueryExecutor();
        long ios = queryExecutor.executeQuery("cb", "cbz", 100000*4096, true);

    }

    public void queryPlanCorrectnessTest2() throws Exception {

        QueryExecutor queryExecutor = new QueryExecutor();
        long ios = queryExecutor.executeQuery("x", "y", 100000*4096, true);

    }

    public void queryPlanCorrectnessTest3() throws Exception {

        QueryExecutor queryExecutor = new QueryExecutor();
        long ios = queryExecutor.executeQuery("w", "z", 100000*4096, true);

    }



    // Helper method for P3
    private int parseIntFromByteArray(byte[] arr, int offset) {
        //int b1 = (arr[offset]   & 0xFF) << 24;
        int b2 = (arr[offset+0] & 0xFF) << 16;
        int b3 = (arr[offset+1] & 0xFF) << 8;
        int b4 = (arr[offset+2] & 0xFF);
        int returnVal = (b2 | b3 | b4);
        if (returnVal == 16777215) {
            return -1;
        }
        return (b2 | b3 | b4);
    }

    private void pinPages(int rootPageId, BufferManager bufferManager, File indexFile) throws Exception {
        Page rootPage = bufferManager.getPage(rootPageId, indexFile);

        ArrayList<Page> curLevelPages = new ArrayList<>();
        curLevelPages.add(rootPage);
        ArrayList<Page> nextLevelPages = new ArrayList<>();

        // adjust condition to change amount of levels pinned in the buffer pool
        for (int i = 0; i < 1; ++i) {
            for (Page page : curLevelPages) {
                Row[] rows = page.getAllRows();
                for (int j = 1; j < rows.length; ++j) {
                    Row curDataRow = rows[j];
                    if (curDataRow != null) {
                        int nextChildId = parseIntFromByteArray(curDataRow.movieId, 0);
                        Page childPage = bufferManager.getPage(nextChildId, indexFile);
                        nextLevelPages.add(childPage);
                    }
                }
            }
            curLevelPages = nextLevelPages;
            nextLevelPages = new ArrayList<>();
        }
    }

//This method to delete and recreate the index files as everytime the file limit exceeds thus it is important to delete and create the index-
//file instead of appending and increasing the size of the file.
    public void deleteAndRecreateIndexFiles(String filePath, String diskFileName, String movieIdIndexFileName, String movieTitleIndexFileName,
                                            String workedOnTableFileName, String personTableFileName) {
        Path path = Paths.get(filePath + diskFileName);
        try {
            Files.deleteIfExists(path); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }
        Path movieIdIndexPath = Paths.get(filePath + movieIdIndexFileName);
        try {
            Files.deleteIfExists(movieIdIndexPath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path movieTitleIndexPath = Paths.get(filePath + movieTitleIndexFileName);
        try {
            Files.deleteIfExists(movieTitleIndexPath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path workedOnFilePath = Paths.get(filePath, workedOnTableFileName);
        try {
            Files.deleteIfExists(workedOnFilePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path peopleTableFilePath = Paths.get(filePath + personTableFileName);
        try {
            Files.deleteIfExists(peopleTableFilePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(path);
            System.out.println("File created: " + path.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(path)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieIdIndexPath);
            System.out.println("File created: " + movieIdIndexPath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieIdIndexPath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieTitleIndexPath);
            System.out.println("File created: " + movieTitleIndexPath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieTitleIndexPath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(workedOnFilePath);
            System.out.println("File created: " + workedOnFilePath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(workedOnFilePath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(peopleTableFilePath);
            System.out.println("File created: " + peopleTableFilePath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(peopleTableFilePath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }
    }

    public void populateAllDiskFiles(String filepath, String titleBasicsTSVReader, String workedOnTSVReader,
                                     String personTSVReader, BufferManager bf) throws Exception{
            Path dbFilePath = Paths.get( filepath + titleBasicsTSVReader).toAbsolutePath();
            Path workedOnTableFilePath = Paths.get(filepath + workedOnTSVReader);
            Path personTableFilePath = Paths.get(filepath + personTSVReader);
            // create page
            BufferedReader diskReader = new BufferedReader(new FileReader(filepath + titleBasicsTSVReader));
            diskReader.readLine();
            String curLine;
            Page nextPage = bf.createPage(File.DISK);
            // add rows using p.insertRow without filling p up
            while ((curLine = diskReader.readLine()) != null) {
                String[] splitLine = curLine.split("\t");
                byte[] movieId = splitLine[0].toLowerCase().getBytes();
                if (movieId.length > 9) {
                    continue;
                }
                byte[] titleId = splitLine[2].toLowerCase().getBytes();
                Row row = new Row(movieId, titleId, null, null, null);
                nextPage.insertRow(row);
                if (nextPage.isFull()) {
                    bf.unpinPage(nextPage.getPid(), File.DISK);
                    nextPage = bf.createPage(File.DISK);
                }
            }
            bf.unpinPage(nextPage.getPid(), File.DISK);
            bf.force();

            System.out.println("main disk file populated");

            BufferedReader workedOnTableReader = new BufferedReader(new FileReader(filepath + workedOnTSVReader));
            workedOnTableReader.readLine();
            nextPage = bf.createPage(File.WORKEDON);
            while ((curLine = workedOnTableReader.readLine()) != null) {
                String[] splitLine = curLine.split("\t");
                byte[] movieId = splitLine[0].toLowerCase().getBytes();
                byte[] personId = splitLine[2].toLowerCase().getBytes();
                byte[] category = splitLine[3].toLowerCase().getBytes();
                if (movieId.length > 9) {
                    continue;
                }
                Row row = new Row(movieId, null, personId, category, null);
                nextPage.insertRow(row);
                if (nextPage.isFull()) {
                    bf.unpinPage(nextPage.getPid(), File.WORKEDON);
                    nextPage = bf.createPage(File.WORKEDON);
                }
            }
            bf.unpinPage(nextPage.getPid(), File.WORKEDON);
            bf.force();


            System.out.println("Worked on table populated");

            BufferedReader peopleTableReader = new BufferedReader(new FileReader(filepath + personTSVReader));
            peopleTableReader.readLine();
            nextPage = bf.createPage(File.PEOPLE);
            while ((curLine = peopleTableReader.readLine()) != null) {
                String[] splitLine = curLine.split("\t");
                byte[] personId = splitLine[0].toLowerCase().getBytes();
                byte[] name = splitLine[1].toLowerCase().getBytes();
                Row row = new Row(null, null, personId, null, name);
                nextPage.insertRow(row);
                if (nextPage.isFull()) {
                    bf.unpinPage(nextPage.getPid(), File.PEOPLE);
                    nextPage = bf.createPage(File.PEOPLE);
                }
            }
            bf.unpinPage(nextPage.getPid(), File.PEOPLE);
            bf.force();

            System.out.println("Person table populated");
    }
}
