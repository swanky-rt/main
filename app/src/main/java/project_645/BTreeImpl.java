package project_645;

import java.util.*;


public class BTreeImpl implements BTree<String, Rid> {


    private static class BTreeMetadata {
        int rootPageId;   // page id of the root node
        int order;// approximate fan out
        boolean isRootLeaf;
    }
    private final BufferManager bufferMgr;
    private final BTreeMetadata metadata;
    private final int maxKeysLeaf;
    private final int maxKeysInternal;
    private final File dataFile;

//B+ tree constructor to start creating tree, if the createNew=true

    public BTreeImpl(BufferManager bufferMgr, int order, boolean createNew, File dataFile) throws Exception {
        this.bufferMgr = bufferMgr;
        this.metadata = new BTreeMetadata();
        this.metadata.order = order;
        this.maxKeysLeaf = 2 * order;
        this.maxKeysInternal = 2 * order;
        this.dataFile = dataFile;

        if (createNew) {
            // Create a new root page as a leaf node.
            Page rootPage = bufferMgr.createPage(dataFile);
            int rootPid = rootPage.getPid();
            this.metadata.rootPageId = rootPid;
            this.metadata.isRootLeaf = true;
            initLeafPage(rootPage, -1, -1, 0);
            bufferMgr.markDirty(rootPid, dataFile);
            bufferMgr.unpinPage(rootPid, dataFile);
        } else {
            // For simplicity, assume root is page 0.
            this.metadata.rootPageId = findRoot(0);
            this.metadata.isRootLeaf = isLeafNode(this.metadata.rootPageId);
        }
    }

