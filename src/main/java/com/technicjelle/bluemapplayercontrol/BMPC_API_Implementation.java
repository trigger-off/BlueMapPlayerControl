package com.technicjelle.bluemapplayercontrol;

import com.technicjelle.bluemapplayercontrol.api.BMPC_API;
import de.bluecolored.bluemap.api.BlueMapAPI;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class BMPC_API_Implementation implements BMPC_API {
    private final DatabaseManager databaseManager;

    public BMPC_API_Implementation(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void setPlayerVisibility(UUID uuid, boolean visible) {
        // Сохраняем в БД
        databaseManager.setVisibility(uuid, visible);
        
        // Обновляем в BlueMap, если он загружен
        BlueMapAPI.getInstance().ifPresent(api -> 
            api.getWebApp().setPlayerVisibility(uuid, visible)
        );
    }

    @Override
    public boolean isPlayerVisible(UUID uuid) {
        return databaseManager.getVisibility(uuid, true);
    }
}