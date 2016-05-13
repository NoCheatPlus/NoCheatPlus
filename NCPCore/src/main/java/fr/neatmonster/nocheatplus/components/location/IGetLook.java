package fr.neatmonster.nocheatplus.components.location;

/**
 * Having a looking direction, represented by pitch and yaw.
 * 
 * @author asofold
 *
 */
public interface IGetLook {

    /**
     * Angle for vertical looking direction component in grad.
     * 
     * @return
     */
    public float getPitch();

    /**
     * Angle on xz-plane for the looking direction in grad.
     * 
     * @return
     */
    public float getYaw();

}
