package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.quest.Quest;

public class TechRebornEnergyTask extends EnergyTask {
    public static TaskType TYPE;
    public static final Identifier EMPTY_TEXTURE = FTBQuestsAPI.id("textures/tasks/ic2_empty.png");
    public static final Identifier FULL_TEXTURE = FTBQuestsAPI.id("textures/tasks/ic2_full.png");

    public TechRebornEnergyTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public EnergyTaskClientData getClientData() {
        return TREnergyTaskClientData.INSTANCE;
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.task.ftbquests.tech_reborn_energy.text", StringUtils.formatDouble(getValue(), true));
    }
}
