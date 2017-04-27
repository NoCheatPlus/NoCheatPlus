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
package fr.neatmonster.nocheatplus.checks.net.model;

/**
 * Location allowing for a count and a time, both mutable. Meant for queues,
 * increasing count and updating time with repetition.
 * 
 * @author asofold
 *
 */
public class CountableLocation extends DataLocation {

    public int count;
    public long time;
    /** Confirm teleport id: INTEGER.MIN_VALUE means it's not been provided. */
    public int teleportId;

    public CountableLocation(double x, double y, double z, float yaw, float pitch, int count, long time) {
        this(x, y, z, yaw, pitch, count, time, Integer.MIN_VALUE);
    }

    public CountableLocation(double x, double y, double z, float yaw, float pitch, 
            int count, long time, int teleportId) {
        super(x, y, z, yaw, pitch);
        this.time = time;
        this.count = count;
        this.teleportId = teleportId;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("CountableLocation(");
        builder.append("x=");
        builder.append(getX());
        builder.append(",y=");
        builder.append(getY());
        builder.append(",z=");
        builder.append(getZ());
        builder.append(",pitch=");
        builder.append(getPitch());
        builder.append(",yaw=");
        builder.append(getYaw());
        builder.append(",count=");
        builder.append(count);
        if (teleportId != Integer.MIN_VALUE) {
            builder.append(",tpid=");
            builder.append(teleportId);
        }
        // Skip time for now.
        builder.append(")");
        return builder.toString();
    }

}
