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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class BMPC implements CommandExecutor, TabCompleter {

	private final DatabaseManager databaseManager;

	public BMPC(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (BlueMapAPI.getInstance().isPresent()) {
			BlueMapAPI api = BlueMapAPI.getInstance().get();

			// === SELF ===
			if (sender instanceof Player player) { // only players can self
				UUID senderUUID = player.getUniqueId();
				if (args.length == 0) {
					sender.sendMessage("Сссылка на карту");
					return true;
				}
				if (args.length == 1) {
					if (selfAllowed(sender)) {
						if (args[0].equalsIgnoreCase("show")) {
							showSelf(api, sender, senderUUID);
						} else if (args[0].equalsIgnoreCase("hide")) {
							hideSelf(api, sender, senderUUID);
						} else if (args[0].equalsIgnoreCase("toggle")) {
							if (api.getWebApp().getPlayerVisibility(senderUUID)) {
								hideSelf(api, sender, senderUUID);
							} else {
								showSelf(api, sender, senderUUID);
							}

						}
					} else {
						sender.sendMessage(ChatColor.RED + "Вы не можете менять видимость");
					}
					return true;
				}
			} else {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Только игрок может скрыть себя");
					return true;
				}
			}


			// === OTHER ===
			if (!othersAllowed(sender)) {
				sender.sendMessage(ChatColor.RED + "Вы не можете менять видимость других игроков");
			} else {
				String targetName = args[args.length - 1];
				List<Entity> targets = Bukkit.selectEntities(sender, targetName);
				if (targets.isEmpty()) {
					sender.sendMessage(ChatColor.YELLOW + "Игрок \"" + targetName + "\" не найден");
					return true;
				}
				for (Entity target : targets) {
					if (!(target instanceof Player targetPlayer)) continue;

					if (args[0].equalsIgnoreCase("show")) {
						showOther(api, sender, targetPlayer);
					} else if (args[0].equalsIgnoreCase("hide")) {
						hideOther(api, sender, targetPlayer);
					} else if (args[0].equalsIgnoreCase("toggle")) {
						if (api.getWebApp().getPlayerVisibility(targetPlayer.getUniqueId())) {
							hideOther(api, sender, targetPlayer);
						} else {
							showOther(api, sender, targetPlayer);
						}
					}
				}
			}
			return true;
		}

		return false;
	}

	private void showSelf(BlueMapAPI blueMapAPI, CommandSender sender, UUID senderUUID) {
		blueMapAPI.getWebApp().setPlayerVisibility(senderUUID, true);
		databaseManager.setVisibility(senderUUID, true);
		sender.sendMessage("Теперь вы " + ChatColor.AQUA + "видимы" + ChatColor.RESET + " на карте");
	}

	private void hideSelf(BlueMapAPI blueMapAPI, CommandSender sender, UUID senderUUID) {
		blueMapAPI.getWebApp().setPlayerVisibility(senderUUID, false);
		databaseManager.setVisibility(senderUUID, false);
		sender.sendMessage("Теперь вы " + ChatColor.GOLD + "невидимы" + ChatColor.RESET + " на карте");
	}

	private void showOther(BlueMapAPI api, @NotNull CommandSender sender, Player targetPlayer) {
		api.getWebApp().setPlayerVisibility(targetPlayer.getUniqueId(), true);
		databaseManager.setVisibility(targetPlayer.getUniqueId(), true);
		sender.sendMessage(targetPlayer.getDisplayName() + " теперь " + ChatColor.AQUA + "видим" + ChatColor.RESET + " на карте");
		targetPlayer.sendMessage("Теперь вы " + ChatColor.AQUA + "видимы" + ChatColor.RESET + " на карте");
	}

	private void hideOther(BlueMapAPI api, @NotNull CommandSender sender, Player targetPlayer) {
		api.getWebApp().setPlayerVisibility(targetPlayer.getUniqueId(), false);
		databaseManager.setVisibility(targetPlayer.getUniqueId(), false);
		sender.sendMessage(targetPlayer.getDisplayName() + " теперь " + ChatColor.GOLD + "невидим" + ChatColor.RESET + " на карте");
		targetPlayer.sendMessage("Теперь вы " + ChatColor.GOLD + "невидимы" + ChatColor.RESET + " на карте");
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			if (selfAllowed(sender)) {
				completions.add("show");
				completions.add("hide");
				completions.add("toggle");
			}
		} else if (args.length == 2) {
			if (othersAllowed(sender)) {
				if (sender.getServer().getPlayer(args[0]) == null
						|| args[0].equalsIgnoreCase("show")
						|| args[0].equalsIgnoreCase("hide")
						|| args[0].equalsIgnoreCase("toggle")
						|| args[0].isBlank()) {
                    for (Player player : sender.getServer().getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                    completions.add("@a");
                    completions.add("@p");
                    completions.add("@r");
                    completions.add("@s");
                }
			}
		}
		return completions;
	}

	private boolean othersAllowed(CommandSender sender) {
		return sender.isOp() || sender.hasPermission("bmpc.others");
	}
	private boolean selfAllowed(CommandSender sender) {
		return sender.isOp() || sender.hasPermission("bmpc.self");
	}
}