package project_645;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BNLJoinOperator implements Operator {
    private final Operator outer;
    private final Operator inner;
    private final String outerJoinKey;
    private final String innerJoinKey;
    private final HashMap<String, List<Rid>> hashTable;
    private boolean outerExhausted;

    public BNLJoinOperator(Operator outer, Operator inner, String outerJoinKey, String innerJoinKey) {
        this.outer = outer;
        this.inner = inner;
        this.outerJoinKey = outerJoinKey;
        this.innerJoinKey = innerJoinKey;
        this.hashTable = new HashMap<>();
        this.outerExhausted = false;
    }

    @Override
    public void open() {
        outer.open();
        inner.open();
    }

    @Override
    public boolean hasNext() {
        return !outerExhausted; // Continue if outer operator still has data
    }

    @Override
    public Record next() {
        if (outerExhausted) {
            return null;
        }

        while (outer.hasNext()) {
            // Process outer record and join with inner records
            Record outerRecord = outer.next();
            if (outerRecord != null) {
                String outerKey = outerRecord.getMovieId();  // Get the join key from outer table

                // Convert outerKey (byte[]) to Rid
                Rid outerRid = new Rid(byteArrayToInt(outerRecord.getRawKey(), 0), byteArrayToInt(outerRecord.getRawKey(), 4));
                if (!hashTable.containsKey(outerKey)) {
                    hashTable.put(outerKey, new ArrayList<>());
                }
                hashTable.get(outerKey).add(outerRid); // Store the RID in the hash table

                while (inner.hasNext()) {
                    Record innerRecord = inner.next();
                    String innerKey = innerRecord.getMovieId();  // Get the join key from inner table

                    if (hashTable.containsKey(innerKey)) {
                        System.out.println("Found matching RIDs for innerKey: " + innerKey);
                        List<Rid> matchedRids = hashTable.get(innerKey);
                        for (Rid matchedRid : matchedRids) {
                            System.out.println("Matched RID: " + matchedRid);
                            // Create a combined Row for the join result
                            String combinedMovieId = outerRecord.getMovieId() + "_" + innerRecord.getMovieId();
                            String combinedTitle = outerRecord.getTitle() + " " + innerRecord.getTitle();

                            // Pass null for personId, category, and name (not used in the join result)
                            Row combinedRow = new Row(
                                    combinedMovieId.getBytes(),
                                    combinedTitle.getBytes(), null, null, null
                            );

                            // Return the combined record
                            return new Record(combinedRow, null, null, null);  // Pass null for unused fields
                        }
                    }
                }
            }

            outerExhausted = true;
            return null;
        }
        return null;
    }

    @Override
    public void close() {
        outer.close();
        inner.close();
    }

    // Helper method to convert byte array to int
    private int byteArrayToInt(byte[] byteArray, int offset) {
        return (byteArray[offset] & 0xFF) << 24
                | (byteArray[offset + 1] & 0xFF) << 16
                | (byteArray[offset + 2] & 0xFF) << 8
                | (byteArray[offset + 3] & 0xFF);
    }
}
