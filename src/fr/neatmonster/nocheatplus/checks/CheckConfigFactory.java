package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;

public interface CheckConfigFactory {
	
	public CheckConfig getConfig(Player player);
	
}
