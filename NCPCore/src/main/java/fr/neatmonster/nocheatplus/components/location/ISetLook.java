package fr.neatmonster.nocheatplus.components.location;

/**
 * Allow setting the looking direction (float).
 * 
 * @author asofold
 *
 */
public interface ISetLook {

    /**
     * 
     * Set the angle on xz-plane for the looking direction in grad.
     * 
     * @param yaw
     */
    public void setYaw(float yaw);

    /**
     * Set the angle for vertical looking direction component in grad.
     * 
     * @param pitch
     */
    public void setPitch(float pitch);

    // public void setLook(float yaw, float pitch);

}
