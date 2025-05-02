package project_645;

public class RunQuery {

    public static void main(String[] args) throws Exception {
        // Ensure arguments for startRange, endRange, and bufferSize are passed
        if (args.length < 3) {
            System.err.println("Usage: java RunQuery <start_range> <end_range> <buffer_size> <relative_path_to_csv>");
            return;
        }

        String startRange = args[0];
        String endRange = args[1];
        int bufferSize = Integer.parseInt(args[2]);
        String csvPath = null;
        if (args.length >= 4) {
            csvPath = new String(args[3]);
        }

        // Create and run the query
        QueryExecutor executor = new QueryExecutor();
        executor.executeQuery(startRange, endRange, bufferSize, true, csvPath);  // Execute the query
    }
}