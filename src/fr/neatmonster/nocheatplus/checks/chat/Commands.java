package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Check only for commands
 * @author mc_dev
 *
 */
public class Commands extends Check {
	public Commands() {
		super(CheckType.CHAT_COMMANDS);
	}

    public boolean check(final Player player, final String message, final Captcha captcha) {
        
        final long now = System.currentTimeMillis();
        final int tick = TickTask.getTick();
        
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);
        
        // Weight might later be read from some prefix tree (also known / unknown).
        final float weight = 1f;
        
        data.commandsWeights.add(now, weight);
        if (tick - data.commandsShortTermTick < cc.commandsShortTermTicks){
            // Add up.
            data.commandsShortTermWeight += weight;
        }
        else{
            // Reset.
            data.commandsShortTermTick = tick;
            data.commandsShortTermWeight = 1.0;
        }
        
        final double violation = Math.max(data.commandsWeights.getScore(1f) - cc.commandsLevel, data.commandsShortTermWeight - cc.commandsShortTermLevel);
        
        if (violation > 0.0){
            data.commandsVL += violation;
            if (executeActions(player, data.commandsVL, violation, cc.commandsActions))
                return true;
        }
        else{
            // TODO: This might need invalidation with time.
            data.commandsVL *= 0.99;
        }
        return false;
    }

}
