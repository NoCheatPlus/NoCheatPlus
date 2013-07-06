package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Quick replacement for InstantEat, partly reusing InstantEat data.<br>
 * This check is added by fr.neatmonster.nocheatplus.compat.DefaultComponentFactory.
 * @author mc_dev
 *
 */
public class FastConsume extends Check implements Listener{
	
	

	public static void testAvailability(){
		if (!PlayerItemConsumeEvent.class.getSimpleName().equals("PlayerItemConsumeEvent")){
			throw new RuntimeException("This exception should not even get thrown.");
		}
	}
	
	public FastConsume() {
		super(CheckType.INVENTORY_FASTCONSUME);
		// Overrides the instant-eat check.
		ConfigManager.setForAllConfigs(ConfPaths.INVENTORY_INSTANTEAT_CHECK, false);
		LogUtil.logInfo("[NoCheatPlus] Inventory checks: FastConsume is available, disabled InstantEat.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemConsume(final PlayerItemConsumeEvent event){
		final Player player = event.getPlayer();
		if (!isEnabled(player)) return;
		final InventoryData data = InventoryData.getData(player);
		if (check(player, event.getItem(), data)){
			event.setCancelled(true);
			DataManager.getPlayerData(player.getName(), true).task.updateInventory();
		}
		data.instantEatInteract = 0;
		data.instantEatFood = null;
	}
	
	private boolean check(final Player player, final ItemStack stack, final InventoryData data){
		// Uses the instant-eat data for convenience.
		// Consistency checks...
		if (stack == null){ // || stack.getType() != data.instantEatFood){
			// TODO: Strict version should prevent other material (?).
			return false;
		}
		final long time = System.currentTimeMillis();
		final long ref = Math.max(data.instantEatInteract, data.lastClickTime);
		if (time < ref){
			return false;
		}
		// Check exceptions.
		final InventoryConfig cc = InventoryConfig.getConfig(player);
		final Material mat = stack == null ? null : stack.getType();
		if (mat != null){
			if (cc.fastConsumeWhitelist){
				if (!cc.fastConsumeItems.contains(mat.getId())){
					return false;
				}
			}
			else if (cc.fastConsumeItems.contains(mat.getId())){
				return false;
			}
		}
		// Actually check.
		final long timeSpent = ref == 0 ? 0 : (time - ref); // Not interact = instant.
		final long expectedDuration = cc.fastConsumeDuration;
		if (timeSpent < expectedDuration){
			// TODO: Might have to do a specialized check for lag spikes here instead.
			final float lag = TickTask.getLag(expectedDuration);
			if (timeSpent * lag < expectedDuration){
				final double difference = (expectedDuration - timeSpent * lag) / 100.0;
				data.instantEatVL += difference;
				final ViolationData vd = new ViolationData(this, player, data.instantEatVL, difference, cc.fastConsumeActions);
				vd.setParameter(ParameterName.FOOD, "" + mat);
				if (data.instantEatFood != mat){
					vd.setParameter(ParameterName.TAGS, "inconsistent(" + data.instantEatFood + ")");
				}
				else{
					vd.setParameter(ParameterName.TAGS, "");
				}
				if (executeActions(vd)){
					return true;
				}
			}
		}
		else{
			data.instantEatVL *= 0.6; 
		}
		return false;
	}

}
