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

import osp.IFLModules.IflFrameTableEntry;

/**
 * The FrameTableEntry class contains information about a specific page
 * frame of memory.
 */

public class FrameTableEntry extends IflFrameTableEntry {
    /**
     * The frame constructor.
     */
    public FrameTableEntry(int frameID) {
        super(frameID);
    }

} // end FrameTableEntry class

