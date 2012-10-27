package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class Items extends Check{
    
    private static Items instance = null;

    public Items() {
        super(CheckType.INVENTORY_ITEMS);
        instance = this;
    }
    
    /**
     * 
     * @param player
     * @param stack
     * @return True if the check is failed.
     */
    public static final boolean checkIllegalEnchantments(final Player player, final ItemStack stack){
        if (stack == null) return false;
        final Material type = stack.getType();
        // Fastest checks first.
        // TODO: Make stuff configurable.
        if (type == Material.WRITTEN_BOOK){
            if (!stack.getEnchantments().isEmpty() && instance.isEnabled(player)){
                // TODO: differentiate sub checks maybe or add extra permissions, later.
                for (final Enchantment ench : stack.getEnchantments().keySet()){
                    stack.removeEnchantment(ench);
                }
                // TODO: actions and similar.
                return true;
            }
        }
        return false;
    }

}
