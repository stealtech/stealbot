package org.stealtech.stealbot

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin
import org.stealtech.stealbot.discord.DiscordBot


class DiscordCommand(
    private val plugin: JavaPlugin,
    private val discordBot: DiscordBot
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> {
                if (!sender.hasPermission("stealbot.discord.reload")) {
                    sender.sendMessage("${ChatColor.RED}You don't have permission to reload the Discord bot.")
                    return true
                }

                plugin.reloadConfig()
                discordBot.stop()
                discordBot.start()
                sender.sendMessage("${ChatColor.GREEN}Discord bot configuration reloaded.")
            }
            "status" -> {
                if (!sender.hasPermission("stealbot.discord.status")) {
                    sender.sendMessage("${ChatColor.RED}You don't have permission to check Discord bot status.")
                    return true
                }

                sender.sendMessage("${ChatColor.GOLD}Discord bot is running.")
                sender.sendMessage("${ChatColor.GOLD}Players online: ${plugin.server.onlinePlayers.size}/${plugin.server.maxPlayers}")
            }
            "setstatus" -> {
                if (!sender.hasPermission("stealbot.discord.setstatus")) {
                    sender.sendMessage("${ChatColor.RED}You don't have permission to set the bot status.")
                    return true
                }

                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /discord setstatus <message>")
                    return true
                }

                val message = args.drop(1).joinToString(" ")
                discordBot.setActivity(message)
                sender.sendMessage("${ChatColor.GREEN}Discord bot status updated to: $message")
            }
            else -> sendHelp(sender)
        }

        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GOLD}=== Discord Bot Commands ===")
        sender.sendMessage("${ChatColor.YELLOW}/discord reload ${ChatColor.WHITE}- Reload the Discord bot")
        sender.sendMessage("${ChatColor.YELLOW}/discord status ${ChatColor.WHITE}- Check Discord bot status")
        sender.sendMessage("${ChatColor.YELLOW}/discord setstatus <message> ${ChatColor.WHITE}- Set the bot's status message")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            val subCommands = mutableListOf<String>()

            if (sender.hasPermission("stealbot.discord.reload")) subCommands.add("reload")
            if (sender.hasPermission("stealbot.discord.status")) subCommands.add("status")
            if (sender.hasPermission("stealbot.discord.setstatus")) subCommands.add("setstatus")

            return subCommands.filter { it.startsWith(args[0].lowercase()) }
        }

        return emptyList()
    }
}
