package dev.ftb.mods.ftbquests.quest.loot;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LootCrate {
	private static final Pattern NON_ALPHANUM = Pattern.compile("[^a-z0-9_]");
	private static final Pattern MULTI_UNDERSCORE = Pattern.compile("_{2,}");

	public static Map<String, LootCrate> LOOT_CRATES = new LinkedHashMap<>();

	private final RewardTable table;
	private String stringID;
	private String itemName = "";
	private Color4I color = Color4I.WHITE;
	private boolean glow = false;
	private EntityWeight drops = new EntityWeight();

	public LootCrate(RewardTable table, boolean initFromTable) {
		this.table = table;

		if (initFromTable) {
			initFromTable();
		} else {
			stringID = table.toString();
		}
	}

	public void initFromTable() {
		stringID = buildStringID(table);
		Defaults def = Defaults.NAME_MAP.getNullable(stringID);
		if (def != null) {
			color = Color4I.rgb(def.color);
			glow = def.glow;
			drops.passive = def.passive;
			drops.monster = def.monster;
			drops.boss = def.boss;
		}
	}

	private static String buildStringID(RewardTable table) {
		Matcher matcher = NON_ALPHANUM.matcher(table.getTitle().getString().toLowerCase());
		Matcher matcher1 = MULTI_UNDERSCORE.matcher(matcher.replaceAll("_"));
		return matcher1.replaceAll("_");
	}

	public RewardTable getTable() {
		return table;
	}

	public String getItemName() {
		return itemName;
	}

	public Color4I getColor() {
		return color;
	}

	public boolean isGlow() {
		return glow;
	}

	public EntityWeight getDrops() {
		return drops;
	}

	public void writeData(CompoundTag nbt) {
		nbt.putString("string_id", stringID);

		if (!itemName.isEmpty()) {
			nbt.putString("item_name", itemName);
		}

		nbt.putInt("color", color.rgb());

		if (glow) {
			nbt.putBoolean("glow", true);
		}

		CompoundTag nbt1 = new CompoundTag();
		drops.writeData(nbt1);
		nbt.put("drops", nbt1);
	}

	public void readData(CompoundTag nbt) {
		stringID = nbt.getString("string_id");
		itemName = nbt.getString("item_name");
		color = Color4I.rgb(nbt.getInt("color"));
		glow = nbt.getBoolean("glow");
		drops.readData(nbt.getCompound("drops"));
	}

	public void writeNetData(FriendlyByteBuf data) {
		data.writeUtf(stringID, Short.MAX_VALUE);
		data.writeUtf(itemName, Short.MAX_VALUE);
		data.writeInt(color.rgb());
		data.writeBoolean(glow);
		drops.writeNetData(data);
	}

	public void readNetData(FriendlyByteBuf data) {
		stringID = data.readUtf(Short.MAX_VALUE);
		itemName = data.readUtf(Short.MAX_VALUE);
		color = Color4I.rgb(data.readInt());
		glow = data.readBoolean();
		drops.readNetData(data);
	}

	public void fillConfigGroup(ConfigGroup config) {
		config.addString("id", stringID, v -> stringID = v, "", Pattern.compile("[a-z0-9_]+"));
		config.addString("item_name", itemName, v -> itemName = v, "");
		config.addColor("color", color, v -> color = v, Color4I.WHITE);
		config.addBool("glow", glow, v -> glow = v, true);

		ConfigGroup d = config.getOrCreateSubgroup("drops");
		d.setNameKey("ftbquests.loot.entitydrops");
		d.addInt("passive", drops.passive, v -> drops.passive = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.passive");
		d.addInt("monster", drops.monster, v -> drops.monster = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.monster");
		d.addInt("boss", drops.boss, v -> drops.boss = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.loot.entitytype.boss");
	}

	public String getStringID() {
		return stringID.isEmpty() ? QuestObjectBase.getCodeString(table) : stringID;
	}

	public ItemStack createStack() {
		ItemStack stack = new ItemStack(ModItems.LOOTCRATE.get());
		stack.set(ModDataComponents.LOOT_CRATE.get(), getStringID());
		return stack;
	}

	public static Collection<ItemStack> allCrateStacks() {
		return LOOT_CRATES.values().stream().map(LootCrate::createStack).toList();
	}

	private enum Defaults {
		COMMON("common", 0x92999A, 350, 10, 0, false),
		UNCOMMON("uncommon", 0x37AA69, 200, 90, 0, false),
		RARE("rare", 0x0094FF, 50, 200, 0, false),
		EPIC("epic", 0x8000FF, 9, 10, 10, false),
		LEGENDARY("legendary", 0xFFC147, 1, 1, 190, true),
		;

		private final String name;
		private final int color;
		private final int passive;
		private final int monster;
		private final int boss;
		private final boolean glow;

		static final NameMap<Defaults> NAME_MAP = NameMap.of(COMMON, values()).create();

		Defaults(String name, int color, int passive, int monster, int boss, boolean glow) {
			this.name = name;
			this.color = color;
			this.passive = passive;
			this.monster = monster;
			this.boss = boss;
			this.glow = glow;
		}
	}
}