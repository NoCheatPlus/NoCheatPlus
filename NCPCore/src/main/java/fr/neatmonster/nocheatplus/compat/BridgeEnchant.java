package fr.neatmonster.nocheatplus.compat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

    private final static Enchantment THORNS = parseEnchantment("THORNS");

    /**
     * 
     * @param player
     * @return Maximum level of DEPTH_STRIDER found on armor items, capped at 3. Will return 0 if not available.
     */
    public static int getDepthStriderLevel(final Player player) {
        int level = 0;
        if (DEPTH_STRIDER != null) {
            // Find the maximum level of depth strider.
            final ItemStack[] armor = player.getInventory().getArmorContents();
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

    /**
     * Check if a player might return some damage due to the "thorns" enchantment.
     * @param player
     * @return
     */
    public static boolean hasThorns(final Player player) {
        if (THORNS == null) {
            return false;
        }
        final PlayerInventory inv = player.getInventory();
        final ItemStack[] contents = inv.getArmorContents();
        for (int i = 0; i < contents.length; i++){
            final ItemStack stack = contents[i];
            if (stack != null && stack.getEnchantmentLevel(THORNS) > 0){
                return true;
            }
        }
        return false;
    }

}
