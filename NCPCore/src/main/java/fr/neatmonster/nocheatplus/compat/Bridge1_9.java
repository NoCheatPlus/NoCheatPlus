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
package fr.neatmonster.nocheatplus.compat;

import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class Bridge1_9 {

    private static final PotionEffectType LEVITATION = PotionEffectType.getByName("LEVITATION");

    public static final Material END_ROD = Material.getMaterial("END_ROD");
    public static final Material ELYTRA = Material.getMaterial("ELYTRA");
    public static final Material END_CRYSTAL_ITEM = Material.getMaterial("END_CRYSTAL");

    private static Method getItemInOffHand = ReflectionUtil.getMethodNoArgs(PlayerInventory.class, "getItemInOffHand", ItemStack.class);
    private static Method getItemInMainHand = ReflectionUtil.getMethodNoArgs(PlayerInventory.class, "getItemInMainHand", ItemStack.class);

    private static Method isGliding = ReflectionUtil.getMethodNoArgs(Player.class, "isGliding", boolean.class);

    public static boolean hasLevitation() {
        return LEVITATION != null;
    }

    public static boolean hasEndRod() {
        return END_ROD != null;
    }

    public static boolean hasEndCrystalItem() {
        return END_CRYSTAL_ITEM != null;
    }

    public static boolean hasElytra() {
        return ELYTRA != null;
    }

    public static boolean hasGetItemInOffHand() {
        return getItemInOffHand != null;
    }

    public static boolean hasGetItemInMainHand() {
        return getItemInMainHand != null;
    }

    public static boolean hasIsGliding() {
        return isGliding != null;
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
     * Gliding + wearing elytra.
     * @param player
     * @return
     */
    public static boolean isGlidingWithElytra(final Player player) {
        return isGliding(player) && isWearingElytra(player);
    }

    /**
     * Just test if the player has elytra equipped in the chest plate slot. The
     * player may or may not be holding an end rod in either hand.
     * 
     * @param player
     * @return
     */
    public static boolean isWearingElytra(final Player player) {
        if (ELYTRA == null) {
            return false;
        }
        else {
            final ItemStack stack = player.getInventory().getChestplate();
            return stack != null && stack.getType() == ELYTRA;
        }
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

    @SuppressWarnings("deprecation")
    public static ItemStack getItemInMainHand(final Player player) {
        if (getItemInMainHand == null) {
            return player.getItemInHand(); // As long as feasible (see: CraftInventoryPlayer).
        }
        else {
            return player.getInventory().getItemInMainHand();
        }
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
            return player.getInventory().getItemInOffHand();
        }
    }

    public static boolean isGliding(final Player player) {
        if (isGliding == null) {
            return false;
        }
        else {
            return player.isGliding();
        }
    }

    /**
     * Get the item that was used with this event, assume clicking left or right rather (not feet etc.).
     * @param player
     * @param event
     */
    public static ItemStack getUsedItem(final Player player, final PlayerInteractEvent event) {
        if (!hasGetItemInOffHand()) { // Optimistic check.
            return getItemInMainHand(player);
        }
        else {
            switch (event.getHand()) {
                case HAND: {
                    return getItemInMainHand(player);
                }
                case OFF_HAND: {
                    return getItemInOffHand(player);
                }
                default: {
                    return null;
                }
            }
        }
    }
    
    /**
     * Get the item that was used with this event, assume right click (not feet etc.).
     * @param player
     * @param event
     */
    public static ItemStack getUsedItem(final Player player, final PlayerInteractEntityEvent event) {
        if (!hasGetItemInOffHand()) { // Optimistic check.
            return getItemInMainHand(player);
        }
        else {
            switch (event.getHand()) {
                case HAND: {
                    return getItemInMainHand(player);
                }
                case OFF_HAND: {
                    return getItemInOffHand(player);
                }
                default: {
                    return null;
                }
            }
        }
    }

}
