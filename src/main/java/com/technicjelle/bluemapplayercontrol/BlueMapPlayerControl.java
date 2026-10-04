package com.technicjelle.bluemapplayercontrol;

import com.technicjelle.bluemapplayercontrol.commands.BMPC;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class BlueMapPlayerControl extends JavaPlugin implements Listener {
    private BMPC executor;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("BlueMapPlayerControl enabled");

        // Инициализация БД
        databaseManager = new DatabaseManager(this);

        new Metrics(this, 18378);

        PluginCommand bmpc = Bukkit.getPluginCommand("map");
        executor = new BMPC(databaseManager); // Передаем БД в команду
        if (bmpc != null) {
            bmpc.setExecutor(executor);
            bmpc.setTabCompleter(executor);
        } else {
            getLogger().warning("bmpc is null. This is not good");
        }

        // Регистрируем слушатель для входа игроков
        getServer().getPluginManager().registerEvents(this, this);

        // Применяем сохраненную видимость для всех при загрузке BlueMap
        BlueMapAPI.onEnable(api -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean isVisible = databaseManager.getVisibility(player.getUniqueId(), true); // По умолчанию - видим (true)
                api.getWebApp().setPlayerVisibility(player.getUniqueId(), isVisible);
            }
        });
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("BlueMapPlayerControl disabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BlueMapAPI.getInstance().ifPresent(api -> {
            // Загружаем данные асинхронно, чтобы не вызывать лагов при заходе игрока
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (!event.getPlayer().hasPermission("bmpc.self")) {
                    api.getWebApp().setPlayerVisibility(event.getPlayer().getUniqueId(), true);
                    return;
                }
                boolean isVisible = databaseManager.getVisibility(event.getPlayer().getUniqueId(), true);
                api.getWebApp().setPlayerVisibility(event.getPlayer().getUniqueId(), isVisible);
            });
        });
    }
}