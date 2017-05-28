package fr.neatmonster.nocheatplus.compat.activation;

import fr.neatmonster.nocheatplus.components.registry.activation.Activation;

/**
 * Static auxiliary functionality.
 * 
 * @author asofold
 *
 */
public class ActivationUtil {

    private static final String ERROR_NEED_CONDITION_OR = "Must use setConditionOR() with this method.";

    /**
     * Auxiliary method to activate with any of the typical protocol support
     * plugins existing (currently: ViaVersion, ProtocolSupport).
     * 
     * @param activation
     * @return The given Activation instance.
     * @throws IllegalArgumentException
     *             If getConditionOr does not return true.
     */
    public static Activation addMultiProtocolSupportPlugins(Activation activation) {
        // TODO: Typically there is more confinement necessary, more specific naming will be needed.
        if (!activation.getConditionsOR()) {
            throw new IllegalArgumentException(ERROR_NEED_CONDITION_OR);
        }
        // ViaVersion
        activation.pluginExist("ViaVersion");
        // ProtocolSupport
        activation.pluginExist("ProtocolSupport");
        return activation;
    }

}
