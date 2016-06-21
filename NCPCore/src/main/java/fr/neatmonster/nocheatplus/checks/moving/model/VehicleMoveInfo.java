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
package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.utilities.location.RichEntityLocation;

public class VehicleMoveInfo extends MoveInfo<RichEntityLocation, Entity> {

    /** Add to fullWidth for the bounding box. */
    private double extendFullWidth = 0.0;

    public VehicleMoveInfo(final IHandle<MCAccess> mcAccess){
        super(mcAccess, new RichEntityLocation(mcAccess, null), new RichEntityLocation(mcAccess, null));
    }

    @Override
    protected void set(final RichEntityLocation rLoc, final Location loc, final Entity entity, final double yOnGround) {
        if (getExtendFullWidth() > 0.0) {
            final MCAccess mcAccess = from.getMCAccess();
            rLoc.set(loc, entity, mcAccess.getWidth(entity) + getExtendFullWidth(), mcAccess.getHeight(entity), yOnGround);
        } else {
            rLoc.set(loc, entity, yOnGround);
        }
    }

    public double getExtendFullWidth() {
        return extendFullWidth;
    }

    public void setExtendFullWidth(double extendFullWidth) {
        this.extendFullWidth = extendFullWidth;
    }

}
