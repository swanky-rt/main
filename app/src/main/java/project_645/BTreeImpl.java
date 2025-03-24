package project_645;

import java.io.Serializable;
import java.util.*;

public class BTreeImpl<K extends Comparable<K>> implements BTree<K, Rid> {
    private BufferManager bufferManager;  // the buffer manager to access index pages
    private int rootPageId;               // page id of the root node
    private final int order;                    // maximum number of entries per node (defines node capacity)

    // ---------------------------
    // Node classes used in the B+ tree
    // ---------------------------
    abstract class Node implements Serializable {
        List<K> keys;
        boolean isLeaf;
        Node(boolean isLeaf) {
            this.keys = new ArrayList<>();
            this.isLeaf = isLeaf;
        }
    }



    // Internal node: contains keys and pointers to child pages.
    class InternalNode extends Node {
        List<Integer> childrenPageIds;  // pointers to child node pages
        InternalNode() {
            super(false);
            childrenPageIds = new ArrayList<>();
        }
    }

    // Leaf node: contains key-value pairs and a pointer to the next leaf.
    class LeafNode extends Node {
        List<Rid> values;   // one value (record id) per key
        int nextLeafPageId; // pointer to the next leaf page (-1 if none)
        LeafNode() {
            super(true);
            values = new ArrayList<>();
            nextLeafPageId = -1;
        }
    }

    // A helper class to capture node-split information.
    private static class SplitResult<K> {
        K newKey;        // key to be pushed up to the parent
        int newPageId;   // page id of the newly created node
    }

    // ---------------------------
    // Constructor
    // ---------------------------
    /**
     * Constructs a B+ tree index.
     * @param bufferManager The buffer manager instance to read/write index pages.
     * @param order Maximum number of entries a node can hold.
     */
    public BTreeImpl(BufferManager bufferManager, int order) throws Exception {
        this.bufferManager = bufferManager;
        this.order = order;
        // Create an initial empty leaf node as the root.
        LeafNode root = new LeafNode();
        Page rootPage = bufferManager.createPage();
        rootPageId = rootPage.getPid();
        writeNodeToPage(root, rootPage);
        bufferManager.unpinPage(rootPageId);
    }

