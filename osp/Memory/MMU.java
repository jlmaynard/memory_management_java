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

import osp.IFLModules.*;
import osp.Threads.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
 * The MMU class contains the student code that performs the work of
 * handling a memory reference.  It is responsible for calling the
 * interrupt handler if a page fault is required.
 */
public class MMU extends IflMMU {
    /**
     * This method is called once before the simulation starts.
     * Can be used to initialize the frame table and other static variables.
     */
    public static void init() {
        // Pseudocode ----------------------------------------------------------
        // foreach (frame in frame table)
        //      setFrame()
        for (int i = 0; i < MMU.getFrameTableSize(); i++){
            setFrame(i, new FrameTableEntry(i));
        }

        // Initialize variables from other classes???

        // ---------------------------------------------------------------------
    }

    /**
     * This method handles memory references. The method must
     * calculate, which memory page contains the memoryAddress,
     * determine, whether the page is valid, start page fault
     * by making an interrupt if the page is invalid, finally,
     * if the page is still valid, i.e., not swapped out by another
     * thread while this thread was suspended, set its frame
     * as referenced and then set it as dirty if necessary.
     * (After page fault, the thread will be placed on the ready queue,
     * and it is possible that some other thread will take away the frame.)
     *
     * @param memoryAddress A virtual memory address
     * @param referenceType The type of memory reference to perform
     * @param thread        that does the memory access
     *                      (e.g., MemoryRead or MemoryWrite).
     * @return The referenced page.
     */
    static public PageTableEntry do_refer(int memoryAddress,
                                          int referenceType, ThreadCB thread) {
        // Pseudocode ----------------------------------------------------------
        // Determine page
        //      calc num bits used for offset in address
        //          getVirtualAddressBits()
        int offset_bits = getVirtualAddressBits();

        //          getPageAddressBits()
        int address_bits = getPageAddressBits();

        //      calc page size
        int page_size = (int)Math.pow(2, (offset_bits - address_bits));

        int page_num = memoryAddress / page_size;

        //      determine page to which memory address belongs
        PageTableEntry laPage = thread.getTask().getPageTable().pages[page_num];

        // if (page is valid)
        //      set referenced and dirty bits
        //      return page of type PageTableEntry

        if (laPage.isValid()){
            if (referenceType == MemoryWrite){
                laPage.getFrame().setDirty(true);
            }
            laPage.getFrame().setReferenced(true);
            return laPage;
        }

        // else
        //      if (thread already caused a page fault)
        //          suspend this thread until page becomes available
        //              ThreadCB.suspend()
        else {
            ThreadCB oldThread = laPage.getValidatingThread();
            ThreadCB newThread = thread;
            if (oldThread == newThread){
                newThread.suspend(laPage);
            }

            //      else
            //          issue page fault interrupt
            //              InterruptVector.setPage()
            //              InterruptVector.setReferenceType()
            //              InterruptVector.setThread()
            //              CPU.interrupt()
            else {
                InterruptVector.setInterruptType(PageFault);
                InterruptVector.setPage(laPage);
                InterruptVector.setReferenceType(referenceType);
                InterruptVector.setThread(thread);
                CPU.interrupt(PageFault);
                return laPage;
            }
        }

        // if (thread killed) //getStatus()
        //      return page of type PageTableEntry
        if (thread.getStatus() == ThreadKill){
            return laPage;
        }

        // else
        //      set referenced and dirty bits
        //          return page of type PageTableEntry
        else{
            laPage.getFrame().setReferenced(true);
            laPage.getFrame().setDirty(true);
            return laPage;
        }

        // ---------------------------------------------------------------------

    }//end do_refer()

    /**
     * Called by OSP after printing an error message. The student can
     * insert code here to print various tables and data structures
     * in their state just after the error happened.  The body can be
     * left empty, if this feature is not used.
     */
    public static void atError() {
        System.out.println("\n*** I am atError()");
    }

    /**
     * Called by OSP after printing a warning message. The student
     * can insert code here to print various tables and data
     * structures in their state just after the warning happened.
     * The body can be left empty, if this feature is not used.
     */
    public static void atWarning() {
        System.out.println("\n*** Warning!!! Check OSP.log file...");
    }

} // end MMU class