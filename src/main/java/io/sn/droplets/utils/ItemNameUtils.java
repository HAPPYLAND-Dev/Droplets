package io.sn.droplets.utils;

import com.alibaba.fastjson2.JSONObject;
import kotlin.text.Charsets;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;

public class ItemNameUtils {

    private static JSONObject translationMap;

    static {
        //noinspection DataFlowIssue
        var inp = Bukkit.getPluginManager().getPlugin("Dumortierite").getResource("zh_CN.lang.json");
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            int _byte;
            //noinspection DataFlowIssue
            while ((_byte = inp.read()) != -1)
                byteArrayOutputStream.write(_byte);
            var result = byteArrayOutputStream.toString(Charsets.UTF_8);
            byteArrayOutputStream.close();
            inp.close();

            translationMap = JSONObject.parse(result);
        } catch (Exception ignored) {
        }
    }

    public static String getItemName(ItemStack itm) {
        if (itm == null) return "";

        if (itm.getItemMeta().hasDisplayName()) {
            return MiniMessage.miniMessage().serialize(itm.displayName());
        } else {
            return translationMap.getString(itm.getType().translationKey());
        }
    }
}
