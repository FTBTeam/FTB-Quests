package dev.ftb.mods.ftbquests.quest.task.neoforge;

import dev.ftb.mods.ftbquests.client.EnergyTaskClientData;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public enum ForgeEnergyTaskClientData implements EnergyTaskClientData {
    INSTANCE;

    @Override
    public TextureAtlasSprite getEmptyTexture() {
        return FTBQuestsClientEventHandler.feEnergyEmptySprite;
    }

    @Override
    public TextureAtlasSprite getFullTexture() {
        return FTBQuestsClientEventHandler.feEnergyFullSprite;
    }
}
