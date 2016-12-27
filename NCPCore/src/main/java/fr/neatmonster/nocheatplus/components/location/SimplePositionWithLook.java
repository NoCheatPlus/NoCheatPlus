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
package fr.neatmonster.nocheatplus.components.location;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.utilities.location.LocUtil;

public class SimplePositionWithLook implements IPositionWithLook {

    // TODO: Package organization...

    private double x, y, z;
    private float yaw, pitch;

    /**
     * Empty constructor, undefined variable states (actually java defaults).
     */
    public SimplePositionWithLook() {
    }

    public SimplePositionWithLook(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Convenience method.
     * @param loc
     */
    public void set(final Location loc)  {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        yaw = loc.getYaw();
        pitch = loc.getPitch();
    }

    /**
     * Convenience method.
     * @param pos
     */
    public void set(final IGetPositionWithLook pos) {
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        yaw = pos.getYaw();
        pitch = pos.getPitch();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "SimplePositionWithLook(" + LocUtil.simpleFormat(this) + ")";
    }

    // TODO: hashCode, equals?

}
