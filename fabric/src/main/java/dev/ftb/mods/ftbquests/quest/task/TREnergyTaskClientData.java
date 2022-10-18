package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

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
