package fr.neatmonster.nocheatplus;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet0KeepAlive;
import net.minecraft.server.Packet101CloseWindow;
import net.minecraft.server.Packet102WindowClick;
import net.minecraft.server.Packet106Transaction;
import net.minecraft.server.Packet107SetCreativeSlot;
import net.minecraft.server.Packet108ButtonClick;
import net.minecraft.server.Packet10Flying;
import net.minecraft.server.Packet130UpdateSign;
import net.minecraft.server.Packet13PlayerLookMove;
import net.minecraft.server.Packet14BlockDig;
import net.minecraft.server.Packet15Place;
import net.minecraft.server.Packet16BlockItemSwitch;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet19EntityAction;
import net.minecraft.server.Packet202Abilities;
import net.minecraft.server.Packet203TabComplete;
import net.minecraft.server.Packet204LocaleAndViewDistance;
import net.minecraft.server.Packet205ClientCommand;
import net.minecraft.server.Packet250CustomPayload;
import net.minecraft.server.Packet255KickDisconnect;
import net.minecraft.server.Packet3Chat;
import net.minecraft.server.Packet7UseEntity;
import net.minecraft.server.Packet9Respawn;
import net.minecraft.server.ServerConnection;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/*
 * M"""""""`YM MM'""""'YMM MM"""""""`YM M"""""""`YM            dP   MP""""""`MM                                              
 * M  mmmm.  M M' .mmm. `M MM  mmmmm  M M  mmmm.  M            88   M  mmmmm..M                                              
 * M  MMMMM  M M  MMMMMooM M'        .M M  MMMMM  M .d8888b. d8888P M.      `YM .d8888b. 88d888b. dP   .dP .d8888b. 88d888b. 
 * M  MMMMM  M M  MMMMMMMM MM  MMMMMMMM M  MMMMM  M 88ooood8   88   MMMMMMM.  M 88ooood8 88'  `88 88   d8' 88ooood8 88'  `88 
 * M  MMMMM  M M. `MMM' .M MM  MMMMMMMM M  MMMMM  M 88.  ...   88   M. .MMM'  M 88.  ... 88       88 .88'  88.  ... 88       
 * M  MMMMM  M MM.     .dM MM  MMMMMMMM M  MMMMM  M `88888P'   dP   Mb.     .dM `88888P' dP       8888P'   `88888P' dP       
 * MMMMMMMMMMM MMMMMMMMMMM MMMMMMMMMMMM MMMMMMMMMMM                 MMMMMMMMMMM                                              
 *                                                                                                                           
 * M""MMMMM""MM                         dP dP                   
 * M  MMMMM  MM                         88 88                   
 * M         `M .d8888b. 88d888b. .d888b88 88 .d8888b. 88d888b. 
 * M  MMMMM  MM 88'  `88 88'  `88 88'  `88 88 88ooood8 88'  `88 
 * M  MMMMM  MM 88.  .88 88    88 88.  .88 88 88.  ... 88       
 * M  MMMMM  MM `88888P8 dP    dP `88888P8 dP `88888P' dP       
 * MMMMMMMMMMMM                                                 
 */
/**
 * A custom NetServerHandler used as a workaround to prevent CraftBukkit from blocking fly mods.
 */
public class NCPNetServerHandler extends NetServerHandler {

    /** The default stance of a player. */
    private static final double STANCE = 1.6200000047683716D;

    /**
     * Sets the NetServerHandler of the player.
     * 
     * @param player
     *            the player
     * @param useProxy
     *            the use proxy
     */
    public static void changeNetServerHandler(final Player player, final boolean useProxy) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final NetServerHandler vanillaNSH = entityPlayer.netServerHandler;
        final NCPNetServerHandler customNSH = new NCPNetServerHandler(MinecraftServer.getServer(),
                vanillaNSH.networkManager, entityPlayer, useProxy ? vanillaNSH : null);
        customNSH.a(entityPlayer.locX, entityPlayer.locY, entityPlayer.locZ, entityPlayer.yaw, entityPlayer.pitch);
        MinecraftServer.getServer().ac().a(customNSH);
        try {
            final Field connectionsField = ServerConnection.class.getDeclaredField("d");
            connectionsField.setAccessible(true);
            ((List<?>) connectionsField.get(MinecraftServer.getServer().ac())).remove(vanillaNSH);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        vanillaNSH.disconnected = !useProxy;
    }

