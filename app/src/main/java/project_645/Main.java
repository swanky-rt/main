package project_645;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            String filePath = System.getProperty("user.dir") + "/app/src/main/java/project_645/DB files/";
            BufferManagerImpl bufferManager = new BufferManagerImpl(4 * 4096, filePath, "testdb.dat", "title.basics.csv");
            Utilities utilities = new Utilities();
            utilities.loadDataset(bufferManager, filePath);
            System.out.println("Hello World!");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}