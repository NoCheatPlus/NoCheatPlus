package fr.neatmonster.nocheatplus.checks.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check used to verify if the player isn't using a forcefield in order to attack multiple entities at the same time
 * 
 * Thanks asofold for the original idea!
 */
public class AngleCheck extends FightCheck {

    public class AngleCheckEvent extends FightEvent {

        public AngleCheckEvent(final AngleCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

    public class AngleData implements Comparable<AngleData> {
        public final long   time;
        public final double x;
        public final double y;
        public final double z;
        public final float  yaw;

        public AngleData(final Location location) {
            time = System.currentTimeMillis();
            x = location.getX();
            y = location.getY();
            z = location.getZ();
            yaw = location.getYaw();
        }

        @Override
        public int compareTo(final AngleData otherData) {
            if (otherData.time < time)
                return 1;
            else if (otherData.time == time)
                return 0;
            else
                return -1;
        }

        public boolean shouldBeRemoved() {
            return System.currentTimeMillis() - time > 1000L;
        }
    }

    public AngleCheck() {
        super("angle", Permissions.FIGHT_ANGLE);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        // Add the current attack to the list
        data.attacks.add(new AngleData(player.getLocation()));

        // Sort the list of the attacks for the oldest to the newest
        Collections.sort(data.attacks, Collections.reverseOrder());

        // Declare 3 list which will contain the times, yaws and pitches
        final List<Long> dTime = new ArrayList<Long>();
        final List<Float> dYaw = new ArrayList<Float>();
        final List<Double> dMove = new ArrayList<Double>();

        AngleData previousAngleData = null;
        for (final AngleData angleData : new ArrayList<AngleData>(data.attacks))
            // If the data is older than a second...
            if (angleData.shouldBeRemoved())
                // ...remove it from the list
                data.attacks.remove(angleData);
            else {
                // If we have a previous data (to calculate deltas)...
                if (previousAngleData != null) {
                    // ...calculate the time delta...
                    dTime.add(Math.abs(previousAngleData.time - angleData.time));
                    // ...the yaw delta...
                    dYaw.add(Math.abs(previousAngleData.yaw - angleData.yaw) % 360F);
                    // ...the move delta
                    dMove.add(Math.sqrt(Math.pow(previousAngleData.x - angleData.x, 2)
                            + Math.pow(previousAngleData.y - angleData.y, 2)
                            + Math.pow(previousAngleData.z - angleData.z, 2)));
                }
                // Remember this data to calculate the next deltas
                previousAngleData = angleData;
            }

        /**
         * TIME
         **/
        // First we calculate the average time between each attack
        double mTime = 0D;
        for (final long time : dTime)
            mTime += time;
        if (dTime.size() != 0D)
            mTime /= dTime.size();

        // Then if the time is superior to 150 ms, we set the violation to 0...
        if (mTime == 0D || mTime > 150D)
            mTime = 0D;

        // ...but otherwise we calculate a percentage of violation
        else
            mTime = 100D * (150D - mTime) / 150D;

        /**
         * YAW
         **/
        // First we calculate the average yaw change between each attack
        double mYaw = 0D;
        for (final double yaw : dYaw)
            mYaw += yaw;
        if (dYaw.size() != 0D)
            mYaw /= dYaw.size();

        // Then if the yaw is inferior to 50°, we set the violation to 0...
        if (mYaw == 0D || mYaw < 50D)
            mYaw = 0D;

        // ...but otherwise we calculate a percentage of violation
        else
            mYaw = 100D * (360D - mYaw) / 360D;

        /**
         * MOVE
         **/
        // First we calculate the average move between each attack
        double mMove = 0D;
        for (final double move : dMove)
            mMove += move;
        if (dMove.size() != 0)
            mMove /= dMove.size();

        // Then, if the move is bigger than 0.2 block(s), we set the violation to 0...
        if (mMove == 0D || mMove > 0.2D)
            mMove = 0D;

        // ...but otherwise we calculate a percentage of violation
        else
            mMove = 100D * (0.2D - mMove) / 0.2D;

        // Now we are ready to make the average of the three "checks" violation level
        // Each "check" has his coefficient: 5 for the time, 3 for the yaw, 2 for the move
        final double mTotal = (5D * mTime + 3D * mYaw + 2D * mMove) / 10D;

        // If the total is superior the value defined in the configuration file...
        if (mTotal > cc.angleThreshold) {
            // If there was lag, don't count it towards statistics and vl...
            if (!NoCheatPlus.skipCheck()) {
                // ...otherwise increment the violation level...
                data.angleVL += mTotal;
                // ...and the statistics
                incrementStatistics(player, Id.FI_ANGLE, mTotal);
            }

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.angleActions, data.angleVL);

        } else
            // Otherwise reward the player by lowering his violation level
            data.angleVL *= 0.98;

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final AngleCheckEvent event = new AngleCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).angleVL));
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.angleCheck;
    }
}
