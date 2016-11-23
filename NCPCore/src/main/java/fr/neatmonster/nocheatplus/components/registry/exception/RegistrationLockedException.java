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
 * A registration item has been locked versus changes, but was attempted to be
 * changed.
 * 
 * @author asofold
 *
 */
public class RegistrationLockedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7278363049512687206L;

    public RegistrationLockedException() {
        super();
    }

    public RegistrationLockedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RegistrationLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationLockedException(String message) {
        super(message);
    }

    public RegistrationLockedException(Throwable cause) {
        super(cause);
    }

}
