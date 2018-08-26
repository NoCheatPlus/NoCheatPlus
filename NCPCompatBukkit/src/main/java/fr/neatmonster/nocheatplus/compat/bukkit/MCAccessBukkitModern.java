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
package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BridgeMaterial;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitDirectionalCentered;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitDoor;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitEndPortalFrame;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitFence;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitGate;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitShapeModel;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitShulkerBox;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitSlab;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitStairs;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitStatic;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitTrapDoor;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MaterialUtil;

public class MCAccessBukkitModern extends MCAccessBukkit {

    protected final Map<Material, BukkitShapeModel> shapeModels = new HashMap<Material, BukkitShapeModel>();

    // Blocks that change shape based on interaction or redstone.
    private static final BukkitShapeModel MODEL_DOOR = new BukkitDoor();
    private static final BukkitShapeModel MODEL_TRAP_DOOR = new BukkitTrapDoor();
    private static final BukkitShapeModel MODEL_GATE = new BukkitGate(
            0.375, 1.5);
    private static final BukkitShapeModel MODEL_SHULKER_BOX = new BukkitShulkerBox();

    // Blocks with different heights based on whatever.
    private static final BukkitShapeModel MODEL_END_PORTAL_FRAME = new BukkitEndPortalFrame();

    // Blocks that have a different shape, based on how they have been placed.
    private static final BukkitShapeModel MODEL_SLAB = new BukkitSlab();
    private static final BukkitShapeModel MODEL_STAIRS= new BukkitStairs();
    private static final BukkitShapeModel MODEL_END_ROD = new BukkitDirectionalCentered(
            0.375, 1.0, false);

    // Blocks that have a different shape with neighbor blocks (bukkit takes care though).
    private static final BukkitShapeModel MODEL_THIN_FENCE = new BukkitFence(
            0.4375, 1.0);
    private static final BukkitShapeModel MODEL_THICK_FENCE = new BukkitFence(
            0.375, 1.5);


    // Static blocks (various height and inset values).
    private static final BukkitShapeModel MODEL_LILY_PAD = new BukkitStatic(
            0.09375);
    private static final BukkitShapeModel MODEL_FLOWER_POT = new BukkitStatic(
            0.33, 0.375); // TODO: XZ really?
    private static final BukkitShapeModel MODEL_GROUND_HEAD= new BukkitStatic(
            0.25, 0.5); // TODO: XZ-really? 275 ?
    private static final BukkitShapeModel MODEL_SINGLE_CHEST = new BukkitStatic(
            0.062, .875); // TODO: 0.0625?

    // Static blocks with full height sorted by inset.
    private static final BukkitShapeModel MODEL_INSET16_1_HEIGHT100 = new BukkitStatic(
            0.0625, 1.0);


    // Static blocks with full xz-bounds sorted by height.
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT16_1 = new BukkitStatic(
            0.0625);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT8_1 = new BukkitStatic(
            0.125);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT8_3 = new BukkitStatic(
            0.375);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT16_9 = new BukkitStatic(
            0.5625);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT4_3 = new BukkitStatic(
            0.75);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT8_7 = new BukkitStatic(
            0.875);
    private static final BukkitShapeModel MODEL_XZ100_HEIGHT16_15 = new BukkitStatic(
            0.9375);

    /*
     * TODO:
     * LADDER,
     * CONDUIT, 
     * CHORUS_FLOWER, CHORUS_PLANT, COCOA, 
     * TURTLE_EGG, SEA_PICKLE, 
     * VINE, 
     * CAKE,
     */
    // TODO: anvils, dead coral fans
    // TODO: Liquid (all leveled).

    public MCAccessBukkitModern() {
        super();
        // TODO: Generic setup via Bukkit interface existence/relations, +- fetching methods.
        BlockInit.assertMaterialExists("OAK_LOG");
        BlockInit.assertMaterialExists("CAVE_AIR");
    }

    @Override
    public String getMCVersion() {
        return "1.13|?";
    }

    @Override
    public BlockCache getBlockCache() {
        return new BlockCacheBukkitModern(shapeModels);
    }

