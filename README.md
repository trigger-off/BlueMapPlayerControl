# BlueMap Player Control

[![GitHub Total Downloads](https://img.shields.io/github/downloads/TechnicJelle/BlueMapPlayerControl/total?label=Downloads&color=success "Click here to download the plugin")](https://github.com/TechnicJelle/BlueMapPlayerControl/releases/latest)
[![Servers using this plugin](https://img.shields.io/bstats/servers/18378?label=Servers)](https://bstats.org/plugin/bukkit/BlueMap%20Area%20Control/18378)

Simple [Paper](https://papermc.io/) plugin that allows you to show/hide players on [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap/). 

## Commands
| Command                | Usage                                                 | Permission                                     |
|------------------------|-------------------------------------------------------|------------------------------------------------|
| `/map`                 | Показать ссылку на карту                              |                                                |
| `/map show [player]`   | Сделать себя видимым / видимым другого игрока         | `bmpc.self.show` / `bmpc.others.show` (OP)         |
| `/map hide [player]`   | Сделать себя невидимым / невидимым другого игрока     | `bmpc.self.hide` / `bmpc.others.hide` (OP)     |
| `/map toggle [player]` | Переключить свою видимость / видимость другого игрока | `bmpc.self.toggle` / `bmpc.others.toggle` (OP) |

ℹ️️ Supports `@a`, `@p`, `@r` and `@s` as player arguments.
