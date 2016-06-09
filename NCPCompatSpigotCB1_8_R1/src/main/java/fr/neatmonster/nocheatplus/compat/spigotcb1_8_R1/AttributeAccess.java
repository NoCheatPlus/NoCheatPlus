package fr.neatmonster.nocheatplus.compat.spigotcb1_8_R1;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.components.modifiers.IAttributeAccess;
import fr.neatmonster.nocheatplus.utilities.AttribUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.AttributeModifier;
import net.minecraft.server.v1_8_R1.GenericAttributes;

public class AttributeAccess implements IAttributeAccess {

    public AttributeAccess() {
        if (ReflectionUtil.getClass("net.minecraft.server.v1_8_R1.AttributeInstance") == null) {
            throw new RuntimeException("Service not available.");
        }
    }

    @Override
    public double getSpeedAttributeMultiplier(Player player) {
        final AttributeInstance attr = ((CraftLivingEntity) player).getHandle().getAttributeInstance(GenericAttributes.d);
        final double val = attr.getValue() / attr.b();
        final AttributeModifier mod = attr.a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return val;
        } else {
            return val / AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

    @Override
    public double getSprintAttributeMultiplier(Player player) {
        final AttributeModifier mod = ((CraftLivingEntity) player).getHandle().getAttributeInstance(GenericAttributes.d).a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return 1.0;
        } else {
            return AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

}
