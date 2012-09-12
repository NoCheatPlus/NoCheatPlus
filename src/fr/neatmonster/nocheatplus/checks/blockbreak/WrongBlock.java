package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;

public class WrongBlock extends Check {

	public WrongBlock() {
		super(CheckType.BLOCKBREAK_WRONGBLOCK);
	}
	
	/**
	 * Check if the player destroys another block than interacted with last.<br>
	 * This does occasionally trigger for players that destroy grass or snow, 
	 * probably due to packet delaying issues for insta breaking. very effective against some nuker techniques.
	 * @param player
	 * @param block
	 * @return
	 */
	public boolean check(final Player player, final Block block){
        final BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
        final BlockBreakData data = BlockBreakData.getData(player);
        
        boolean cancel = false;
        if (data.clickedX != block.getX() || data.clickedZ != block.getZ() || data.clickedY != block.getY()){
        	final long now = System.currentTimeMillis();
        	data.wrongBlockVL.add(now, 1f);
        	if (executeActions(player, data.wrongBlockVL.getScore(0.9f), 1D, cc.wrongBlockActions))
        		cancel = true;
        	if (Improbable.check(player, 5.0f, now))
        		cancel = true;
        	// Reset, to prevent endless violation level farming.
    		data.fastBreakDamageTime = now;
    		data.clickedX = block.getX();
    		data.clickedY = block.getY();
    		data.clickedZ = block.getZ();
        }
        
        return cancel;
	}

}
