package project_645;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BPlusTreeTests {

    private BufferManagerImpl bufferManager;
    private RandomAccessFile randomAccessFile;
    private Path path1;
    private Page page;
    String movieIdIndexFileName = "movieIdIndex.dat";
    String movieTitleIndexFileName = "movieTitleIndex.dat";
    String workedOnFileName = "workedOnTable.dat";
    String peopleTableFileName = "peopleTable.dat";
    String workingDirectory = System.getProperty("user.dir");
    String testFileDirectory = "/src/test/java/project_645/DB files/";
    String testFilePath = System.getProperty("user.dir") + testFileDirectory;

    String fileName = "testdbfile.dat";

    @AfterEach
    public void teardown() {
        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
        try {
            Files.deleteIfExists(path); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path movieIdPath = Paths.get(workingDirectory, testFileDirectory, movieIdIndexFileName);
        try {
            Files.deleteIfExists(movieIdPath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path movieTitlePath = Paths.get(workingDirectory, testFileDirectory, movieTitleIndexFileName);
        try {
            Files.deleteIfExists(movieTitlePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path workedOnFilePath = Paths.get(workingDirectory, testFileDirectory, workedOnFileName);
        try {
            Files.deleteIfExists(workedOnFilePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }

        Path peopleTableFilePath = Paths.get(workingDirectory, testFileDirectory, peopleTableFileName);
        try {
            Files.deleteIfExists(peopleTableFilePath); // Deletes the file if it exists
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while deleting the file.");
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        bufferManager = mock(BufferManagerImpl.class);
        randomAccessFile = mock(RandomAccessFile.class);
        path1 = mock(Path.class);
        page = mock(Page.class);

        Path path = Paths.get(workingDirectory + testFileDirectory + fileName);
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

        Path movieIdPath = Paths.get(workingDirectory + testFileDirectory + movieIdIndexFileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieIdPath);
            System.out.println("File created: " + movieIdPath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieIdPath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }

        Path movieTitlePath = Paths.get(workingDirectory + testFileDirectory + movieTitleIndexFileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(movieTitlePath);
            System.out.println("File created: " + movieTitlePath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(movieTitlePath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }

        Path workedOnFilePath = Paths.get(workingDirectory + testFileDirectory + workedOnFileName);
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

        Path peopleTablePath = Paths.get(workingDirectory + testFileDirectory + peopleTableFileName);
        try {
            // Create an empty file if it doesn't exist
            Files.createFile(peopleTablePath);
            System.out.println("File created: " + peopleTablePath.toAbsolutePath());
        } catch (IOException e) {
            if (Files.exists(peopleTablePath)) {
                System.out.println("File already exists.");
            } else {
                System.err.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        }

    }

    @Test
    public void testSingleInsertAndSearch() throws Exception {
        BufferManagerImpl bufferManager = new BufferManagerImpl(4*4096, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleTableFileName);

        BTreeImpl index = new BTreeImpl(bufferManager, 1, File.MOVIE_ID_IDX);

        index.insert("key1", new Rid(1, 2));
        Iterator<Rid> rids = index.search("key1");

        assertTrue(rids.hasNext());
        Rid rid = rids.next();
        assertEquals(rid.getPageId(), 1);
        assertEquals(rid.getSlotId(), 2);
        assertFalse(rids.hasNext());

        // check that searching by a non-existant key returns nothing

        Iterator<Rid> rids2 = index.search("key2");
        assertFalse(rids2.hasNext());
    }

    @Test
    public void testSplitOfLeafNode() throws Exception {
        BufferManagerImpl bufferManager = new BufferManagerImpl(4*4096, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleTableFileName);

        BTreeImpl index = new BTreeImpl(bufferManager, 1, File.MOVIE_ID_IDX);
        index.insert("key1", new Rid(0, 0));
        index.insert("key2", new Rid(0, 1));
        index.insert("key3", new Rid(0 ,2));

        // leaf node should have split now
        assertEquals(bufferManager.bufferPool.size(), 3);

        // check if node with ID 2 is root
        Page root = bufferManager.getPage(2, File.MOVIE_ID_IDX);
        bufferManager.unpinPage(root.getPid(), File.MOVIE_ID_IDX);

        Row rootMeta = root.getRow(0);
        int parent = parseIntFromByteArray(rootMeta.getTitle(), 0);
        assertEquals(parent, -1);

        // check key, and pointers
        Row firstPointerRow = root.getRow(1);
        Row secondPointerRow = root.getRow(2);

        assertEquals(new String(firstPointerRow.getTitle()).trim(), "key2");
        assertEquals(parseIntFromByteArray(firstPointerRow.getMovieId(), 0), 0);
        assertEquals(parseIntFromByteArray(secondPointerRow.getMovieId(), 0), 1);

        // check leaf nodes have the proper keys

        Page leaf0 = bufferManager.getPage(0, File.MOVIE_ID_IDX);
        Page leaf1 = bufferManager.getPage(1, File.MOVIE_ID_IDX);

        bufferManager.unpinPage(leaf0.getPid(), File.MOVIE_ID_IDX);
        bufferManager.unpinPage(leaf1.getPid(), File.MOVIE_ID_IDX);


        Row leaf0KeyRow = leaf0.getRow(1);
        assertEquals(new String(leaf0KeyRow.getTitle()).trim(), "key1");
        Row leaf1KeyRow1 = leaf1.getRow(1);
        assertEquals(new String(leaf1KeyRow1.getTitle()).trim(), "key2");
        Row leaf1KeyRow2 = leaf1.getRow(2);
        assertEquals(new String(leaf1KeyRow2.getTitle()).trim(), "key3");
    }

    @Test
    public void testLargeInsertionSearchAndRangeSearch() throws Exception {
        BufferManagerImpl bufferManager = new BufferManagerImpl(20*4096, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleTableFileName);

        BTreeImpl index = new BTreeImpl(bufferManager, 5, File.MOVIE_ID_IDX);

        // insert a bunch of random records
        int pageId = 0;
        int slotId = 0;
        for (int i = 0; i < 1000; ++i) {
            index.insert("" + i, new Rid(pageId, slotId));
            slotId += 1;
            if (slotId == 105) {
                slotId = 0;
                pageId += 1;
            }
        }

        // search every ID and see if it returns the expected RID
        for (int i = 0; i < 1000; ++i) {
            int curPageId = i / 105;
            int curSlotId = i % 105;

            Iterator<Rid> rid = index.search("" + i);
            Rid curRid = rid.next();
            assertEquals(curRid.getPageId(), curPageId);
            assertEquals(curRid.getSlotId(), curSlotId);
        }

        Iterator<Rid> rids = index.rangeSearch("" + 32, "" + 765);

        String[] keys = new String[1000];

        for (int i = 0; i < 1000; ++i) {
            keys[i] = "" + i;
        }

        int test = 2;
        Arrays.sort(keys);

        int curIndex = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i].equals("32")) {
                curIndex = i;
                break;
            }
        }
        while (rids.hasNext()) {
            Rid nextRid = rids.next();
            String comparisonKey = keys[curIndex];
            int curIntegerValue = Integer.parseInt(comparisonKey);
            int curPageId = curIntegerValue / 105;
            int curSlotId = curIntegerValue % 105;
            assertEquals(nextRid.getPageId(), curPageId);
            assertEquals(nextRid.getSlotId(), curSlotId);
            curIndex += 1;
        }

    }

    // Helper method for testing purposes
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

    // Test find root. Find root assumes that pages have been written to the disk file via "force".
    // Note, subsequent indexes should have the same order.
    @Test
    public void testFindRoot() throws Exception {
        BufferManagerImpl bufferManager = new BufferManagerImpl(20*4096, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleTableFileName);

        BTreeImpl index = new BTreeImpl(bufferManager, 1, true, File.MOVIE_ID_IDX);

        // write entries to index
        index.insert("key1", new Rid(1, 1));
        index.insert("key2", new Rid(1, 2));
        index.insert("key3", new Rid(1, 3));

        bufferManager.force();

        BTreeImpl index2 = new BTreeImpl(bufferManager, 1, File.MOVIE_ID_IDX);

        // searching on key3 should find that the entry exists on page with index 1. Page with index 0 will be in when the root is found,
        // but the evict page should remove it, and the search should never find it, indicating it is no longer the root
        bufferManager.evictPage();
        Iterator<Rid> rids = index2.search("key3");


        Rid rid = rids.next();
        assertEquals(rid.getPageId(), 1);
        assertEquals(rid.getSlotId(), 3);

        assertFalse(bufferManager.bufferPool.containsKey("MOVIE_ID_IDX-0"));
    }

    @Test
    public void testPopulateIndexAndBulkLoadIndex() throws Exception {
        BufferManagerImpl bufferManager = new BufferManagerImpl(20*4096, testFilePath, fileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleTableFileName);

        Page newPage = bufferManager.createPage(File.DISK);

        String[] keysToInsert = new String[110];

        for (int i = 0; i < keysToInsert.length; ++i) {
            keysToInsert[i] = "" + i + i;
        }

        Arrays.sort(keysToInsert);

        for (int i = 0; i < 105; ++i) {
            newPage.insertRow(new Row(keysToInsert[i].getBytes(), ("" + i + i + i + i).getBytes(), null, null, null));
        }
        bufferManager.unpinPage(newPage.getPid(), File.DISK);
        Page evenNewerPage = bufferManager.createPage(File.DISK);
        for (int i = 105; i < 110; ++i) {
            evenNewerPage.insertRow(new Row(keysToInsert[i].getBytes(),  ("" + i + i + i + i).getBytes(), null, null, null));
        }
        bufferManager.unpinPage(evenNewerPage.getPid(), File.DISK);
        bufferManager.force();

        BTreeImpl index = new BTreeImpl(bufferManager, 2, File.MOVIE_TITLE_IDX);

        index.populateIndex();

        for (int i = 0; i < 110; ++i) {
            Iterator<Rid> rids = index.search(("" + i + i + i + i));
            assertTrue(rids.hasNext());
            rids.next();
            assertFalse(rids.hasNext());
        }

        BTreeImpl bulkIndex = new BTreeImpl(bufferManager, 2, File.MOVIE_ID_IDX);

        bulkIndex.bulkLoad();

        for (int i = 0; i < 110; ++i) {
            Iterator<Rid> rids = bulkIndex.search("" + i + i);
            assertTrue(rids.hasNext());
            rids.next();
            assertFalse(rids.hasNext());
        }
    }

}
