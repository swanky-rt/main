package project_645;

public class RunQuery {

    public static void main(String[] args) throws Exception {
        // Ensure arguments for startRange, endRange, and bufferSize are passed
        if (args.length < 3) {
            System.err.println("Usage: java RunQuery <start_range> <end_range> <buffer_size>");
            return;
        }

        String startRange = args[0];
        String endRange = args[1];
        int bufferSize = Integer.parseInt(args[2]);
        boolean useIndex = false;
        if (args.length >= 4) {
            useIndex = Boolean.parseBoolean(args[3]);
        }

        // Create and run the query
        QueryExecutor executor = new QueryExecutor();
        executor.executeQuery(startRange, endRange, bufferSize, true, useIndex);  // Execute the query
    }
}
