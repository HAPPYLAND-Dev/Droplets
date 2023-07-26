package io.sn.droplets

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.sn.droplets.utils.GuiUtils
import io.sn.droplets.utils.StorageUtils
import org.bukkit.entity.Player
import java.io.File
import java.util.concurrent.CompletableFuture

class CommandBus(private val plug: DropletsCore) {

    fun setup() {
        CommandAPICommand("droplets")
            .withSubcommand(
                CommandAPICommand("create")
                    .withArguments(StringArgument("id"), TextArgument("name"))
                    .withPermission("droplets.edit.create")
                    .executesPlayer(PlayerCommandExecutor { plr, args ->
                        val id = args["id"] as String
                        val name = args["name"] as String

                        try {
                            StorageUtils.createStock(plug, id, name)
                            plug.sendmsg(plr, "<green>货架创建成功")
                        } catch (ex: Exception) {
                            plug.sendmsg(plr, "<red>${ex.message}")
                        }
                    })
            )
            .withSubcommand(
                CommandAPICommand("del")
                    .withArguments(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                        CompletableFuture.supplyAsync {
                            val dd = File(plug.dataFolder.path + File.separator + "storage")
                            dd.list { _, name ->
                                name.endsWith(".yml")
                            }?.map {
                                it.split(".yml")[0]
                            }?.toTypedArray()
                        }
                    }))
                    .withPermission("droplets.edit.del")
                    .executesPlayer(PlayerCommandExecutor { plr, args ->
                        val id = args["id"] as String

                        try {
                            StorageUtils.delStock(plug, id)
                            plug.sendmsg(plr, "<green>货架删除成功")
                        } catch (ex: Exception) {
                            plug.sendmsg(plr, "<red>${ex.message}")
                        }
                    })
            )
            .withSubcommand(
                CommandAPICommand("put")
                    .withArguments(
                        StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                            CompletableFuture.supplyAsync {
                                val dd = File(plug.dataFolder.path + File.separator + "storage")
                                dd.list { _, name ->
                                    name.endsWith(".yml")
                                }?.map {
                                    it.split(".yml")[0]
                                }?.toTypedArray()
                            }
                        }),
                        DoubleArgument("price", 0.0),
                        IntegerArgument("slot", -1).setOptional(true)
                    )
                    .withPermission("droplets.edit.put")
                    .executesPlayer(PlayerCommandExecutor { plr, args ->
                        val id = args["id"] as String
                        val price = args["price"] as Double
                        val slot = args.getOptional("slot").orElse(-1) as Int
                        val hand = plr.inventory.itemInMainHand.asOne()

                        try {
                            StorageUtils.putStock(plug, id, hand, price, slot)
                            plug.sendmsg(plr, "<green>商品上架成功")
                        } catch (ex: Exception) {
                            plug.sendmsg(plr, "<red>${ex.message}")
                        }
                    })
            )
            .withSubcommand(
                CommandAPICommand("unstock")
                    .withArguments(
                        StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                            CompletableFuture.supplyAsync {
                                val dd = File(plug.dataFolder.path + File.separator + "storage")
                                dd.list { _, name ->
                                    name.endsWith(".yml")
                                }?.map {
                                    it.split(".yml")[0]
                                }?.toTypedArray()
                            }
                        }),
                        IntegerArgument("slot", 0)
                    )
                    .withPermission("droplets.edit.unstock")
                    .executesPlayer(PlayerCommandExecutor { plr, args ->
                        val id = args["id"] as String
                        val slot = args["slot"] as Int

                        try {
                            StorageUtils.unStock(plug, id, slot)
                            plug.sendmsg(plr, "<green>商品下架成功")
                        } catch (ex: Exception) {
                            plug.sendmsg(plr, "<red>${ex.message}")
                        }
                    })
            )
            .withSubcommand(
                CommandAPICommand("gui")
                    .withPermission("droplets.view.gui")
                    .withArguments(
                        StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                            CompletableFuture.supplyAsync {
                                val dd = File(plug.dataFolder.path + File.separator + "storage")
                                dd.list { _, name ->
                                    name.endsWith(".yml")
                                }?.map {
                                    it.split(".yml")[0]
                                }?.toTypedArray()
                            }
                        }),
                    )
                    .executesPlayer(PlayerCommandExecutor { plr, args ->
                        val id = args["id"] as String

                        try {
                            GuiUtils.openGuiFor(plug, plr, id, 0)
                        } catch (ex: Exception) {
                            plr.closeInventory()
                            plug.sendmsg(plr, "<red>${ex.message}")
                        }
                    })
            ).withSubcommand(
                CommandAPICommand("openfor")
                    .withPermission("droplets.view.openfor")
                    .withArguments(
                        PlayerArgument("player"),
                        StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                            CompletableFuture.supplyAsync {
                                val dd = File(plug.dataFolder.path + File.separator + "storage")
                                dd.list { _, name ->
                                    name.endsWith(".yml")
                                }?.map {
                                    it.split(".yml")[0]
                                }?.toTypedArray()
                            }
                        }),
                    )
                    .executes(CommandExecutor { _, args ->
                        val p = args["player"] as Player
                        val id = args["id"] as String

                        try {
                            GuiUtils.openGuiFor(plug, p, id, 0)
                        } catch (ex: Exception) {
                            p.closeInventory()
                            plug.sendmsg(p, "<red>${ex.message}")
                        }
                    })
            ).register()
    }
}
