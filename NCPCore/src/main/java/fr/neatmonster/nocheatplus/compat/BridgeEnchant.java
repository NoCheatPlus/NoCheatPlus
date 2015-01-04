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
     * @return Level of DEPTH_STRIDER capped at 3. Will return 0 if not available.
     */
    public static int getDepthStriderLevel(Player player) {
        if (DEPTH_STRIDER != null) {
            final ItemStack boots = player.getInventory().getBoots();
            if (!BlockProperties.isAir(boots)) {
                return Math.min(3, boots.getEnchantmentLevel(BridgeEnchant.DEPTH_STRIDER));
            }
        }
        return 0;
    }

    public static boolean hasDepthStrider() {
        return DEPTH_STRIDER != null;
    }

}
