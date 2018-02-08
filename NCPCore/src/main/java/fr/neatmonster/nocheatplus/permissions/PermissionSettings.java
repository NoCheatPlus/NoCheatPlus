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
package fr.neatmonster.nocheatplus.permissions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Configuration interface. PermissionPolicy instances can be fetched based on
 * explicit definition, more general rules and a default policy. This is not
 * particularly efficient, but every permission policy should only be fetched
 * once per registry.
 * <hr>
 * Thread-safe read by PermissionRegistry.
 * 
 * @author asofold
 *
 */
public class PermissionSettings {

    /**
     * For checking against
     * RegisteredPermission.getLowerCaseStringRepresentation.
     * 
     * @author asofold
     *
     */
    public static abstract class PermissionRule {

        private final PermissionPolicy policy;

        /**
         * 
         * @param policy
         *            Internally a copy is stored.
         */
        public PermissionRule(final PermissionPolicy policy) {
            this.policy = new PermissionPolicy(policy);
        }

        /**
         * 
         * @return not a copy.
         */
        public PermissionPolicy getPermissionPolicy() {
            return policy;
        }

        public abstract boolean matches(String permissionName);
    }

    // TODO: Rules for any-start, any-end, both, regex:...
    public static class StartsWithRule extends PermissionRule {
        private final String startsWith;
        public StartsWithRule(String startsWith, PermissionPolicy policy) {
            super(policy);
            this.startsWith = RegisteredPermission.toLowerCaseStringRepresentation(startsWith);
        }
        @Override
        public boolean matches(String permissionName) {
            return permissionName.startsWith(startsWith);
        }
    }
    public static class EndsWithRule extends PermissionRule {
        private final String endsWith;
        public EndsWithRule(String endsWith, PermissionPolicy policy) {
            super(policy);
            this.endsWith = RegisteredPermission.toLowerCaseStringRepresentation(endsWith);
        }
        @Override
        public boolean matches(String permissionName) {
            return permissionName.endsWith(endsWith);
        }
    }
    public static class ContainsRule extends PermissionRule {
        private final String contains;
        public ContainsRule(String contains, PermissionPolicy policy) {
            super(policy);
            this.contains = RegisteredPermission.toLowerCaseStringRepresentation(contains);
        }
        @Override
        public boolean matches(String permissionName) {
            return permissionName.indexOf(contains) >= 0;
        }
    }
    public static class RegexRule extends PermissionRule {
        private final String matches;
        public RegexRule(String matches, PermissionPolicy policy) {
            super(policy);
            this.matches = RegisteredPermission.toLowerCaseStringRepresentation(matches);
        }
        @Override
        public boolean matches(String permissionName) {
            return permissionName.matches(matches);
        }
    }

    /**
     * Factory method.
     * @param input
     * @param policy
     * @return null in case no specific rule setup is contained.
     */
    public static PermissionRule getMatchingRule(String input, PermissionPolicy policy) {
        input = RegisteredPermission.toLowerCaseStringRepresentation(input.trim());
        if (input.startsWith("*")) {
            if (input.endsWith("*")) {
                // ContainsRule
                return new ContainsRule(input.substring(1, input.length() - 1).trim(), policy);
            }
            else {
                // EndsWithRule
                return new EndsWithRule(input.substring(1).trim(), policy);
            }
        }
        else if (input.endsWith("*")) {
            // StartsWithRule
            return new StartsWithRule(input.substring(0, input.length() - 1).trim(), policy);
        }
        else if (input.startsWith("regex:")) {
            // RegexRule
            return new RegexRule(input.split(":", 2)[1].trim(), policy);
        }
        else if (StringUtil.startsWithAnyOf(input, "startswith:", "starts:", "start:", "prefix:", "head:")) {
            // StartsWithRule
            return new StartsWithRule(input.split(":", 2)[1].trim(), policy);
        }
        else if (StringUtil.startsWithAnyOf(input, "endsswith:", "ends:", "end:", "suffix:", "tail:")) {
            // EndsWithRule
            return new StartsWithRule(input.split(":", 2)[1].trim(), policy);
        }
        else if (StringUtil.startsWithAnyOf(input, "contains:", "contain:", "has:")) {
            // ContainsRule
            return new ContainsRule(input.split(":", 2)[1].trim(), policy);
        }
        else {
            return null;
        }
    }

