package dev.ftb.mods.ftbquests.quest.reward;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyHelper;
import dev.ftb.mods.ftblibrary.integration.currency.CurrencyProvider;
import dev.ftb.mods.ftbquests.net.NotifyRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;

public class CurrencyReward extends Reward {
    private int coinAmount;

    public CurrencyReward(long id, Quest q) {
        this(id, q, 1);
    }

    public CurrencyReward(long id, Quest q, int coinAmount) {
        super(id, q);
        this.coinAmount = coinAmount;
    }

    public int getCoinAmount() {
        return coinAmount;
    }

    @Override
    public RewardType getType() {
        return RewardTypes.CURRENCY;
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putInt("amount", coinAmount);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        coinAmount = nbt.getIntOr("amount", 0);
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarInt(coinAmount);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        coinAmount = buffer.readVarInt();
    }

    @Override
    public Component getAltTitle() {
        return Component.empty().append(Component.literal("⬤ ").withStyle(ChatFormatting.YELLOW)).append(String.valueOf(coinAmount));
    }

    @Override
    public void fillConfigGroup(EditableConfigGroup config) {
        super.fillConfigGroup(config);

        config.addInt("coins", coinAmount, v -> coinAmount = v, 100, 1, Integer.MAX_VALUE)
                .setNameKey("ftbquests.reward.ftbquests.currency");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        CurrencyProvider provider = CurrencyHelper.getInstance().getProvider();

        if (provider.isValidProvider()) {
            provider.giveCurrency(player, coinAmount);

            if (notify) {
                Component msg = Component.literal(Integer.toString(coinAmount)).append(" ").append(provider.coinName(coinAmount > 1));
                NetworkManager.sendToPlayer(player, new NotifyRewardMessage(id, msg, Icons.MONEY, disableRewardScreenBlur));
            }
        }
    }
}
