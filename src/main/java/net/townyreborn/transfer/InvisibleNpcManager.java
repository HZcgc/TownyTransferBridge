package net.townyreborn.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class InvisibleNpcManager implements Listener {
    private static final String CITIZENS_PLUGIN_NAME = "Citizens";
    private static final String NPC_MARKER_KEY = "NPC";
    private static final String NPC_ID_KEY = "NPC-ID";

    private final Map<UUID, OriginalState> managedEntities = new HashMap<>();
    private final TownyTransferBridgePlugin plugin;

    private Set<Integer> configuredNpcIds = Set.of();
    private boolean enabled;
    private boolean hideNameplates;
    private BukkitTask repeatingTask;

    InvisibleNpcManager(TownyTransferBridgePlugin plugin) {
        this.plugin = plugin;
    }

    int getConfiguredNpcCount() {
        return configuredNpcIds.size();
    }

    void reload(FileConfiguration config) {
        stopRepeatingTask();
        restoreManagedEntities();

        enabled = config.getBoolean("invisible-npcs.enabled", true);
        hideNameplates = config.getBoolean("invisible-npcs.hide-nameplates", true);
        List<?> configuredValues = config.getList("invisible-npcs.ids", List.of());
        configuredNpcIds = NpcIdParser.parse(configuredValues, value -> plugin.getLogger().warning(
                "Ignoring invalid Citizens NPC ID in invisible-npcs.ids: " + value));

        if (!enabled || configuredNpcIds.isEmpty()) {
            plugin.getLogger().info("Persistent Citizens NPC invisibility is "
                    + (!enabled ? "disabled." : "enabled, but no NPC IDs are configured."));
            return;
        }

        if (!plugin.getServer().getPluginManager().isPluginEnabled(CITIZENS_PLUGIN_NAME)) {
            plugin.getLogger().warning(
                    "Citizens is not enabled. Configured invisible NPC IDs cannot be applied yet.");
        }

        plugin.getServer().getScheduler().runTask(plugin, this::applyToLoadedEntities);

        long interval = config.getLong("invisible-npcs.reapply-interval-ticks", 20L);
        if (interval > 0L) {
            repeatingTask = plugin.getServer().getScheduler().runTaskTimer(
                    plugin,
                    this::applyToLoadedEntities,
                    Math.max(1L, interval),
                    Math.max(1L, interval));
        }

        plugin.getLogger().info(
                "Keeping " + configuredNpcIds.size() + " Citizens NPC ID(s) permanently invisible.");
    }

    void shutdown() {
        stopRepeatingTask();
        restoreManagedEntities();
        configuredNpcIds = Set.of();
        enabled = false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!enabled || configuredNpcIds.isEmpty()) {
            return;
        }
        Entity entity = event.getEntity();
        plugin.getServer().getScheduler().runTask(plugin, () -> applyIfConfigured(entity));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!enabled || configuredNpcIds.isEmpty()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Entity entity : event.getChunk().getEntities()) {
                applyIfConfigured(entity);
            }
        });
    }

    private void applyToLoadedEntities() {
        if (!enabled) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                applyIfConfigured(entity);
            }
        }
        managedEntities.keySet().removeIf(uuid -> Bukkit.getEntity(uuid) == null);
    }

    private void applyIfConfigured(Entity entity) {
        if (!entity.isValid() || !entity.hasMetadata(NPC_MARKER_KEY)) {
            return;
        }

        OptionalInt npcId = findCitizensNpcId(entity);
        if (npcId.isEmpty() || !configuredNpcIds.contains(npcId.getAsInt())) {
            return;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        managedEntities.computeIfAbsent(entity.getUniqueId(), ignored -> new OriginalState(
                livingEntity.isInvisible(), livingEntity.isCustomNameVisible()));
        livingEntity.setInvisible(true);
        if (hideNameplates) {
            livingEntity.setCustomNameVisible(false);
        }
    }

    private OptionalInt findCitizensNpcId(Entity entity) {
        for (MetadataValue value : entity.getMetadata(NPC_ID_KEY)) {
            Plugin owner = value.getOwningPlugin();
            if (owner == null || !owner.getName().equalsIgnoreCase(CITIZENS_PLUGIN_NAME)) {
                continue;
            }
            Object rawValue = value.value();
            if (rawValue instanceof Number number) {
                return OptionalInt.of(number.intValue());
            }
            try {
                return OptionalInt.of(Integer.parseInt(String.valueOf(rawValue)));
            } catch (NumberFormatException ignored) {
                return OptionalInt.empty();
            }
        }
        return OptionalInt.empty();
    }

    private void restoreManagedEntities() {
        for (Map.Entry<UUID, OriginalState> entry : managedEntities.entrySet()) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity livingEntity) || !entity.isValid()) {
                continue;
            }
            OriginalState originalState = entry.getValue();
            livingEntity.setInvisible(originalState.invisible());
            livingEntity.setCustomNameVisible(originalState.customNameVisible());
        }
        managedEntities.clear();
    }

    private void stopRepeatingTask() {
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
    }

    private record OriginalState(boolean invisible, boolean customNameVisible) {
    }
}
