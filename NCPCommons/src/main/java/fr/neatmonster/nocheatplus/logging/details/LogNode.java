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
package fr.neatmonster.nocheatplus.logging.details;

import fr.neatmonster.nocheatplus.logging.LoggerID;


public class LogNode<C> {
    
    public final LoggerID loggerID;
    public final ContentLogger<C> logger;
    public final LogOptions options;

    public LogNode(LoggerID loggerID, ContentLogger<C> logger, LogOptions options) {
        this.loggerID = loggerID;
        this.logger = logger;
        this.options = new LogOptions(options);
    }
    
}
