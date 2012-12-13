package fr.neatmonster.nocheatplus.hooks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;

/*
 * MMP"""""""MM dP                  dP                                dP   
 * M' .mmmm  MM 88                  88                                88   
 * M         `M 88d888b. .d8888b. d8888P 88d888b. .d8888b. .d8888b. d8888P 
 * M  MMMMM  MM 88'  `88 Y8ooooo.   88   88'  `88 88'  `88 88'  `""   88   
 * M  MMMMM  MM 88.  .88       88   88   88       88.  .88 88.  ...   88   
 * M  MMMMM  MM 88Y8888' `88888P'   dP   dP       `88888P8 `88888P'   dP   
 * MMMMMMMMMMMM                                                            
 * 
 * M"""""""`YM MM'""""'YMM MM"""""""`YM M""MMMMM""MM                   dP       
 * M  mmmm.  M M' .mmm. `M MM  mmmmm  M M  MMMMM  MM                   88       
 * M  MMMMM  M M  MMMMMooM M'        .M M         `M .d8888b. .d8888b. 88  .dP  
 * M  MMMMM  M M  MMMMMMMM MM  MMMMMMMM M  MMMMM  MM 88'  `88 88'  `88 88888"   
 * M  MMMMM  M M. `MMM' .M MM  MMMMMMMM M  MMMMM  MM 88.  .88 88.  .88 88  `8b. 
 * M  MMMMM  M MM.     .dM MM  MMMMMMMM M  MMMMM  MM `88888P' `88888P' dP   `YP 
 * MMMMMMMMMMM MMMMMMMMMMM MMMMMMMMMMMM MMMMMMMMMMMM                            
 */
/**
 * Extend this class for maximum future compatibility.<br>
 * Especially the onCheckFailure method might get extended with check specific arguments, this class will provide
 * compatibility with older method signatures, where possible.
 * 
 * @author asofold
 */
public abstract class AbstractNCPHook implements NCPHook {
    /**
     * 
     * @deprecated See new signature in NCPHook.
     * @param checkType
     * @param player
     * @return
     */
    public boolean onCheckFailure(CheckType checkType, Player player){
        // Implemented because of API change.
        return false;
    }

    @Override
    public boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        // Kept for compatibility reasons.
        return onCheckFailure(checkType, player);
    }
    
}