    /**
     * Bukkit configuration compatible: Fetch full permission settings using the
     * given paths. The configuration must contain these paths (default or not).
     * 
     * @param config
     * @param pathDefaultPolicy
     *            policy def
     * @param pathRules
     *            rule def -> policy def mapping (ConfigurationSection). For
     *            rule definitions '#' is replaced by '.', to avoid issues with
     *            configuration paths.
     * @return
     * @throws RuntimeException
     * @throws NullPointerException
     * @throws IllegalArgumentException and possibly others.
     */
    public static PermissionSettings fromConfig(final ConfigurationSection config,
            final String pathDefaultPolicy, final String pathRules) {
        final PermissionPolicy defaultPolicy = new PermissionPolicy();
        try {
            defaultPolicy.setPolicyFromConfigLine(config.getString(pathDefaultPolicy));
        }
        catch (Exception e) {
            throw new RuntimeException("Bad default policy definition.", e);
        }
        final Map<String, PermissionPolicy> explicitPolicy = new LinkedHashMap<String, PermissionPolicy>();
        final List<PermissionRule> implicitRules = new LinkedList<PermissionSettings.PermissionRule>();

        // TODO: Change to List ! +- separators.
        final List<String> defs = config.getStringList(pathRules);
        for (String def : defs) {
            String[] split = def.split(": ", 2);
            if (split.length != 2) {
                throw new IllegalArgumentException("Must the separate matching rule from the policy definition by ' :: '.");
            }
            final String ruleDef = RegisteredPermission.toLowerCaseStringRepresentation(split[0].trim());
            final String policyDef = split[1].trim();
            try {
                final PermissionPolicy policy = new PermissionPolicy().setPolicyFromConfigLine(policyDef);
                final PermissionRule rule = getMatchingRule(ruleDef, policy);
                if (rule == null) {
                    explicitPolicy.put(ruleDef, policy);
                }
                else {
                    implicitRules.add(rule);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Bad rule definition (Match='" + ruleDef + "' Policy='" + policyDef  +"')", e);
            }
        }
        return new PermissionSettings(explicitPolicy, implicitRules, defaultPolicy);
    }

    //////////////////////
    // Instance
    //////////////////////

    private final Map<String, PermissionPolicy> explicitPolicy;
    private final PermissionRule[] implicitRules;
    private final PermissionPolicy defaultPolicy;

    /**
     * Constructor subject to change with additions like asynchronous policies.
     * Ensure to only pass arguments that won't be altered during further during
     * runtime.
     * 
     * @param explicitPolicy
     * @param implicitRules
     * @param defaultPolicy
     * @throws IllegalArgumentException
     *             If the defaultPolicy is null.
     */
    public PermissionSettings(Map<String, PermissionPolicy> explicitPolicy, 
            List<PermissionRule> implicitRules, 
            PermissionPolicy defaultPolicy) {
        if (defaultPolicy == null) {
            throw new IllegalArgumentException("The default policy must not be null.");
        }
        // (Null entries within explicit/implicit yield the default policy for now.)
        this.explicitPolicy = (explicitPolicy == null || explicitPolicy.isEmpty()) ? null 
                : new HashMap<String, PermissionPolicy>(explicitPolicy);
        this.implicitRules = (implicitRules == null || implicitRules.isEmpty()) ? null 
                : implicitRules.toArray(new PermissionRule[implicitRules.size()]);
        this.defaultPolicy = defaultPolicy;
    }

    public PermissionPolicy getPermissionPolicy(RegisteredPermission registeredPermission) {
        final String permissionName = registeredPermission.getLowerCaseStringRepresentation();
        PermissionPolicy ref = null;
        // Explicit first.
        if (explicitPolicy != null) {
            ref = explicitPolicy.get(registeredPermission.getLowerCaseStringRepresentation());
        }
        if (ref == null) {
            // Implicit second.
            if (implicitRules != null) {
                for (int i = 0; i < implicitRules.length; i++) {
                    if (implicitRules[i].matches(permissionName)) {
                        ref =implicitRules[i].getPermissionPolicy();
                    }
                }
            }
            // Default, finally.
            if (ref == null) {
                ref = defaultPolicy;
            }
        }
        return new PermissionPolicy(ref);
    }

}
