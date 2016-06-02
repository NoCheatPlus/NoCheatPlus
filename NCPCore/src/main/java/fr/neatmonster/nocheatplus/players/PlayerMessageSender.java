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
package fr.neatmonster.nocheatplus.players;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Unregisters during delegateTick to achieve thread-safety.
 * @author mc_dev
 *
 */
public class PlayerMessageSender extends OnDemandTickListener {

    private final class MessageEntry{
        public final String playerName;
        public final String message;
        public MessageEntry(final String playerName, final String message){
            this.playerName = playerName;
            this.message = message;
        }
    }

    /** Queued entries, also used as lock. */
    private List<MessageEntry> messageEntries = new LinkedList<MessageEntry>();

    @Override
    public boolean delegateTick(int tick, long timeLast) {
        // Copy entries.
        final List<MessageEntry> entries;
        synchronized (this) {
            if (messageEntries.isEmpty()){
                // Force unregister.
                unRegister(true);
                // Always continue here to never use external setRegistered.
                return true;
            }
            entries = messageEntries;
            messageEntries = new LinkedList<PlayerMessageSender.MessageEntry>();
        }
        // Do messaging.
        for (final MessageEntry entry : entries){
            final Player player = DataManager.getPlayerExact(entry.playerName);
            if (player != null && player.isOnline()){
                player.sendMessage(entry.message);
            }
        }
        // Unregister if no further entries are there.
        synchronized (this) {
            if (messageEntries.isEmpty()){
                // Force unregister.
                unRegister(true);
            }
        }
        // Always continue here to never use external setRegistered.
        return true;
    }

    public void sendMessageThreadSafe(final String playerName, final String message){
        final MessageEntry entry = new MessageEntry(playerName.toLowerCase(), message);
        synchronized (this) {
            messageEntries.add(entry);
            // Called register asynchronously, potentially.
            register();
        }
    }

}
