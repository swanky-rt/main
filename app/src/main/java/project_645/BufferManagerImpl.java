package project_645;
import java.io.IOException;
import java.util.*;

public class BufferManagerImpl extends BufferManager{
    public int MAX_PAGE;
    private int PAGE_SIZE;
    public Map<Integer, Page> bufferPool;
    public Set<Integer> dirtyPages;
    public Map<Page, Integer> pageMap;
    public LinkedList<Integer> lru;
    public Set<Integer> pinnedPages;

    public BufferManagerImpl(int bufferSize) throws IOException {
        super(bufferSize);
        this.PAGE_SIZE = 4096;
        this.MAX_PAGE = bufferSize/PAGE_SIZE;
        this.bufferPool = new HashMap<>();
        this.lru = new LinkedList<>();
        this.dirtyPages = new HashSet<>();
        this.pageMap = new HashMap<>();
        this.pinnedPages = new HashSet<>();

    }
    Utilities utilities = new Utilities();

    @Override
    public Page getPage(int pageId) throws Exception {
        if(bufferPool.containsKey(pageId)){
            lru.remove(pageId);
            lru.addFirst(pageId);
            return bufferPool.get(pageId);
        }
        if(bufferPool.size()>=MAX_PAGE){
            evictPage();
        }
        Page page =utilities.loadPageFromDisk(pageId);
        bufferPool.put(pageId, page);
        pageMap.put(page, pageId);
        lru.addFirst(pageId);
        this.pinPage(pageId);
        System.out.println("the page " + pageId + " is pinned");
        return page;

    }

    public void evictPage() throws Exception {
        for (Integer curPageId : lru.reversed()) {
            Page removedPage = bufferPool.get(curPageId);
            if (!pinnedPages.contains(curPageId)) {
                lru.remove(curPageId);
                try {
                    if (removedPage.getDirtyStatus()) {
                        utilities.writePageToDisk(curPageId, removedPage);
                        dirtyPages.remove(curPageId);
                        removedPage.markNotDirty();
                    }
                    bufferPool.remove(curPageId);
                    pageMap.remove(removedPage);
                } catch (IOException e) {
                    // Handle the exception, e.g., log it or rethrow it
                    System.err.println("Failed to write page to disk: " + e.getMessage());
                }
                return;
            }
        }
        throw new Exception("Every page in the buffer pool is currently pinned");
    }


    @Override
    public Page createPage() throws Exception {
        PageImpl page = null;
        int pageId = utilities.getNextPageId();
        if (this.bufferPool.size() >= this.MAX_PAGE) {
            evictPage();
        }
        lru.addFirst(pageId);
        page = new PageImpl(pageId);
        bufferPool.put(pageId, page);
        pageMap.put(page, pageId);
        this.pinPage(pageId);
        return page;
    }

    @Override
    public void markDirty(int pageId) {
        if(isDirty(pageId).equals(Boolean.FALSE)){
            dirtyPages.add(pageId);
        }

        if (bufferPool.containsKey(pageId)) {
            Page page = bufferPool.get(pageId);
            page.markDirty();
        }
    }

    public Boolean isDirty(int pageId){
        return bufferPool.containsKey(pageId) && bufferPool.get(pageId).getDirtyStatus();
    }


    @Override
    public void unpinPage(int pageId) {
        if(bufferPool.containsKey(pageId) && pinnedPages.contains(pageId)){
            Page pageToUnpin = bufferPool.get(pageId);
            pageToUnpin.decrementPinCount();
            if (pageToUnpin.getPinCount() == 0) {
                pinnedPages.remove(pageId);
            }
        }
    }

    public void pinPage(int pageId) {
        if(bufferPool.containsKey(pageId)){
            pinnedPages.add(pageId);
            bufferPool.get(pageId).incrementPinCount();
            if(lru.contains(pageId)){
                System.out.println("the page id is" + pageId + "the size is " + lru.size());
            }
        }
    }

    public Boolean isBufferPoolFull(){
        return bufferPool.size()==MAX_PAGE;
    }
}