    //Constructor to create the new B+tree object
    public BTreeImpl(BufferManager bufferMgr, int order, File dataFile) throws Exception {
        this.bufferMgr = bufferMgr;
        this.metadata = new BTreeMetadata();
        this.metadata.order = order;
        this.maxKeysLeaf = 2 * order;
        this.maxKeysInternal = 2 * order;
        this.dataFile = dataFile;

        if (bufferMgr.getFileSizeOfChosenFile(dataFile) == 0) {
            // Create a new root page as a leaf node.
            Page rootPage = bufferMgr.createPage(dataFile);
            int rootPid = rootPage.getPid();
            this.metadata.rootPageId = rootPid;
            this.metadata.isRootLeaf = true;
            initLeafPage(rootPage, -1, -1, 0);
            bufferMgr.markDirty(rootPid, dataFile);
            bufferMgr.unpinPage(rootPid, dataFile);
        } else {
            // For simplicity, assume root is page 0.
            this.metadata.rootPageId = findRoot(0);
            this.metadata.isRootLeaf = isLeafNode(this.metadata.rootPageId);
        }
    }


//method to insert the key and rid
    @Override
    public void insert(String key, Rid rid) {
        int leafPid = findLeafPageId(key.getBytes());
        byte[] arrRid = new byte[9];
        storeIntInByteArray(rid.getPageId(), arrRid, 0);
        storeIntInByteArray(rid.getSlotId(), arrRid, 3);
        insertIntoLeaf(leafPid, padByteArrayToLength(key.getBytes(), 30), arrRid);
    }

//method to search the key and rid
    @Override
    public Iterator<Rid> search(String key) {
        List<Rid> results = new ArrayList<>();
        boolean flag = true;
        int curLeafPid = findLeafPageId(padByteArrayToLength(key.getBytes(), 30));
        while (flag) {
            List<Rid> tempResults = searchInLeaf(curLeafPid, padByteArrayToLength(key.getBytes(), 30));
            results.addAll(tempResults);
            curLeafPid = getNextLeafId(curLeafPid);
            flag = !tempResults.isEmpty() && curLeafPid != -1;
        }

        return results.iterator();
    }

//Method to do the range search for start key and end key
    @Override
    public Iterator<Rid> rangeSearch(String startKey, String endKey) {
        List<Rid> results = new ArrayList<>();
        int leafPid = findLeafPageId(startKey.getBytes());
        int currentPid = leafPid;
        //byte[] testBytes = startKey.getBytes();
        //byte[] testEndBytes = endKey.getBytes();
        while (currentPid != -1) {
            List<byte[]> leafKeys = new ArrayList<>();
            List<byte[]> leafRids = new ArrayList<>();
            readLeafNode(currentPid, leafKeys, leafRids);
            for (int i = 0; i < leafKeys.size(); i++) {
                byte[] k = leafKeys.get(i);
                if (Arrays.compare(k, padByteArrayToLength(startKey.getBytes(), 30)) >= 0 && Arrays.compare(k,
                        padByteArrayToLength(endKey.getBytes(), 30)) <= 0) {

                    results.add(new Rid(parseIntFromByteArray(leafRids.get(i), 0), parseIntFromByteArray(leafRids.get(i), 3)));
                } else if (Arrays.compare(k, padByteArrayToLength(endKey.getBytes(), 30)) > 0) {
                    break;
                }
            }
            int nextLeaf = getNextLeafId(currentPid);
            bufferMgr.unpinPage(currentPid, dataFile);
            if (!leafKeys.isEmpty() && Arrays.compare(leafKeys.get(leafKeys.size()-1), padByteArrayToLength(endKey.getBytes(), 30)) > 0) {
                break;
            }
            currentPid = nextLeaf;
        }
        return results.iterator();
    }

//method to insert key and corresponding rid into leaf page
    private Page insertIntoLeaf(int leafPid, byte[] key, byte[] rid) {
        try {
            List<byte[]> keys = new ArrayList<>();
            List<byte[]> rids = new ArrayList<>();
            readLeafNode(leafPid, keys, rids);
            Page returnPage = null;
            int pos = 0;
            while (pos < keys.size() && Arrays.compare(key, keys.get(pos)) > 0) {
                pos++;
            }
            keys.add(pos, key);
            rids.add(pos, rid);
            if (keys.size() > maxKeysLeaf) {
                int mid = keys.size() / 2;
                Page siblingPage = bufferMgr.createPage(dataFile);
                int siblingPid = siblingPage.getPid();
                bufferMgr.unpinPage(siblingPid, dataFile);
                List<byte[]> siblingKeys = new ArrayList<>();
                List<byte[]> siblingRids = new ArrayList<>();
                for (int i = mid; i < keys.size(); i++) {
                    siblingKeys.add(keys.get(i));
                    siblingRids.add(rids.get(i));
                }
                for (int i = keys.size() - 1; i >= mid; i--) {
                    keys.remove(i);
                    rids.remove(i);
                }
                int oldNext = getNextLeafId(leafPid);
                initLeafPage(siblingPage, getParentId(leafPid), oldNext, siblingKeys.size());
                writeLeafKeysAndRids(siblingPid, siblingKeys, siblingRids);
                setNextLeafId(leafPid, siblingPid);
                writeLeafKeysAndRids(leafPid, keys, rids);
                bufferMgr.markDirty(siblingPid, dataFile);
                bufferMgr.unpinPage(siblingPid, dataFile);
                bufferMgr.markDirty(leafPid, dataFile);
                bufferMgr.unpinPage(leafPid, dataFile);
                returnPage = siblingPage;
                if (leafPid == metadata.rootPageId && metadata.isRootLeaf) {
                    createNewRoot(leafPid, siblingPid, siblingKeys.get(0));
                } else {
                    insertInParent(leafPid, siblingPid, siblingKeys.get(0));
                }
                bufferMgr.markDirty(siblingPid, dataFile);
                bufferMgr.unpinPage(siblingPid, dataFile);
            } else {
                writeLeafKeysAndRids(leafPid, keys, rids);
            }
            bufferMgr.markDirty(leafPid, dataFile);
            bufferMgr.unpinPage(leafPid, dataFile);
            return returnPage;
        } catch (Exception e) {
            System.err.println("Error in insertIntoLeaf: " + e.getMessage());
        }
        return null;
    }

//method to create new root if insertion is splitting the page
    private void createNewRoot(int oldRootPid, int siblingPid, byte[] splitKey) throws Exception {
        Page newRootPage = bufferMgr.createPage(dataFile);
        int newRootPid = newRootPage.getPid();
        initInternalPage(newRootPage, -1, 1);
        List<byte[]> keys = new ArrayList<>();
        List<Integer> children = new ArrayList<>();
        keys.add(splitKey);
        children.add(oldRootPid);
        children.add(siblingPid);
        writeInternalKeysAndChildren(newRootPid, keys, children);
        metadata.rootPageId = newRootPid;
        metadata.isRootLeaf = false;
        setParentId(oldRootPid, newRootPid);
        setParentId(siblingPid, newRootPid);
        bufferMgr.markDirty(newRootPid, dataFile);
        bufferMgr.unpinPage(newRootPid, dataFile);
    }

//method to insert into parent
    private void insertInParent(int leftPid, int rightPid, byte[] splitKey) throws Exception {
        int parentPid = getParentId(leftPid);
        if (parentPid == -1) {
            createNewRoot(leftPid, rightPid, splitKey);
            return;
        }
        List<byte[]> keys = new ArrayList<>();
        List<Integer> children = new ArrayList<>();
        readInternalNode(parentPid, keys, children);
        int pos = children.indexOf(leftPid);
        keys.add(pos, splitKey);
        children.add(pos + 1, rightPid);
        if (keys.size() > maxKeysInternal) {
            int mid = keys.size() / 2;
            Page newPage = bufferMgr.createPage(dataFile);
            int newPid = newPage.getPid();
            initInternalPage(newPage, getParentId(parentPid), 0);
            List<byte[]> siblingKeys = new ArrayList<>();
            List<Integer> siblingChildren = new ArrayList<>();
            byte[] upKey = keys.get(mid);
            for (int i = mid + 1; i < keys.size(); i++) {
                siblingKeys.add(keys.get(i));
            }
            for (int i = mid + 1; i < children.size(); i++) {
                siblingChildren.add(children.get(i));
            }
            for (int i = keys.size() - 1; i >= mid; i--) {
                keys.remove(i);
            }
            for (int i = children.size() - 1; i > mid; i--) {
                children.remove(i);
            }
            writeInternalKeysAndChildren(newPid, siblingKeys, siblingChildren);
            setNumKeys(newPid, siblingKeys.size());
            for (int c : siblingChildren) {
                setParentId(c, newPid);
            }
            writeInternalKeysAndChildren(parentPid, keys, children);
            setNumKeys(parentPid, keys.size());
            bufferMgr.markDirty(newPid, dataFile);
            bufferMgr.unpinPage(newPid, dataFile);
            bufferMgr.markDirty(parentPid, dataFile);
            bufferMgr.unpinPage(parentPid, dataFile);
            if (parentPid == metadata.rootPageId) {
                createNewRoot(parentPid, newPid, upKey);
            } else {
                insertInParent(parentPid, newPid, upKey);
            }
            bufferMgr.markDirty(newPid, dataFile);
            bufferMgr.unpinPage(newPid, dataFile);
        } else {
            writeInternalKeysAndChildren(parentPid, keys, children);
            setParentId(rightPid, parentPid);
        }
        bufferMgr.markDirty(parentPid, dataFile);
        bufferMgr.unpinPage(parentPid, dataFile);

    }


//method to find leaf page using key
    private int findLeafPageId(byte[] key) {
        int currentPid = metadata.rootPageId;
        while (true) {
            if (currentPid == metadata.rootPageId && metadata.isRootLeaf) {
                return currentPid;
            }
            if (isLeafNode(currentPid)) {
                return currentPid;
            } else {
                List<byte[]> keys = new ArrayList<>();
                List<Integer> children = new ArrayList<>();
                readInternalNode(currentPid, keys, children);
                int i = 0;
                while (i < keys.size() && Arrays.compare(padByteArrayToLength(key, 30), keys.get(i)) >= 0) {
                    i++;
                    if (Arrays.compare(padByteArrayToLength(key, 30), keys.get(i - 1)) == 0) {
                        break;
                    }
                }
                int childPid = children.get(i);
                bufferMgr.unpinPage(currentPid, dataFile);
                currentPid = childPid;
            }
        }
    }

//method to search in leaf page using leaf id and key byte
    private List<Rid> searchInLeaf(int leafPid, byte[] key) {
        List<byte[]> keys = new ArrayList<>();
        List<byte[]> rids = new ArrayList<>();
        readLeafNode(leafPid, keys, rids);
        List<Rid> result = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            if (Arrays.compare(keys.get(i), key) == 0) {
                result.add(new Rid(parseIntFromByteArray(rids.get(i), 0), parseIntFromByteArray(rids.get(i), 3)));
            }
        }
        bufferMgr.unpinPage(leafPid, dataFile);
        return result;
    }



    private void initLeafPage(Page page, int parentId, int nextLeafId, int numKeys) {
        PageImpl p = (PageImpl) page;
        p.setAllRows(new Row[p.MAX_TUPLES]);
        p.setRowCount(0);
        Row metaRow = new Row(new byte[9], new byte[30]);
        metaRow.movieId[0] = 'L';
        storeIntInByteArray(parentId, metaRow.title, 0);
        storeIntInByteArray(nextLeafId, metaRow.title, 3);
        storeIntInByteArray(numKeys, metaRow.title, 6);
        p.insertRow(metaRow); // row 0 stores metadata
    }

