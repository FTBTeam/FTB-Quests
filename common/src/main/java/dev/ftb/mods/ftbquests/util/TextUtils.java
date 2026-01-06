package dev.ftb.mods.ftbquests.util;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    // deprecated in apache commons, but we don't have apache commons text available here
    @SuppressWarnings("deprecation")
    private static final UnicodeUnescaper UNESCAPER = new UnicodeUnescaper();

    /**
     * Parse some rich text into a Component. Use vanilla-style raw JSON if applicable, fall back to old-style FTB
     * Quests rich text otherwise. (FTB Quests rich text is more concise, raw JSON is much more powerful)
     *
     * @param str the raw string to parse
     * @return a component, which could be the error message if parsing failed
     */
    public static Component parseRawText(String str, HolderLookup.Provider provider) {
        String str2 = str.trim();
        if (str2.startsWith("[") && str2.endsWith("]") || str2.startsWith("{") && str2.endsWith("}")) {
            // could be JSON raw text, but not for definite...
            try {
                var jsonData = JsonParser.parseString(UNESCAPER.translate(str2));
                MutableComponent res = ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), jsonData).getOrThrow().copy();
                if (res != null) {
                    return res;
                }
            } catch (JsonParseException ignored) {
            }
        }
        return ClientTextComponentUtils.parse(UNESCAPER.translate(str));
    }

    public static List<String> fromListTag(ListTag tag) {
        List<String> res = new ArrayList<>();
        tag.forEach(el -> {
            if (el.getId() == Tag.TAG_STRING) {
                res.add(el.asString().orElseThrow());
            }
        });
        return res;
    }

    public static boolean isComponentEmpty(Component c) {
        return c.getSiblings().isEmpty() && c.getContents() == PlainTextContents.EMPTY;
    }
}
