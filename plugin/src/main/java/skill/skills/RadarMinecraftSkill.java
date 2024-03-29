package skill.skills;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import config.PlayerDataConfig;
import main.ChatHelper;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.Command;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.lang.reflect.Field;
import java.util.*;

@Configurable("radar")
@Command("radar")
public class RadarMinecraftSkill extends MinecraftSkill implements CommandExecutor {
    @ConfigValue("max-distance")
    private static double MAX_DISTANCE;
    @InjectPlugin(postInject = "startTickTimer")
    private static Plugin plugin;

    private final Set<UUID> activeForPlayers = new HashSet<>();

    @SuppressWarnings("unused")
    public void startTickTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 1);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                if (!isActiveFor(player) || !isRadarEnabled(player)) {
                    return;
                }

                ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) event.getPacket().getHandle();
                Entity target = getEntityFromMetadataPacket(player.getWorld(), packet);
                if (target == null) {
                    return;
                }

                if (target.getUniqueId().equals(player.getUniqueId())) {
                    return;
                }

                double distanceSquared = target.getLocation().distanceSquared(player.getLocation());
                if (distanceSquared < MAX_DISTANCE * MAX_DISTANCE) {
                    setGlowing(packet);
                }
            }
        });
    }

    private void tick() {
        activePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> player.getNearbyEntities(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE).forEach(entity -> sendUpdateEntityMetadataPacket(entity, player)));
    }

    private void sendUpdateEntityMetadataPacket(Entity target, Player observer) {
        net.minecraft.world.entity.Entity nmsTarget = ((CraftEntity) target).getHandle();
        ServerPlayer nmsObserver = ((CraftPlayer) observer).getHandle();

        nmsObserver.connection.send(new ClientboundSetEntityDataPacket(nmsTarget.getId(), nmsTarget.getEntityData(), true));
    }

    private Entity getEntityFromMetadataPacket(World world, ClientboundSetEntityDataPacket packet) {
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.world.entity.Entity nmsEntity = nmsWorld.getEntity(packet.getId());
        if (nmsEntity == null) {
            return null;
        }
        return nmsEntity.getBukkitEntity();
    }

    /**
     * DO NOT OPEN
     */
    @SuppressWarnings("unchecked")
    private void setGlowing(ClientboundSetEntityDataPacket packet) {
        Class<?> clazz = packet.getClass();
        try {
            Field f = clazz.getDeclaredField("b"); // field packedItems
            f.setAccessible(true);
            List<SynchedEntityData.DataItem<?>> list = (List<SynchedEntityData.DataItem<?>>) f.get(packet);
            SynchedEntityData.DataItem<Byte> b = (SynchedEntityData.DataItem<Byte>) list.get(0);
            b.setValue((byte) ((b.getValue() & ~0x40) + 0x40));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean isTrackingEntity(Player observer, Player playerUsingRadar) {
        Location l1 = observer.getLocation();
        Location l2 = playerUsingRadar.getLocation();

        if (l1.getWorld() == null || l2.getWorld() == null) {
            return false;
        }

        if (!l1.getWorld().equals(l2.getWorld())) {
            return false;
        }

        return l1.distanceSquared(l2) < MAX_DISTANCE * MAX_DISTANCE && isActiveFor(observer);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }

        if (!command.getName().equalsIgnoreCase("radar")) {
            return false;
        }

        if (!isActiveFor(player)) {
            ChatHelper.sendMessage(player, "Hierfür musst die Fähigkeit Radar besitzen");
            return true;
        }

        PlayerDataConfig.PlayerData data = PlayerDataConfig.get(player);
        data.setRadarEnabled(!data.isRadarEnabled());
        PlayerDataConfig.set(player, data);
        if (data.isRadarEnabled()) {
            activeForPlayers.add(player.getUniqueId());
            ChatHelper.sendMessage(player, ChatColor.GREEN + "Das Radar ist nun aktiviert.");
        } else {
            activeForPlayers.remove(player.getUniqueId());
            ChatHelper.sendMessage(player, ChatColor.RED + "Das Radar ist nun deaktiviert.");
        }

        return true;
    }

    private boolean isRadarEnabled(Player player) {
        if (!activeForPlayers.contains(player.getUniqueId())) {
            if (PlayerDataConfig.get(player).isRadarEnabled()) {
                activeForPlayers.add(player.getUniqueId());
            }
            return activeForPlayers.contains(player.getUniqueId());
        } else {
            return true;
        }
    }
}
