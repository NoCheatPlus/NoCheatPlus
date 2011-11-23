package cc.co.evenprime.bukkit.nocheat.player;

import net.minecraft.server.EntityPlayer;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class NoCheatPlayer_181 extends NoCheatPlayerImpl {

    public NoCheatPlayer_181(Player player, NoCheat plugin, BaseData data) {
        super(player, plugin, data);
    }

    static {
        try {
            incAge = EntityPlayer.class.getMethod("b", boolean.class);
        } catch(Exception e) {
            System.out.println("NoCheat couldn't initialize variable incAge");
        }
    }

}
