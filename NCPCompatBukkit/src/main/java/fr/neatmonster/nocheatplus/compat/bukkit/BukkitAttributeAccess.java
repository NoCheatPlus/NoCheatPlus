package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.AttribUtil;
import fr.neatmonster.nocheatplus.components.modifier.IAttributeAccess;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class BukkitAttributeAccess implements IAttributeAccess {

    public BukkitAttributeAccess() {
        if (ReflectionUtil.getClass("org.bukkit.attribute.AttributeInstance") == null) {
            throw new RuntimeException("Service not available.");
        }
    }

    private int operationToInt(final Operation operation) {
        switch (operation) {
            case ADD_NUMBER:
                return 0;
            case ADD_SCALAR:
                return 1;
            case MULTIPLY_SCALAR_1:
                return 2;
            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }
    }

    /**
     * The first modifier with the given id that can be found, or null if none
     * is found.
     * 
     * @param attrInst
     * @param id
     * @return
     */
    private AttributeModifier getModifier(final AttributeInstance attrInst, final UUID id) {
        for (final AttributeModifier mod : attrInst.getModifiers()) {
            if (id.equals(mod.getUniqueId())) {
                return mod;
            }
        }
        return null;
    }

    private double getMultiplier(final AttributeModifier mod) {
        return AttribUtil.getMultiplier(operationToInt(mod.getOperation()), mod.getAmount());
    }

    @Override
    public double getSpeedAttributeMultiplier(final Player player) {
        final AttributeInstance attrInst = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final double val = attrInst.getValue() / attrInst.getBaseValue();
        final AttributeModifier mod = getModifier(attrInst, AttribUtil.ID_SPRINT_BOOST);
        return mod == null ? val : (val / getMultiplier(mod));
    }

    @Override
    public double getSprintAttributeMultiplier(final Player player) {
        final AttributeInstance attrInst = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final AttributeModifier mod = getModifier(attrInst, AttribUtil.ID_SPRINT_BOOST);
        return mod == null ? 1.0 : getMultiplier(mod);
    }

}
