package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class Gutenberg extends Check implements Listener {

    public static void testAvailability(){
        if (!PlayerEditBookEvent.class.getSimpleName().equals("PlayerEditBookEvent")){
            throw new RuntimeException("This exception should not even get thrown.");
        }
    }

    public Gutenberg() {
        super(CheckType.INVENTORY_GUTENBERG);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEditBook(final PlayerEditBookEvent event) {
        final Player player = event.getPlayer();
        if (!isEnabled(player)) {
            return;
        }
        final InventoryConfig cc = InventoryConfig.getConfig(player);
        final BookMeta newMeta = event.getNewBookMeta();
        final int pages = newMeta.getPageCount();
        if (pages <= 50) {
            // Legitimate.
            return;
        }
        // Violation.
        final int vl = pages - 50;
        if (executeActions(player, vl, vl, cc.gutenbergActions).willCancel()) {
            event.setCancelled(true);
        }
    }

}
