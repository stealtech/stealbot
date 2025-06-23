package org.stealtech.stealbot.discord

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DiscordConfig(private val plugin: JavaPlugin) {

    private var config: FileConfiguration = plugin.config

    // Discord bot token
    val token: String
        get() = config.getString("discord.token") ?: ""
    // Command prefix
    val prefix: String
        get() = config.getString("discord.prefix") ?: "!"
    // Guild ID
    val guildId: String
        get() = config.getString("discord.guild-id") ?: ""
    // Status message
    val statusMessage: String
        get() = config.getString("discord.status-message") ?: "Minecraft Server"
    // Bot status
    val enabled: Boolean
        get() = config.getBoolean("discord.enabled", false)
    fun load() {
        plugin.saveDefaultConfig()
        config = plugin.config
        if (!config.contains("discord")) {
            config.set("discord.token", "token here")
            config.set("discord.prefix", "!")
            config.set("discord.guild-id", "guild id")
            config.set("discord.status-message", "Minecraft Server")
            config.set("discord.enabled", false)
            plugin.saveConfig()
        }
    }
    fun reload() {
        plugin.reloadConfig()
        config = plugin.config
    }
}
