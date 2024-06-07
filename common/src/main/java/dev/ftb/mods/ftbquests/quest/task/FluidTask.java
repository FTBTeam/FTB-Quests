package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidTask extends Task {
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuestsAPI.MOD_ID, "textures/tasks/tank.png");
	private static final FluidStack WATER = FluidStack.create(Fluids.WATER, FluidStack.bucketAmount());

	private FluidStack fluidStack = FluidStack.create(Fluids.WATER, FluidStack.bucketAmount());

	public FluidTask(long id, Quest quest) {
		super(id, quest);
	}

	public Fluid getFluid() {
		return fluidStack.getFluid();
	}

	public FluidTask setFluid(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
		return this;
	}

	public CompoundTag getFluidNBT() {
		return fluidStack.getTag();
	}

	@Override
	public TaskType getType() {
		return TaskTypes.FLUID;
	}

	@Override
	public long getMaxProgress() {
		return fluidStack.getAmount();
	}

	@Override
	public String formatMaxProgress() {
		return getVolumeString(fluidStack.getAmount());
	}

	@Override
	public String formatProgress(TeamData teamData, long progress) {
		return getVolumeString((int) Math.min(Integer.MAX_VALUE, progress));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public void addMouseOverText(TooltipList list, TeamData teamData) {
		super.addMouseOverText(list, teamData);

		if (FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
			list.blankLine();
			list.add(Component.translatable("ftbquests.task.ftbquests.item.shift_click_recipe").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		}
	}

	@Override
	public void onButtonClicked(Button button, boolean canClick) {
		if (FTBQuestsClient.isShiftPressed() && FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
			FTBQuests.getRecipeModHelper().showRecipes(fluidStack);
		} else {
			super.onButtonClicked(button, canClick);
		}
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);

		fluidStack.write(nbt);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);

		fluidStack = FluidStack.read(nbt);
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		fluidStack.write(buffer);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);

		fluidStack = FluidStack.read(buffer);
	}

	public static String getVolumeString(long a) {
		StringBuilder builder = new StringBuilder();

		if (a >= FluidStack.bucketAmount()) {
			if (a % FluidStack.bucketAmount() != 0L) {
				builder.append(StringUtils.formatDouble(a / (double) FluidStack.bucketAmount()));
			} else {
				builder.append(a / FluidStack.bucketAmount());
			}
			builder.append(" B");
		} else {
			builder.append(a).append(" mB");
		}

		return builder.toString();
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.literal(getVolumeString(fluidStack.getAmount()) + " of ").append(fluidStack.getName());
	}

	@Override
	public Icon getAltIcon() {
		return Icon.getIcon(ClientUtils.getStillTexture(fluidStack)).withTint(Color4I.rgb(ClientUtils.getFluidColor(fluidStack)));
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addFluidStack("fluid", fluidStack, v -> fluidStack = v, WATER, false);
	}

	@Override
	public boolean canInsertItem() {
		return true;
	}

	@Override
	@Nullable
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(fluidStack, widget);
	}
}
