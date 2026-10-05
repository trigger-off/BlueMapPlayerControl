package com.technicjelle.bluemapplayercontrol.api;

import java.util.UUID;

public interface BMPC_API {
    /**
     * Изменяет видимость игрока на карте BlueMap.
     */
    void setPlayerVisibility(UUID uuid, boolean visible);

    /**
     * Проверяет текущий статус видимости игрока.
     */
    boolean isPlayerVisible(UUID uuid);
}