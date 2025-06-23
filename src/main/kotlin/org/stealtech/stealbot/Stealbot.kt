package org.stealtech.stealbot

import org.bukkit.plugin.java.JavaPlugin
import org.stealtech.stealbot.discord.DiscordBot

class Stealbot : JavaPlugin() {
    private var discordBot: DiscordBot? = null
    override fun onEnable() {
        saveDefaultConfig()
        discordBot = DiscordBot(this).apply {
            start()
        }
        getCommand("discord")?.setExecutor(DiscordCommand(this, discordBot!!))
        logger.info("Stealbot has been enabled!")
    }
    override fun onDisable() {
        discordBot?.stop()
        discordBot = null

        logger.info("Stealbot has been disabled!")
    }
}
