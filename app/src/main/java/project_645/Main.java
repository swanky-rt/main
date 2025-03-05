package project_645;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
//            String filePath = "./DB files/";
//            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096);
            Utilities utilities = new Utilities();
//            utilities.loadDataset(bufferManager, filePath);
//            int testPageId = 2;
//            bufferManager.getPage(testPageId);
            // utilities.populateDisk(500);
            Page testPage = utilities.loadPageFromDisk(4);

            System.out.println(testPage.getAllRows().length);
            testPage.deserializeRows();
            for (int i = 0; i < testPage.getAllRows().length; ++i) {
                String test1 = new String(testPage.getAllRows()[i].movieId, StandardCharsets.US_ASCII);
                String test2 = new String(testPage.getAllRows()[i].title, StandardCharsets.US_ASCII);

                String test3 = testPage.getDeserializedRows()[i][0];
                String test4 = testPage.getDeserializedRows()[i][1];

                System.out.println(test3 + ", " + test4);
            }

        } catch (Exception e) {

        }
//        } catch (IOException e){
//            System.out.println(e.getMessage());
//        }
    }
}