//method to read leaf page using leaf id and list of bytes
    private void readLeafNode(int pageId, List<byte[]> keysOut, List<byte[]> ridsOut) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.unpinPage(page.getPid(), dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            if (meta.movieId[0] != 'L') {
                throw new RuntimeException("Page " + pageId + " is not a leaf node!");
            }
            int numKeys = parseIntFromByteArray(meta.title, 6);
            keysOut.clear();
            ridsOut.clear();
            for (int i = 1; i <= numKeys; i++) {
                Row r = p.getRow(i);
                if (r == null) break;
                keysOut.add(r.title);
                ridsOut.add(r.movieId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in readLeafNode: " + e.getMessage());
        }
    }

//method to write leaf page using page id and list of bytes of keys and rids
    private void writeLeafKeysAndRids(int pageId, List<byte[]> keys, List<byte[]> rids) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.unpinPage(pageId, this.dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            Row[] replacementRowArray = new Row[105];
            replacementRowArray[0] = meta;
            p.setAllRows(replacementRowArray);
            storeIntInByteArray(keys.size(), meta.title, 6);
            // Write each key and RID starting at row 1.
            for (int i = 0; i < keys.size(); i++) {
                Row row = new Row(rids.get(i), keys.get(i));
                setRowAtIndex(p, i + 1, row);
            }
            // Update row count (1 meta row + one row per key)
            p.setRowCount(keys.size() + 1);
        } catch (Exception e) {
            throw new RuntimeException("Error in writeLeafKeysAndRids: " + e.getMessage());
        }
    }



    private void initInternalPage(Page page, int parentId, int numKeys) {
        PageImpl p = (PageImpl) page;
        p.setAllRows(new Row[p.MAX_TUPLES]);
        p.setRowCount(0);
        Row metaRow = new Row(new byte[9], new byte[30]);
        metaRow.movieId[0] = 'I';
        storeIntInByteArray(parentId, metaRow.title, 0);
        storeIntInByteArray(-1, metaRow.title, 3);
        storeIntInByteArray(numKeys, metaRow.title, 6);
        p.insertRow(metaRow);
    }

    private void readInternalNode(int pageId, List<byte[]> keysOut, List<Integer> childrenOut) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.unpinPage(pageId, this.dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            if (meta.movieId[0] != 'I') {
                throw new RuntimeException("Page " + pageId + " is not an internal node!");
            }
            int numKeys = parseIntFromByteArray(meta.title, 6);
            keysOut.clear();
            childrenOut.clear();
            // Rows 1..numKeys store child-pointer and key.
            for (int i = 1; i <= numKeys; i++) {
                Row row = p.getRow(i);
                if (row == null) break;
                int childPid = parseIntFromByteArray(row.movieId, 0);
                childrenOut.add(childPid);
                keysOut.add(row.title);
            }
            // Row numKeys+1 holds the final child pointer.
            Row lastRow = p.getRow(numKeys + 1);
            if (lastRow != null) {
                int lastChild = parseIntFromByteArray(lastRow.movieId, 0);
                childrenOut.add(lastChild);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in readInternalNode: " + e.getMessage());
        }
    }

    private void writeInternalKeysAndChildren(int pageId, List<byte[]> keys, List<Integer> children) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            Row[] newRows = new Row[105];
            newRows[0] = meta;
            p.setAllRows(newRows);
            storeIntInByteArray(keys.size(), meta.title, 6);
            int rowCountNeeded = keys.size() + 1;
            for (int i = 1; i <= keys.size(); i++) {
                Row row = new Row(new byte[9], keys.get(i - 1));
                storeIntInByteArray(children.get(i - 1), row.movieId, 0);
                setRowAtIndex(p, i, row);
            }
            // Final child pointer at row keys.size() + 1.
            Row lastRow = new Row(new byte[9], new byte[30]);
            storeIntInByteArray(children.get(keys.size()), lastRow.movieId, 0);
            setRowAtIndex(p, keys.size() + 1, lastRow);
            p.setRowCount(rowCountNeeded + 1);
            bufferMgr.unpinPage(p.getPid(), this.dataFile);
        } catch (Exception e) {
            throw new RuntimeException("Error in writeInternalKeysAndChildren: " + e.getMessage());
        }
    }


