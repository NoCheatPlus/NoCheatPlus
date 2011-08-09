package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

/**
 * 
 * @author Evenprime
 * 
 */
public abstract class Check {

    private boolean         active              = false;
    private boolean         listenersRegistered = false;
    private final int       permission;
    private final String    name;
    protected final NoCheat plugin;

    // Should OPs be checked if Permissions plugin is not available?
    public boolean          checkOPs;

    protected Check(NoCheat plugin, String name, int permission, NoCheatConfiguration config) {
        this.plugin = plugin;
        this.permission = permission;
        this.name = name;

        try {
            checkOPs = config.getBooleanValue(name + ".checkops");
        } catch(ConfigurationException e) {
            checkOPs = false;
        }

        configure(config);
    }

    public boolean skipCheck(Player player) {
        // Should we check at all?
        return !active || plugin.hasPermission(player, permission, checkOPs);
    }

    protected abstract void configure(NoCheatConfiguration config);

    protected abstract void registerListeners();

    public boolean isActive() {
        return active;
    }

    protected void setActive(boolean active) {
        synchronized(this) {
            if(active && !listenersRegistered) {
                listenersRegistered = true;
                registerListeners();
            }
        }

        // There is no way to unregister listeners ...
        this.active = active;
    }

    public String getName() {
        return name;
    }

}
