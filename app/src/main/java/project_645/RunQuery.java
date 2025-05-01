//package project_645;
//
//public class RunQuery {
//
//    public static void main(String[] args) throws Exception {
//        // Ensure arguments for startRange, endRange, and bufferSize are passed
////        if (args.length < 3) {
////            System.err.println("Usage: java RunQuery <start_range> <end_range> <buffer_size>");
////            return;
////        }
//
//        //String startRange = args[0];
//        //String endRange = args[1];
//       // int bufferSize = Integer.parseInt(args[2]);
//        System.out.println("path test");
//
//                    // define parameters to our various objects
//            String path = "/main/app/src/main/java/project_645/DB files/";
//            String mainFileName = "title.basics.tsv";
//            String diskFileName = "testdb.dat";
//            String movieIdIndexFileName = "movieIdIndex.dat";
//            String movieTitleIndexFileName = "movieTitleIndex.dat";
//            String filePath = System.getProperty("user.dir") + path;
//            String workedOnFileName = "name.basics.tsv";
//            String peopleFileName = "title.principals.tsv";
//
//        // Initialize BufferManagerImpl
////        BufferManagerImpl bufferManager = new BufferManagerImpl(
////                bufferSize,
////                filePath,
////                diskFileName,
////                movieIdIndexFileName,
////                movieTitleIndexFileName
////        );
//        BufferManagerImpl bufferManager = new BufferManagerImpl(1000 * 4096, filePath, diskFileName, movieIdIndexFileName, movieTitleIndexFileName, workedOnFileName, peopleFileName);
//
//        // Create and run the query
//        QueryExecutor executor = new QueryExecutor();
//        executor.executeQuery("clown et ses chiens", "Un bon bock", 500*4096);  // Execute the query
//    }
//}
