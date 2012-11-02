package fr.neatmonster.nocheatplus.components;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Interface to indicate a component is associated with a CheckType.
 * @author mc_dev
 *
 */
public interface IHaveCheckType {
	public CheckType getCheckType();
}
