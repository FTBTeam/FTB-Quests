package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.architectury.fluid.FluidStack;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class FluidTask extends Task {
	public static final Identifier TANK_TEXTURE = FTBQuestsAPI.id("textures/tasks/tank.png");
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

	public DataComponentMap getFluidDataComponents() {
		return fluidStack.getComponents();
	}

	public DataComponentPatch getFluidDataComponentPatch() {
		return fluidStack.getComponents().asPatch();
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
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		nbt.put("fluid", fluidStack.write(provider, new CompoundTag()));
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		var stringComp = nbt.getString("fluid");
		if (stringComp.isPresent()) {
			// legacy - fluid stored as string ID
			Identifier id = Identifier.tryParse(stringComp.get());
			if (id == null) {
				// Default to water if invalid
				fluidStack = FluidStack.create(Fluids.WATER, 1000L);
			} else {
				BuiltInRegistries.FLUID.get(id).ifPresentOrElse(stack -> {
					fluidStack = FluidStack.create(stack, nbt.getLongOr("amount", 1000L));
				}, () -> {
					// Default to water if invalid
					fluidStack = FluidStack.create(Fluids.WATER, 1000L);
				});
			}
			return;
		}

		nbt.getCompound("fluid")
				.ifPresentOrElse(tag -> fluidStack = FluidStack.read(provider, tag).orElse(FluidStack.empty()), () -> fluidStack = FluidStack.empty());
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		fluidStack.write(buffer);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
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
	public void fillConfigGroup(EditableConfigGroup config) {
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
