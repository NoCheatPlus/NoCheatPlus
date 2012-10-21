package fr.neatmonster.nocheatplus.hooks;

/**
 * Interface to indicate an object is just used to collect stats,
 * it can not cancel vl-processing. If ILast is implemented as well, 
 * this will be sorted after hooks that can cancel, otherwise before those.
 * @author mc_dev
 *
 */
public interface IStats {
}
