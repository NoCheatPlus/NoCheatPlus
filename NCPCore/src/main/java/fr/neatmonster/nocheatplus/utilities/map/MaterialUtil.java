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
package fr.neatmonster.nocheatplus.utilities.map;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.BridgeMaterial;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Auxiliary collections and functionality for Material.
 * 
 * @author asofold
 *
 */
public class MaterialUtil {

    // TODO: Naming conventions for collections (_BLOCKS suffix?).


    ///////////////////////
    // Internal
    ///////////////////////

    private static final List<String> woodTypes = Arrays.asList(
            "acacia", "birch", "dark_oak", "jungle", "oak", "spruce", 
            "wood" // Legacy
            );

    /**
     * Get a new set containing the given set, as well as all non-null results
     * from names.
     * 
     * @param set
     * @param names
     * @return
     */
    private static Set<Material> add(Set<Material> set, String... names) {
        final LinkedHashSet<Material> res = new LinkedHashSet<Material>(set);
        res.addAll(BridgeMaterial.getAll(names));
        return res;
    }

    /**
     * Dump public static fields of type Set<?> to StaticLog, using
     * StringUtil.join (not recursive).
     */
    public static void dumpStaticSets(final Class<?> clazz, final Level level) {
        final StringBuilder builder = new StringBuilder(6000);
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) 
                    && Modifier.isPublic(field.getModifiers())) {
                try {
                    Object obj = field.get(clazz);
                    if (obj instanceof Set<?>) {
                        Set<?> set = (Set<?>) obj;
                        builder.append(clazz.getName());
                        builder.append('.');
                        builder.append(field.getName());
                        builder.append(": ");
                        StringUtil.join(set, ", ", builder);
                        builder.append('\n');
                    }
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
        }
        StaticLog.log(level, builder.toString());
    }

    /////////////////////////////////////////////////
    // Material collections with common properties
    // (May not always have all aspects in common.)
    /////////////////////////////////////////////////

    public static final Set<Material> BANNERS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    null, 
                    Arrays.asList("_banner"),
                    AlmostBoolean.YES,
                    "legacy", "_wall"
                    ), "standing_banner"));

    public static final Set<Material> BEDS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_bed", AlmostBoolean.YES, 
                    "legacy"), "bed_block"));

    public static final Set<Material> BOATS;
    static {
        HashSet<Material> temp = new HashSet<Material>();
        if (BridgeMaterial.get("boat") != null) {
            temp.add(BridgeMaterial.get("boat"));
        }
        temp.addAll(InventoryUtil.collectItemsByPrefix("BOAT_"));
        temp.addAll(InventoryUtil.collectItemsBySuffix("_BOAT"));
        BOATS = Collections.unmodifiableSet(temp);
    }

    /** Bushes (block). */
    public static final Set<Material> BUSHES = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("bush", AlmostBoolean.YES, "legacy", "potted"));

    public static final Set<Material> CARPETS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_carpet", AlmostBoolean.YES, "legacy"), 
            "carpet"));

    public static final Set<Material> CONCRETE_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_concrete", AlmostBoolean.YES, "legacy"),
            "concrete"));

    public static final Set<Material> CONCRETE_POWDER_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_concrete_powder", AlmostBoolean.YES, "legacy"),
            "concrete_powder"));

    /** Dead or alive. */
    public static final Set<Material> CORAL_BLOCKS = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("coral_block", AlmostBoolean.YES, "legacy")
            );

    /**
     * Dead coral parts, that have been passable alive, but which are like stone
     * when dead.
     */
    public static final Set<Material> DEAD_CORAL_PARTS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    Arrays.asList("dead_"),
                    Arrays.asList("coral_fan", "coral_wall_fan", "coral"),
                    AlmostBoolean.YES, 
                    "block", "legacy"
                    ));

    /** Flower pot and potted plants / things. */
    public static final Set<Material> FLOWER_POTS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefix(
                    "potted_", AlmostBoolean.YES), "flower_pot"));

    /** Stained and other. */
    public static final Set<Material> GLASS_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_glass", AlmostBoolean.YES, "legacy"),
            "glass"));

    public static final Set<Material> GLASS_PANES = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_glass_pane", AlmostBoolean.YES, "legacy"), 
            "glass_pane", "thin_glass"));

    public static final Set<Material> GLAZED_TERRACOTTA_BLOCKS = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("glazed_terracotta", AlmostBoolean.YES, "legacy")
            );

    /** Heads placed on the ground. Includes skulls. */
    public static final Set<Material> HEADS_GROUND = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    null, 
                    Arrays.asList("_skull", "_head"),
                    AlmostBoolean.YES,
                    "legacy", "_wall_"
                    ), "skull"));

    /** Heads placed on the wall. Includes skulls. */
    public static final Set<Material> HEADS_WALL = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    null, 
                    Arrays.asList("_wall_skull", "_wall_head"),
                    AlmostBoolean.YES,
                    "legacy"
                    ));

    /** Blocks that are infested with silverfish. */
    public static final Set<Material> INFESTED_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefix("infested_", AlmostBoolean.YES), "monster_eggs"));

    /** All lava blocks. */
    public static final Set<Material> LAVA = Collections.unmodifiableSet(
            BridgeMaterial.getAll("lava", "stationary_lava"));

    public static final Set<Material> LEAVES = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_leaves"), // Strictly _pressure_plate for 1.13.
                    AlmostBoolean.YES
                    // , ...
                    ), "leaves", "leaves_2"));

    /** LOGS. */
    public static final Set<Material> LOGS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_log"),
                    AlmostBoolean.YES,
                    "legacy"
                    ), "log", "log_2"));

    /** Mushroom blocks (huge ones). */ 
    public static final Set<Material> MUSHROOM_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix(
                    "_mushroom_block",
                    AlmostBoolean.YES,
                    "legacy"
                    ), "mushroom_stem" , "huge_mushroom_1", "huge_mushroom_2"));

    /** Coral parts that are passable when alive, but become solid when dead. */
    public static final Set<Material> PASSABLE_CORAL_PARTS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    null,
                    Arrays.asList("coral_fan", "coral_wall_fan", "coral"),
                    AlmostBoolean.YES, 
                    "dead", "legacy"
                    ));

    public static final Set<Material> PLANKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_planks", AlmostBoolean.YES, "legacy"),
            "wood"));

    /** All rail types. */
    public static final Set<Material> RAILS = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix(Arrays.asList("rail", "rails"), AlmostBoolean.YES, "legacy"));

    /** Places saplings (block). */
    public static final Set<Material> SAPLINGS = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("sapling", AlmostBoolean.YES, "legacy", "potted"));

    public static final Set<Material> SHULKER_BOXES = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("shulker_box", AlmostBoolean.YES, "legacy"));

    public static final Set<Material> SPAWN_EGGS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix("_spawn_egg", AlmostBoolean.YES, "legacy"),
            "monster_egg"
            ));

    public static final Set<Material> STRIPPED_LOGS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    Arrays.asList("stripped_"), 
                    Arrays.asList("_log"), AlmostBoolean.YES)
            );

    public static final Set<Material> STRIPPED_WOOD_BLOCKS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    Arrays.asList("stripped_"), 
                    Arrays.asList("_wood"), AlmostBoolean.YES)
            );

    /** All ordinary terracotta (hard clay) blocks. */
    public static final Set<Material> TERRACOTTA_BLOCKS = Collections.unmodifiableSet(add(
            // TODO: exclude GLAZED or not?
            BridgeMaterial.getByPrefixAndSuffix(
                    null, Arrays.asList("_terracotta"), 
                    AlmostBoolean.YES, 
                    "legacy", "glazed"),
            "terracotta", "hard_clay", "stained_clay"
            ));

    /** Tulips (block). */
    public static final Set<Material> TULIPS = Collections.unmodifiableSet(
            BridgeMaterial.getBySuffix("tulip", AlmostBoolean.YES, "legacy", "potted"));

    public static final Set<Material> WALL_BANNERS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    null, 
                    Arrays.asList("_wall_banner"),
                    AlmostBoolean.YES,
                    "legacy"
                    ), "wall_banner"));

    /** All water blocks. */
    public static final Set<Material> WATER = Collections.unmodifiableSet(
            BridgeMaterial.getAll("water", "stationary_water"));

    /** Wood types (1.13 rather). */
    public static final Set<Material> WOOD_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_wood"),
                    AlmostBoolean.YES
                    // , ...
                    ), "wood"));

    public static final Set<Material> WOODEN_BUTTONS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_button"),
                    AlmostBoolean.YES
                    ));

    public static final Set<Material> WOODEN_DOORS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_door"),
                    AlmostBoolean.YES,
                    "trap"
                    ));

    public static final Set<Material> WOODEN_FENCES = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_fence"),
                    AlmostBoolean.YES
                    // , ...
                    ), "fence"));

    public static final Set<Material> WOODEN_FENCE_GATES = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_fence_gate"),
                    AlmostBoolean.YES
                    // , ...
                    ), "fence_gate"));

    /** Wooden pressure plates. */
    public static final Set<Material> WOODEN_PRESSURE_PLATES = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_plate"), // Strictly _pressure_plate for 1.13.
                    AlmostBoolean.YES
                    // , ...
                    ));

    /** Wooden slabs. */
    public static final Set<Material> WOODEN_SLABS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_slab", "_step"),
                    AlmostBoolean.YES, 
                    "double" // Legacy
                    ));

    /** Wooden stairs. */
    public static final Set<Material> WOODEN_STAIRS = Collections.unmodifiableSet(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_stairs"),
                    AlmostBoolean.YES
                    // , ...
                    ));

    public static final Set<Material> WOODEN_TRAP_DOORS = Collections.unmodifiableSet(add(
            BridgeMaterial.getByPrefixAndSuffix(
                    woodTypes, 
                    Arrays.asList("_trap_door", "_trapdoor"),
                    AlmostBoolean.YES
                    // , ...
                    ), "trap_door"));

    public static final Set<Material> WOOL_BLOCKS = Collections.unmodifiableSet(add(
            BridgeMaterial.getBySuffix(
                    "_wool", 
                    AlmostBoolean.YES,
                    "legacy"
                    ), "wool"));


    ////////////////////
    // Access methods.
    ////////////////////

    /**
     * Test if the material is a boat.
     * 
     * @param mat
     * @return
     */
    public static boolean isBoat(final Material mat) {
        return BOATS.contains(mat);
    }

    /**
     * Test if the material is a spawn egg.
     * 
     * @param mat
     * @return
     */
    public static boolean isSpawnEgg(final Material mat) {
        return SPAWN_EGGS.contains(mat);
    }

}
