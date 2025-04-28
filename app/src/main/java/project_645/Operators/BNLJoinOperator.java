package project_645.Operators;

import project_645.*;
import project_645.Record;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BNLJoinOperator implements Operator {
    private final Operator outer;
    private final Operator inner;
    private final ColumnNames outerJoinKey;
    private final ColumnNames innerJoinKey;
    private HashMap<String, List<Rid>> hashTable;
    private Page currentOuterBnlPage;
    private BufferManager bufferManager;
    File bnlRelation;
    private boolean outerExhausted;
    private int freeBufferPoolFrames;
    private boolean getNextOuterBlock = true;
    private int curListIdx = 0;
    private Record curInnerRecord;
    private boolean getNextRecord = true;
    private boolean firstNext = true;

    public BNLJoinOperator(Operator outer, Operator inner, ColumnNames outerJoinKey, ColumnNames innerJoinKey,
                           BufferManagerImpl bufferManager, File bnlRelation) {
        this.outer = outer;
        this.inner = inner;
        this.outerJoinKey = outerJoinKey;
        this.innerJoinKey = innerJoinKey;
        this.hashTable = new HashMap<>();
        this.outerExhausted = false;
        this.bufferManager = bufferManager;
        this.bnlRelation = bnlRelation;
        this.freeBufferPoolFrames = (bufferManager.MAX_PAGE - 100) / 2;
    }

    @Override
    public void open() throws Exception {
        outer.open();
        inner.open();
    }

    @Override
    public boolean hasNext() {
        return !outerExhausted; // Continue if outer operator still has data
    }

    @Override
    public Record next() throws Exception {
        if (outerExhausted) {
            return null;
        }

        // Process outer record and join with inner records
        int curInnerCount = 0;
        while (true) {
            if (!hashTable.isEmpty() || firstNext) {
                firstNext = false;
                if (getNextOuterBlock) {
                    getNextOuterBlock();
                    getNextOuterBlock = false;
                }
                if (getNextRecord) {
                    curInnerCount = curInnerCount + 1;
//                    if (curInnerCount % 500000 == 0) {
//                        System.out.println(curInnerCount);
//                    }
                    curInnerRecord = inner.next();
                    getNextRecord = false;
                }
                if (curInnerRecord != null) {
                    String key = new String(getRecordKey(curInnerRecord, innerJoinKey)).trim();
                    List<Rid> curInnerRids = hashTable.getOrDefault(key, null);
                    Rid curRid = curInnerRids != null ? curInnerRids.get(curListIdx++) : null;
                    if (curInnerRids == null || curListIdx == curInnerRids.size() || curRid == null) {
                        getNextRecord = true;
                        curListIdx = 0;
                    }
                    if (curRid == null) {
                        continue;
                    }
                    return createJoinedRecord(curInnerRecord, curRid);
                }
                getNextOuterBlock = true;
                getNextRecord = true;

                for (List<Rid> curRids : hashTable.values()) {
                    for (Rid curRid : curRids) {
                        bufferManager.unpinPage(curRid.getPageId(), bnlRelation);
                    }
                }
                inner.makeResetOperatorTrue();
            }
            else {
                break;
            }
        }
        outerExhausted = true;
        return null;

    }

    private void getNextOuterBlock() throws Exception {
        PageImpl bnlRelationPage = new PageImpl(-1, bnlRelation);
        Page curPage = null;
        int numTuples = freeBufferPoolFrames * bnlRelationPage.MAX_TUPLES;
        boolean createPage = true;
        hashTable = new HashMap<>();
//        if (outerExhausted) {
//            return;
//        }
        for (int i = 0; i < numTuples; ++i) {
            Record nextOuterRecord = outer.next();
            if (nextOuterRecord == null) {
                // outerExhausted = true;
                return;
            }
            if (createPage) {
                curPage = bufferManager.createPage(bnlRelation);
                createPage = false;
            }
            Row curRow = nextOuterRecord.getRow();
            int curRecordCount = curPage.getRowCount();
            String key = new String(getRecordKey(nextOuterRecord, outerJoinKey)).trim();
            Rid curRid = new Rid(curPage.getPid(), curRecordCount);
            curPage.insertRow(curRow);
            curPage.markNotDirty();
            if (hashTable.containsKey(key)) {
                List<Rid> curList = hashTable.get(key);
                curList.add(curRid);
            }
            else {
                hashTable.put(key, new ArrayList<>(List.of(curRid)));
            }
            if (curPage.isFull()) {
                createPage = true;
            }
        }
        this.currentOuterBnlPage = bufferManager.createPage(bnlRelation);
    }

    private Record createJoinedRecord(Record innerRecord, Rid outerRid) throws Exception {
        long pageId = outerRid.getPageId();
        int slotId = outerRid.getSlotId();

        Page joinRecordPage = bufferManager.getPage(pageId, bnlRelation);
        Row curRowRecord = joinRecordPage.getRow(slotId);
        switch (bnlRelation) {
            case BNL1:
                Row returnRow2 = new Row(curRowRecord.getMovieId(), curRowRecord.getTitle(), innerRecord.getPersonIdBytes(), true);
                Record returnRecord2 = new Record(returnRow2, null, null, null, null);
                return returnRecord2;
            case BNL2:
                Row returnRow = new Row(curRowRecord.getMovieId(), curRowRecord.getTitle(), curRowRecord.getPersonId(), innerRecord.getNameBytes());
                Record returnRecord = new Record(returnRow, null, null, null, null);
                return returnRecord;
            default:
                return null;
        }
    }

    @Override
    public void close() {
        outer.close();
        inner.close();
    }

    @Override
    public File getRelation() {
        return bnlRelation;
    }
    @Override
    public void makeResetOperatorTrue() {
        return;
    }

    public byte[] getRecordKey(Record record, ColumnNames columnName) {
        return switch (columnName) {
            case ColumnNames.TITLE -> record.getMovieTitleBytes();
            case ColumnNames.NAME -> record.getNameBytes();
            case ColumnNames.CATEGORY -> record.getCategoryBytes();
            case ColumnNames.MOVIEID -> record.getMovieIdBytes();
            case ColumnNames.PERSONID -> record.getPersonIdBytes();
            default -> null;
        };
    }

    // Helper method to convert byte array to int
    private int byteArrayToInt(byte[] byteArray, int offset) {
        return (byteArray[offset] & 0xFF) << 24
                | (byteArray[offset + 1] & 0xFF) << 16
                | (byteArray[offset + 2] & 0xFF) << 8
                | (byteArray[offset + 3] & 0xFF);
    }
}
