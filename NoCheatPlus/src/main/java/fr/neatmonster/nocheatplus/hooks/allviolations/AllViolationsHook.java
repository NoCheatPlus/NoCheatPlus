/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
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
        boolean debugSet = false;
        if (config.debugOnly || config.debug) {
            // TODO: Better mix the debug flag into IViolationInfo, for best performance AND consistency.
            // TODO: (If debug is not in IViolationInfo, switch to PlayerData.isDebug(CheckType).)
            final IPlayerData pData = DataManager.getPlayerData(player);
            debugSet = pData.isDebugActive(checkType);
            if (config.debugOnly && !debugSet) {
                return false;
            }

        }
        log(checkType, player, info, config.allToTrace || debugSet, config.allToNotify);
        return false;
    }

    private void log(final CheckType checkType, final Player player, final IViolationInfo info, final boolean toTrace, final boolean toNotify) {
        // Generate the message.
        // TODO: More colors?
        final StringBuilder builder = new StringBuilder(300);
        final String playerName = player.getName();
        builder.append("[VL] [" + checkType.toString() + "] ");
        builder.append("[" + ChatColor.YELLOW + playerName);
        builder.append(ChatColor.WHITE + "] ");
        final String displayName = ChatColor.stripColor(player.getDisplayName()).trim();
        if (!playerName.equals(displayName)) {
            builder.append("[->" + ChatColor.YELLOW + displayName + ChatColor.WHITE + "] ");
        }
        builder.append("VL=" + StringUtil.fdec1.format(info.getTotalVl()));
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
        // Send the message.
        final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        if (toNotify) {
            logManager.info(Streams.NOTIFY_INGAME, message);
        }
        if (toTrace) {
            logManager.info(Streams.TRACE_FILE, ChatColor.stripColor(message));
        }
    }

}
