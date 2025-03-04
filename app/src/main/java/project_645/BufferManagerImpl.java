package project_645;
import java.util.*;

public class BufferManagerImpl extends BufferManager{
    public int MAX_PAGE;
    private int PAGE_SIZE;
    public Map<Integer, Page> bufferPool;
    public Set<Integer> dirtyPages;
    public Map<Page, Integer> pageMap;
    public LinkedList<Integer> lru;
    public Set<Integer> pinnedPages;

    public BufferManagerImpl(int bufferSize){
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
    public Page getPage(int pageId) {
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

    public void evictPage(){
        int pageId = lru.removeLast();
        Page removedPage = bufferPool.get(pageId);
        if(!pinnedPages.contains(pageId)){
            if(isDirty(pageId)){
                utilities.writePageToDisk(pageId, removedPage);
                dirtyPages.remove(pageId);
            }
            bufferPool.remove(pageId);
            pageMap.remove(removedPage);
        }
    }

    @Override
    public Page createPage() {
        PageImpl page = null;
        int pageId = utilities.getNextPageId();
        if(pageId<MAX_PAGE) {
            page = new PageImpl();
            pageMap.put(page, pageId);
            this.pinPage(pageId);
        }
        return page;
    }

    @Override
    public void markDirty(int pageId) {
        if(isDirty(pageId).equals(Boolean.FALSE)){
            dirtyPages.add(pageId);
        }
    }

    public Boolean isDirty(int pageId){
        return dirtyPages.contains(pageId);
    }


    @Override
    public void unpinPage(int pageId) {
        if(bufferPool.containsKey(pageId) && pinnedPages.contains(pageId)){
            pinnedPages.remove(pageId);
            if(lru.contains(pageId)){
                lru.remove(pageId);
            }
            lru.addLast(pageId);
        }
    }

    public void pinPage(int pageId) {
        if(bufferPool.containsKey(pageId)){
            pinnedPages.add(pageId);
            if(lru.contains(pageId)){
                System.out.println("the page id is" + pageId + "the size is " + lru.size());
                lru.remove(pageId);
            }
            lru.addFirst(pageId);
        }
    }

    public Boolean isBufferPoolFull(){
        return bufferPool.size()==MAX_PAGE;
    }
}
