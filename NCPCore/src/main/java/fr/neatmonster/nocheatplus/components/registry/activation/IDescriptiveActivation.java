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
package fr.neatmonster.nocheatplus.components.registry.activation;

/**
 * An activation checker with description (can be checked for activation
 * conditions and has some kind of description for logging). Possibly just a
 * delegate thing.
 * 
 * @author asofold
 *
 */
public interface IDescriptiveActivation extends IActivation {

    /**
     * Retrieve a neutral (short) description fit for logging under which
     * conditions this feature can be used, regardless of what isAvailable may
     * have returned.
     * 
     * @return
     */
    public String getNeutralDescription();

    /**
     * Test if this feature/combination is intended to be advertised, e.g. on
     * printing an overview of supported configurations.
     * 
     * @return
     */
    public boolean advertise();

}
