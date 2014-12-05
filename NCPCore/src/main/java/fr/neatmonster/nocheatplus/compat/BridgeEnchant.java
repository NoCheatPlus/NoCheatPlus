package fr.neatmonster.nocheatplus.compat;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BridgeEnchant {

    private static Enchantment DEPTH_STRIDER = null;

    static {
        try {
            DEPTH_STRIDER = Enchantment.DEPTH_STRIDER;
        } catch (Throwable t) {}
    }

    public static int getDepthStriderLevel(Player player) {
        if (DEPTH_STRIDER != null) {
            final ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() != Material.AIR) {
                return Math.min(3, boots.getEnchantmentLevel(BridgeEnchant.DEPTH_STRIDER));
            }
        }
        return 0;
    }

    public static boolean hasDepthStrider() {
        return DEPTH_STRIDER != null;
    }

}
