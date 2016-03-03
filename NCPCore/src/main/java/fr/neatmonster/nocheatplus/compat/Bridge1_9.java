package fr.neatmonster.nocheatplus.compat;

import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class Bridge1_9 {

    private static final PotionEffectType LEVITATION = PotionEffectType.getByName("LEVITATION");

    private static final Material END_ROD = Material.getMaterial("END_ROD");

    private static final Material ELYTRA = Material.getMaterial("ELYTRA");

    private static Method getItemInOffHand = ReflectionUtil.getMethodNoArgs(PlayerInventory.class, "getItemInOffHand", ItemStack.class);

    public static boolean hasLevitation() {
        return LEVITATION != null;
    }

    public static boolean hasEndRod() {
        return END_ROD != null;
    }

    public static boolean hasElytra() {
        return ELYTRA != null;
    }

    public static boolean hasGetItemInOffHand() {
        return getItemInOffHand != null;
    }

    /**
     * Test for the 'levitation' potion effect.
     * 
     * @param player
     * @return Double.NEGATIVE_INFINITY if not present.
     */
    public static double getLevitationAmplifier(final Player player) {
        if (LEVITATION == null) {
            return Double.NEGATIVE_INFINITY;
        }
        return PotionUtil.getPotionEffectAmplifier(player, LEVITATION);
    }

    /**
     * Check if a player has an elytra equipped and is holding an end rod. This
     * doesn't check for other conditions, like if the player is in air.
     * 
     * @param player
     * @return
     */
    public static boolean isReadyForElytra(final Player player) {
        // TODO: Off-hand testing needs to be supported, pass an instance for access.
        // TODO: Generic jet-pack / potion support (triggers + model configs).
        if (END_ROD == null || ELYTRA == null) {
            return false;
        }
        ItemStack stack = player.getInventory().getChestplate();
        if (stack == null || stack.getType() != ELYTRA) {
            return false;
        }
        if (!hasItemInAnyHand(player, END_ROD)) {
            return false;
        }
        // Possible to use elytra.
        return true;
    }

    /**
     * Test if a player has an item of the specified type in any hand.
     * 
     * @param player
     * @param material
     * @return
     */
    public static boolean hasItemInAnyHand(final Player player, final Material material) {
        ItemStack stack = getItemInMainHand(player);
        if (stack != null && stack.getType() == material) {
            return true;
        }
        stack = getItemInOffHand(player);
        if (stack != null && stack.getType() == material) {
            return true;
        }
        return false;
    }

    public static ItemStack getItemInMainHand(final Player player) {
        return player.getItemInHand(); // As long as feasible (see: CraftInventoryPlayer).
    }

    /**
     * Get the off hand item.
     * 
     * @param player
     * @return In case the method is not present, null will be returned.
     */
    public static ItemStack getItemInOffHand(final Player player) {
        if (getItemInOffHand == null) {
            return null;
        }
        else {
            return(ItemStack) ReflectionUtil.invokeMethodNoArgs(getItemInOffHand, player.getInventory());
        }
    }

}
