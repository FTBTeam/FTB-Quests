package dev.ftb.mods.ftbquests.quest.loot;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
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

	private static final Map<String, LootCrate> LOOT_CRATES_CLIENT = new LinkedHashMap<>();
	private static final Map<String, LootCrate> LOOT_CRATES_SERVER = new LinkedHashMap<>();

	private final RewardTable table;
	private String stringID;
	private String itemName = "";
	private Color4I color = Color4I.WHITE;
	private boolean glow = false;
	private EntityWeight drops = new EntityWeight(0, 0, 0);

	public LootCrate(RewardTable table, boolean initFromTable) {
		this.table = table;

		if (initFromTable) {
			stringID = initFromTable();
		} else {
			stringID = table.toString();
		}
	}

	public static Map<String,LootCrate> getLootCrates(boolean isClient) {
		return isClient ? LOOT_CRATES_CLIENT : LOOT_CRATES_SERVER;
	}

	private String initFromTable() {
		String id = buildStringID(table);
		Defaults def = Defaults.NAME_MAP.getNullable(id);
		if (def != null) {
			color = Color4I.rgb(def.color);
			glow = def.glow;
			drops = new EntityWeight(def.passive, def.monster, def.boss);
		}
		return id;
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

	public void writeData(Json5Object json) {
		json.addProperty("string_id", stringID);
		json.addProperty("color", color.rgb());
		if (!itemName.isEmpty()) json.addProperty("item_name", itemName);
		if (glow) json.addProperty("glow", true);
		Json5Util.store(json, "drops", EntityWeight.CODEC, drops);
	}

	public void readData(Json5Object json) {
		stringID = Json5Util.getString(json, "string_id").orElseThrow();
		color = Color4I.rgb(Json5Util.getInt(json, "color").orElseThrow());
		Json5Util.getString(json,"item_name").ifPresent(s -> itemName = s);
		Json5Util.getBoolean(json, "glow").ifPresent(bool -> glow = bool);
		drops = Json5Util.fetch(json, "drops", EntityWeight.CODEC).orElse(EntityWeight.zero());
	}

	public void writeNetData(FriendlyByteBuf buf) {
		buf.writeUtf(stringID, Short.MAX_VALUE);
		buf.writeUtf(itemName, Short.MAX_VALUE);
		buf.writeInt(color.rgb());
		buf.writeBoolean(glow);
		EntityWeight.STREAM_CODEC.encode(buf, drops);
	}

	public void readNetData(FriendlyByteBuf buf) {
		stringID = buf.readUtf(Short.MAX_VALUE);
		itemName = buf.readUtf(Short.MAX_VALUE);
		color = Color4I.rgb(buf.readInt());
		glow = buf.readBoolean();
		drops = EntityWeight.STREAM_CODEC.decode(buf);
	}

	public void fillConfigGroup(EditableConfigGroup config) {
		config.addString("id", stringID, v -> stringID = v, "", Pattern.compile("[a-z0-9_]+"));
		config.addString("item_name", itemName, v -> itemName = v, "");
		config.addColor("color", color, v -> color = v, Color4I.WHITE);
		config.addBool("glow", glow, v -> glow = v, true);

		EditableConfigGroup d = config.getOrCreateSubgroup("drops");
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

	public static Collection<ItemStack> allCrateStacks(boolean isClientSide) {
		return getLootCrates(isClientSide).values().stream().map(LootCrate::createStack).toList();
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