//method to check whether a node is leaf or not
    private boolean isLeafNode(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            char c = (char) meta.movieId[0];
            bufferMgr.unpinPage(pageId, dataFile);
            return (c == 'L');
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//method to get parent id from page id
    private int getParentId(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            int pid = parseIntFromByteArray(meta.title, 0);
            bufferMgr.unpinPage(pageId, dataFile);
            return pid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//method to set parent id using page id and parent id
    private void setParentId(int pageId, int parentId) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.markDirty(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(parentId, meta.title, 0);
            bufferMgr.unpinPage(pageId, dataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getNextLeafId(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.unpinPage(page.getPid(), dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            int nxt = parseIntFromByteArray(meta.title, 3);
            return nxt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setNextLeafId(int pageId, int nxtLeafId) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.markDirty(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(nxtLeafId, meta.title, 3);
            bufferMgr.unpinPage(pageId, dataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setNumKeys(int pageId, int numKeys) {
        try {
            Page page = bufferMgr.getPage(pageId, dataFile);
            bufferMgr.markDirty(pageId, dataFile);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(numKeys, meta.title, 6);
            bufferMgr.unpinPage(pageId, dataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void storeIntInByteArray(int val, byte[] arr, int offset) {
        //arr[offset]   = (byte) (val >>> 24);
        arr[offset+0] = (byte) (val >>> 16);
        arr[offset+1] = (byte) (val >>> 8);
        arr[offset+2] = (byte) (val);
    }

    private int parseIntFromByteArray(byte[] arr, int offset) {
        //int b1 = (arr[offset]   & 0xFF) << 24;
        int b2 = (arr[offset+0] & 0xFF) << 16;
        int b3 = (arr[offset+1] & 0xFF) << 8;
        int b4 = (arr[offset+2] & 0xFF);
        int returnVal = (b2 | b3 | b4);
        if (returnVal == 16777215) {
            return -1;
        }
        return (b2 | b3 | b4);
    }

    private void setRowAtIndex(PageImpl p, int index, Row row) {
        Row[] rows = p.getAllRows();
        if (rows == null || rows.length < index + 1) {
            Row[] newRows = new Row[p.MAX_TUPLES];
            if (rows != null) {
                System.arraycopy(rows, 0, newRows, 0, rows.length);
            }
            rows = newRows;
        }
        rows[index] = row;
        p.setAllRows(rows);
    }

    private byte[] padByteArrayToLength(byte[] arr, int totalLength) {
        byte[] returnArr = new byte[totalLength];
        System.arraycopy(arr, 0, returnArr, 0, Math.min(totalLength, arr.length));
        return returnArr;
    }

//method to find root
    public int findRoot(int curPid) {
        int parentPid = getParentId(curPid);
        if (parentPid == -1) {
            return curPid;
        }
        return findRoot(parentPid);
    }

//method to populate movie id index and movie title index.
    public String populateIndex() throws Exception {
        int numPages = bufferMgr.getNumPagesOnDisk();
        for (int curPageIdx = 0; curPageIdx < numPages; ++curPageIdx) {
            Page curDiskPage = bufferMgr.getPage(curPageIdx, File.DISK);
            for (int curRowIdx = 0; curRowIdx < curDiskPage.getRowCount(); ++curRowIdx) {
                Row curRow = curDiskPage.getRow(curRowIdx);
                byte[] key = dataFile == File.MOVIE_ID_IDX ? curRow.getMovieId() : curRow.getTitle();
                String keyStr = new String(key).trim();
                insert(keyStr, new Rid(curPageIdx, curRowIdx));
            }
            bufferMgr.unpinPage(curDiskPage.getPid(), File.DISK);
        }
        bufferMgr.force();
        return "Successfully populated " + dataFile.toString();
    }

 // method to do bulk-loading for movieId index
    public String bulkLoad() throws Exception {
        Page curLeafPage = bufferMgr.getPage(metadata.rootPageId, File.MOVIE_ID_IDX);
        int numPages = bufferMgr.getNumPagesOnDisk();
        for (int curPageIdx = 0; curPageIdx < numPages; ++curPageIdx) {
            Page curDiskPage = bufferMgr.getPage(curPageIdx, File.DISK);
            for (int curRowIdx = 0; curRowIdx < curDiskPage.getRowCount(); ++curRowIdx) {
                Row curRow = curDiskPage.getRow(curRowIdx);
                byte[] arrRid = new byte[9];
                storeIntInByteArray(curPageIdx, arrRid, 0);
                storeIntInByteArray(curRowIdx, arrRid, 3);
                Page tempLeafPage = insertIntoLeaf(curLeafPage.getPid(), curRow.getMovieId(), arrRid);
                if (tempLeafPage != null) {
                    bufferMgr.unpinPage(curLeafPage.getPid(), File.MOVIE_ID_IDX);
                    curLeafPage = bufferMgr.getPage(tempLeafPage.getPid(), File.MOVIE_ID_IDX);
                }
            }
            bufferMgr.unpinPage(curDiskPage.getPid(), File.DISK);
        }
        bufferMgr.unpinPage(curLeafPage.getPid(), File.MOVIE_ID_IDX);
        bufferMgr.force();
        return "successfully bulk loaded the index";
    }
}