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
package fr.neatmonster.nocheatplus.utilities.collision;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

public class PassableRayTracing extends RayTracing implements ICollidePassable {

    protected BlockCache blockCache = null;

    protected boolean collides = false;

    protected boolean ignorefirst = false;

    @Override
    public BlockCache getBlockCache() {
        return blockCache;
    }

    @Override
    public void setBlockCache(BlockCache blockCache) {
        this.blockCache = blockCache;
    }

    @Override
    public void set(final PlayerLocation from, final PlayerLocation to){
        set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        setBlockCache(from.getBlockCache()); // TODO: This might better be done extra.
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.utilities.RayTracing#set(double, double, double, double, double, double)
     */
    @Override
    public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
        super.set(x0, y0, z0, x1, y1, z1);
        collides = false;
        ignorefirst = false;
    }

    @Override
    public boolean collides(){
        return collides;
    }

    @Override
    public void setIgnoreFirst(){
        this.ignorefirst = true;
    }

    @Override
    public boolean getIgnoreFirst(){
        return ignorefirst;
    }

    @Override
    public void cleanup(){
        blockCache = null;
    }

    @Override
    protected boolean step(final int blockX, final int blockY, final int blockZ, final double oX, final double oY, final double oZ, final double dT, final boolean isPrimary) {
        // Just delegate.
        if (isPrimary && step == 1 && ignorefirst){
            return true;
        }
        if (BlockProperties.isPassableRay(blockCache, blockX, blockY, blockZ, oX, oY, oZ, dX, dY, dZ, dT)){
            return true;
        }
        else{
            collides = true;
            return false;
        }
    }

    @Override
    public boolean mightNeedSplitAxisHandling() {
        return true;
    }

    @Override
    public void setMargins(double height, double xzMargin) {
        // (No effect.)
    }

}
