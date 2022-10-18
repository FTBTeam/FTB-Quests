package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.FluidConfig;
import dev.ftb.mods.ftblibrary.config.NBTConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class FluidTask extends Task {
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public Fluid fluid = Fluids.WATER;
	public CompoundTag fluidNBT = null;
	public long amount = FluidStack.bucketAmount();

	private FluidStack cachedFluidStack = null;

	public FluidTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.FLUID;
	}

	@Override
	public long getMaxProgress() {
		return amount;
	}

	@Override
	public String formatMaxProgress() {
		return getVolumeString(amount);
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
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("fluid", Registries.getId(fluid, Registry.FLUID_REGISTRY).toString());
		nbt.putLong("amount", amount);

		if (fluidNBT != null) {
			nbt.put("nbt", fluidNBT);
		}
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);

		fluid = Registry.FLUID.get(new ResourceLocation(nbt.getString("fluid")));

		if (fluid == null || fluid == Fluids.EMPTY) {
			fluid = Fluids.WATER;
		}

		amount = Math.max(1L, nbt.getLong("amount"));
		fluidNBT = (CompoundTag) nbt.get("nbt");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeResourceLocation(Registries.getId(fluid, Registry.FLUID_REGISTRY));
		buffer.writeNbt(fluidNBT);
		buffer.writeVarLong(amount);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		fluid = Registry.FLUID.get(buffer.readResourceLocation());

		if (fluid == null || fluid == Fluids.EMPTY) {
			fluid = Fluids.WATER;
		}

		fluidNBT = buffer.readNbt();
		amount = buffer.readVarLong();
	}

	@Override
	public void clearCachedData() {
		super.clearCachedData();
		cachedFluidStack = null;
	}

	public FluidStack createFluidStack() {
		if (cachedFluidStack == null) {
			cachedFluidStack = FluidStack.create(fluid, FluidStack.bucketAmount(), fluidNBT);
		}

		return cachedFluidStack;
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
		return Component.literal(getVolumeString(amount) + " of ").append(createFluidStack().getName());
	}

	@Override
	public Icon getAltIcon() {
		FluidStack stack = createFluidStack();
		String id = Optional.ofNullable(FluidStackHooks.getStillTexture(stack))
				.map(TextureAtlasSprite::getName).map(ResourceLocation::toString)
				.orElse("missingno");
		return Icon.getIcon(id).withTint(Color4I.rgb(FluidStackHooks.getColor(stack)));
	}

	@Override
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);

		config.add("fluid", new FluidConfig(false), fluid, v -> fluid = v, Fluids.WATER);
		config.add("fluid_nbt", new NBTConfig(), fluidNBT, v -> fluidNBT = v, null);
		config.addLong("amount", amount, v -> amount = v, FluidStack.bucketAmount(), 1, Long.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem() {
		return true;
	}

	@Override
	@Nullable
	public Object getIngredient() {
		return createFluidStack();
	}

}
