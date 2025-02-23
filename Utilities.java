import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities implements Serializable {

    private static final int BUFFER_SIZE = 4 * 4096;
    public int currentPageID = 0;
    private static String filepath = "/../../Users/amitkumar/Downloads/DBtest/";

    // Loads the buffer manager with the imdb dataset
    public void loadDataset(BufferManager bf, String filepath) throws IOException {
        BufferManagerImpl bufferManager = new BufferManagerImpl(BUFFER_SIZE);
        Page currentPage = bufferManager.getPage(currentPageID) != null ? bufferManager.getPage(currentPageID) : bufferManager.createPage();
        BufferedReader reader = new BufferedReader(new FileReader(filepath + "title.basics.tsv"));
        String line;
        while ((line = reader.readLine()) != null && bufferManager.isBufferPoolFull().equals(Boolean.FALSE)) {
            if (bufferManager.pageMap.containsKey(currentPage)) {
                currentPageID = bufferManager.pageMap.get(currentPage);
            }
            String[] columns = line.split("\t");
            byte[] title = columns[0].getBytes();
            byte[] movieId = columns[2].getBytes();
            Row row = new Row(movieId, title);
            if (currentPage.isFull()) {
                writePageToDisk(currentPageID, currentPage);
                bufferManager.bufferPool.put(currentPageID, currentPage);
                currentPage = bufferManager.createPage();
            }
            currentPage.insertRow(row);
            bufferManager.markDirty(currentPageID);

        }
        reader.close();
    }

    public void writePageToDisk(int pageId, Page page) {
        try {
            Path path = Paths.get(filepath + pageId + ".dat");
            if (Files.exists(path)) {
                System.out.println("file exists in the disk already, updating it.." + path);
                try (ObjectOutputStream outputStream = new AppendableObjectOutputStream(Files.newOutputStream(path))) {
                    outputStream.writeObject(page);
                }
            } else {
                ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(filepath + pageId + ".dat")));
                outputStream.writeObject(page);
                outputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Writing to the disk is failing due to this error" + e.getMessage());
        }
    }

    public Page loadPageFromDisk(int pageId) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(Paths.get(String.valueOf(filepath + pageId + ".dat"))));
            Page page = (Page) inputStream.readObject();
            inputStream.close();
            return page;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.out.println("Writing to the disk is failing due to this error" + e.getMessage());
        }
        return null;
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
}
