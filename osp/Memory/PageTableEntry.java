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
import osp.Devices.*;
import osp.IFLModules.*;

/**
 * The PageTableEntry object contains information about a specific virtual
 * page in memory, including the page frame in which it resides.
 */

public class PageTableEntry extends IflPageTableEntry {
    /**
     * The constructor.
     */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber) {
        super(ownerPageTable, pageNumber);
    }

    /**
     * This method increases the lock count on the page by one.
     * <p/>
     * The method must FIRST increment lockCount, THEN
     * check if the page is valid, and if it is not and no
     * page validation event is present for the page, start page fault
     * by calling PageFaultHandler.handlePageFault().
     *
     * @return SUCCESS or FAILURE
     * FAILURE happens when the page fault due to locking fails or the
     * that created the IORB thread gets killed.
     */
    public int do_lock(IORB iorb) {
        // Pseudocode ----------------------------------------------------------
        // if (page is valid)
        //      incrementLockCount()
        //      return SUCCESS
        // else
        //      if (oldThread caused page fault )
        //          if (oldThread same as the newThread)
        //              incrementLockCount()
        //              return SUCCESS
        //          else
        //              try
        //                  newThread.suspend(page)
        //                  incrementLockCount()
        //                  return SUCCESS
        //              catch error
        //                  return FAILURE
        //      else
        //          if (handlePageFault() == SUCCESS)
        //              incrementLockCount()
        //              return SUCCESS
        //          else
        //              return FAILURE
        // ---------------------------------------------------------------------

        PageTableEntry page = this;
        ThreadCB oldThread = page.getValidatingThread();
        ThreadCB newThread = iorb.getThread();

        if (page.isValid()) {
            page.getFrame().incrementLockCount();
            return SUCCESS;

        } else {
            if (oldThread != null) {
                if (oldThread == newThread) {
                    page.getFrame().incrementLockCount();
                    return SUCCESS;
                } // end if threads are equal
                else {
                    try {
                        newThread.suspend(page);
                        page.getFrame().incrementLockCount();
                        return SUCCESS;
                    } catch (Exception e) {
                        System.out.println("\nCaught exception in do_lock");
                        return FAILURE;
                    }
                } // end threads are not equal
            } // end if an old thread caused page fault
            else {
                // referenceType = MemoryLock (pg. 95)
                // Store the int result of PageFaultHandler in a variable
                // for easier manipulation of result in condition below.
                int h = PageFaultHandler.handlePageFault(newThread,
                        MemoryLock, page);

                if (h == SUCCESS) {
                    page.getFrame().incrementLockCount();
                    return SUCCESS;
                } else {
                    return FAILURE;
                }

            } // end oldThread did not cause a page fault
        } // end page not valid
    } // end do_lock

    /**
     * This method decreases the lock count on the page by one.
     * <p/>
     * This method must decrement lockCount, but not below zero.
     */
    public void do_unlock() {
        // Pseudocode ----------------------------------------------------------
        // if (lockCount > 0)
        //      decrementLockCount()
        // ---------------------------------------------------------------------

        if (getFrame().getLockCount() > 0) {
            getFrame().decrementLockCount();
        }

    }

} // end PageTableEntry class