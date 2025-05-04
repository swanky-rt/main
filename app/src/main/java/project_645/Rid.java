   package project_645;

   public class Rid {
       private long pageId;
       private int slotId;

       public Rid(long pageId, int slotId) {
           this.pageId = pageId;
           this.slotId = slotId;
       }

       public long getPageId() {
           return pageId;
       }

       public int getSlotId() {
           return slotId;
       }
   }