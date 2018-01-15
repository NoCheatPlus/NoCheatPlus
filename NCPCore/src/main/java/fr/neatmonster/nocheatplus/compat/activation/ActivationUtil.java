package fr.neatmonster.nocheatplus.compat.activation;

import fr.neatmonster.nocheatplus.components.registry.activation.Activation;

/**
 * Static auxiliary functionality.
 * 
 * @author asofold
 *
 */
public class ActivationUtil {

    /**
     * Auxiliary method to get an Activation instance that activates with any of
     * the typical protocol support plugins existing (currently: ViaVersion,
     * ProtocolSupport).
     * 
     * @param activation
     * @return An activation instance for any of the typical multi protocol
     *         support plugins.
     */
    public static Activation getMultiProtocolSupportPluginActivation() {
        return new Activation()
                .setConditionsOR()

                // ViaVersion
                .pluginExist("ViaVersion")
                // ProtocolSupport
                .pluginExist("ProtocolSupport")
                ;
    }

}
