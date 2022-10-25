package dev.ftb.mods.ftbquests.util;

import com.google.gson.JsonParseException;
import dev.ftb.mods.ftblibrary.util.ClientTextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

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
    public static Component parseRawText(String str) {
        return JSON_TEXT_PATTERN.matcher(str).find() ?
                deserializeRawJsonText(str) :
                ClientTextComponentUtils.parse(str);
    }

    private static Component deserializeRawJsonText(String raw) {
        try {
            return Component.Serializer.fromJson(raw);
        } catch (JsonParseException e) {
            return new TextComponent("ERROR: " + e.getMessage()).withStyle(ChatFormatting.RED);
        }
    }
}
