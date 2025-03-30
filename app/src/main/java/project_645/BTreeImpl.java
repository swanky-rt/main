package project_645;

import java.util.*;


public class BTreeImpl implements BTree<String, Rid> {



    private enum NodeType {
        LEAF, INTERNAL
    }


    private static class BTreeMetadata {
        int rootPageId;   // page id of the root node
        boolean isRootLeaf;
        int order;        // approximate fanout
    }


    private final int maxKeysLeaf;
    private final int maxKeysInternal;


    private final BufferManager bufferMgr;
    private final BTreeMetadata metadata;


    public BTreeImpl(BufferManager bufferMgr, int order, boolean createNew) throws Exception {
        this.bufferMgr = bufferMgr;
        this.metadata = new BTreeMetadata();
        this.metadata.order = order;
        this.maxKeysLeaf = 2 * order;
        this.maxKeysInternal = 2 * order;

        if (createNew) {
            // Create a new root page as a leaf node.
            Page rootPage = bufferMgr.createPage(File.DISK);
            int rootPid = rootPage.getPid();
            this.metadata.rootPageId = rootPid;
            this.metadata.isRootLeaf = true;
            initLeafPage(rootPage, -1, -1, 0);
            bufferMgr.markDirty(rootPid, File.DISK);
            bufferMgr.unpinPage(rootPid, File.DISK);
        } else {
            // For simplicity, assume root is page 0.
            this.metadata.rootPageId = 0;
            this.metadata.isRootLeaf = true;
        }
    }



    @Override
    public void insert(String key, Rid rid) {
        int leafPid = findLeafPageId(key.getBytes());
        byte[] arrRid = new byte[9];
        storeIntInByteArray(rid.getPageId(), arrRid, 0);
        storeIntInByteArray(rid.getSlotId(), arrRid, 3);
        insertIntoLeaf(leafPid, padByteArrayToLength(key.getBytes(), 30), arrRid);
    }

    @Override
    public Iterator<Rid> search(String key) {
        List<Rid> results = new ArrayList<>();
        boolean flag = true;
        int curLeafPid = findLeafPageId(padByteArrayToLength(key.getBytes(), 30));
        while (flag) {
            List<Rid> tempResults = searchInLeaf(curLeafPid, padByteArrayToLength(key.getBytes(), 30));
            results.addAll(tempResults);
            curLeafPid = getNextLeafId(curLeafPid);
            flag = !results.isEmpty() && curLeafPid != -1;
        }

        return results.iterator();
    }

