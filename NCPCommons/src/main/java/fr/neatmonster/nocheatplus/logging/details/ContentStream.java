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


/**
 * Contracts:
 * <li>Intended usage: register=seldom, log=often</li>
 * <li>Asynchronous access must be possible and fast, without locks (rather copy on write). </li>
 * @author dev1mc
 *
 * @param <C>
 */
public interface ContentStream <C> extends ContentLogger<C> {
    
    // TODO: Maybe also an abstract class.
    
    // Maybe not: addFilter (filter away some stuff, e.g. by regex from config).
    
    // TODO: Consider extra arguments for efficient registratioon with COWs.
    public void addNode(LogNode<C> node);
    
    // addAdapter(ContentAdapter<C, ?> adapter) ? ID etc., attach to another stream.
    
    // Removal and look up methods.
    
    /**
     * Remove all attached loggers and other.
     */
    public void clear();
    
}
