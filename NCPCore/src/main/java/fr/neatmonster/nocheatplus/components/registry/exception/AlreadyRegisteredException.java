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
package fr.neatmonster.nocheatplus.components.registry.exception;

/**
 * A registry item has already been registered (for a given context), and the
 * registry does not support overriding via register(...) - it might still
 * support unregister(...) and register(...), in case what to unregister is
 * known.
 * 
 * @author asofold
 *
 */
public class AlreadyRegisteredException extends RegistryException {

    private static final long serialVersionUID = -72557863263954102L;

    public AlreadyRegisteredException() {
        super();
    }

    public AlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyRegisteredException(String message) {
        super(message);
    }

    public AlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

}
