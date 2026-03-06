package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;

public enum TREnergyTaskClientData implements EnergyTaskClientData {
    INSTANCE;

    @Override
    public TextureAtlasSprite getEmptyTexture() {
        return FTBQuestsClientEventHandler.trEnergyEmptySprite;
    }

    @Override
    public TextureAtlasSprite getFullTexture() {
        return FTBQuestsClientEventHandler.trEnergyFullSprite;
    }
}
