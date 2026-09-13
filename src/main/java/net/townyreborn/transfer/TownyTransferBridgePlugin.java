package net.townyreborn.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TownyTransferBridgePlugin extends JavaPlugin {
    private static final String BUNGEE_CHANNEL = "BungeeCord";

    private final Map<UUID, Long> lastTransferNanos = new HashMap<>();
    private String targetServer;
    private long cooldownNanos;
    private String noPermissionMessage;
    private String playerOnlyMessage;
    private String cooldownMessage;
    private String reloadedMessage;
    private InvisibleNpcManager invisibleNpcManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getMessenger().registerOutgoingPluginChannel(this, BUNGEE_CHANNEL);
        invisibleNpcManager = new InvisibleNpcManager(this);
        getServer().getPluginManager().registerEvents(invisibleNpcManager, this);
        invisibleNpcManager.reload(getConfig());
        getLogger().info("Transfer command enabled for Velocity server '" + targetServer + "'.");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, BUNGEE_CHANNEL);
        if (invisibleNpcManager != null) {
            invisibleNpcManager.shutdown();
        }
        lastTransferNanos.clear();
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("townytransfer.admin")) {
                sender.sendMessage(noPermissionMessage);
                return true;
            }

            reloadConfig();
            loadSettings();
            invisibleNpcManager.reload(getConfig());
            sender.sendMessage(reloadedMessage.replace(
                    "{count}", Integer.toString(invisibleNpcManager.getConfiguredNpcCount())));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        if (!player.hasPermission("townytransfer.use")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        long now = System.nanoTime();
        Long previous = lastTransferNanos.get(player.getUniqueId());
        if (previous != null && now - previous < cooldownNanos) {
            player.sendMessage(cooldownMessage);
            return true;
        }

        lastTransferNanos.put(player.getUniqueId(), now);
        player.sendPluginMessage(this, BUNGEE_CHANNEL, TransferPayload.connect(targetServer));
        return true;
    }

    private void loadSettings() {
        targetServer = getConfig().getString("target-server", "TownyReborn").trim();
        if (targetServer.isEmpty()) {
            throw new IllegalStateException("target-server must not be empty");
        }

        long cooldownMillis = Math.max(0L, getConfig().getLong("cooldown-milliseconds", 1500L));
        cooldownNanos = TimeUnit.MILLISECONDS.toNanos(cooldownMillis);
        noPermissionMessage = message("messages.no-permission", "&cYou do not have permission to use this.");
        playerOnlyMessage = message("messages.player-only", "This command can only be used by a player.");
        cooldownMessage = message("messages.cooldown", "&cPlease wait before trying again.");
        reloadedMessage = message(
                "messages.reloaded",
                "&aTownyTransferBridge reloaded. Managing &f{count} &ainvisible Citizens NPC(s).");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args) {
        if (args.length == 1
                && sender.hasPermission("townytransfer.admin")
                && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }

    private String message(String path, String fallback) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, fallback));
    }
}
