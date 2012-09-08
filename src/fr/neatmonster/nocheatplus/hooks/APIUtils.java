package fr.neatmonster.nocheatplus.hooks;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * MMP"""""""MM MM"""""""`YM M""M M""MMMMM""M   dP   oo dP          
 * M' .mmmm  MM MM  mmmmm  M M  M M  MMMMM  M   88      88          
 * M         `M M'        .M M  M M  MMMMM  M d8888P dP 88 .d8888b. 
 * M  MMMMM  MM MM  MMMMMMMM M  M M  MMMMM  M   88   88 88 Y8ooooo. 
 * M  MMMMM  MM MM  MMMMMMMM M  M M  `MMM'  M   88   88 88       88 
 * M  MMMMM  MM MM  MMMMMMMM M  M Mb       dM   dP   dP dP `88888P' 
 * MMMMMMMMMMMM MMMMMMMMMMMM MMMM MMMMMMMMMMM                       
 */
/**
 * A class providing utilities to the NoCheatPlus API.
 * 
 * @author asofold
 */
public class APIUtils {

    /** Only the children. */
    private static final Map<CheckType, CheckType[]> childrenMap = new HashMap<CheckType, CheckType[]>();

    static {
        final Map<CheckType, Set<CheckType>> map = new HashMap<CheckType, Set<CheckType>>();
        for (final CheckType type : CheckType.values())
            map.put(type, new HashSet<CheckType>());
        for (final CheckType type : CheckType.values())
            for (final CheckType other : CheckType.values())
                if (isParent(other, type))
                    map.get(other).add(type);
        for (final CheckType parent : map.keySet())
            childrenMap.put(parent, map.get(parent).toArray(new CheckType[] {}));
    }

    /**
     * Return an unmodifiable collection of children for the given check type. Always returns a collection, does not
     * contain check type itself.
     * 
     * @param type
     *            the check type
     * @return the children
     */
    public static final Collection<CheckType> getChildren(final CheckType type) {
        return Arrays.asList(childrenMap.get(type));
    }

    /**
     * Check if the supposed parent is ancestor of the supposed child. Does not check versus the supposed child
     * directly.
     * 
     * @param supposedParent
     *            the supposed parent
     * @param supposedChild
     *            the supposed child
     * @return true, if is parent
     */
    public static final boolean isParent(final CheckType supposedParent, final CheckType supposedChild) {
        CheckType parent = supposedChild.getParent();
        while (parent != null)
            if (parent == supposedParent)
                return true;
            else
                parent = parent.getParent();
        return false;
    }

    /**
     * Return if the check type requires synchronization.
     * <hr>
     * The should be chat checks, currently.
     * 
     * @param type
     *            the check type
     * @return true, if successful
     */
    public static final boolean needsSynchronization(final CheckType type) {
        return type == CheckType.CHAT || isParent(CheckType.CHAT, type);
    }

}
