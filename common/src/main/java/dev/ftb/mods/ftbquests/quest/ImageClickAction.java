package dev.ftb.mods.ftbquests.quest;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.integration.docsmod.DocsModRegistry;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.function.Consumer;

public record ImageClickAction(ActionType actionType, String actionData) {
    public static final ImageClickAction NONE = new ImageClickAction(ActionType.NONE, "");

    public static final Codec<ImageClickAction> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            StringRepresentable.fromEnum(ActionType::values).fieldOf("type").forGetter(ImageClickAction::actionType),
            Codec.STRING.fieldOf("data").forGetter(ImageClickAction::actionData)
    ).apply(builder, ImageClickAction::new));
    public static final StreamCodec<FriendlyByteBuf, ImageClickAction> STREAM_CODEC = StreamCodec.composite(
            NetworkHelper.enumStreamCodec(ActionType.class), ImageClickAction::actionType,
            ByteBufCodecs.STRING_UTF8, ImageClickAction::actionData,
            ImageClickAction::new
    );

    public static ImageClickAction fromLegacy(String legacy) {
        if (legacy.startsWith("http://") || legacy.startsWith("https://") || legacy.startsWith("file://")) {
            URI uri = URI.create(legacy);
            return new ImageClickAction(ActionType.OPEN_URI, uri.toString());
        } else if (legacy.startsWith("#")) {
            return new ImageClickAction(ActionType.OPEN_QUEST, legacy.substring(1));
        } else if (legacy.startsWith("custom:")) {
            return new ImageClickAction(ActionType.CUSTOM_EVENT, legacy.substring(7));
        } else if (legacy.startsWith("command:")) {
            return new ImageClickAction(ActionType.RUN_COMMAND, legacy.substring(8));
        } else {
            return NONE;
        }
    }

    public static ImageClickAction fromString(String str) {
        String[] parts = str.split(":", 2);
        if (parts.length != 2) {
            return NONE;
        }
        ActionType type = ActionType.NAME_MAP.getNullable(parts[0]);
        return type == null ? NONE : new ImageClickAction(type, parts[1]);
    }

    public void run() {
        try {
            actionType.accept(actionData);
        } catch (Exception ex) {
            logHandleClickException(ex);
        }
    }

    @Override
    public String toString() {
        return actionType.id + ":" + actionData;
    }

    private static void openQuest(String questIdStr) {
        String[] fields = questIdStr.split("/");
        QuestObjectBase.parseHexId(fields[0]).ifPresentOrElse(questId -> {
            QuestObject qo = ClientQuestFile.getInstance().get(questId);
            if (qo != null) {
                ClientQuestFile.getInstance().getQuestScreen().ifPresent(questScreen -> {
                    if (qo instanceof Quest && fields.length >= 2 && StringUtils.isNumeric(fields[1]) && questScreen.getViewedQuest() != null) {
                        questScreen.viewQuestPanel.setCurrentPage(questId, Integer.parseInt(fields[1]) - 1);
                    }
                    questScreen.open(qo, false);
                });
            } else {
                QuestScreen.displayError("Unknown quest object id: %s", questIdStr);
            }
        }, () -> QuestScreen.displayError("Invalid quest object id: %s", questIdStr));
    }

    private static void postCustomEvent(String eventData) {
        try {
            CustomClickEvent.TYPE.post(new CustomClickEvent.Data(Identifier.parse(eventData)));
        } catch (IdentifierException ignored) {
        }
    }

    private static void runCommand(String command) {
        ClientUtils.execClientCommand(command, false);
    }

    private static void showRecipes(String itemIdStr) {
        try {
            Identifier id = Identifier.parse(itemIdStr);
            Item item = BuiltInRegistries.ITEM.get(id).orElseThrow().value();
            FTBQuests.getRecipeModHelper().showRecipes(new ItemStack(item));
        } catch (IdentifierException ignored) {
        }
    }

    private static void showDocs(String docsPath) {
        String[] fields = docsPath.split(",\\s*", 4);
        Preconditions.checkState(fields.length >= 2 && fields.length <= 4,
                "data must be in format: '<mod-id>,<book-id>[,<page-id>[,anchor]]'");
        String mod = fields[0];
        Identifier book = Identifier.tryParse(fields[1]);
        Identifier page = fields.length >= 3 ? Identifier.tryParse(fields[2]) : null;
        String anchor = fields.length == 4 ? fields[3] : "";

        DocsModRegistry.INSTANCE.getDocsMod(mod).ifPresentOrElse(
                docsMod -> docsMod.openDocsPage(ClientUtils.getClientPlayer(), book, page, anchor),
                () -> QuestScreen.displayError("Docs mod '%s' is not installed", mod));
    }

    private void logHandleClickException(Throwable ex) {
        FTBLibrary.LOGGER.warn("handleClick: unexpected exception handling action {} / '{}': {}", actionType.name(), actionData, ex.getMessage());
    }

    public ImageClickAction withType(ActionType newType) {
        return new ImageClickAction(newType, actionData);
    }

    public ImageClickAction withData(String newData) {
        return new ImageClickAction(actionType, newData);
    }

    public boolean isNone() {
        return actionType == ActionType.NONE;
    }

    // method refs cause class loading (unlike lambdas), don't use if it's a client-only class
    public enum ActionType implements Consumer<String>, StringRepresentable {
        NONE("none", _ -> {}),
        @SuppressWarnings("Convert2MethodRef") OPEN_URI("open_uri", uriStr -> FTBQuestsClient.openUri(uriStr)),
        OPEN_QUEST("open_quest", ImageClickAction::openQuest),
        RUN_COMMAND("run_command", ImageClickAction::runCommand),
        CUSTOM_EVENT("custom_event", ImageClickAction::postCustomEvent),
        SHOW_RECIPE("show_recipe", ImageClickAction::showRecipes),
        SHOW_DOCS("show_docs", ImageClickAction::showDocs)
        ;

        public static final NameMap<ActionType> NAME_MAP = NameMap.of(ActionType.OPEN_URI, ActionType.values())
                .baseNameKey("ftbquests.click_action_type")
                .id(type -> type.id)
                .create();

        private final String id;
        private final Consumer<String> consumer;

        ActionType(String id, Consumer<String> consumer) {
            this.id = id;
            this.consumer = consumer;
        }

        @Override
        public void accept(String s) {
            consumer.accept(s);
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }
}
