package fr.neatmonster.nocheatplus.checks.moving.model;

/**
 * This is for tracking what medium a player has been in before lift-off.
 * @author mc_dev
 *
 */
public enum MediumLiftOff {
	/** Ordinary ground, normal jumping. */
	GROUND,
	/** 
	 * Used for reduced jumping height. Until known better this is used for liquids, cobweb.
	 */
	LIMIT_JUMP,
	
	// TODO: Might add NO_JUMP (web?).
}
