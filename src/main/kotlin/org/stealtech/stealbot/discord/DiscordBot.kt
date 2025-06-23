package org.stealtech.stealbot.discord

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicReference


class DiscordBot(private val plugin: JavaPlugin) {
    private val logger = LoggerFactory.getLogger(DiscordBot::class.java)
    private val config = DiscordConfig(plugin)
    private val clientRef = AtomicReference<GatewayDiscordClient?>()
    private var messageListener: Disposable? = null
    fun start() {
        config.load()

        if (!config.enabled) {
            logger.info("Discord bot is disabled in the config.")
            return
        }
        val token = config.token
        if (token.isBlank() || token == "token here") {
            logger.warn("Discord bot token not configured! Update the config.yml with your bot token.")
            return
        }

        try {
            val client = DiscordClient.create(token)
            client.login().subscribe { gateway ->
                clientRef.set(gateway)
                setActivity(config.statusMessage)
                messageListener = gateway.on(MessageCreateEvent::class.java)
                    .filter { event -> event.message.content.startsWith(config.prefix) }
                    .subscribe { event -> handleCommand(event) }
                logger.info("Discord bot has started and logged in successfully!")
                gateway.onDisconnect().subscribe { logger.info("Discord bot disconnected") }
            }
        } catch (e: Exception) {
            logger.error("Failed to start Discord bot", e)
        }
    }
    fun stop() {
        messageListener?.dispose()
        messageListener = null

        val gateway = clientRef.getAndSet(null)
        gateway?.logout()?.block()

        logger.info("Discord bot has been shut down")
    }
    fun setActivity(message: String) {
        clientRef.get()?.let { gateway ->
            val activity = ClientActivity.playing(message)
            val presence = ClientPresence.online(activity)
            gateway.updatePresence(presence).subscribe()
        }
    }
    private fun handleCommand(event: MessageCreateEvent) {
        val message = event.message
        val content = message.content
        val parts = content.substring(config.prefix.length).split("\\s+".toRegex(), 2)
        val command = parts[0].lowercase()
        val args = if (parts.size > 1) parts[1] else ""

        when (command) {
            "ping" -> sendPing(message)
            "status" -> sendServerStatus(message)
            "help" -> sendHelp(message)
            // more commands when ready
        }
    }
    private fun sendPing(message: Message) {
        message.channel.flatMap { channel ->
            channel.createMessage("Pong! 🏓")
        }.subscribe()
    }
    private fun sendServerStatus(message: Message) {
        val onlinePlayers = plugin.server.onlinePlayers.size
        val maxPlayers = plugin.server.maxPlayers
        val tps = plugin.server.tps[0] // Get short term TPS

        val statusMessage = """
            **Server Status**
            Players: $onlinePlayers/$maxPlayers
            TPS: ${String.format("%.2f", tps)}
        """.trimIndent()

        message.channel.flatMap { channel ->
            channel.createMessage(statusMessage)
        }.subscribe()
    }
    private fun sendHelp(message: Message) {
        val helpMessage = """
            **Available Commands**
            `${config.prefix}ping` - Check if the bot is responding
            `${config.prefix}status` - Show server status
            `${config.prefix}help` - Show this help message
        """.trimIndent()

        message.channel.flatMap { channel ->
            channel.createMessage(helpMessage)
        }.subscribe()
    }
}
