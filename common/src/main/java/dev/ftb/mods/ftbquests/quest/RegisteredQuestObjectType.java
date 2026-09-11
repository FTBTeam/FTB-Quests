package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.util.FTBQUtils;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class RegisteredQuestObjectType<T extends QuestObjectBase> {
    private final Identifier typeId;
    private final Provider<T> provider;
    private final Supplier<Icon<?>> iconSupplier;
    private final Component displayName;
    @Nullable
    private GuiProvider<T> guiProvider;
    private final int internalId;

    public RegisteredQuestObjectType(Identifier typeId, Provider<T> provider, Supplier<Icon<?>> iconSupplier, int internalId, Component displayName, @Nullable GuiProvider<T> guiProvider) {
        this.typeId = typeId;
        this.provider = provider;
        this.iconSupplier = iconSupplier;
        this.internalId = internalId;
        this.displayName = displayName;
        this.guiProvider = guiProvider;
    }

    @Nullable
    public static <T extends RegisteredQuestObjectType<? extends QuestObjectBase>> T get(String typeStr, Map<Identifier, T> map, String fallback) {
        try {
            return map.get(FTBQUtils.modDefaultedId(typeStr, fallback));
        } catch (IdentifierException e) {
            return null;
        }
    }

    public static <T extends RegisteredQuestObjectType<? extends QuestObjectBase>> T getOrThrow(String typeStr, Map<Identifier, T> map, String fallback) {
        var type = get(typeStr, map, fallback);
        if (type == null) {
            throw new IllegalArgumentException("Invalid task type '" + typeStr + "'");
        }
        return type;
    }

    public Identifier getTypeId() {
        return typeId;
    }

    public int getInternalId() {
        return internalId;
    }

    public T create(long id, Quest quest) {
        return provider.create(id, quest);
    }

    public String getTypeForSerialization() {
        return FTBQUtils.modDefaultedString(typeId);
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Icon<?> getIcon() {
        return iconSupplier.get();
    }

    public RegisteredQuestObjectType<T> setGuiProvider(GuiProvider<T> guiProvider) {
        this.guiProvider = guiProvider;
        return this;
    }

    public GuiProvider<T> getGuiProviderOrThrow() {
        return Objects.requireNonNull(guiProvider);
    }

    public void ifGuiProvider(Consumer<GuiProvider<T>> consumer) {
        if (guiProvider != null) {
            consumer.accept(guiProvider);
        }
    }

    @FunctionalInterface
    public interface Provider<T> {
        T create(long id, Quest quest);
    }

    @FunctionalInterface
    public interface GuiProvider<T> {
        void openCreationGui(Panel panel, Quest quest, Consumer<T> callback);
    }
}