    public void addModel(Material mat, BukkitShapeModel model) {
        processedBlocks.add(mat);
        shapeModels.put(mat, model);
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {

        // TODO: Also consider removing flags (passable_x4 etc).

        // Adjust flags for individual blocks.
        BlockProperties.setBlockFlags(Material.CAULDRON, 
                BlockFlags.SOLID_GROUND | BlockProperties.F_GROUND_HEIGHT 
                | BlockProperties.F_MIN_HEIGHT4_1);

        // Directly keep blocks as is.
        for (final Material mat : new Material[] {
                Material.CAULDRON,
                BridgeMaterial.COBWEB,
                Material.HOPPER,
                BridgeMaterial.MOVING_PISTON,
                Material.SNOW
        }) {
            processedBlocks.add(mat);
        }

        // Lily pad
        addModel(BridgeMaterial.LILY_PAD, MODEL_LILY_PAD);

        // End portal frame.
        addModel(BridgeMaterial.END_PORTAL_FRAME, MODEL_END_PORTAL_FRAME);

        // End rod.
        addModel(Material.END_ROD, MODEL_END_ROD);

        // 1/16 inset at full height.
        for (Material mat : new Material[] {
                Material.CACTUS,
                Material.DRAGON_EGG
        }) {
            addModel(mat, MODEL_INSET16_1_HEIGHT100);
        }

        // 1/8 height.
        for (Material mat : new Material[] {
                BridgeMaterial.REPEATER,
                Material.COMPARATOR
        }) {
            addModel(mat, MODEL_XZ100_HEIGHT8_1);
        }

        // 3/8 height.
        for (Material mat : new Material[] {
                Material.DAYLIGHT_DETECTOR
        }) {
            addModel(mat, MODEL_XZ100_HEIGHT8_3);
        }

        // 3/4 height.
        for (Material mat : new Material[] {
                BridgeMaterial.ENCHANTING_TABLE
        }) {
            addModel(mat, MODEL_XZ100_HEIGHT4_3);
        }

        // 7/8 height.
        for (Material mat : new Material[] {
                Material.BREWING_STAND // TODO: base is 1/8, center 0.875 - needs multi-cuboid.
        }) {
            addModel(mat, MODEL_XZ100_HEIGHT8_7);
        }

        // 16/15 height, full xz bounds.
        for (Material mat : new Material[] {
                Material.GRASS_PATH, BridgeMaterial.FARMLAND
        }) {
            addModel(mat, MODEL_XZ100_HEIGHT16_15);
        }

        // Thin fence: Glass panes, iron bars.
        for (final Material mat : MaterialUtil.addBlocks(
                MaterialUtil.GLASS_PANES, BridgeMaterial.IRON_BARS)) {
            addModel(mat, MODEL_THIN_FENCE);
        }

        // Slabs
        for (final Material mat : MaterialUtil.SLABS) {
            addModel(mat, MODEL_SLAB);
        }

        // Shulker boxes.
        for (final Material mat : MaterialUtil.SHULKER_BOXES) {
            addModel(mat, MODEL_SHULKER_BOX);
        }

        // Chests.
        // TOOD: Might add a facing/directional extension for double chests.
        for (Material mat : BridgeMaterial.getAllBlocks(
                "chest", "trapped_chest",
                "ender_chest"
                )) {
            addModel(mat, MODEL_SINGLE_CHEST);
        }

        // Beds
        for (Material mat : MaterialUtil.BEDS) {
            addModel(mat, MODEL_XZ100_HEIGHT16_9);
        }

        // Flower pots.
        for (Material mat : MaterialUtil.FLOWER_POTS) {
            addModel(mat, MODEL_FLOWER_POT);
        }

        // Carpets.
        for (final Material mat : MaterialUtil.CARPETS) {
            addModel(mat, MODEL_XZ100_HEIGHT16_1);
        }

        // Ground heads.
        for (final Material mat : MaterialUtil.HEADS_GROUND) {
            addModel(mat, MODEL_GROUND_HEAD);
        }

        // Doors.
        for (final Material mat : MaterialUtil.ALL_DOORS) {
            addModel(mat, MODEL_DOOR);
        }

        // Trapdoors.
        for (final Material mat : MaterialUtil.ALL_TRAP_DOORS) {
            addModel(mat, MODEL_TRAP_DOOR);
        }

        // Sort to processed by flags.
        for (final Material mat : Material.values()) {
            final long flags = BlockProperties.getBlockFlags(mat);
            // Stairs.
            if (BlockFlags.hasAnyFlag(flags, BlockProperties.F_STAIRS)) {
                addModel(mat, MODEL_STAIRS);
            }
            // Fences, cobblestone wall.
            if (BlockFlags.hasAnyFlag(flags, BlockProperties.F_THICK_FENCE)) {
                if (BlockFlags.hasAnyFlag(flags, BlockProperties.F_PASSABLE_X4)) {
                    // TODO: Perhaps another model flag.
                    addModel(mat, MODEL_GATE);
                }
                else {
                    addModel(mat, MODEL_THICK_FENCE);
                }
            }
        }

        super.setupBlockProperties(worldConfigProvider);
    }

}
