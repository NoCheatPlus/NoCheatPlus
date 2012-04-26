package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.checks.CheckData;

/**
 * Player specific data for the inventory checks
 * 
 */
public class InventoryData extends CheckData {

    // Keep track of the violation levels of the three checks
    public int      dropVL;
    public int      instantBowVL;
    public double   instantEatVL;

    // Time and amount of dropped items
    public long     dropLastTime;
    public int      dropCount;

    // Times when bow shooting and eating started
    public long     lastBowInteractTime;
    public long     lastEatInteractTime;

    // What the player is eating
    public Material foodMaterial;
}
