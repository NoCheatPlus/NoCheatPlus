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
package fr.neatmonster.nocheatplus.actions;

/**
 * Namse subject to change.
 * @author mc_dev
 *
 */
public interface ParameterHolder extends ActionData {

    /**
     * 
     * @param parameterName
     * @return Will always return some string, if not set: "<?PARAMETERNAME>".
     */
    public String getParameter(final ParameterName parameterName);

    /**
     * This will set the parameter, even if needsParameters() returns false.
     * @param parameterName
     * @param value
     */
    public void setParameter(final ParameterName parameterName, String value);

    /**
     * Check if any of the actions needs parameters.
     * @return If true, actions are likely to contain command or logging actions.
     */
    public boolean needsParameters();

    /**
     * Check if any parameters are set (in case of special settings NCP might add parameters for debugging purposes.).
     * @return
     */
    public boolean hasParameters();
}
