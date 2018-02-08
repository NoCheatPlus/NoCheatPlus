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
 * An item is not registered, although that is demanded in this context.
 * 
 * @author asofold
 *
 */
public class NotRegisteredException extends RegistryException {

    /**
     * 
     */
    private static final long serialVersionUID = 6240601169826276653L;

    public NotRegisteredException() {
        super();
    }

    public NotRegisteredException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotRegisteredException(String message) {
        super(message);
    }

    public NotRegisteredException(Throwable cause) {
        super(cause);
    }

}
