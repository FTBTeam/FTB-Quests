package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class LocaleValue extends StringValue {
    public LocaleValue(SNBTConfig config, String value, String def) {
        super(config, value, def); // TODO: Wait for FTBLibrary
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void createClientConfig(ConfigGroup group) {
        group.add(key, new LocaleConfig(this), get(), this::set, defaultValue);
    }
}
