package project_645;

public class RunQuery {

    public static void main(String[] args) throws Exception {
        // Ensure arguments for startRange, endRange, and bufferSize are passed
        if (args.length < 4) {
            System.err.println("Usage: java RunQuery <start_range> <end_range> <buffer_size> <file_name> (optional) <useIndex>");
            return;
        }

        String startRange = args[0];
        String endRange = args[1];
        int bufferSize = Integer.parseInt(args[2]);
        String fileName = args[3];
        boolean useIndex = false;
        if (args.length >= 5) {
            useIndex = Boolean.parseBoolean(args[4]);
        }

        // Create and run the query
        QueryExecutor executor = new QueryExecutor();
        executor.executeQuery(startRange, endRange, bufferSize, false,"C1results.csv", useIndex);  // Execute the query
    }
}