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
package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Convenience for providing several checks with a lazy-init handle for fetching
 * queued flying packets. Typical use means only fetching this, if really
 * necessary, so isFlyingQueueFetched() returns false, if a default or current
 * position/look has passed checks. Future concept should be somehow linking
 * follow-up packets (flying->dig) to each other...
 * 
 * @author asofold
 *
 */
public class FlyingQueueHandle implements IHandle<DataPacketFlying[]> {

    private final IPlayerData pData;
    private DataPacketFlying[] queue;
    /**
     * Convenience flag for keeping track amongst multiple checks, which all get
     * passed this FlyingQueueHandle.
     */
    private boolean currentLocationValid = true;

    public FlyingQueueHandle(IPlayerData pData) {
        // TODO: PlayerData ?
        this.pData = pData;
    }

    @Override
    public DataPacketFlying[] getHandle() {
        if (queue == null) {
            queue = pData.getGenericInstance(NetData.class).copyFlyingQueue();
        }
        return queue;
    }

    /**
     * Test if the queue has been fetched.
     * 
     * @return
     */
    public boolean isFlyingQueueFetched() {
        return queue != null;
    }

    /**
     * Get the first index where the element is not null, if not fetched or no
     * non-null element is found, -1 is returned.
     * 
     * @return -1 if the queue has not been fetched yet or if all elements are
     *         null, otherwise the lowest index of a non null element is
     *         returned.
     */
    public int getFirstIndexWithContentIfFetched() {
        if (queue == null) {
            return -1;
        }
        else {
            for (int i = 0; i < queue.length; i++) {
                if (queue[i] != null) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * Get the element at the given index, only if fetched.
     * 
     * @param index
     * @return If already feteched, the element at the index is returned,
     *         otherwiese null is returned.
     * @throws ArrayIndexOutOfBoundsException
     *             If the index is out of range.
     */
    public DataPacketFlying getIfFetched(final int index) {
        return queue == null ? null : queue[index];
    }

    /**
     * Get the queue size only if fetched - otherwise -1 is returned.
     * 
     * @return Size of the queue if fetched, -1 otherwise.
     */
    public int sizeIfFetched() {
        return queue == null ? -1 : queue.length;
    }

    /**
     * Test the currentLocationValid flag (convenience to keep track of a custom
     * flag).
     * 
     * @return
     */
    public boolean isCurrentLocationValid() {
        return currentLocationValid;
    }

    /**
     * Set the currentLocationValid flag (convenience to keep track of a custom
     * flag).
     * 
     * @param currentLocationValid
     */
    public void setCurrentLocationValid(boolean currentLocationValid) {
        this.currentLocationValid = currentLocationValid;
    }

}
