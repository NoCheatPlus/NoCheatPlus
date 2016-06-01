package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;

public class Items extends Check{

    private static Items instance = null;

    public Items() {
        super(CheckType.INVENTORY_ITEMS);
        instance = this;
    }

    /**
     * Checks for illegal enchantments (legacy). Removes enchantments from
     * WRITTEN_BOOK. Check both main and off hand.
     * 
     * @param player
     * @return True if the check is failed.
     */
    public static final boolean checkIllegalEnchantmentsAllHands(final Player player) {
        boolean result = false;
        if (checkIllegalEnchantments(player, Bridge1_9.getItemInMainHand(player))) {
            result = true;
        }
        if (Bridge1_9.hasGetItemInOffHand() && checkIllegalEnchantments(player, Bridge1_9.getItemInOffHand(player))) {
            result = true;
        }
        return result;
    }

    /**
     * Checks for illegal enchantments (legacy). Removes enchantments from
     * WRITTEN_BOOK.
     * 
     * @param player
     * @param stack
     * @return True if the check is failed.
     */
    public static final boolean checkIllegalEnchantments(final Player player, final ItemStack stack){
        if (stack == null) {
            return false;
        }
        final Material type = stack.getType();
        // Fastest checks first.
        // TODO: Make stuff configurable.
        if (type == Material.WRITTEN_BOOK){
            final Map<Enchantment, Integer> enchantments = stack.getEnchantments();
            if (enchantments != null && !enchantments.isEmpty() && instance.isEnabled(player)){
                // TODO: differentiate sub checks maybe or add extra permissions, later.
                for (final Enchantment ench : new HashSet<Enchantment>(enchantments.keySet())){
                    stack.removeEnchantment(ench);
                }
                // TODO: actions and similar.
                return true;
            }
        }
        return false;
    }

}
