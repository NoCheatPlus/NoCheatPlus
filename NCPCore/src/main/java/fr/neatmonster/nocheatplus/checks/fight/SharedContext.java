package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.MCAccess;

public class SharedContext {
    public final double damagedHeight;
    
    public SharedContext(Entity damaged, MCAccess mcAccess) {
        this.damagedHeight = mcAccess.getHeight(damaged);
    }
}
