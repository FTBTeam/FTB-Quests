package dev.ftb.mods.ftbquests.quest.task;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.client.util.TextureAtlasSpriteRef;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class FluidTask extends Task {
	public static final Identifier TANK_TEXTURE = FTBQuestsAPI.id("textures/tasks/tank.png");
	private static final FluidStack WATER = new FluidStack(Fluids.WATER, Platform.get().misc().bucketFluidAmount());

	private FluidStack fluidStack = new FluidStack(Fluids.WATER, Platform.get().misc().bucketFluidAmount());

	public FluidTask(long id, Quest quest) {
		super(id, quest);
	}

	public Fluid getFluid() {
		return fluidStack.fluid();
	}

	public FluidTask setFluid(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
		return this;
	}

	public DataComponentMap getFluidDataComponents() {
		return fluidStack.getComponents();
	}

	public DataComponentPatch getFluidDataComponentPatch() {
		return fluidStack.getComponents() instanceof PatchedDataComponentMap pdcm ? pdcm.asPatch() : DataComponentPatch.EMPTY;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.FLUID;
	}

	@Override
	public long getMaxProgress() {
		return fluidStack.amount();
	}

	@Override
	public String formatMaxProgress() {
		return getVolumeString(fluidStack.amount());
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
	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		super.writeData(json, provider);

		json.add("fluid", FluidStack.CODEC.encodeStart(provider.createSerializationContext(Json5Ops.INSTANCE), fluidStack).getOrThrow());
	}

	@Override
	public void readData(Json5Object json, HolderLookup.Provider provider) {
		super.readData(json, provider);

		var stringComp = Json5Util.getString(json, "fluid");
		if (stringComp.isPresent()) {
			// legacy - fluid stored as string ID
			Identifier id = Identifier.tryParse(stringComp.get());
			long bucketAmount = Platform.get().misc().bucketFluidAmount();
			if (id == null) {
				// Default to water if invalid
				fluidStack = new FluidStack(Fluids.WATER, bucketAmount);
			} else {
				BuiltInRegistries.FLUID.get(id).ifPresentOrElse(
						stack -> fluidStack = new FluidStack(stack, Json5Util.getLong(json, "amount").orElse(bucketAmount)),
						() -> fluidStack = new FluidStack(Fluids.WATER, bucketAmount)
				);
			}
		} else {
			Json5Util.getJson5Object(json, "fluid").ifPresentOrElse(
					o -> fluidStack = FluidStack.CODEC.parse(provider.createSerializationContext(Json5Ops.INSTANCE), o).result()
							.orElse(FluidStack.empty()),
					() -> fluidStack = FluidStack.empty()
			);
		}
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluidStack);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
	}

	public static String getVolumeString(long a) {
		StringBuilder builder = new StringBuilder();

		long bucketAmount = Platform.get().misc().bucketFluidAmount();
		if (a >= bucketAmount) {
			if (a % bucketAmount != 0L) {
				builder.append(StringUtils.formatDouble(a / (double) bucketAmount));
			} else {
				builder.append(a / bucketAmount);
			}
			builder.append(" B");
		} else {
			builder.append(a).append(" mB");
		}

		return builder.toString();
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.literal(getVolumeString(fluidStack.amount()) + " of ").append(fluidStack.name());
	}

	@Override
	public Icon<?> getAltIcon() {
		return new TextureAtlasSpriteRef(ClientUtils.getStillTexture(fluidStack))
				.createIcon(Color4I.rgb(ClientUtils.getFluidColor(fluidStack)));
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
