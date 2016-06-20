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

/**
 * Context data for the reach check, for repeated use within a loop.
 * 
 * @author asofold
 *
 */
public class ReachContext {

    public double distanceLimit;
    public double distanceMin;
    //    /** Attacking player. */
    //    public double eyeHeight;
    /** Eye location y of the attacking player. */
    public double pY;
    /** Minimum value of lenpRel that was a violation. */
    public double minViolation = Double.MAX_VALUE;
    /** Minimum value of lenpRel. */
    public double minResult = Double.MAX_VALUE;

}
