package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;

public class RegistryHelper {

    /**
     * Set up a generic instance, according to settings. On success it will be
     * registered with the default GenericInstanceRegistry (NoCheatPlusAPI).
     * 
     * @param cbDedicatedNames
     * @param cbReflectNames
     * @param registerFor
     * @param config
     * @param logDebug
     *            If true, exceptions for failed instantiation attempts will be
     *            logged.
     * @return
     */
    public static <T> T setupGenericInstance(String[] cbDedicatedNames, String[] cbReflectNames, Class<T> registerFor, MCAccessConfig config, boolean logDebug) {
        return setupGenericInstance(cbDedicatedNames, cbReflectNames, null, registerFor, config, logDebug);
    }

    /**
     * Set up a generic instance, according to settings. On success it will be
     * registered with the default GenericInstanceRegistry (NoCheatPlusAPI).
     * 
     * @param cbDedicatedNames
     *            May be null.
     * @param cbReflectNames
     *            May be null.
     * @param registerFor
     * @param config
     * @param fallBackInstance
     *            Use this as a fall back, in case none of the classes could be
     *            instantiated.
     * @param logDebug
     *            If true, exceptions for failed instantiation attempts will be
     *            logged.
     * @return
     */
    public static <T> T setupGenericInstance(String[] cbDedicatedNames, String[] cbReflectNames, 
            T fallBackInstance, Class<T> registerFor, MCAccessConfig config, boolean logDebug) {
        return setupGenericInstance(cbDedicatedNames, null, cbReflectNames, fallBackInstance, registerFor, config, logDebug);
    }

    /**
     * Set up a generic instance, according to settings. On success it will be
     * registered with the default GenericInstanceRegistry (NoCheatPlusAPI).
     * 
     * @param cbDedicatedNames
     *            May be null.
     * @param fallBackDedicatedInstance
     *            Fall back to this, in case none of cbDedicatedNames could be
     *            instantiated. This will be used, even if dedicated modules are
     *            deactivated.
     * @param cbReflectNames
     *            May be null.
     * @param fallBackReflectInstance
     *            Fall back to this, in case no other could be
     *            instantiated/used. This will be used, even if reflection-based
     *            modules are deactivated.
     * @param registerFor
     * @param config
     * @param logDebug
     *            If true, exceptions for failed instantiation attempts will be
     *            logged.
     * @return
     */
    public static <T> T setupGenericInstance(String[] cbDedicatedNames, T fallBackDedicatedInstance,
            String[] cbReflectNames, T fallBackReflectInstance,
            Class<T> registerFor, MCAccessConfig config, boolean logDebug) {
        T res = null;

        // Reference by class name (dedicated/native access).
        if (config.enableCBDedicated && cbDedicatedNames != null) {
            res = getFirstAvailable(cbDedicatedNames, registerFor, logDebug);
        }

        // Fall back (after dedicated/native).
        if (res == null && fallBackDedicatedInstance != null) {
            res = fallBackDedicatedInstance;
        }

        // Reflection based.
        if (res == null && config.enableCBReflect && cbReflectNames != null) {
            res = getFirstAvailable(cbReflectNames, registerFor, logDebug);
        }

        // Fall back (after reflection).
        if (res == null && fallBackReflectInstance != null) {
            res = fallBackReflectInstance;
        }

        // Register / log.
        RegistryHelper.registerGenericInstance(registerFor, res);
        return res;
    }

    /**
     * Return the first instance for which a class can be instantiated via the
     * default constructor.
     * 
     * @param classNames
     *            Full class names (including package name).
     * @param registerFor
     *            The type to register classes for later (not registered here).
     * @param logDebug
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFirstAvailable(String[] classNames, Class<T> registerFor, boolean logDebug) {
        T res = null;
        for (String name : classNames) {
            try {
                res = (T) Class.forName(name).newInstance();
                if (res != null) {
                    return res;
                }
            }
            catch (Throwable t) {
                // Skip.
                if (logDebug) {
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, t);
                }
            }
        }
        return null;
    }

    /**
     * Register result as generic instance, if not null. Log if registered or if
     * not. Meant to register the output of a factory method.
     * 
     * @param registerFor
     *            The class the given result will be registered for.
     * @param result
     * @return The given result as instance of T.
     */
    public static <T, ET extends T> T registerGenericInstance(Class<T> registerFor, ET result) {
        if (result != null) {
            NCPAPIProvider.getNoCheatPlusAPI().registerGenericInstance(registerFor, result);
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Registered for " + registerFor.getName() + ": " + result.getClass().getName());
        }
        else {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Could not register an instance for: " + registerFor.getName());
        }
        return result;
    }

}