    @Override
    public Iterator<Rid> rangeSearch(String startKey, String endKey) {
        List<Rid> results = new ArrayList<>();
        int leafPid = findLeafPageId(startKey.getBytes());
        int currentPid = leafPid;
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
            bufferMgr.unpinPage(currentPid, File.DISK);
            if (!leafKeys.isEmpty() && Arrays.compare(leafKeys.get(leafKeys.size()-1), padByteArrayToLength(endKey.getBytes(), 30)) > 0) {
                break;
            }
            currentPid = nextLeaf;
        }
        return results.iterator();
    }


    private void insertIntoLeaf(int leafPid, byte[] key, byte[] rid) {
        try {

            List<byte[]> keys = new ArrayList<>();
            List<byte[]> rids = new ArrayList<>();
            readLeafNode(leafPid, keys, rids);
            int pos = 0;
            while (pos < keys.size() && Arrays.compare(key, keys.get(pos)) > 0) {
                pos++;
            }
            keys.add(pos, key);
            rids.add(pos, rid);
            if (keys.size() > maxKeysLeaf) {
                int mid = keys.size() / 2;
                Page siblingPage = bufferMgr.createPage(File.DISK);
                int siblingPid = siblingPage.getPid();
                bufferMgr.unpinPage(siblingPid, File.DISK);
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
                if (leafPid == metadata.rootPageId && metadata.isRootLeaf) {
                    createNewRoot(leafPid, siblingPid, siblingKeys.get(0));
                } else {
                    insertInParent(leafPid, siblingPid, siblingKeys.get(0));
                }
                bufferMgr.markDirty(siblingPid, File.DISK);
                bufferMgr.unpinPage(siblingPid, File.DISK);
            } else {
                writeLeafKeysAndRids(leafPid, keys, rids);
            }
            bufferMgr.markDirty(leafPid, File.DISK);
            bufferMgr.unpinPage(leafPid, File.DISK);
        } catch (Exception e) {
            System.err.println("Error in insertIntoLeaf: " + e.getMessage());
        }
    }

    private void createNewRoot(int oldRootPid, int siblingPid, byte[] splitKey) throws Exception {
        Page newRootPage = bufferMgr.createPage(File.DISK);
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
        bufferMgr.markDirty(newRootPid, File.DISK);
        bufferMgr.unpinPage(newRootPid, File.DISK);
    }

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
            Page newPage = bufferMgr.createPage(File.DISK);
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
            if (parentPid == metadata.rootPageId) {
                createNewRoot(parentPid, newPid, upKey);
            } else {
                insertInParent(parentPid, newPid, upKey);
            }
            bufferMgr.markDirty(newPid, File.DISK);
            bufferMgr.unpinPage(newPid, File.DISK);
        } else {
            writeInternalKeysAndChildren(parentPid, keys, children);
        }
        bufferMgr.markDirty(parentPid, File.DISK);
        bufferMgr.unpinPage(parentPid, File.DISK);
        setParentId(rightPid, parentPid);
    }



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
                bufferMgr.unpinPage(currentPid, File.DISK);
                currentPid = childPid;
            }
        }
    }

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
        bufferMgr.unpinPage(leafPid, File.DISK);
        return result;
    }



    private void initLeafPage(Page page, int parentId, int nextLeafId, int numKeys) {
        PageImpl p = (PageImpl) page;
        p.setAllRows(new Row[PageImpl.MAX_TUPLES]);
        p.setRowCount(0);
        Row metaRow = new Row(new byte[9], new byte[30]);
        metaRow.movieId[0] = 'L';
        storeIntInByteArray(parentId, metaRow.title, 0);
        storeIntInByteArray(nextLeafId, metaRow.title, 3);
        storeIntInByteArray(numKeys, metaRow.title, 6);
        p.insertRow(metaRow); // row 0 stores metadata
    }

    private void readLeafNode(int pageId, List<byte[]> keysOut, List<byte[]> ridsOut) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            bufferMgr.unpinPage(page.getPid(), File.DISK);
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
                String k = parseStringFromByteArray(r.title, 0, r.title.length);
                int pid = parseIntFromByteArray(r.movieId, 0);
                int slot = parseIntFromByteArray(r.movieId, 3);
                // Currently changing the readLeafNode function definition to instead take byte arrays as input.
                // This is done so that data stays serialized the entire time, which is a requirement for the lab description.
