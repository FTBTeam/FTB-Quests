package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;

public class LocaleValue extends StringValue {
    public LocaleValue(SNBTConfig config, String value, String def) {
        super(config, value, def);
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
        group.add(key, new LocaleConfig(), get(), this::set, defaultValue);
    }
}
