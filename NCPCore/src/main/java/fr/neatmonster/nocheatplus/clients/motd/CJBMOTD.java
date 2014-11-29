package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.permissions.Permissions;

public class CJBMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(String message, Player player, boolean allowAll) {

        if (allowAll){
            return message;
        }

        String cjb = "";

        // TODO: Is there a compact version (just one prefix)?

        // TODO: Fly and xray removed ?

        // Disable CJB's fly mod.
        if (!player.hasPermission(Permissions.CJB_FLY)){
            cjb += "§3 §9 §2 §0 §0 §1";
        }

        // Disable CJB's xray.
        if (!player.hasPermission(Permissions.CJB_XRAY)){
            cjb += "§3 §9 §2 §0 §0 §2";
        }

        // Disable CJB's radar.
        if (!player.hasPermission(Permissions.CJB_RADAR)){
            cjb += "§3 §9 §2 §0 §0 §3";
        }

        if (cjb.isEmpty()){
            return message;
        }
        else{
            return message + cjb;
        }
    }

}
