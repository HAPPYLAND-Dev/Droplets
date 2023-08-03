package io.sn.droplets.utils

import io.sn.droplets.DropletsCore
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.IOException
import java.nio.file.Files

enum class ShopType {
    SELL, BUY
}

object StorageUtils {

    fun createStock(plug: DropletsCore, id: String, name: String, type: ShopType) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + "$id.yml")) {
            if (exists()) throw IOException("该货架已存在")

            val yml = this.let { YamlConfiguration.loadConfiguration(it) }
            yml.set("id", id)
            yml.set("name", name)
            yml.set("type", type.name)
            yml.save(this)
        }
    }

    private tailrec fun findLastNewIndex(yml: YamlConfiguration, idx: Int): Int = when {
        yml.contains("stock.$idx") -> findLastNewIndex(yml, idx.inc())
        else -> idx
    }

    @Suppress("SENSELESS_COMPARISON")
    fun putStock(plug: DropletsCore, id: String, toSell: ItemStack, price: Double, slot: Int) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + "$id.yml")) {
            if (!exists()) throw IOException("不存在该货架")
            if (toSell == null || toSell.type == Material.AIR) throw Exception("不能上架空物品")

            val yml = this.let { YamlConfiguration.loadConfiguration(it) }

            val idx = findLastNewIndex(yml, 0)

            if (slot == -1 || slot >= idx) {
                yml.set("stock.$idx.item", toSell)
                yml.set("stock.$idx.price", price)
            } else {
                (slot + 1..idx).reversed().forEach {
                    yml.set("stock.$it.item", yml.getItemStack("stock.${it - 1}.item"))
                    yml.set("stock.$it.price", yml.getDouble("stock.${it - 1}.price"))
                }

                yml.set("stock.$slot.item", toSell)
                yml.set("stock.$slot.price", price)
            }

            yml.save(this)
        }
    }

    fun getStock(plug: DropletsCore, id: String): Triple<List<Pair<ItemStack, Double>>, String?, ShopType> {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + "$id.yml")) {
            if (!exists()) throw IOException("不存在该货架")

            val yml = this.let { YamlConfiguration.loadConfiguration(it) }

            val lidx = findLastNewIndex(yml, 0)

            return Triple((0 until lidx).map {
                Pair(
                    yml.getItemStack("stock.$it.item") ?: throw Exception("配置文件出错, 请检查"),
                    yml.getDouble("stock.$it.price")
                )
            }, yml.getString("name"), ShopType.valueOf(yml.getString("type")!!))
        }
    }

    fun delStock(plug: DropletsCore, id: String) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + "$id.yml")) {
            Files.delete(this.toPath())
        }
    }

    fun unStock(plug: DropletsCore, id: String, slot: Int) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + "$id.yml")) {
            if (!exists()) throw IOException("不存在该货架")

            val yml = this.let { YamlConfiguration.loadConfiguration(it) }

            val fidx = findLastNewIndex(yml, 0) - 1

            if (slot > fidx) throw Exception("该货架不存在这个货物")

            (slot until fidx).forEach {
                yml.set("stock.$it.item", yml.getItemStack("stock.${it + 1}.item"))
                yml.set("stock.$it.price", yml.getDouble("stock.${it + 1}.price"))
            }

            yml.set("stock.$fidx", null)

            yml.save(this)
        }
    }

}
