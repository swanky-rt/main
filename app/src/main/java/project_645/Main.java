package project_645;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            String filePath = "./DB files/";
            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096);
            Utilities utilities = new Utilities();
            utilities.loadDataset(bufferManager, filePath);

        } catch (Exception e) {
            int test = 2;
        }
//        } catch (IOException e){
//            System.out.println(e.getMessage());
//        }
    }
}