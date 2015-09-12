package fr.neatmonster.nocheatplus.compat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.utilities.BlockProperties;

public final class BridgeEnchant {

    private static final Enchantment parseEnchantment(final String name) {
        try {
            return Enchantment.getByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    private final static Enchantment DEPTH_STRIDER = parseEnchantment("DEPTH_STRIDER");

    /**
     * 
     * @param player
     * @return Maximum level of DEPTH_STRIDER found on armor items, capped at 3. Will return 0 if not available.
     */
    public static int getDepthStriderLevel(Player player) {
        int level = 0;
        if (DEPTH_STRIDER != null) {
            // Find the maximum level of depth strider.
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                final ItemStack item = armor[i];
                if (!BlockProperties.isAir(item)) {
                    level = Math.max(item.getEnchantmentLevel(DEPTH_STRIDER), level);
                }
            }
        }
        // Cap at three.
        return Math.min(3, level);
    }

    public static boolean hasDepthStrider() {
        return DEPTH_STRIDER != null;
    }

}
