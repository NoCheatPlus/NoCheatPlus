package fr.neatmonster.nocheatplus.compat.cb2794;

import org.bukkit.craftbukkit.v1_6_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.components.modifiers.IAttributeAccess;
import fr.neatmonster.nocheatplus.utilities.AttribUtil;
import net.minecraft.server.v1_6_R1.AttributeInstance;
import net.minecraft.server.v1_6_R1.AttributeModifier;
import net.minecraft.server.v1_6_R1.GenericAttributes;

public class AttributeAccess implements IAttributeAccess {

    @Override
    public double getSpeedAttributeMultiplier(Player player) {
        final AttributeInstance attr = ((CraftLivingEntity) player).getHandle().a(GenericAttributes.d);
        double val = attr.e() / attr.b();
        final AttributeModifier mod = attr.a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return val;
        } else {
            return val / AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

    @Override
    public double getSprintAttributeMultiplier(Player player) {
        final AttributeModifier mod = ((CraftLivingEntity) player).getHandle().a(GenericAttributes.d).a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return 1.0;
        } else {
            return AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

}
