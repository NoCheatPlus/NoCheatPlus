package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.compat.bukkit.BukkitAttributeAccess;
import fr.neatmonster.nocheatplus.components.modifiers.DummyAttributeAccess;
import fr.neatmonster.nocheatplus.components.modifiers.IAttributeAccess;

public class AttributeAccessFactory {

    /**
     * Set up alongside with MCAccess. The MCAccess instance is passed here,
     * before it has been set internally and before it has been advertised to
     * MCAccessHolder instances, so the latter can get other specific access
     * providers during handling setMCAccess.
     * 
     * @param mcAccess
     * @param config
     */
    public void setupAttributeAccess(final MCAccess mcAccess, final MCAccessConfig config) {
        final IAttributeAccess fallBackReflect = new DummyAttributeAccess();
        IAttributeAccess fallBackDedicated = null;
        try {
            fallBackDedicated = new BukkitAttributeAccess();
        }
        catch (Throwable t) {}
        RegistryHelper.setupGenericInstance(new String[] {
                "fr.neatmonster.nocheatplus.compat.cbdev.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_9_R2.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_9_R1.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R3.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R2.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R1.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb3100.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb3043.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb3026.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb2922.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb2882.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb2808.AttributeAccess",
                "fr.neatmonster.nocheatplus.compat.cb2794.AttributeAccess"
        }, fallBackDedicated, new String[] {
                "fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectAttributeAccess" // Legacy
        }, fallBackReflect, IAttributeAccess.class, config);
    }

}
