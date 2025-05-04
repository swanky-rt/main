package project_645;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CompareFiles {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java CompareFiles <absolute/path/to/our/generated/CSV> </absolute/path/to/postgres/CSV>");
            return;
        }

        ArrayList<String> file1Strings = new ArrayList<>();
        ArrayList<String> file2Strings = new ArrayList<>();
        BufferedReader workedOnTableReader = new BufferedReader(new FileReader(args[0]));
        String curLine;
        while ((curLine = workedOnTableReader.readLine()) != null) {
            file1Strings.add(curLine);
        }

        BufferedReader workedOnTableReaderPostgres = new BufferedReader(new FileReader(args[1]));
        while ((curLine = workedOnTableReaderPostgres.readLine()) != null) {
            file2Strings.add(curLine);
        }
        for (int i = 0; i < file2Strings.size(); ++i) {
            String postgresResultsTitle = file2Strings.get(i).split(",")[0];
            String postgresResultsName = file2Strings.get(i).split(",")[1];
            String PostgresResultsSubstring = postgresResultsTitle.substring(0, Math.min(postgresResultsTitle.length(), 30)).trim();
            String str2 = PostgresResultsSubstring+ "," + postgresResultsName;
            file2Strings.set(i, str2);
        }
        // file1Strings.removeFirst();
        file2Strings.removeFirst();
        Collections.sort(file1Strings);
        Collections.sort(file2Strings);


        if (file1Strings.size() != file2Strings.size()) {
            System.out.println("The queries did not return the same number of entries");
        }
        for (int i = 0; i < file1Strings.size(); ++i) {
            if (!file1Strings.get(i).equals(file2Strings.get(i))) {
                String curStr1 = file1Strings.get(i);
                String curStr2 = file2Strings.get(i);
                System.out.println("Record " + i + " does not match. ");
            }
        }

        System.out.println("All records match");
    }
}
