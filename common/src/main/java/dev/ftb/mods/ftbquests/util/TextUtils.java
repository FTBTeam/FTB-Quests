package dev.ftb.mods.ftbquests.util;

import com.google.gson.JsonParseException;
import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern JSON_TEXT_PATTERN = Pattern.compile("^[{\\[]\\s*\"");

    /**
     * Parse some rich text into a Component. Use vanilla-style raw JSON if possible, fall back to old-style FTB
     * Quests rich text otherwise. (FTB Quests rich text is more concise, raw JSON is much more powerful)
     *
     * @param str the raw string to parse
     * @return a component, which could be the error message if parsing failed
     */
    public static Component parseRawText(String str, HolderLookup.Provider provider) {
        return JSON_TEXT_PATTERN.matcher(str).find() ?
                deserializeRawJsonText(str, provider) :
                ClientTextComponentUtils.parse(str);
    }

    private static Component deserializeRawJsonText(String raw, HolderLookup.Provider provider) {
        try {
            return Component.Serializer.fromJson(raw, provider);
        } catch (JsonParseException e) {
            return Component.literal("ERROR: " + e.getMessage()).withStyle(ChatFormatting.RED);
        }
    }

    public static List<String> fromListTag(ListTag tag) {
        List<String> res = new ArrayList<>();
        tag.forEach(el -> {
            if (el.getId() == Tag.TAG_STRING) {
                res.add(el.getAsString());
            }
        });
        return res;
    }
}
