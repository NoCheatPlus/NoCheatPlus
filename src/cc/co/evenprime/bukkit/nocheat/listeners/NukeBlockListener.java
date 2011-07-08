package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

import cc.co.evenprime.bukkit.nocheat.checks.NukeCheck;

public class NukeBlockListener extends BlockListener {

	private NukeCheck check;
	
	public NukeBlockListener(NukeCheck check) {
		this.check = check;
		
	}
	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		
		//System.out.println("Damage "+ event.getInstaBreak() + " " + event.getItemInHand() + " " + event.getBlock());
		check.check(event);
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		//System.out.println("Break "+ event.getPlayer() + " " + event.getBlock());
		check.check(event);
	}
	
}
