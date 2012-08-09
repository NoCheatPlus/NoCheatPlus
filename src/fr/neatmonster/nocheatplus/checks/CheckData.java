package fr.neatmonster.nocheatplus.checks;


/**
 * This is for future purposes. Might remove...<br>
 * Some checks in NoPwnage synchronize over data, so using this from exectueActions can deadlock (!).<br>
 * One might think of making this an interface not for the internally used data, but for copy of data for external use only.
 * Then sync could go over other objects for async access. 
 * 
 * @author mc_dev
 *
 */
public interface CheckData {

}
