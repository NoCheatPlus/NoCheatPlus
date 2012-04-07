package me.neatmonster.nocheatplus.checks.blockbreak;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class FastBreakCheck extends BlockBreakCheck {

    public FastBreakCheck(final NoCheatPlus plugin) {
        super(plugin, "blockbreak.fastbreak");
    }

    public boolean check(final NoCheatPlusPlayer player, final BlockBreakData data, final BlockBreakConfig cc) {

        int level = 0;

        // Get the player's item in hand material
        Material tool = player.getPlayer().getItemInHand() == null ? null : player.getPlayer().getItemInHand()
                .getType();
        if (isTool(tool))
            // It's a tool, let's check its enchantment level
            level = player.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED);
        else
            // It's not a tool but something else
            tool = null;

        // Get the block's material
        final Material block = player.getPlayer().getWorld()
                .getBlockAt(data.brokenBlockLocation.x, data.brokenBlockLocation.y, data.brokenBlockLocation.z)
                .getType();

        // Default break time value (creative mode minimum break time)
        long breakTime = 145L;
        if (player.getPlayer().getGameMode() == GameMode.SURVIVAL)
            breakTime = Math.round(getBreakTime(level, tool, block));

        // Elapsed time since the previous block was broken
        final long elapsedTime = Math.round((System.nanoTime() - data.lastBreakTime) / Math.pow(10, 6));

        boolean cancel = false;

        // Has the player broke blocks too quickly
        if (data.lastBreakTime != 0 && elapsedTime < breakTime) {
            // He failed, increase vl and statistics
            data.fastBreakVL += breakTime - elapsedTime;
            incrementStatistics(player, Id.BB_FASTBREAK, breakTime - elapsedTime);
            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.fastBreakActions, data.fastBreakVL);
        } else
            // Reward with lowering of the violation level
            data.fastBreakVL *= 0.90D;

        data.lastBreakTime = System.nanoTime();

        return cancel;

    }

    private double getBreakTime(final int level, final Material tool, final Material block) {

        double breakTime = -1D;

        /** SHOVEL **/
        if (block == Material.CLAY || block == Material.GRASS || block == Material.GRAVEL || block == Material.MYCEL) {
            if (tool == null)
                breakTime = 900D;
            else if (isWood(tool))
                breakTime = 450D;
            else if (isStone(tool))
                breakTime = 250D;
            else if (isIron(tool) || isDiamond(tool))
                breakTime = 150D;
            else if (isGold(tool))
                breakTime = 100D;
        } else if (block == Material.DIRT || block == Material.SAND || block == Material.SOUL_SAND) {
            if (tool == null)
                breakTime = 750D;
            else if (isWood(tool))
                breakTime = 400D;
            else if (isStone(tool))
                breakTime = 200D;
            else if (isIron(tool))
                breakTime = 150D;
            else if (isDiamond(tool) || isGold(tool))
                breakTime = 100D;
        } else if (block == Material.SNOW_BLOCK) {
            if (tool == null)
                breakTime = 1000D;
            else if (isWood(tool))
                breakTime = 150D;
            else if (isStone(tool))
                breakTime = 100D;
            else if (isIron(tool) || isDiamond(tool) || isGold(tool))
                breakTime = 50D;
        } else if (block == Material.SNOW) {
            if (tool == null)
                breakTime = 500D;
            else if (isWood(tool))
                breakTime = 100D;
            else if (isStone(tool) || isIron(tool) || isDiamond(tool) || isGold(tool))
                breakTime = 50D;
        }

        /** AXE **/
        else if (block == Material.CHEST) {
            if (tool == null)
                breakTime = 3750D;
            else if (isWood(tool))
                breakTime = 1900D;
            else if (isStone(tool))
                breakTime = 950D;
            else if (isIron(tool))
                breakTime = 650D;
            else if (isDiamond(tool))
                breakTime = 500D;
            else if (isGold(tool))
                breakTime = 350D;
        } else if (block == Material.LOG || block == Material.WOOD) {
            if (tool == null)
                breakTime = 3000D;
            else if (isStone(tool))
                breakTime = 1500D;
            else if (isStone(tool))
                breakTime = 750D;
            else if (isIron(tool))
                breakTime = 500D;
            else if (isDiamond(tool))
                breakTime = 400D;
            else if (isGold(tool))
                breakTime = 250D;
        } else if (block == Material.BOOKSHELF) {
            if (tool == null)
                breakTime = 2250D;
            else if (isWood(tool))
                breakTime = 1150D;
            else if (isStone(tool))
                breakTime = 600D;
            else if (isIron(tool))
                breakTime = 400D;
            else if (isDiamond(tool))
                breakTime = 300D;
            else if (isGold(tool))
                breakTime = 200D;
        }

        /** PICKAXE **/
        else if (block == Material.OBSIDIAN) {
            if (tool == null)
                breakTime = 250000D;
            else if (isWood(tool) || isStone(tool) || isIron(tool) || isGold(tool))
                breakTime = 50000D;
            else if (isDiamond(tool))
                breakTime = 10000D;
        } else if (block == Material.IRON_DOOR || block == Material.MOB_SPAWNER) {
            if (tool == null)
                breakTime = 25000D;
            else if (isWood(tool) || isStone(tool) || isIron(tool) || isGold(tool) || isDiamond(tool))
                breakTime = 75000D;
        } else if (block == Material.DIAMOND_BLOCK) {
            if (tool == null || isWood(tool) || isStone(tool) || isGold(tool))
                breakTime = 25000D;
            else if (isIron(tool))
                breakTime = 1250D;
            else if (isDiamond(tool))
                breakTime = 850D;
        } else if (block == Material.IRON_BLOCK) {
            if (tool == null || isWood(tool) || isGold(tool))
                breakTime = 25000D;
            else if (isStone(tool))
                breakTime = 2500D;
            else if (isIron(tool))
                breakTime = 1250D;
            else if (isDiamond(tool))
                breakTime = 950D;
        } else if (block == Material.DISPENSER || block == Material.FURNACE) {
            if (tool == null)
                breakTime = 17500D;
            else if (isStone(tool) || isWood(tool) || isGold(tool) || isIron(tool) || isDiamond(tool))
                breakTime = 5250D;
        } else if (block == Material.COAL_ORE) {
            if (tool == null)
                breakTime = 15000D;
            else if (isWood(tool))
                breakTime = 2250D;
            else if (isStone(tool))
                breakTime = 1150D;
            else if (isIron(tool))
                breakTime = 750D;
            else if (isDiamond(tool))
                breakTime = 600D;
            else if (isGold(tool))
                breakTime = 400D;
        } else if (block == Material.DIAMOND_ORE || block == Material.GOLD_ORE || block == Material.REDSTONE_ORE
                || block == Material.GOLD_BLOCK) {
            if (tool == null || isWood(tool) || isStone(tool) || isGold(tool))
                breakTime = 15000D;
            else if (isIron(tool))
                breakTime = 750D;
            else if (isDiamond(tool))
                breakTime = 600D;
        } else if (block == Material.IRON_ORE || block == Material.LAPIS_ORE || block == Material.LAPIS_BLOCK) {
            if (tool == null || isWood(tool) || isGold(tool))
                breakTime = 15000D;
            else if (isStone(tool))
                breakTime = 1150D;
            else if (isIron(tool))
                breakTime = 750D;
            else if (isDiamond(tool))
                breakTime = 600D;
        } else if (block == Material.BRICK || block == Material.NETHER_BRICK || block == Material.WOOD_STAIRS
                || block == Material.COBBLESTONE_STAIRS || block == Material.BRICK_STAIRS
                || block == Material.SMOOTH_STAIRS || block == Material.NETHER_BRICK_STAIRS) {
            if (tool == null)
                breakTime = 10000D;
            else if (isStone(tool) || isWood(tool) || isGold(tool) || isIron(tool) || isDiamond(tool))
                breakTime = 3000D;
        } else if (block == Material.COBBLESTONE || block == Material.MOSSY_COBBLESTONE || block == Material.STEP) {
            if (tool == null)
                breakTime = 10000D;
            else if (isWood(tool))
                breakTime = 1500D;
            else if (isStone(tool))
                breakTime = 750D;
            else if (isIron(tool))
                breakTime = 500D;
            else if (isDiamond(tool))
                breakTime = 400D;
            else if (isGold(tool))
                breakTime = 250D;
        } else if (block == Material.STONE || block == Material.GLOWSTONE) {
            if (tool == null)
                breakTime = 7500D;
            else if (isWood(tool))
                breakTime = 1150D;
            else if (isStone(tool))
                breakTime = 600D;
            else if (isIron(tool))
                breakTime = 400D;
            else if (isDiamond(tool))
                breakTime = 300D;
            else if (isGold(tool))
                breakTime = 200D;
        } else if (block == Material.SANDSTONE) {
            if (tool == null)
                breakTime = 4000D;
            else if (isWood(tool))
                breakTime = 600D;
            else if (isStone(tool))
                breakTime = 300D;
            else if (isIron(tool))
                breakTime = 200D;
            else if (isDiamond(tool))
                breakTime = 150D;
            else if (isGold(tool))
                breakTime = 100D;
        } else if (block == Material.ICE) {
            if (tool == null)
                breakTime = 1000D;
            else if (isWood(tool))
                breakTime = 400D;
            else if (isStone(tool))
                breakTime = 200D;
            else if (isIron(tool))
                breakTime = 150D;
            else if (isDiamond(tool) || isGold(tool))
                breakTime = 100D;
        } else if (block == Material.WOOD_PLATE || block == Material.STONE_PLATE) {
            if (tool == null)
                breakTime = 2500D;
            else if (isWood(tool) || isStone(tool) || isIron(tool) || isDiamond(tool) || isGold(tool))
                breakTime = 750D;
        } else if (block == Material.NETHERRACK) {
            if (tool == null)
                breakTime = 2000D;
            else if (isWood(tool))
                breakTime = 300D;
            else if (isStone(tool))
                breakTime = 150D;
            else if (isIron(tool))
                breakTime = 100D;
            else if (isDiamond(tool))
                breakTime = 100D;
            else if (isGold(tool))
                breakTime = 50D;
        } else if (block == Material.MONSTER_EGGS)
            if (tool == null || isStone(tool) || isWood(tool) || isGold(tool) || isIron(tool) || isDiamond(tool))
                breakTime = 3000D;

        // If we haven't any data for the current block, just apply the default value
        if (breakTime == -1D)
            return 45D;

        // Adjust break time if the tool is enchanted
        for (int i = level; i > 0; i--)
            breakTime = breakTime -= 0.25D * breakTime;

        // Set a minimum value for the break time
        if (breakTime < 50D)
            return 45D;

        // Subtract 5 ms (margin of error)
        return breakTime - 5D;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).fastBreakVL);
        else
            return super.getParameter(wildcard, player);
    }

    private boolean isDiamond(final Material tool) {
        return tool == Material.DIAMOND_AXE || tool == Material.DIAMOND_PICKAXE || tool == Material.DIAMOND_SPADE
                || tool == Material.DIAMOND_SWORD;
    }

    private boolean isGold(final Material tool) {
        return tool == Material.GOLD_AXE || tool == Material.GOLD_PICKAXE || tool == Material.GOLD_SPADE
                || tool == Material.GOLD_SWORD;
    }

    private boolean isIron(final Material tool) {
        return tool == Material.IRON_AXE || tool == Material.IRON_PICKAXE || tool == Material.IRON_SPADE
                || tool == Material.IRON_SWORD;
    }

    private boolean isStone(final Material tool) {
        return tool == Material.STONE_AXE || tool == Material.STONE_PICKAXE || tool == Material.STONE_SPADE
                || tool == Material.STONE_SWORD;
    }

    private boolean isTool(final Material tool) {
        return isWood(tool) || isStone(tool) || isIron(tool) || isDiamond(tool) || isGold(tool);
    }

    private boolean isWood(final Material tool) {
        return tool == Material.WOOD_AXE || tool == Material.WOOD_PICKAXE || tool == Material.WOOD_SPADE
                || tool == Material.WOOD_SWORD;
    }

}
