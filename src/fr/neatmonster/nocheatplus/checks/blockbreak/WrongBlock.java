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
        }
        
        return cancel;
	}

}
