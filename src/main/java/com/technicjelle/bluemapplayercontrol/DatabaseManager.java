package com.technicjelle.bluemapplayercontrol;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final JavaPlugin plugin;
    private final Object dbLock = new Object(); // Для потокобезопасности

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            initTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось подключиться к базе данных SQLite: " + e.getMessage());
        }
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_visibility (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "visible BOOLEAN NOT NULL" +
                ");";
        synchronized (dbLock) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                plugin.getLogger().severe("Не удалось создать таблицу: " + e.getMessage());
            }
        }
    }

    // Сохраняем видимость асинхронно, чтобы не стопить основной поток сервера
    public void setVisibility(UUID uuid, boolean visible) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT OR REPLACE INTO player_visibility (uuid, visible) VALUES (?, ?);";
            synchronized (dbLock) {
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, uuid.toString());
                    pstmt.setBoolean(2, visible);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().warning("Ошибка при сохранении статуса игрока " + uuid + ": " + e.getMessage());
                }
            }
        });
    }

    // Получение статуса (синхронный метод)
    public boolean getVisibility(UUID uuid, boolean defaultVisibility) {
        String sql = "SELECT visible FROM player_visibility WHERE uuid = ?;";
        synchronized (dbLock) {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("visible");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка при загрузке статуса игрока " + uuid + ": " + e.getMessage());
            }
        }
        return defaultVisibility;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Ошибка при закрытии соединения SQLite: " + e.getMessage());
        }
    }
}