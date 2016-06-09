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

}