    // ---------------------------
    // Insertion
    // ---------------------------
    /**
     * Inserts a key-value pair into the B+ tree.
     * @param key The movieId to insert.
     * @param r   The Rid (representing a page id) associated with the movieId.
     */
    @Override
    public void insert(K key, Rid r) {
        try {
            SplitResult<K> result = insertRecursive(rootPageId, key, r);
            if (result != null) {
                // Root split occurred. Create a new root.
                InternalNode newRoot = new InternalNode();
                newRoot.keys.add(result.newKey);
                newRoot.childrenPageIds.add(rootPageId);
                newRoot.childrenPageIds.add(result.newPageId);
                Page newRootPage = bufferManager.createPage();
                int newRootPageId = newRootPage.getPid();
                writeNodeToPage(newRoot, newRootPage);
                bufferManager.unpinPage(newRootPageId);
                rootPageId = newRootPageId;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively inserts a key into the subtree rooted at pageId.
     * Returns a SplitResult if a split occurs; otherwise, returns null.
     */
    private SplitResult<K> insertRecursive(int pageId, K key, Rid r) throws Exception {
        Page page = bufferManager.getPage(pageId);
        Node node = readNodeFromPage(page);
        SplitResult<K> splitResult = null;

        if (node.isLeaf) {
            // Leaf node insertion
            LeafNode leaf = (LeafNode) node;
            int pos = Collections.binarySearch(leaf.keys, key);
            if (pos < 0) {
                pos = -pos - 1;
            }
            leaf.keys.add(pos, key);
            leaf.values.add(pos, r);
            if (leaf.keys.size() < order) {
                writeNodeToPage(leaf, page);
                bufferManager.unpinPage(pageId);
                return null;
            } else {
                // Split the leaf node.
                LeafNode newLeaf = new LeafNode();
                int mid = leaf.keys.size() / 2;
                newLeaf.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
                newLeaf.values.addAll(leaf.values.subList(mid, leaf.values.size()));
                // Remove the moved keys/values from the original leaf.
                leaf.keys.subList(mid, leaf.keys.size()).clear();
                leaf.values.subList(mid, leaf.values.size()).clear();
                newLeaf.nextLeafPageId = leaf.nextLeafPageId;
                // Allocate a new page for the new leaf.
                int newLeafPageId = allocateNewPage();
                leaf.nextLeafPageId = newLeafPageId;
                // Write changes.
                writeNodeToPage(leaf, page);
                bufferManager.unpinPage(pageId);
                Page newLeafPage = bufferManager.getPage(newLeafPageId);
                writeNodeToPage(newLeaf, newLeafPage);
                bufferManager.unpinPage(newLeafPageId);
                // Prepare split result.
                SplitResult<K> result = new SplitResult<>();
                result.newKey = newLeaf.keys.get(0);  // push up the first key of new leaf.
                result.newPageId = newLeafPageId;
                return result;
            }
        } else {
            // Internal node insertion.
            InternalNode internal = (InternalNode) node;
            int pos = Collections.binarySearch(internal.keys, key);
            if (pos < 0) {
                pos = -pos - 1;
            } else {
                pos++; // if key equals an internal key, go to the right child.
            }
            int childPageId = internal.childrenPageIds.get(pos);
            splitResult = insertRecursive(childPageId, key, r);
            if (splitResult != null) {
                int insertPos = Collections.binarySearch(internal.keys, splitResult.newKey);
                if (insertPos < 0) {
                    insertPos = -insertPos - 1;
                } else {
                    insertPos++;
                }
                internal.keys.add(insertPos, splitResult.newKey);
                internal.childrenPageIds.add(insertPos + 1, splitResult.newPageId);
                if (internal.keys.size() < order) {
                    writeNodeToPage(internal, page);
                    bufferManager.unpinPage(pageId);
                    return null;
                } else {
                    // Split the internal node.
                    InternalNode newInternal = new InternalNode();
                    int mid = internal.keys.size() / 2;
                    K midKey = internal.keys.get(mid);
                    newInternal.keys.addAll(internal.keys.subList(mid + 1, internal.keys.size()));
                    newInternal.childrenPageIds.addAll(internal.childrenPageIds.subList(mid + 1, internal.childrenPageIds.size()));
                    internal.keys.subList(mid, internal.keys.size()).clear();
                    internal.childrenPageIds.subList(mid + 1, internal.childrenPageIds.size()).clear();
                    int newInternalPageId = allocateNewPage();
                    writeNodeToPage(internal, page);
                    bufferManager.unpinPage(pageId);
                    Page newInternalPage = bufferManager.getPage(newInternalPageId);
                    writeNodeToPage(newInternal, newInternalPage);
                    bufferManager.unpinPage(newInternalPageId);
                    SplitResult<K> res = new SplitResult<>();
                    res.newKey = midKey;
                    res.newPageId = newInternalPageId;
                    return res;
                }
            } else {
                writeNodeToPage(internal, page);
                bufferManager.unpinPage(pageId);
                return null;
            }
        }
    }

    // ---------------------------
    // Search (Point Query)
    // ---------------------------
    /**
     * Searches for all record ids (Rids) associated with the specified movieId.
     */
    @Override
    public Iterator<Rid> search(K key) {
        List<Rid> results = new ArrayList<>();
        try {
            int leafPageId = findLeafPage(rootPageId, key);
            Page page = bufferManager.getPage(leafPageId);
            LeafNode leaf = (LeafNode) readNodeFromPage(page);
            for (int i = 0; i < leaf.keys.size(); i++) {
                if (leaf.keys.get(i).compareTo(key) == 0) {
                    results.add(leaf.values.get(i));
                }
            }
            bufferManager.unpinPage(leafPageId);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return results.iterator();
    }

    // ---------------------------
    // Range Query
    // ---------------------------
    /**
     * Performs a range query over movieIds from startKey to endKey (inclusive).
     */
    @Override
    public Iterator<Rid> rangeSearch(K startKey, K endKey) {
        List<Rid> results = new ArrayList<>();
        try {
            int leafPageId = findLeafPage(rootPageId, startKey);
            boolean continueSearch = true;
            while (continueSearch) {
                Page page = bufferManager.getPage(leafPageId);
                LeafNode leaf = (LeafNode) readNodeFromPage(page);
                for (int i = 0; i < leaf.keys.size(); i++) {
                    K key = leaf.keys.get(i);
                    if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        results.add(leaf.values.get(i));
                    } else if (key.compareTo(endKey) > 0) {
                        continueSearch = false;
                        break;
                    }
                }
                int nextPageId = leaf.nextLeafPageId;
                bufferManager.unpinPage(leafPageId);
                if (nextPageId == -1 || !continueSearch) {
                    break;
                }
                leafPageId = nextPageId;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return results.iterator();
    }

    // ---------------------------
    // Helper Methods
    // ---------------------------
    /**
     * Traverses the tree to find the leaf page that should contain the given key.
     */
    private int findLeafPage(int currentPageId, K key) throws Exception {
        Page page = bufferManager.getPage(currentPageId);
        Node node = readNodeFromPage(page);
        if (node.isLeaf) {
            bufferManager.unpinPage(currentPageId);
            return currentPageId;
        } else {
            InternalNode internal = (InternalNode) node;
            int pos = Collections.binarySearch(internal.keys, key);
            if (pos < 0) {
                pos = -pos - 1;
            } else {
                pos++;
            }
            int childPageId = internal.childrenPageIds.get(pos);
            bufferManager.unpinPage(currentPageId);
            return findLeafPage(childPageId, key);
        }
    }

    /**
     * Allocates a new page (for a split) using the BufferManager.
     */
    private int allocateNewPage() throws Exception {
        Page newPage = bufferManager.createPage();
        int newPageId = newPage.getPid();
        bufferManager.unpinPage(newPageId);
        return newPageId;
    }

    /**
     * Writes the given Node object to the provided Page.
     * (Placeholder: you would serialize the Node into the binary format used in your index file.)
     */
    private void writeNodeToPage(Node node, Page page) {
        // TODO: Serialize 'node' and store its bytes in the page.
        // For example, you might convert the node's keys and pointers to a byte array and write it to the page.
    }

    /**
     * Reads and deserializes a Node object from the provided Page.
     * (Placeholder: you would deserialize the page's byte content into a Node object.)
     */
    private Node readNodeFromPage(Page page) {
        // TODO: Deserialize the page's bytes into a Node (InternalNode or LeafNode).
        // For demonstration purposes, we return a new LeafNode. Replace with real deserialization.
        return new LeafNode();
    }
}
