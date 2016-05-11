package fr.neatmonster.nocheatplus.checks.moving.model;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Encapsulate past move tracking here.
 * 
 * @author asofold
 *
 */
public class MoveTrace {

    // TODO: Class name.
    // TODO: With splitting to PlayerMoveData and VehicleMoveData, a generic type parameter would be needed here. 

    /**
     * Keep track of past moves edge data. First entry always is the last fully
     * processed move, or invalid, even during processing. The currently
     * processed move always is currentMove. The list length always stays the
     * same.
     */
    private final LinkedList<MoveData> pastMoves = new LinkedList<MoveData>();

    /**
     * The move currently being processed. Will be inserted to first position
     * when done, and exchanged for the invalidated last element of the past
     * moves.
     */
    private MoveData currentMove = new MoveData();

    /**
     * Minimal and default size is 2 past moves with current move set extra.
     */
    public MoveTrace() {
        // Past moves data: initialize with dummies.
        for (int i = 0; i < 2; i++) { // Two past moves allow better workarounds than 1.
            pastMoves.add(new MoveData());
        }
    }

    public MoveData getCurrentMove() {
        return currentMove;
    }

    /**
     * Retrieve the number of stored past moves, disregarding the current move.
     * 
     * @return
     */
    public int getNumberOfPastMoves() {
        return pastMoves.size();
    }

    /**
     * Get the first stored past move (latest past move, the current move is not
     * considered).
     * 
     * @return
     */
    public MoveData getFirstPastMove() {
        return pastMoves.getFirst();
    }

    /**
     * Get the second stored past move (second latest past move, the current
     * move is not considered).
     * 
     * @return
     */
    public MoveData getSecondPastMove() {
        return pastMoves.get(1);
    }

    /**
     * Get past move by index (0 is the latest past move, the current move is
     * not considered).
     * 
     * @param index
     * @return
     */
    public MoveData getPastMove(final int index) {
        return pastMoves.get(index);
    }

    /**
     * Invalidate the current move and all past moves.
     */
    public void invalidate() {
        final Iterator<MoveData> it = pastMoves.iterator();
        while (it.hasNext()) {
            // TODO: If using many elements ever, stop at the first already invalidated one.
            it.next().invalidate();
        }
        currentMove.invalidate();
    }

    /**
     * Call after processing with a valid the current move field. Insert the
     * current move as first in past moves, set the current move to the
     * invalidated last element of the past moves.
     */
    public void finishCurrentMove() {
        pastMoves.addFirst(currentMove);
        currentMove = pastMoves.removeLast();
        currentMove.invalidate();
    }

}
