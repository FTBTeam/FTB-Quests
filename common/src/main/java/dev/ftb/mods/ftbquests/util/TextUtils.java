package dev.ftb.mods.ftbquests.util;

import com.google.gson.JsonParseException;
import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextUtils {
    /**
     * Parse some rich text into a Component. Use vanilla-style raw JSON if applicable, fall back to old-style FTB
     * Quests rich text otherwise. (FTB Quests rich text is more concise, raw JSON is much more powerful)
     *
     * @param str the raw string to parse
     * @return a component, which could be the error message if parsing failed
     */
    public static Component parseRawText(String str) {
        String str2 = str.trim();
        if (str2.startsWith("[") && str2.endsWith("]") || str2.startsWith("{") && str2.endsWith("}")) {
            // could be JSON raw text, but not for definite...
            try {
                MutableComponent res = Component.Serializer.fromJson(str2);
                if (res != null) {
                    return res;
                }
            } catch (JsonParseException ignored) {
            }
        }
        return ClientTextComponentUtils.parse(str);
    }
}
