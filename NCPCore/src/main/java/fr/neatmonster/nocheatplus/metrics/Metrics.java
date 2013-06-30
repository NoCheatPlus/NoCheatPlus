package fr.neatmonster.nocheatplus.metrics;

import java.io.IOException;

import org.bukkit.plugin.Plugin;

/**
 * Extend modified Metrics class, might one day route to multiple backends.
 * @author mc_dev
 *
 */
public class Metrics extends fr.neatmonster.nocheatplus.metrics.org.mcstats.Metrics{

	public Metrics(Plugin plugin) throws IOException {
		super(plugin);
	}

}