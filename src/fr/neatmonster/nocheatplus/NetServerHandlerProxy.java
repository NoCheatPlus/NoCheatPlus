package fr.neatmonster.nocheatplus;

import java.lang.reflect.Field;

import net.minecraft.server.AxisAlignedBB;
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
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * M"""""""`YM            dP   MP""""""`MM                                              
 * M  mmmm.  M            88   M  mmmmm..M                                              
 * M  MMMMM  M .d8888b. d8888P M.      `YM .d8888b. 88d888b. dP   .dP .d8888b. 88d888b. 
 * M  MMMMM  M 88ooood8   88   MMMMMMM.  M 88ooood8 88'  `88 88   d8' 88ooood8 88'  `88 
 * M  MMMMM  M 88.  ...   88   M. .MMM'  M 88.  ... 88       88 .88'  88.  ... 88       
 * M  MMMMM  M `88888P'   dP   Mb.     .dM `88888P' dP       8888P'   `88888P' dP       
 * MMMMMMMMMMM                 MMMMMMMMMMM                                              
 * 
 * M""MMMMM""MM                         dP dP                   MM"""""""`YM                                     
 * M  MMMMM  MM                         88 88                   MM  mmmmm  M                                     
 * M         `M .d8888b. 88d888b. .d888b88 88 .d8888b. 88d888b. M'        .M 88d888b. .d8888b. dP.  .dP dP    dP 
 * M  MMMMM  MM 88'  `88 88'  `88 88'  `88 88 88ooood8 88'  `88 MM  MMMMMMMM 88'  `88 88'  `88  `8bd8'  88    88 
 * M  MMMMM  MM 88.  .88 88    88 88.  .88 88 88.  ... 88       MM  MMMMMMMM 88       88.  .88  .d88b.  88.  .88 
 * M  MMMMM  MM `88888P8 dP    dP `88888P8 dP `88888P' dP       MM  MMMMMMMM dP       `88888P' dP'  `dP `8888P88 
 * MMMMMMMMMMMM                                                 MMMMMMMMMMMM                                 .88 
 *                                                                                                       d8888P  
 */
/**
 * A proxy used to fix Bukkit preventing players from using fly mods.
 */
public class NetServerHandlerProxy extends NetServerHandler {

    /** The default stance of a player. */
    private static final double    STANCE         = 1.6200000047683716D;

    private Field                  hField;
    private final MinecraftServer  minecraftServer;
    private final NetServerHandler netServerHandler;

    private double                 bukkitX        = Double.MAX_VALUE;
    private double                 bukkitY        = Double.MAX_VALUE;
    private double                 bukkitZ        = Double.MAX_VALUE;
    private float                  bukkitPitch    = Float.MAX_VALUE;
    private float                  bukkitYaw      = Float.MAX_VALUE;
    private boolean                checkMovement;
    private int                    flying;
    private boolean                justTeleported = false;
    private double                 vanillaX;
    private double                 vanillaY;
    private double                 vanillaZ;

    /**
     * Instantiates a new net server handler proxy.
     * 
     * @param minecraftServer
     *            the minecraft server
     * @param netServerHandler
     *            the net server handler
     */
    public NetServerHandlerProxy(final MinecraftServer minecraftServer, final NetServerHandler netServerHandler) {
        super(minecraftServer, netServerHandler.networkManager, netServerHandler.player);
        this.minecraftServer = minecraftServer;
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
        return netServerHandler.a();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(double, double, double, float, float)
     */
    @Override
    public void a(final double x, final double y, final double z, final float yaw, final float pitch) {
        netServerHandler.a(x, y, z, yaw, pitch);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet0KeepAlive)
     */
    @Override
    public void a(final Packet0KeepAlive packet0keepalive) {
        netServerHandler.a(packet0keepalive);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet102WindowClick)
     */
    @Override
    public void a(final Packet102WindowClick packet102windowclick) {
        netServerHandler.a(packet102windowclick);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet106Transaction)
     */
    @Override
    public void a(final Packet106Transaction packet106transaction) {
        netServerHandler.a(packet106transaction);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet107SetCreativeSlot)
     */
    @Override
    public void a(final Packet107SetCreativeSlot packet107setcreativeslot) {
        netServerHandler.a(packet107setcreativeslot);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet108ButtonClick)
     */
    @Override
    public void a(final Packet108ButtonClick packet108buttonclick) {
        netServerHandler.a(packet108buttonclick);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet10Flying)
     */
    @Override
    public void a(final Packet10Flying packet10flying) {
        if (player.getBukkitEntity().hasPermission(Permissions.MOVING_OVERRIDEBUKKIT)) {
            final WorldServer worldserver = minecraftServer.getWorldServer(player.dimension);

            try {
                hField.set(netServerHandler, true);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            if (!player.viewingCredits) {
                double d0;

                if (!checkMovement) {
                    d0 = packet10flying.y - vanillaY;
                    if (packet10flying.x == vanillaX && d0 * d0 < 0.01D && packet10flying.z == vanillaZ)
                        checkMovement = true;
                }

                final Player bPlayer = getPlayer();
                final Location from = new Location(bPlayer.getWorld(), bukkitX, bukkitY, bukkitZ, bukkitYaw,
                        bukkitPitch);
                final Location to = bPlayer.getLocation().clone();

                if (packet10flying.hasPos && (packet10flying.y != -999.0D || packet10flying.stance != -999.0D)) {
                    to.setX(packet10flying.x);
                    to.setY(packet10flying.y);
                    to.setZ(packet10flying.z);
                }

                if (packet10flying.hasLook) {
                    to.setYaw(packet10flying.yaw);
                    to.setPitch(packet10flying.pitch);
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
                        minecraftServer.server.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            netServerHandler.sendPacket(new Packet13PlayerLookMove(from.getX(), from.getY() + STANCE,
                                    from.getY(), from.getZ(), from.getYaw(), from.getPitch(), false));
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

                if (Double.isNaN(packet10flying.x) || Double.isNaN(packet10flying.y) || Double.isNaN(packet10flying.z)
                        || Double.isNaN(packet10flying.stance)) {
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
                        if (packet10flying.hasLook) {
                            f = packet10flying.yaw;
                            f1 = packet10flying.pitch;
                        }

                        if (packet10flying.hasPos && packet10flying.y == -999.0D && packet10flying.stance == -999.0D) {
                            if (Math.abs(packet10flying.x) > 1 || Math.abs(packet10flying.z) > 1) {
                                System.err.println(bPlayer.getName()
                                        + " was caught trying to crash the server with an invalid position.");
                                bPlayer.kickPlayer("Nope!");
                                return;
                            }

                            d5 = packet10flying.x;
                            d4 = packet10flying.z;
                        }

                        player.onGround = packet10flying.g;
                        player.g();
                        player.move(d5, 0.0D, d4);
                        player.setLocation(d1, d2, d3, f, f1);
                        player.motX = d5;
                        player.motZ = d4;
                        if (player.vehicle != null)
                            worldserver.vehicleEnteredWorld(player.vehicle, true);

                        if (player.vehicle != null)
                            player.vehicle.V();

                        minecraftServer.getServerConfigurationManager().d(player);
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

                    if (packet10flying.hasPos && packet10flying.y == -999.0D && packet10flying.stance == -999.0D)
                        packet10flying.hasPos = false;

                    if (packet10flying.hasPos) {
                        d1 = packet10flying.x;
                        d2 = packet10flying.y;
                        d3 = packet10flying.z;
                        d4 = packet10flying.stance - packet10flying.y;
                        if (!player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                            disconnect("Illegal stance");
                            logger.warning(player.name + " had an illegal stance: " + d4);
                            return;
                        }

                        if (Math.abs(packet10flying.x) > 3.2E7D || Math.abs(packet10flying.z) > 3.2E7D) {
                            disconnect("Illegal position");
                            return;
                        }
                    }

                    if (packet10flying.hasLook) {
                        f2 = packet10flying.yaw;
                        f3 = packet10flying.pitch;
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

                    if (player.onGround && !packet10flying.g && d6 > 0.0D)
                        player.j(0.2F);

                    player.move(d4, d6, d7);
                    player.onGround = packet10flying.g;
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

                    final AxisAlignedBB axisalignedbb = player.boundingBox.clone().grow(f4, f4, f4)
                            .a(0.0D, -0.55D, 0.0D);

                    if (!minecraftServer.getAllowFlight() && !player.abilities.canFly && !worldserver.c(axisalignedbb)) {
                        if (d12 >= -0.03125D) {
                            flying++;
                            if (flying > 80) {
                                logger.warning(player.name + " was kicked for floating too long!");
                                disconnect("Flying is not enabled on this server");
                                return;
                            }
                        }
                    } else
                        flying = 0;

                    player.onGround = packet10flying.g;
                    minecraftServer.getServerConfigurationManager().d(player);
                    if (player.itemInWorldManager.isCreative())
                        return;
                    player.b(player.locY - d0, packet10flying.g);
                }
            }
        } else
            netServerHandler.a(packet10flying);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet130UpdateSign)
     */
    @Override
    public void a(final Packet130UpdateSign packet130updatesign) {
        netServerHandler.a(packet130updatesign);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet14BlockDig)
     */
    @Override
    public void a(final Packet14BlockDig packet14blockdig) {
        netServerHandler.a(packet14blockdig);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet15Place)
     */
    @Override
    public void a(final Packet15Place packet15place) {
        netServerHandler.a(packet15place);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet16BlockItemSwitch)
     */
    @Override
    public void a(final Packet16BlockItemSwitch packet16blockitemswitch) {
        netServerHandler.a(packet16blockitemswitch);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet18ArmAnimation)
     */
    @Override
    public void a(final Packet18ArmAnimation packet18armanimation) {
        netServerHandler.a(packet18armanimation);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet19EntityAction)
     */
    @Override
    public void a(final Packet19EntityAction packet19entityaction) {
        netServerHandler.a(packet19entityaction);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet202Abilities)
     */
    @Override
    public void a(final Packet202Abilities packet202abilities) {
        netServerHandler.a(packet202abilities);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet203TabComplete)
     */
    @Override
    public void a(final Packet203TabComplete packet203tabcomplete) {
        netServerHandler.a(packet203tabcomplete);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet204LocaleAndViewDistance)
     */
    @Override
    public void a(final Packet204LocaleAndViewDistance packet204localeandviewdistance) {
        netServerHandler.a(packet204localeandviewdistance);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet205ClientCommand)
     */
    @Override
    public void a(final Packet205ClientCommand packet205clientcommand) {
        netServerHandler.a(packet205clientcommand);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet250CustomPayload)
     */
    @Override
    public void a(final Packet250CustomPayload packet250custompayload) {
        netServerHandler.a(packet250custompayload);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet255KickDisconnect)
     */
    @Override
    public void a(final Packet255KickDisconnect packet255kickdisconnect) {
        netServerHandler.a(packet255kickdisconnect);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet3Chat)
     */
    @Override
    public void a(final Packet3Chat packet3chat) {
        netServerHandler.a(packet3chat);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet7UseEntity)
     */
    @Override
    public void a(final Packet7UseEntity packet7useentity) {
        netServerHandler.a(packet7useentity);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet9Respawn)
     */
    @Override
    public void a(final Packet9Respawn packet9respawn) {
        netServerHandler.a(packet9respawn);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(java.lang.String, java.lang.Object[])
     */
    @Override
    public void a(final String s, final Object[] aobject) {
        netServerHandler.a(s, aobject);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#b()
     */
    @Override
    public boolean b() {
        return netServerHandler.b();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#chat(java.lang.String, boolean)
     */
    @Override
    public void chat(final String s, final boolean async) {
        netServerHandler.chat(s, async);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#d()
     */
    @Override
    public void d() {
        netServerHandler.d();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#disconnect(java.lang.String)
     */
    @Override
    public void disconnect(final String s) {
        netServerHandler.disconnect(s);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#getPlayer()
     */
    @Override
    public CraftPlayer getPlayer() {
        return netServerHandler.getPlayer();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#handleContainerClose(net.minecraft.server.Packet101CloseWindow)
     */
    @Override
    public void handleContainerClose(final Packet101CloseWindow packet101closewindow) {
        netServerHandler.handleContainerClose(packet101closewindow);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#lowPriorityCount()
     */
    @Override
    public int lowPriorityCount() {
        return netServerHandler.lowPriorityCount();
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#onUnhandledPacket(net.minecraft.server.Packet)
     */
    @Override
    public void onUnhandledPacket(final Packet packet) {
        netServerHandler.onUnhandledPacket(packet);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#sendPacket(net.minecraft.server.Packet)
     */
    @Override
    public void sendPacket(final Packet packet) {
        netServerHandler.sendPacket(packet);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#teleport(org.bukkit.Location)
     */
    @Override
    public void teleport(final Location destination) {
        if (player.getBukkitEntity().hasPermission(Permissions.MOVING_OVERRIDEBUKKIT)) {
            final double x = bukkitX = vanillaX = destination.getX();
            final double y = bukkitY = vanillaY = destination.getY();
            final double z = bukkitZ = vanillaZ = destination.getZ();
            final float yaw = bukkitYaw = Float.isNaN(destination.getYaw()) ? 0f : destination.getYaw();
            final float pitch = bukkitPitch = Float.isNaN(destination.getPitch()) ? 0f : destination.getPitch();
            checkMovement = !(justTeleported = true);
            player.setLocation(x, y, z, yaw, pitch);
            netServerHandler.sendPacket(new Packet13PlayerLookMove(x, y + STANCE, y, z, yaw, pitch, false));
        } else
            netServerHandler.teleport(destination);
    }
}
