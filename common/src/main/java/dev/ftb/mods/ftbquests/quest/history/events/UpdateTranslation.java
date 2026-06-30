package dev.ftb.mods.ftbquests.quest.history.events;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.quest.translation.TranslationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record UpdateTranslation(
        long questObjectId,
        QuestObjectType questObjectType,
        String locale,
        TranslationKey subKey,
        Either<String, List<String>> oldVal,
        Either<String, List<String>> newVal
) implements QuestBookEditEvent
{
    public static Optional<UpdateTranslation> create(ServerQuestFile file, QuestObjectBase qob, String locale,
                                                     TranslationKey subKey, Either<String, List<String>> newVal)
    {
        TranslationManager mgr = file.getTranslationManager();

        Either<String,List<String>> curVal = mgr.getEntry(qob, locale, subKey);
        return Objects.equals(curVal, newVal) ?
                Optional.empty() :
                Optional.of(new UpdateTranslation(qob.getId(), qob.getObjectType(), locale, subKey, curVal, newVal));
    }

    @Override
    public void apply(ServerQuestFile file) {
        if (file.getBase(questObjectId) instanceof QuestObjectBase qo) {
            newVal.ifLeft(string -> file.getTranslationManager().addTranslation(qo, locale, subKey, string))
                    .ifRight(list -> file.getTranslationManager().addTranslation(qo, locale, subKey, list));

            NetworkHelper.sendToAll(file.server, new SyncTranslationMessageToClient(questObjectId, locale, subKey, newVal));
        }
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        if (file.getBase(questObjectId) instanceof QuestObjectBase qo) {
            if (oldVal.map(String::isEmpty, List::isEmpty)) {
                file.getTranslationManager().removeTranslation(qo, locale, subKey);
                NetworkHelper.sendToAll(file.server, new SyncTranslationMessageToClient(questObjectId, locale, subKey, subKey.emptyValue()));
            } else {
                oldVal.ifLeft(string -> file.getTranslationManager().addTranslation(qo, locale, subKey, string))
                        .ifRight(list -> file.getTranslationManager().addTranslation(qo, locale, subKey, list));

                NetworkHelper.sendToAll(file.server, new SyncTranslationMessageToClient(questObjectId, locale, subKey, oldVal));
            }
        }
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of(Component.translatable("ftbquests.event.update_translation",
                questObjectType.getDisplayString(),
                Component.translatable(subKey.getTranslationKey()).withStyle(ChatFormatting.GRAY),
                getTitle(file, questObjectId)
        ));
    }
}
