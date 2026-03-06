package dev.ftb.mods.ftbquests.client.config;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.StringValue;

public class LocaleValue extends StringValue {
    public LocaleValue(Config config, String value, String def) {
        super(config, value, def);
    }

    @Override
    protected EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.add(key, new EditableLocaleConfig(), get(), this::set, defaultValue);
    }
}
