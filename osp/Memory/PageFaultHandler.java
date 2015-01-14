// U30503758
// Jason Maynard
// Operating Systems, Summer '14
// -----------------------------------------------------------------------------
// NOTE: Please see flowchart document OSP_Memory.pdf for additional details
// including class UML diagrams and process flows for each class.  This
// document serves as the source of the pseudocode and was derived after
// trying to make sense of OSP 2 book by Kifer and Smolka.
// -----------------------------------------------------------------------------

package osp.Memory;

import osp.Threads.*;
import osp.IFLModules.*;

/**
 * The page fault handler is responsible for handling a page
 * fault.  If a swap in or swap out operation is required, the page fault
 * handler must request the operation.
 */
public class PageFaultHandler extends IflPageFaultHandler {
    /**
     * This method handles a page fault.
     * <p/>
     * It must check and return if the page is valid,
     * <p/>
     * It must check if the page is already being brought in by some other
     * thread, i.e., if the page's has already page faulted
     * (for instance, using getValidatingThread()).
     * If that is the case, the thread must be suspended on that page.
     * <p/>
     * If none of the above is true, a new frame must be chosen
     * and reserved until the swap in of the requested
     * page into this frame is complete.
     * <p/>
     * Note that you have to make sure that the validating thread of
     * a page is set correctly. To this end, you must set the page's
     * validating thread using setValidatingThread() when a page fault
     * happens and you must set it back to null when the page fault is over.
     * <p/>
     * If a swap-out is necessary (because the chosen frame is
     * dirty), the victim page must be disassociated
     * from the frame and marked invalid. After the swap-in, the
     * frame must be marked clean. The swap-ins and swap-outs
     * must are preformed using regular calls read() and write().
     * <p/>
     * The student implementation should define additional methods, e.g,
     * a method to search for an available frame.
     * <p/>
     * Note: multiple threads might be waiting for completion of the
     * page fault. The thread that initiated the page fault would be
     * waiting on the IORBs that are tasked to bring the page in (and
     * to free the frame during the swap out). However, while
     * page fault is in progress, other threads might request the same
     * page. Those threads won't cause another page fault, of course,
     * but they would enqueue themselves on the page (a page is also
     * an Event!), waiting for the completion of the original
     * page fault. It is thus important to call notifyThreads() on the
     * page at the end -- regardless of whether the page fault
     * succeeded in bringing the page in or not.
     *
     * @param thread        the thread that requested a page fault
     * @param referenceType whether it is memory read or write
     * @param page          the memory page
     * @return SUCCESS is everything is fine; FAILURE if the thread
     * dies while waiting for swap in or swap out or if the page is
     * already in memory and no page fault was necessary (well, this
     * shouldn't happen, but...). In addition, if there is no frame
     * that can be allocated to satisfy the page fault, then it
     * should return NotEnoughMemory
     */
    public static int do_handlePageFault(ThreadCB thread,
                                         int referenceType,
                                         PageTableEntry page) {

        // Pseudocode ----------------------------------------------------------
        // 1. startSuspend()
        //      if(page is valid)
        //          return FAILURE
        //              dispatch()
        if (page.isValid()) {
            ThreadCB.dispatch();
            return FAILURE;
        }

        //          if ( all frames locked or reserved)
        //              return NotEnoughMemory
        //              dispatch()

        // get state for frames to see if all referenced or reserved

        // Temp variable to determine if all frame states are
        // referenced or reserved.  Initialize to false then test in
        // for loop and pass result to next step.
        boolean frameState = false;

        // Use the counter to count how many are referenced or reserved.
        // if they are all referenced or reserved then the count will
        // equal the number of frames in the table.
        int frameStateCounter = 0;

        for (int i = 0; i < MMU.getFrameTableSize(); i++) {
            if (MMU.getFrame(i).isReferenced() ||
                    MMU.getFrame(i).isReserved()) {
                frameStateCounter++;
            }
        }

        if (frameStateCounter == MMU.getFrameTableSize()) {
            frameState = true;
        }

        // We now know if they are all referenced or reserved do if they
        // are then we need to return NotEnoughMemory and dispatch.

        if (frameState) {
            ThreadCB.dispatch();
            return NotEnoughMemory;
        }

        // myEvent = new SystemEvent(pfEvent)
        // suspend(pfEvent)
        // return Success
        // dispatch()

        // else {
        SystemEvent myEvent = new SystemEvent("pfEvent");
        thread.suspend(myEvent);
        // }

        // 2. findFrame() // Is there a free frame?

        // Potential free frame object initialized to null
        FrameTableEntry laFrame = null;

        // Scan the frame table looking for frames
        for (int i = 0; i < MMU.getFrameTableSize(); i++){
            laFrame = MMU.getFrame(i);
        }

        // test the frame variable to see if we found a frame in frame table
        if (laFrame.getPage() != null) {
            //          if (frame isDirty)
            //              swap out
            //                  save frame contents to swap file
            //                  evict frame
            if (laFrame.isDirty()) {
                thread.getTask().getSwapFile().write(laFrame.getID(),
                        laFrame.getPage(), thread);

                laFrame.getPage().setValid(false);
                laFrame.setDirty(false);

                // Don't forget to test if thread was killed
                if (thread.getStatus() == ThreadKill) {
                    page.setValidatingThread(null);
                    ThreadCB.dispatch();
                    return FAILURE;
                } else {
                    laFrame.setPage(null);
                }
            }

        }//end page not null

        // 3. swapIn()
        //    free frame
        //        setPage(null)
        //        setDirty(false)
        //        setReferenced(false)
        //        if (getStatus == killed)
        //           return FAILURE
        //           setDirty(false)
        //           dispatch()

        thread.getTask().getSwapFile().read(page.getID(), page, thread);

        if (thread.getStatus() == ThreadKill) {
            laFrame.setDirty(false);
            ThreadCB.dispatch();
            return FAILURE;
        }

        // 4. finishUp()
        laFrame.setReferenced(false);
        laFrame.setPage(page);
        page.setValid(true);
        page.setValidatingThread(null);
        laFrame.setDirty(referenceType == MemoryWrite);

        // notify threads
        myEvent.notifyThreads();
        page.notifyThreads();

        ThreadCB.dispatch();
        return SUCCESS;

        // ---------------------------------------------------------------------
    }

} // end PageFaultHandler class