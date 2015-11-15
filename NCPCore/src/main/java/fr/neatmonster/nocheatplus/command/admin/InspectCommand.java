package fr.neatmonster.nocheatplus.command.admin;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class InspectCommand extends BaseCommand {
    private static final DecimalFormat f1 = new DecimalFormat("#.#");

    public InspectCommand(JavaPlugin plugin) {
        super(plugin, "inspect", Permissions.COMMAND_INSPECT);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String alias, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                args = new String[]{args[0], sender.getName()};
            } else {
                sender.sendMessage(TAG + "Please specify a player to inspect.");
                return true;
            }
        }
        final ChatColor c1;
        if (sender instanceof Player) {
            c1 = ChatColor.GRAY;
        } else {
            c1 = null;
        }
        for (int i = 1; i < args.length; i++) {
            final Player player = DataManager.getPlayer(args[i].trim().toLowerCase());
            if (player == null) {
                sender.sendMessage("(Not online: " + args[i] + ")");
            } else {
                sender.sendMessage(getInspectMessage(player, c1));
            }
        }
        return true;
    }

    public static String getInspectMessage(final Player player, final ChatColor contentColor) {
        final String c1 = contentColor == null ? "" : contentColor.toString();
        final StringBuilder builder = new StringBuilder(256);
        builder.append(player.getName() + c1);
        builder.append(" (" + (player.isOnline() ? "online" : "offline") + "," + (player.isOp() ? "!OP!," : "") + player.getGameMode() + (player.isDead() ? ",dead" : "") + (player.isValid() ? "" : ",invalid") + (player.isInsideVehicle() ? (",vehicle=" + player.getVehicle().getType() + "@" + locString(player.getVehicle().getLocation())) : "")+ "):");
        // TODO: isValid, isDead,  isInsideVehicle ...
        // Health.
        builder.append(" health=" + f1.format(BridgeHealth.getHealth(player)) + "/" + f1.format(BridgeHealth.getMaxHealth(player)));
        // Food.
        builder.append(" food=" + player.getFoodLevel());
        // Exp.
        if (player.getExp() > 0f) {
            builder.append(" explvl=" + f1.format(player.getExpToLevel()) + "(exp=" + f1.format(player.getExp()) + ")");
        }
        // Fly settings.
        if (player.isFlying()) {
            builder.append(" flying");
        }
        if (player.getAllowFlight()) {
            builder.append(" allowflight");
        }
        // Speed settings.
        builder.append(" flyspeed=" + player.getFlySpeed());
        builder.append(" walkspeed=" + player.getWalkSpeed());
        // Potion effects.
        final Collection<PotionEffect> effects = player.getActivePotionEffects();
        if (!effects.isEmpty()) {
            builder.append(" effects=");
            for (final PotionEffect effect : effects) {
                builder.append(effect.getType() + "@" + effect.getAmplifier() +",");
            }
        }
        // TODO: is..sneaking,sprinting,blocking,
        // Finally the block location.
        final Location loc = player.getLocation();
        builder.append(" pos=" + locString(loc));
        return builder.toString();
    }

    private static final String locString(Location loc) {
        return loc.getWorld().getName() + "/" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
            String alias, String[] args) {
        // Complete players.
        return null;
    }



}
