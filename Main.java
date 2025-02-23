import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            String filePath = "/../../Users/amitkumar/Downloads/";
            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096);
            Utilities utilities = new Utilities();
            utilities.loadDataset(bufferManager, filePath);
            int testPageId = 2;
            bufferManager.getPage(testPageId);

        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}