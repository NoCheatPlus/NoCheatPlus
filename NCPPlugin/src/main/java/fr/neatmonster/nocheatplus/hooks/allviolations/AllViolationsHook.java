package fr.neatmonster.nocheatplus.hooks.allviolations;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.ILast;
import fr.neatmonster.nocheatplus.hooks.IStats;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Default hook for logging all violations in a generic way. This will not log
 * if violation processing got cancelled by compatibility hooks.
 * 
 * @author asofold
 *
 */
public class AllViolationsHook implements NCPHook, ILast, IStats {

    private AllViolationsConfig config;
    private Integer hookId = null;
    
    /** White list. */
    private final ParameterName[] parameters;
    private final String[] noParameterTexts;

    public AllViolationsHook() {
        Collection<ParameterName> parameters = new LinkedHashSet<ParameterName>();
        for (ParameterName name : ParameterName.values()) {
            parameters.add(name);
        }
        for (ParameterName name : Arrays.asList(ParameterName.PLAYER, ParameterName.PLAYER_NAME, ParameterName.PLAYER_DISPLAY_NAME,
                ParameterName.IP, ParameterName.CHECK, ParameterName.UUID, ParameterName.VIOLATIONS, ParameterName.WORLD)) {
            parameters.remove(name);
        }
        this.parameters = parameters.toArray(new ParameterName[parameters.size()]);
        noParameterTexts = new String[parameters.size()];
        for (int i = 0; i < this.parameters.length; i++) {
            this.noParameterTexts[i] = "[" + this.parameters[i].getText() + "]";
        }
    }

    public void setConfig(AllViolationsConfig config) {
        this.config = config;
        if (config == null || !config.doesLogAnything()) {
            unregister();
        } else {
            register();
        }
    }

    public void unregister() {
        if (hookId != null) {
            NCPHookManager.removeHook(this);
            this.hookId = null;
        }
    }

    public void register() {
        unregister();
        this.hookId = NCPHookManager.addHook(CheckType.ALL, this);
    }

    @Override
    public String getHookName() {
        return "AllViolations(NCP)";
    }

    @Override
    public String getHookVersion() {
        return "1.0";
    }

    @Override
    public boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        final AllViolationsConfig config = this.config;
        if (config == null) {
            return false;
        }
        // Generate message.
        // TODO: More colors?
        final StringBuilder builder = new StringBuilder(300);
        final String playerName = player.getName();
        builder.append("[VL] " + ChatColor.YELLOW + playerName);
        final String displayName = ChatColor.stripColor(player.getDisplayName()).trim();
        if (!playerName.equals(displayName)) {
            builder.append(" -> " + displayName);
        }
        builder.append(ChatColor.WHITE + " ");
        builder.append(checkType.toString());
        builder.append(" VL=" + StringUtil.fdec1.format(info.getTotalVl()));
        builder.append("(+" + StringUtil.fdec1.format(info.getAddedVl()) + ")");
        builder.append(ChatColor.GRAY);
        for (int i = 0; i < parameters.length; i++) {
            final ParameterName name = parameters[i];
            final String value = info.getParameter(name);
            if (value != null && !value.isEmpty() && !value.equals(this.noParameterTexts[i])) {
                builder.append(" " + name.getText() + "=" + value);
            }
        }
        final String message = builder.toString();
        // Send to where it is appropriate.
        final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        if (config.allToNotify) {
            logManager.info(Streams.NOTIFY_INGAME, message);
        }
        if (config.allToTrace) {
            logManager.info(Streams.TRACE_FILE, ChatColor.stripColor(message));
        }
        return false;
    }

}
