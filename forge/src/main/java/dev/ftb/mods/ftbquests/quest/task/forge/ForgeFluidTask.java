package dev.ftb.mods.ftbquests.quest.task.forge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.FluidStackHooks;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.FluidConfig;
import dev.ftb.mods.ftblibrary.config.NBTConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ForgeFluidTask extends Task {
	public static TaskType TYPE;
	public static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public Fluid fluid = Fluids.WATER;
	public CompoundTag fluidNBT = null;
	public long amount = FluidStack.bucketAmount().longValue();

	private FluidStack cachedFluidStack = null;

	public ForgeFluidTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return TYPE;
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

		if (a >= FluidStack.bucketAmount().longValue()) {
			if (a % FluidStack.bucketAmount().longValue() != 0L) {
				builder.append(a / (double) FluidStack.bucketAmount().longValue());
			} else {
				builder.append(a / FluidStack.bucketAmount().longValue());
			}
		} else {
			builder.append(a % FluidStack.bucketAmount().longValue());
		}

		builder.append(' ');

		if (a < FluidStack.bucketAmount().longValue()) {
			builder.append('m');
		}

		builder.append('B');
		return builder.toString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public MutableComponent getAltTitle() {
		return new TextComponent(getVolumeString(amount) + " of ").append(createFluidStack().getName());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Icon getAltIcon() {
		FluidStack stack = createFluidStack();
		return Icon.getIcon(Optional.ofNullable(FluidStackHooks.getStillTexture(stack)).map(TextureAtlasSprite::getName).map(ResourceLocation::toString).orElse("missingno"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);

		config.add("fluid", new FluidConfig(false), fluid, v -> fluid = v, Fluids.WATER);
		config.add("fluid_nbt", new NBTConfig(), fluidNBT, v -> fluidNBT = v, null);
		config.addLong("amount", amount, v -> amount = v, FluidStack.bucketAmount().longValue(), 1, Long.MAX_VALUE);
	}

	@Override
	public boolean canInsertItem() {
		return true;
	}

	@Override
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public Object getIngredient() {
		return createFluidStack();
	}

		/*public int fill(FluidStack resource, IFluidHandler.FluidAction action)
		{
			if ( resource.getAmount().isGreaterThan(Fraction.zero()) && !isComplete() && Objects.equals(resource.getFluid(), task.createFluidStack().getFluid()) && data.canStartTasks(task.quest))
			{
				int add = (int) Math.min(resource.getAmount(), Math.min(Integer.MAX_VALUE, task.amount - progress));

				if (add > 0)
				{
					if (action.execute() && data.file.getSide().isServer())
					{
						addProgress(add);
					}

					return add;
				}
			}

			return 0;
		}*/

		/*
		@Override
		public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable Player player)
		{
			if (isComplete())
			{
				return stack;
			}

			IFluidHandlerItem handlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			if (handlerItem == null)
			{
				return stack;
			}

			FluidStack toDrain = task.createFluidStack();
			FluidStack drainedFluid = handlerItem.drain(toDrain, false);

			if (drainedFluid == null || drainedFluid.amount <= 0)
			{
				return stack;
			}

			IItemHandler inv = player == null ? null : player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			if (inv == null)
			{
				inv = ItemDestroyingInventory.INSTANCE;
			}

			ItemStack stack1 = stack.copy();
			FluidActionResult result = FluidUtil.tryFillContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);

			if (!result.isSuccess())
			{
				result = FluidUtil.tryEmptyContainerAndStow(stack1, this, inv, Integer.MAX_VALUE, player, !simulate);
			}

			if (result.isSuccess())
			{
				return player == null ? ItemStack.EMPTY : result.getResult();
			}

			return stack;
		}

		*/
}