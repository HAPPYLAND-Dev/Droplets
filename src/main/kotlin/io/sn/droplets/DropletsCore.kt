package io.sn.droplets

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class DropletsCore : JavaPlugin() {

    companion object {
        private val minimsg: MiniMessage = MiniMessage.miniMessage()
        lateinit var econ: Economy

        fun mini(msg: String): Component = minimsg.deserialize(msg)
    }

    override fun onEnable() {
        if (!setupEconomy()) server.pluginManager.disablePlugin(this)
        setupConfigs()
        setupCommands()
    }

    private fun setupConfigs() {
        saveDefaultConfig()
        with(File(dataFolder.path + File.separator + "storage")) {
            if (!exists()) mkdir()
        }
    }

    private fun setupCommands() {
        CommandBus(this).setup()
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.provider
        return true
    }

    fun sendmsg(audience: Audience, msg: String) {
        audience.sendMessage(minimsg.deserialize(config.getString("prefix") + msg))
    }

}
