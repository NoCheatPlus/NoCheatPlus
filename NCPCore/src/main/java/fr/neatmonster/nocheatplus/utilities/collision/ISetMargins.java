package fr.neatmonster.nocheatplus.utilities.collision;

public interface ISetMargins {

    /**
     * Typical player specific margins (none below feet, eye height, same
     * xzMargin to all sides from the center). Calling this may or may not have
     * effect.
     * 
     * @param height
     * @param xzMargin
     */
    public void setMargins(final double height, final double xzMargin);

    /**
     * Allow cutting off the margins opposite to a checking direction. Call
     * before loop. MAy or may not have any effect.
     * 
     * @param cutOppositeDirectionMargin
     *            If set to true, margins that are opposite to the moving
     *            direction are cut off. This is meant for setups like with
     *            moving out of blocks.
     */
    public void setCutOppositeDirectionMargin(boolean cutOppositeDirectionMargin);

}
