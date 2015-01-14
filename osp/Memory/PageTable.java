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
/**
 The PageTable class represents the page table for a given task.
 A PageTable consists of an array of PageTableEntry objects.  This
 page table is of the non-inverted type.
 */

import osp.Tasks.*;
import osp.IFLModules.*;

public class PageTable extends IflPageTable {
    /**
     * The page table constructor.
     */

    int max_pages;

    public PageTable(TaskCB ownerTask) {
        super(ownerTask);

        // Pseudocode ----------------------------------------------------------
        // Get number of bits for page indexes
        //      MMU.getPageAddressBits()
        int index_bits = MMU.getPageAddressBits();

        // Calc max pages allowed
        max_pages = (int)Math.pow(2, index_bits);

        // Initialize new array
        pages = new PageTableEntry[max_pages];

        // Initialize each page
        // ---------------------------------------------------------------------
        for (int i = 0; i < max_pages; i++){
            pages[i] = new PageTableEntry(this, i);
        }
    }

    /**
     * Frees up main memory occupied by the task.
     * Then un-reserves the freed pages, if necessary.
     */
    public void do_deallocateMemory() {
        // Pseudocode ----------------------------------------------------------
        // for (each page in page table)
        for (int i = 0; i < max_pages; i++) {
            //      Free the frame
            //          setPage(null)
            this.pages[i].getFrame().setPage(null);
            //      Clean the page
            //          setDirty(false)
            this.pages[i].getFrame().setDirty(false);
            //      Unset reference bit
            //          setReferenced(false)
            this.pages[i].getFrame().setReferenced(false);
            //      Un-reserve frames
            //          getReserved(): task
            //          setReserved(task)
            this.pages[i].getFrame().setUnreserved(this.getTask());
        }
        // ---------------------------------------------------------------------
    }

} // end PageTable class