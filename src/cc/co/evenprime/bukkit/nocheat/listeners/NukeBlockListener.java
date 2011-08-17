package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

import cc.co.evenprime.bukkit.nocheat.checks.NukeCheck;

public class NukeBlockListener extends BlockListener {

    private final NukeCheck check;

    public NukeBlockListener(NukeCheck check) {
        this.check = check;

    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        check.check(event);
    }

}
