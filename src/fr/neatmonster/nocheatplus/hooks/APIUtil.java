package fr.neatmonster.nocheatplus.hooks;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class APIUtil {
	
	/**
	 * Only the children.
	 */
	private static final Map<CheckType, CheckType[]> childrenMap = new HashMap<CheckType, CheckType[]>();
	
	static{
		Map<CheckType, Set<CheckType>> temp = new HashMap<CheckType, Set<CheckType>>();
		// uh uh 
		for  (CheckType checkType : CheckType.values()){
			Set<CheckType> set = new HashSet<CheckType>();
			temp.put(checkType, set);
		}
		for  (CheckType checkType : CheckType.values()){
			for (CheckType other : CheckType.values()){
				if (isParent(other, checkType)){
					 temp.get(other).add(checkType);
				}
			}
		}
		for (CheckType parent : temp.keySet()){
			Set<CheckType> set = temp.get(parent);
			CheckType[] a = new CheckType[set.size()];
			set.toArray(a);
			childrenMap.put(parent, a);
		}
	}
	
	/**
	 * Check if propablyParent is ancestor of checkType. Does not check versus checkType directly.
	 * @param probablyParent
	 * @param checkType
	 * @return
	 */
	public static final boolean isParent(final CheckType probablyParent, final CheckType checkType){
		CheckType parent = checkType.getParent();
		while (parent != null){
			if (parent == probablyParent) return true;
			else parent = parent.getParent();
		}
		return false;
	}
	
	/**
	 * Return an unmodifiable collection of children for the given check type. Always returns a collection, does not contain checkType itself.
	 * @param checkType
	 * @return
	 */
	public static final Collection<CheckType> getChildren(final CheckType checkType){
		return Arrays.asList(childrenMap.get(checkType));
	}
	
	public static final boolean needsSynchronization(final CheckType checkType){
		return checkType == CheckType.CHAT || isParent(CheckType.CHAT, checkType);
	}
	
}
