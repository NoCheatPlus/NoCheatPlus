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
package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.checks.access.IRemoveSubCheckData;
import fr.neatmonster.nocheatplus.checks.access.SubCheckDataFactory;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.utilities.PenaltyTime;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Player specific data for the fight checks.
 */
public class FightData extends ACheckData implements IRemoveSubCheckData {

    public static class FightDataFactory implements CheckDataFactory {

        protected FightDataFactory() {
            // Discourage creation here.
        };

        @Override
        public final ICheckData getData(final Player player) {
            return FightData.getData(player);
        }

        @Override
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return FightData.playersMap.get(playerName);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return FightData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }

    }


    /** The factory for general fight data. */
    public static final CheckDataFactory factory = new FightDataFactory();

    /** SelfHit factory */
    public static final CheckDataFactory selfHitDataFactory = new SubCheckDataFactory<FightData>(CheckType.FIGHT, factory) {

        @Override
        protected FightData getData(String playerName) {
            return playersMap.get(playerName);
        }

        @Override
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return playersMap.get(playerName);
        }

        @Override
        protected Collection<String> getPresentData() {
            return playersMap.keySet();
        }

        @Override
        protected boolean hasData(String playerName) {
            return playersMap.containsKey(playerName);
        }

        @Override
        protected boolean removeFromData(String playerName, FightData data) {
            if (data.selfHitVL.score(1f) > 0f) {
                data.selfHitVL.clear(System.currentTimeMillis());
                return true;
            }
            else {
                return false;
            }
        }

    };


    public static CheckDataFactory getCheckDataFactory(CheckType checkType) {
        if (checkType != CheckType.FIGHT && !APIUtils.isParent(CheckType.FIGHT, checkType)) {
            throw new IllegalArgumentException("Can only return a CheckDataFactory for the check group FIGHT.");
        }
        switch(checkType) {
            // Note that CheckType does need adaption for new entries (!).
            case FIGHT_SELFHIT:
                return selfHitDataFactory;
            default:
                return factory;
        }
    }

    /** The map containing the data per players. */
    protected static final Map<String, FightData> playersMap = new HashMap<String, FightData>(); // Not sure about visibility (selfhit).

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static FightData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new FightData(FightConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double                  angleVL;
    public double                  criticalVL;
    public double                  directionVL;
    public double                  fastHealVL;
    public double                  godModeVL;
    public double                  noSwingVL;
    public double                  reachVL;
    public double                  speedVL;

    // Shared
    public String lastWorld			= "";
    public int lastAttackTick		= 0;
    public double lastAttackedX		= Double.MAX_VALUE;
    public double lastAttackedY;
    public double lastAttackedZ;

    /** Attack penalty (close combat, ENTITY_ATTACK). */
    public final PenaltyTime attackPenalty = new PenaltyTime();

    /** The entity id  which might get counter-attacked. */
    public int thornsId = Integer.MIN_VALUE;

    // 1.9: Sweep attack.
    /**
     * The tick of an attack that might lead to further sweep damage to other
     * nearby entities.
     */
    public int sweepTick = 0;
    /** Hash code of the location of last sweep attack. */
    public int sweepLocationHashCode = 0;

    /** Any kind of health regeneration. */
    public long regainHealthTime    = 0;
    //    public double lastAttackedDist = 0.0;
    public long damageTakenByEntityTick;

    // Data of the angle check.
    public LinkedList<Angle.AttackLocation> angleHits = new LinkedList<Angle.AttackLocation>();

    // FastHeal
    public long					   fastHealRefTime = 0;
    /** Buffer has to be initialized in constructor. */
    public long					   fastHealBuffer = 0;

    // Old god mode check.
    public int                     godModeBuffer;
    public int                     godModeLastAge;
    public long                    godModeLastTime;

    // New god mode check [in progress].
    public int					   godModeHealthDecreaseTick 	= 0;
    public double                  godModeHealth       			= 0.0;
    public int                     lastDamageTick 				= 0;
    public int                     lastNoDamageTicks 			= 0;
    /** Accumulator. */
    public int					   godModeAcc 					= 0;

    // Data of the no swing check.
    public boolean                 noSwingArmSwung;

    // Data of the reach check.
    public double                  reachMod = 1.0;

    // Data of the SelfHit check.
    public ActionFrequency selfHitVL = new ActionFrequency(6, 5000);

    // Data of the frequency check.
    public final ActionFrequency   speedBuckets;
    public int                     speedShortTermCount;
    public int                     speedShortTermTick;

    // TNT workaround: Allow ENTITY_ATTACK if these attributes match.
    // Discussion at: https://github.com/NoCheatPlus/NoCheatPlus/pull/17 (@Iceee)
    /** Tick the last explosion damage was dealt at. */
    public int						lastExplosionDamageTick	= -1 ;
    /** Last explosion damaged entity (id). */
    public int						lastExplosionEntityId	= Integer.MAX_VALUE;


    public FightData(final FightConfig config){
        super(config);
        speedBuckets = new ActionFrequency(config.speedBuckets, config.speedBucketDur);
        // Start with full fast-heal buffer.
        fastHealBuffer = config.fastHealBuffer; 
    }

    @Override
    public boolean removeSubCheckData(final CheckType checkType) {
        switch(checkType) {
            // TODO: case FIGHT: ...
            case FIGHT_DIRECTION:
                directionVL = 0;
                return true;
            case FIGHT_REACH:
                reachVL = 0;
                reachMod = 1.0;
                return true;
            case FIGHT_ANGLE:
                angleVL = 0;
                angleHits.clear();
                return true;
            case FIGHT_SPEED:
                speedVL = 0;
                speedBuckets.clear(System.currentTimeMillis());
                speedShortTermCount = 0;
                speedShortTermTick = 0;
                return true;
            case FIGHT_FASTHEAL:
                fastHealVL = 0;
                fastHealRefTime = 0;
                fastHealBuffer = 0;
                regainHealthTime = 0;
                return true;
            case FIGHT_GODMODE:
                godModeVL = 0;
                godModeBuffer = 0;
                godModeAcc = 0;
                godModeLastTime = 0;
                godModeLastAge = 0;
                lastNoDamageTicks = 0; // Not sure here, possibly a shared thing.
                // godModeHealth / ...
                return true;
            case FIGHT_CRITICAL:
                criticalVL = 0;
                return true;
            case FIGHT_NOSWING:
                noSwingVL = 0;
                // Not reset time, for leniency rather.
                return true;
            case FIGHT_SELFHIT:
                selfHitVL.clear(System.currentTimeMillis());
                return true;
            default:
                return false;
        }
    }

    public void onWorldChange() {
        angleHits.clear();
        lastAttackedX = Double.MAX_VALUE;
        lastAttackTick = 0;
        lastWorld = "";
    }

}
