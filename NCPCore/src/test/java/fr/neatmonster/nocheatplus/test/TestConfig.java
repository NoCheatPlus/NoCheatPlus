package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.bukkit.Material;
import org.junit.Test;

import fr.neatmonster.nocheatplus.config.RawConfigFile;

public class TestConfig {
	
	private void testReadMaterial(String input, Material expectedMat) {
		Material mat = RawConfigFile.parseMaterial(input);
		if (expectedMat != mat) {
			fail("Expected " + expectedMat + " for input '" + input + "', got instead: " + mat);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testReadMaterial() {
		// Some really needed parts first.
		testReadMaterial("water lily", Material.WATER_LILY);
		testReadMaterial("water-lily", Material.WATER_LILY);
		testReadMaterial("watEr_lily", Material.WATER_LILY);
		
		testReadMaterial("flint and steel", Material.FLINT_AND_STEEL);
		testReadMaterial("259", Material.FLINT_AND_STEEL);
		
		// Generic test.
		for (final Material mat : Material.values()) {
			if (mat.name().equalsIgnoreCase("LOCKED_CHEST")) {
				continue;
			}
			testReadMaterial(mat.name(), mat);
			testReadMaterial(Integer.toString(mat.getId()), mat);
		}
		
	}
}