    private Field                  hField         = null;
    private final NetServerHandler netServerHandler;

    private double                 bukkitX        = Double.MAX_VALUE;
    private double                 bukkitY        = Double.MAX_VALUE;
    private double                 bukkitZ        = Double.MAX_VALUE;
    private float                  bukkitPitch    = Float.MAX_VALUE;
    private float                  bukkitYaw      = Float.MAX_VALUE;
    private boolean                checkMovement;
    private int                    flyingPackets;
    private boolean                justTeleported = false;
    private double                 vanillaX;
    private double                 vanillaY;
    private double                 vanillaZ;

    /**
     * Instantiates a new nCP net server handler.
     * 
     * @param minecraftServer
     *            the minecraft server
     * @param networkManager
     *            the network manager
     * @param entityPlayer
     *            the entity player
     * @param netServerHandler
     *            the net server handler
     */
    public NCPNetServerHandler(final MinecraftServer minecraftServer, final INetworkManager networkManager,
            final EntityPlayer entityPlayer, final NetServerHandler netServerHandler) {
        super(minecraftServer, networkManager, entityPlayer);
        this.netServerHandler = netServerHandler;
        try {
            hField = NetServerHandler.class.getDeclaredField("h");
            hField.setAccessible(true);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a()
     */
    @Override
    public boolean a() {
        if (netServerHandler != null)
            return netServerHandler.a();
        return super.a();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(double, double, double, float, float)
     */
    @Override
    public void a(final double d0, final double d1, final double d2, final float f, final float f1) {
        if (netServerHandler != null)
            netServerHandler.a(d0, d1, d2, f, f1);
        else
            super.a(d0, d1, d2, f, f1);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet0KeepAlive)
     */
    @Override
    public void a(final Packet0KeepAlive packet0KeepAlive) {
        if (netServerHandler != null)
            netServerHandler.a(packet0KeepAlive);
        else
            super.a(packet0KeepAlive);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet102WindowClick)
     */
    @Override
    public void a(final Packet102WindowClick packet102WindowClick) {
        if (netServerHandler != null)
            netServerHandler.a(packet102WindowClick);
        else
            super.a(packet102WindowClick);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet106Transaction)
     */
    @Override
    public void a(final Packet106Transaction packet106Transaction) {
        if (netServerHandler != null)
            netServerHandler.a(packet106Transaction);
        else
            super.a(packet106Transaction);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet107SetCreativeSlot)
     */
    @Override
    public void a(final Packet107SetCreativeSlot packet107SetCreativeSlot) {
        if (netServerHandler != null)
            netServerHandler.a(packet107SetCreativeSlot);
        else
            super.a(packet107SetCreativeSlot);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet108ButtonClick)
     */
    @Override
    public void a(final Packet108ButtonClick packet108ButtonClick) {
        if (netServerHandler != null)
            netServerHandler.a(packet108ButtonClick);
        else
            super.a(packet108ButtonClick);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet10Flying)
     */
    @Override
    public void a(final Packet10Flying packet10Flying) {
        final WorldServer worldserver = MinecraftServer.getServer().getWorldServer(player.dimension);

        try {
            hField.set(netServerHandler == null ? this : netServerHandler, true);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (!player.viewingCredits) {
            double d0;

            if (!checkMovement) {
                d0 = packet10Flying.y - vanillaY;
                if (packet10Flying.x == vanillaX && d0 * d0 < 0.01D && packet10Flying.z == vanillaZ)
                    checkMovement = true;
            }

            final Player bPlayer = getPlayer();
            final Location from = new Location(bPlayer.getWorld(), bukkitX, bukkitY, bukkitZ, bukkitYaw, bukkitPitch);
            final Location to = bPlayer.getLocation().clone();

            if (packet10Flying.hasPos && (packet10Flying.y != -999.0D || packet10Flying.stance != -999.0D)) {
                to.setX(packet10Flying.x);
                to.setY(packet10Flying.y);
                to.setZ(packet10Flying.z);
            }

            if (packet10Flying.hasLook) {
                to.setYaw(packet10Flying.yaw);
                to.setPitch(packet10Flying.pitch);
            }

            final double delta = Math.pow(bukkitX - to.getX(), 2) + Math.pow(bukkitY - to.getY(), 2)
                    + Math.pow(bukkitZ - to.getZ(), 2);
            final float deltaAngle = Math.abs(bukkitYaw - to.getYaw()) + Math.abs(bukkitPitch - to.getPitch());

            if ((delta > 1f / 256 || deltaAngle > 10f) && checkMovement && !player.dead) {
                bukkitX = to.getX();
                bukkitY = to.getY();
                bukkitZ = to.getZ();
                bukkitYaw = to.getYaw();
                bukkitPitch = to.getPitch();

                if (from.getX() != Double.MAX_VALUE) {
                    final PlayerMoveEvent event = new PlayerMoveEvent(bPlayer, from, to);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        sendPacket(new Packet13PlayerLookMove(from.getX(), from.getY() + STANCE, from.getY(),
                                from.getZ(), from.getYaw(), from.getPitch(), false));
                        return;
                    }

                    if (!to.equals(event.getTo()) && !event.isCancelled()) {
                        player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                        return;
                    }

                    if (!from.equals(getPlayer().getLocation()) && justTeleported) {
                        justTeleported = false;
                        return;
                    }
                }
            }

            if (Double.isNaN(packet10Flying.x) || Double.isNaN(packet10Flying.y) || Double.isNaN(packet10Flying.z)
                    || Double.isNaN(packet10Flying.stance)) {
                bPlayer.teleport(bPlayer.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                System.err.println(bPlayer.getName()
                        + " was caught trying to crash the server with an invalid position.");
                bPlayer.kickPlayer("Nope!");
                return;
            }

            if (checkMovement && !player.dead) {
                double d1;
                double d2;
                double d3;
                double d4;

                if (player.vehicle != null) {
                    float f = player.yaw;
                    float f1 = player.pitch;

                    player.vehicle.V();
                    d1 = player.locX;
                    d2 = player.locY;
                    d3 = player.locZ;
                    double d5 = 0.0D;

                    d4 = 0.0D;
                    if (packet10Flying.hasLook) {
                        f = packet10Flying.yaw;
                        f1 = packet10Flying.pitch;
                    }

                    if (packet10Flying.hasPos && packet10Flying.y == -999.0D && packet10Flying.stance == -999.0D) {
                        if (Math.abs(packet10Flying.x) > 1 || Math.abs(packet10Flying.z) > 1) {
                            System.err.println(bPlayer.getName()
                                    + " was caught trying to crash the server with an invalid position.");
                            bPlayer.kickPlayer("Nope!");
                            return;
                        }

                        d5 = packet10Flying.x;
                        d4 = packet10Flying.z;
                    }

                    player.onGround = packet10Flying.g;
                    player.g();
                    player.move(d5, 0.0D, d4);
                    player.setLocation(d1, d2, d3, f, f1);
                    player.motX = d5;
                    player.motZ = d4;
                    if (player.vehicle != null)
                        worldserver.vehicleEnteredWorld(player.vehicle, true);

                    if (player.vehicle != null)
                        player.vehicle.V();

                    MinecraftServer.getServer().getServerConfigurationManager().d(player);
                    vanillaX = player.locX;
                    vanillaY = player.locY;
                    vanillaZ = player.locZ;
                    worldserver.playerJoinedWorld(player);
                    return;
                }

                if (player.isSleeping()) {
                    player.g();
                    player.setLocation(vanillaX, vanillaY, vanillaZ, player.yaw, player.pitch);
                    worldserver.playerJoinedWorld(player);
                    return;
                }

                d0 = player.locY;
                vanillaX = player.locX;
                vanillaY = player.locY;
                vanillaZ = player.locZ;
                d1 = player.locX;
                d2 = player.locY;
                d3 = player.locZ;
                float f2 = player.yaw;
                float f3 = player.pitch;

                if (packet10Flying.hasPos && packet10Flying.y == -999.0D && packet10Flying.stance == -999.0D)
                    packet10Flying.hasPos = false;

                if (packet10Flying.hasPos) {
                    d1 = packet10Flying.x;
                    d2 = packet10Flying.y;
                    d3 = packet10Flying.z;
                    d4 = packet10Flying.stance - packet10Flying.y;
                    if (!player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                        disconnect("Illegal stance");
                        logger.warning(player.name + " had an illegal stance: " + d4);
                        return;
                    }

                    if (Math.abs(packet10Flying.x) > 3.2E7D || Math.abs(packet10Flying.z) > 3.2E7D) {
                        disconnect("Illegal position");
                        return;
                    }
                }

                if (packet10Flying.hasLook) {
                    f2 = packet10Flying.yaw;
                    f3 = packet10Flying.pitch;
                }

                player.g();
                player.V = 0.0F;
                player.setLocation(vanillaX, vanillaY, vanillaZ, f2, f3);
                if (!checkMovement)
                    return;

                d4 = d1 - player.locX;
                double d6 = d2 - player.locY;
                double d7 = d3 - player.locZ;

                final float f4 = 0.0625F;
                final boolean flag = worldserver.getCubes(player, player.boundingBox.clone().shrink(f4, f4, f4))
                        .isEmpty();

                if (player.onGround && !packet10Flying.g && d6 > 0.0D)
                    player.j(0.2F);

                player.move(d4, d6, d7);
                player.onGround = packet10Flying.g;
                player.checkMovement(d4, d6, d7);
                final double d12 = d6;

                d4 = d1 - player.locX;
                d6 = d2 - player.locY;
                if (d6 > -0.5D || d6 < 0.5D)
                    d6 = 0.0D;

                d7 = d3 - player.locZ;

                player.setLocation(d1, d2, d3, f2, f3);
                final boolean flag2 = worldserver.getCubes(player, player.boundingBox.clone().shrink(f4, f4, f4))
                        .isEmpty();

                if (flag && !flag2 && !player.isSleeping()) {
                    this.a(vanillaX, vanillaY, vanillaZ, f2, f3);
                    return;
                }

                final AxisAlignedBB axisalignedbb = player.boundingBox.clone().grow(f4, f4, f4).a(0.0D, -0.55D, 0.0D);

                if (!MinecraftServer.getServer().getAllowFlight() && !player.abilities.canFly
                        && !worldserver.c(axisalignedbb)) {
                    if (d12 >= -0.03125D) {
                        flyingPackets++;
                        if (flyingPackets > 80) {
                            logger.warning(player.name + " was kicked for floating too long!");
                            disconnect("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else
                    flyingPackets = 0;

                player.onGround = packet10Flying.g;
                MinecraftServer.getServer().getServerConfigurationManager().d(player);
                if (player.itemInWorldManager.isCreative())
                    return;
                player.b(player.locY - d0, packet10Flying.g);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet130UpdateSign)
     */
    @Override
    public void a(final Packet130UpdateSign packet130UpdateSign) {
        if (netServerHandler != null)
            netServerHandler.a(packet130UpdateSign);
        else
            super.a(packet130UpdateSign);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet14BlockDig)
     */
    @Override
    public void a(final Packet14BlockDig packet14BlockDig) {
        if (netServerHandler != null)
            netServerHandler.a(packet14BlockDig);
        else
            super.a(packet14BlockDig);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet15Place)
     */
    @Override
    public void a(final Packet15Place packet15Place) {
        if (netServerHandler != null)
            netServerHandler.a(packet15Place);
        else
            super.a(packet15Place);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet16BlockItemSwitch)
     */
    @Override
    public void a(final Packet16BlockItemSwitch packet16BlockItemSwitch) {
        if (netServerHandler != null)
            netServerHandler.a(packet16BlockItemSwitch);
        else
            super.a(packet16BlockItemSwitch);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet18ArmAnimation)
     */
    @Override
    public void a(final Packet18ArmAnimation packet18ArmAnimation) {
        if (netServerHandler != null)
            netServerHandler.a(packet18ArmAnimation);
        else
            super.a(packet18ArmAnimation);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet19EntityAction)
     */
    @Override
    public void a(final Packet19EntityAction packet19EntityAction) {
        if (netServerHandler != null)
            netServerHandler.a(packet19EntityAction);
        else
            super.a(packet19EntityAction);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet202Abilities)
     */
    @Override
    public void a(final Packet202Abilities packet202Abilities) {
        if (netServerHandler != null)
            netServerHandler.a(packet202Abilities);
        else
            super.a(packet202Abilities);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet203TabComplete)
     */
    @Override
    public void a(final Packet203TabComplete packet203TabComplete) {
        if (netServerHandler != null)
            netServerHandler.a(packet203TabComplete);
        else
            super.a(packet203TabComplete);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet204LocaleAndViewDistance)
     */
    @Override
    public void a(final Packet204LocaleAndViewDistance packet204LocaleAndViewDistance) {
        if (netServerHandler != null)
            netServerHandler.a(packet204LocaleAndViewDistance);
        else
            super.a(packet204LocaleAndViewDistance);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet205ClientCommand)
     */
    @Override
    public void a(final Packet205ClientCommand packet205ClientCommand) {
        if (netServerHandler != null)
            netServerHandler.a(packet205ClientCommand);
        else
            super.a(packet205ClientCommand);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet250CustomPayload)
     */
    @Override
    public void a(final Packet250CustomPayload packet250CustomPayload) {
        if (netServerHandler != null)
            netServerHandler.a(packet250CustomPayload);
        else
            super.a(packet250CustomPayload);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet255KickDisconnect)
     */
    @Override
    public void a(final Packet255KickDisconnect packet255KickDisconnect) {
        if (netServerHandler != null)
            netServerHandler.a(packet255KickDisconnect);
        else
            super.a(packet255KickDisconnect);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet3Chat)
     */
    @Override
    public void a(final Packet3Chat packet3Chat) {
        if (netServerHandler != null)
            netServerHandler.a(packet3Chat);
        else
            super.a(packet3Chat);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet7UseEntity)
     */
    @Override
    public void a(final Packet7UseEntity packet7UseEntity) {
        if (netServerHandler != null)
            netServerHandler.a(packet7UseEntity);
        else
            super.a(packet7UseEntity);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet9Respawn)
     */
    @Override
    public void a(final Packet9Respawn packet9Respawn) {
        if (netServerHandler != null)
            netServerHandler.a(packet9Respawn);
        else
            super.a(packet9Respawn);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(java.lang.String, java.lang.Object[])
     */
    @Override
    public void a(final String s, final Object[] aobject) {
        if (netServerHandler != null)
            netServerHandler.a(s, aobject);
        else
            super.a(s, aobject);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#b()
     */
    @Override
    public boolean b() {
        if (netServerHandler != null)
            return netServerHandler.b();
        return super.b();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#chat(java.lang.String, boolean)
     */
    @Override
    public void chat(final String s, final boolean async) {
        if (netServerHandler != null)
            netServerHandler.chat(s, async);
        else
            super.chat(s, async);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#d()
     */
    @Override
    public void d() {
        if (netServerHandler != null)
            netServerHandler.d();
        else
            super.d();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#disconnect(java.lang.String)
     */
    @Override
    public void disconnect(final String s) {
        if (netServerHandler != null)
            netServerHandler.disconnect(s);
        else
            super.disconnect(s);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#getPlayer()
     */
    @Override
    public CraftPlayer getPlayer() {
        if (netServerHandler != null)
            return netServerHandler.getPlayer();
        return super.getPlayer();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#handleContainerClose(net.minecraft.server.Packet101CloseWindow)
     */
    @Override
    public void handleContainerClose(final Packet101CloseWindow packet101CloseWindow) {
        if (netServerHandler != null)
            netServerHandler.handleContainerClose(packet101CloseWindow);
        else
            super.handleContainerClose(packet101CloseWindow);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#lowPriorityCount()
     */
    @Override
    public int lowPriorityCount() {
        if (netServerHandler != null)
            return netServerHandler.lowPriorityCount();
        return super.lowPriorityCount();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#onUnhandledPacket(net.minecraft.server.Packet)
     */
    @Override
    public void onUnhandledPacket(final Packet packet) {
        if (netServerHandler != null)
            netServerHandler.onUnhandledPacket(packet);
        else
            super.onUnhandledPacket(packet);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#sendPacket(net.minecraft.server.Packet)
     */
    @Override
    public void sendPacket(final Packet packet) {
        if (netServerHandler != null)
            netServerHandler.sendPacket(packet);
        else
            super.sendPacket(packet);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#teleport(org.bukkit.Location)
     */
    @Override
    public void teleport(final Location dest) {
        final double x = bukkitX = vanillaX = dest.getX();
        final double y = bukkitY = vanillaY = dest.getY();
        final double z = bukkitZ = vanillaZ = dest.getZ();
        final float yaw = bukkitYaw = Float.isNaN(dest.getYaw()) ? 0f : dest.getYaw();
        final float pitch = bukkitPitch = Float.isNaN(dest.getPitch()) ? 0f : dest.getPitch();
        checkMovement = !(justTeleported = true);
        player.setLocation(x, y, z, yaw, pitch);
        sendPacket(new Packet13PlayerLookMove(x, y + STANCE, y, z, yaw, pitch, false));
    }
}
