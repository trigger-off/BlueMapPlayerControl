package com.technicjelle.bluemapplayercontrol.commands;

import com.technicjelle.bluemapplayercontrol.DatabaseManager;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class BMPC implements CommandExecutor, TabCompleter {
    private final DatabaseManager databaseManager;

    public BMPC(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (BlueMapAPI.getInstance().isEmpty()) return false;
        BlueMapAPI api = BlueMapAPI.getInstance().get();

        if (sender instanceof Player player) {
            if (args.length == 0) {
				sender.sendMessage(ChatColor.WHITE + "Карта сервера доступна по ссылке:\n(на ссылку можно нажать)\n" + ChatColor.LIGHT_PURPLE + "https://pixel-craft.ru/map");
                return true;
            }
            if (args.length == 1) {
                if (!selfAllowed(sender)) {
                    sender.sendMessage(ChatColor.RED + "Ты не можешь менять видимость");
                    return true;
                }
                handleSelf(api, sender, player, args[0]);
                return true;
            }
        } else if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Только игрок может скрыть себя");
            return true;
        }

        if (!othersAllowed(sender)) {
            sender.sendMessage(ChatColor.RED + "Ты не можешь менять видимость других игроков");
            return true;
        }

        String targetName = args[args.length - 1];
        List<Entity> targets = Bukkit.selectEntities(sender, targetName);
        if (targets.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Игрок \"" + targetName + "\" не найден");
            return true;
        }

		handleOthers(sender, args, targets, api);
		return true;
    }

	private void handleOthers(CommandSender sender, String[] args, List<Entity> targets, BlueMapAPI api) {
		String action = args[0].toLowerCase(Locale.ROOT);
		for (Entity target : targets) {
			if (!(target instanceof Player targetPlayer)) continue;
			switch (action) {
				case "show" -> setVisibility(api, sender, targetPlayer.getUniqueId(), true, false, targetPlayer.getName());
				case "hide" -> setVisibility(api, sender, targetPlayer.getUniqueId(), false, false, targetPlayer.getName());
            }
		}
	}

	private void handleSelf(BlueMapAPI api, CommandSender sender, Player player, String action) {
        UUID uuid = player.getUniqueId();
        switch (action.toLowerCase(Locale.ROOT)) {
            case "show" -> setVisibility(api, sender, uuid, true, true, player.getName());
            case "hide" -> setVisibility(api, sender, uuid, false, true, player.getName());
        }
    }

    private void setVisibility(BlueMapAPI api, CommandSender sender, UUID targetUUID, boolean visible, boolean self, String targetName) {
        api.getWebApp().setPlayerVisibility(targetUUID, visible);
        databaseManager.setVisibility(targetUUID, visible);

        String visibleState = visible ? "видим" : "невидим";

        if (self) {
            sender.sendMessage(ChatColor.GREEN + "Теперь ты " + visibleState + " на карте");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + targetName + " теперь " + visibleState + " на карте");
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.GREEN + "Теперь ты " + visibleState + " на карте");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && selfAllowed(sender)) {
            completions.add("show");
            completions.add("hide");
            return completions;
        }

        if (args.length == 2 && othersAllowed(sender)) {
            String action = args[0].toLowerCase(Locale.ROOT);
            if (action.equals("show") || action.equals("hide")) {
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                completions.add("@a");
                completions.add("@p");
                completions.add("@r");
                completions.add("@s");
            }
        }
        return completions;
    }

    private boolean othersAllowed(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("map.others");
    }

    private boolean selfAllowed(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("map.self");
    }
}