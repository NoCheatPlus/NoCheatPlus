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
package fr.neatmonster.nocheatplus.worlds;

import java.util.UUID;

public class WorldIdentifier {

    public final String lowerCaseName;
    public final String exactName;
    public final UUID id;

    public WorldIdentifier(String exactName, UUID id) {
        this.lowerCaseName = exactName.toLowerCase();
        this.exactName = exactName;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return exactName.hashCode() ^ id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof WorldIdentifier) {
            final WorldIdentifier other = (WorldIdentifier) obj;
            return exactName.equals(other.exactName) && id.equals(other.id);
        }
        else if (obj instanceof String) {
            return lowerCaseName.equalsIgnoreCase((String) obj);
        }
        else if (obj instanceof UUID) {
            return id.equals((UUID) obj);
        }
        else {
            return false;
        }
    }

}
