package project_645;

import java.io.IOException;

public abstract class BufferManager {

        // configurable size of buffer cache.
        final int bufferSize;
        public BufferManager(int bufferSize){
                this.bufferSize = bufferSize;
        }
     /**
             * Fetches a page from memory if available; otherwise, loads it from disk.
                * The page is immediately pinned.
        * @param pageId The ID of the page to fetch.
                * @return The Page object whose content is stored in a frame of the buffer pool manager.
     */


    public abstract Page getPage(long pageId, File dataFile) throws Exception;

    /**
         * Creates a new page.
         * The page is immediately pinned.
         * @return The Page object whose content is stored in a frame of the buffer pool manager.
         */
        abstract Page createPage(File dataFile) throws Exception;

        /**
         * Marks a page as dirty, indicating it needs to be written to disk before eviction.
         * @param pageId The ID of the page to mark as dirty.
         */

    public abstract void markDirty(long pageId, File dataFile);

    /**
         * Unpins a page in the buffer pool, allowing it to be evicted if necessary.
         * @param pageId The ID of the page to unpin.
         */
    public abstract void unpinPage(long pageId, File dataFile);

    /**
     *
     * @returns the configured size of the buffer pool in bytes
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     *
     * Forces a write of all pages in the buffer pool to disk
     */
    public abstract void force() throws Exception;

    /**
     *
     * Gets the number of pages written to disk
     */
    public abstract int getNumPagesOnDisk() throws IOException;

    /**
     *
     * Gets the number of pages in the chosen file
     */
    public abstract int getFileSizeOfChosenFile(File dataFile) throws IOException;

}