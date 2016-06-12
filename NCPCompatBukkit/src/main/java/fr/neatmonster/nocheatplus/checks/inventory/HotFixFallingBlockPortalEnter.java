package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Hot fix for 1.9 and 1.10 (possibly later?): falling block duplication via end
 * portals + pistons.
 * <hr>
 * For registration use NoCheatPlusAPI.addComponent.
 * 
 * @author asofold
 *
 */
public class HotFixFallingBlockPortalEnter implements Listener, MCAccessHolder {

    public static void testAvailability() {
        if (ReflectionUtil.getClass("org.bukkit.event.entity.EntityPortalEnterEvent") == null
                || ReflectionUtil.getClass("org.bukkit.entity.FallingBlock") == null) {
            throw new RuntimeException("Not available.");
        }
        if (!ServerVersion.isMinecraftVersionUnknown() && ServerVersion.compareMinecraftVersion("1.9") < 0) {
            throw new RuntimeException("Not needed.");
        }
    }

    /** Temporary use only: setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    private MCAccess mcAccess;
    private BlockCache blockCache;

    public HotFixFallingBlockPortalEnter() {
        testAvailability();
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        final Entity entity = event.getEntity();
        final Material mat;
        if (entity instanceof FallingBlock) {
            mat = ((FallingBlock) entity).getMaterial();
        }
        else if (entity instanceof Item) {
            // TODO: Not sure if needed.
            if (((Item) entity).getItemStack().getType().hasGravity()) {
                mat = ((Item) entity).getItemStack().getType();
            }
            else {
                mat = null;
            }
        }
        else {
            mat = null;
        }
        if (mat != null) {
            final Location loc = entity.getLocation(useLoc);
            final World world = loc.getWorld();
            if (InventoryConfig.getConfig(world).hotFixFallingBlockEndPortalActive) {
                blockCache.setAccess(world);
                final boolean nearbyPortal = BlockProperties.collidesId(blockCache, loc.getX() - 2.0, loc.getY() - 2.0, loc.getZ() - 2.0, loc.getX() + 3.0, loc.getY() + 3.0, loc.getZ() + 3.0, Material.ENDER_PORTAL);
                blockCache.cleanup();
                if (nearbyPortal) {
                    // Likely spigot currently removes entities entering portals anyway (cross-world teleport issues).
                    // On remove: Looks like setDropItem(false) wouldn't suffice.
                    entity.remove();
                    // TODO: STATUS: should have another stream for violations/protection.
                    final String message = "[INVENTORY_HOTFIX] Remove falling block entering a portal near an end portal: " + mat + " at " + world.getName() + "/" + LocUtil.simpleFormatPosition(loc);
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, message);
                }
            }
            useLoc.setWorld(null);
        }
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
        this.blockCache = mcAccess.getBlockCache(null);
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
    }

}
