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
package fr.neatmonster.nocheatplus.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration path that has been relocated. newPath may be empty (relocated "somewhere").<br>
 * Note that only individual entries can be relocated at present, not sections.
 * @author mc_dev
 *
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Moved {

    /**
     * The new path where the content has moved to.
     * @return
     */
    public String newPath() default "";

    /**
     * Only added to be able to explicitly deny moving configuration sections.
     * Moving configuration sections might not actually be supported.
     * 
     * @return If to allow moving configuration sections.
     */
    public boolean configurationSection() default false;
}