//                keysOut.add(k.trim());
//                ridsOut.add(new Rid(pid, slot));

                keysOut.add(r.title);
                ridsOut.add(r.movieId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in readLeafNode: " + e.getMessage());
        }
    }

    private void writeLeafKeysAndRids(int pageId, List<byte[]> keys, List<byte[]> rids) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            Row[] replacementRowArray = new Row[105];
            replacementRowArray[0] = meta;
            p.setAllRows(replacementRowArray);
            storeIntInByteArray(keys.size(), meta.title, 6);
            // Write each key and RID starting at row 1.
            for (int i = 0; i < keys.size(); i++) {
                Row row = new Row(rids.get(i), keys.get(i));
//                storeStringInByteArray(keys.get(i), row.movieId, 0, row.movieId.length);
//                storeIntInByteArray(rids.get(i).getPageId(), row.title, 0);
//                storeIntInByteArray(rids.get(i).getSlotId(), row.title, 4);
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
        p.setAllRows(new Row[PageImpl.MAX_TUPLES]);
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
            Page page = bufferMgr.getPage(pageId, File.DISK);
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
//                String k = parseStringFromByteArray(row.title, 0, row.title.length);
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
            Page page = bufferMgr.getPage(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(keys.size(), meta.title, 6);
            int rowCountNeeded = keys.size() + 1;
            for (int i = 1; i <= keys.size(); i++) {
                Row row = new Row(new byte[9], keys.get(i - 1));
                storeIntInByteArray(children.get(i - 1), row.movieId, 0);
//                storeStringInByteArray(keys.get(i - 1), row.title, 0, row.title.length);
                setRowAtIndex(p, i, row);
            }
            // Final child pointer at row keys.size() + 1.
            Row lastRow = new Row(new byte[9], new byte[30]);
            storeIntInByteArray(children.get(keys.size()), lastRow.movieId, 0);
            setRowAtIndex(p, keys.size() + 1, lastRow);
            p.setRowCount(rowCountNeeded + 1);
        } catch (Exception e) {
            throw new RuntimeException("Error in writeInternalKeysAndChildren: " + e.getMessage());
        }
    }



    private boolean isLeafNode(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            char c = (char) meta.movieId[0];
            bufferMgr.unpinPage(pageId, File.DISK);
            return (c == 'L');
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getParentId(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            int pid = parseIntFromByteArray(meta.title, 0);
            bufferMgr.unpinPage(pageId, File.DISK);
            return pid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setParentId(int pageId, int parentId) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            bufferMgr.markDirty(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(parentId, meta.title, 0);
            bufferMgr.unpinPage(pageId, File.DISK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getNextLeafId(int pageId) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            bufferMgr.unpinPage(page.getPid(), File.DISK);
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
            Page page = bufferMgr.getPage(pageId, File.DISK);
            bufferMgr.markDirty(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(nxtLeafId, meta.title, 3);
            bufferMgr.unpinPage(pageId, File.DISK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setNumKeys(int pageId, int numKeys) {
        try {
            Page page = bufferMgr.getPage(pageId, File.DISK);
            bufferMgr.markDirty(pageId, File.DISK);
            PageImpl p = (PageImpl) page;
            Row meta = p.getRow(0);
            storeIntInByteArray(numKeys, meta.title, 6);
            bufferMgr.unpinPage(pageId, File.DISK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



//    private void storeIntInByteArray(int val, byte[] arr, int offset) {
//        arr[offset]   = (byte) (val >>> 24);
//        arr[offset+1] = (byte) (val >>> 16);
//        arr[offset+2] = (byte) (val >>> 8);
//        arr[offset+3] = (byte) (val);
//    }
//
//    private int parseIntFromByteArray(byte[] arr, int offset) {
//        int b1 = (arr[offset]   & 0xFF) << 24;
//        int b2 = (arr[offset+1] & 0xFF) << 16;
//        int b3 = (arr[offset+2] & 0xFF) << 8;
//        int b4 = (arr[offset+3] & 0xFF);
//        return (b1 | b2 | b3 | b4);
//    }

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


    private void storeStringInByteArray(String str, byte[] arr, int offset, int length) {
        byte[] bytes = str.getBytes();
        // Truncate if necessary.
        int len = Math.min(bytes.length, length);
        System.arraycopy(bytes, 0, arr, offset, len);
        // Pad with 0 if necessary.
        for (int i = len; i < length; i++) {
            arr[offset + i] = 0;
        }
    }


    private String parseStringFromByteArray(byte[] arr, int offset, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(arr, offset, bytes, 0, length);
        return new String(bytes).trim();
    }


    private void setRowAtIndex(PageImpl p, int index, Row row) {
        Row[] rows = p.getAllRows();
        if (rows == null || rows.length < index + 1) {
            Row[] newRows = new Row[PageImpl.MAX_TUPLES];
            if (rows != null) {
                System.arraycopy(rows, 0, newRows, 0, rows.length);
            }
            rows = newRows;
        }
        rows[index] = row;
        p.setAllRows(rows);
    }

    private byte[] padByteArrayToLength(byte[] arr, int totalLength) {
        if (arr.length > totalLength) {
            throw new IllegalArgumentException("Length of the passed arr is greater than expected padded length of the array");
        }
        byte[] returnArr = new byte[totalLength];
        System.arraycopy(arr, 0, returnArr, 0, arr.length);
        return returnArr;
    }
}