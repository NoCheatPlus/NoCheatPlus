package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
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
        
        final boolean captchaEnabled = captcha.isEnabled(player); 
        if (captchaEnabled){
            synchronized (data) {
                if (captcha.shouldCheckCaptcha(cc, data)){
                    captcha.checkCaptcha(player, message, cc, data, true);
                    return true;
                }
            }
        }
        
        // Rest of the check is done without sync, because the data is only used by this check.
        
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
        
        final float nw = data.commandsWeights.getScore(1f);
        final double violation = Math.max(nw - cc.commandsLevel, data.commandsShortTermWeight - cc.commandsShortTermLevel);
        
        if (violation > 0.0){
            data.commandsVL += violation;
            // TODO: Evaluate if sync(data) is necessary or better for executeActions.
            if (captchaEnabled){
                synchronized (data) {
                    captcha.sendNewCaptcha(player, cc, data);
                }
                return true;
            }
            else if (executeActions(player, data.commandsVL, violation, cc.commandsActions))
                return true;
        }
        else if (cc.chatWarningCheck && now - data.chatWarningTime > cc.chatWarningTimeout && (100f * nw / cc.commandsLevel > cc.chatWarningLevel || 100f * data.commandsShortTermWeight / cc.commandsShortTermLevel > cc.chatWarningLevel)){
            player.sendMessage(CheckUtils.replaceColors(cc.chatWarningMessage));
            data.chatWarningTime = now;
        }
        else{
            // TODO: This might need invalidation with time.
            data.commandsVL *= 0.99;
        }
        return false;
    }

}
