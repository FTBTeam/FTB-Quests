package dev.ftb.mods.ftbquests.quest;

import com.mojang.serialization.Codec;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.config.EditableIconItemStack;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToServer;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.registry.ModDataComponents;
import dev.ftb.mods.ftbquests.registry.ModItems;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class QuestObjectBase implements Comparable<QuestObjectBase> {
	private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9_]*$");
	private static Tristate sendNotifications = Tristate.DEFAULT;

	public final long id;

	protected boolean invalid = false;
	private ItemStack rawIcon = ItemStack.EMPTY;
	private List<String> tags = new ArrayList<>(0);

	@Nullable
	private Icon<?> cachedIcon = null;
	@Nullable
	private Component cachedTitle = null;
	@Nullable
	private Set<String> cachedTags = null;

	// stores translations in the client-side proto-quest-object before it's sent to server
	protected EnumMap<TranslationKey,String> protoTranslations = new EnumMap<>(TranslationKey.class);

	public QuestObjectBase(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static boolean isNull(@Nullable QuestObjectBase object) {
		return object == null || object.invalid;
	}

	public static long getID(@Nullable QuestObjectBase object) {
		return isNull(object) ? 0L : object.id;
	}

	public static String getCodeString(long id) {
		return String.format("%016X", id);
	}

	public static String getCodeString(@Nullable QuestObjectBase object) {
		return getCodeString(getID(object));
	}

	public static boolean shouldSendNotifications() {
		return sendNotifications.get(true);
	}

	public static ItemStack itemOrMissingFromJson(Json5Object json, HolderLookup.Provider provider) {
//		CompoundTag compoundTag = processItemTagData(tag);
		return json.isEmpty() ?
				ItemStack.EMPTY :
				ItemStack.CODEC.parse(provider.createSerializationContext(Json5Ops.INSTANCE), json).result()
								.orElse(createMissing(json));
	}

//	public static ItemStack singleItemOrMissingFromJson(Json5Object json, HolderLookup.Provider provider) {
//		return json.isEmpty() ?
//				ItemStack.EMPTY :
//				ItemStack.CODEC.parse(provider.createSerializationContext(Json5Ops.INSTANCE), json).result()
//						.orElse(createMissing(json));
//	}

	// support for importing SNBT for itemstacks from legacy (1.20 and older) quest book data
//	private static CompoundTag processItemTagData(Json5Object tag) {
//		if (tag instanceof StringTag s) {
//			// 1.20 or earlier, item name only
//			return Util.make(new CompoundTag(), t -> t.putString("id", s.asString().orElseThrow()));
//		} else if (tag instanceof CompoundTag c) {
//			if (c.contains("Count") || c.contains("tag")) {
//				// 1.20 or earlier with count and/or NBT data; migrate, but no NBT -> component data conversion
//				return Util.make(new CompoundTag(), t -> {
//					t.putString("id", c.getString("id").orElseThrow());
//					int count = c.getInt("Count").orElse(1);
//					if (count != 0) t.putInt("count", count);
//				});
//			} else {
//				// 1.21 or later; it's good as-is
//				return c;
//			}
//		} else {
//			// shouldn't get here?
//			return new CompoundTag();
//		}
//	}

	private static ItemStack createMissing(Json5Object json) {
		String id = Json5Util.getString(json, "id").orElse("unknown");
		int count = Math.max(1, Json5Util.getInt(json, "count").orElse(1));
		String text = count == 1 ? id : count + "x " + id;

		return Util.make(new ItemStack(ModItems.MISSING_ITEM.get()),
				stack -> stack.set(ModDataComponents.MISSING_ITEM_DESC.get(), text));
	}

	public final boolean isValid() {
		return !invalid;
	}

	public final void setRawIcon(ItemStack rawIcon) {
		this.rawIcon = rawIcon;
	}

	public String getRawTitle() {
		if (!getQuestFile().isServerSide() && protoTranslations.containsKey(TranslationKey.TITLE)) {
			return protoTranslations.get(TranslationKey.TITLE);
		}
		return getQuestFile().getTranslationManager().getStringTranslation(this, getQuestFile().getLocale(), TranslationKey.TITLE)
				.orElse("");
	}

	public void setRawTitle(String rawTitle) {
		setTranslatableValue(TranslationKey.TITLE, rawTitle);
		cachedTitle = null;
	}

	protected final void setTranslatableValue(TranslationKey translationKey, String value) {
		if (id != 0L) {
			String locale = getQuestFile().getLocale();
			getQuestFile().getTranslationManager().addTranslation(this, locale, translationKey, value);
			if (!getQuestFile().isServerSide()) {
				Play2ServerNetworking.send(SyncTranslationMessageToServer.create(this, locale, translationKey, value));
			}
		} else if (!getQuestFile().isServerSide()) {
			protoTranslations.put(translationKey, value);
		}
	}

	protected final void setTranslatableValue(TranslationKey translationKey, List<String> value) {
		if (id != 0L) {
			String locale = getQuestFile().getLocale();
			getQuestFile().getTranslationManager().addTranslation(this, locale, translationKey, value);
			if (!getQuestFile().isServerSide()) {
				Play2ServerNetworking.send(SyncTranslationMessageToServer.create(this, locale, translationKey, value));
			}
		}
		// proto-translations not handled here since there aren't any list values that need handling
	}

	/**
	 * Only used client-side; get the translation for a proto-quest-object currently being built on the client before
	 * it's sent to the server.
	 *
	 * @param key the translation key type
	 * @return the raw translation string
	 */
	public final String getProtoTranslation(TranslationKey key) {
		return protoTranslations.getOrDefault(key, "");
	}

	public final void modifyTranslatableListValue(TranslationKey translationKey, Consumer<List<String>> setter) {
		if (translationKey.isListVal()) {
			List<String> mutable = getQuestFile().getTranslationManager().getStringListTranslation(this, getQuestFile().getLocale(), translationKey)
					.map(ArrayList::new).orElse(new ArrayList<>());
			setter.accept(mutable);
			setTranslatableValue(translationKey, List.copyOf(mutable));
		}
	}

	public static long parseCodeString(String id) {
		if (id.isEmpty() || id.equals("-")) {
			return 0L;
		}

		try {
			return Long.parseLong(id.charAt(0) == '#' ? id.substring(1) : id, 16);
		} catch (Exception ex) {
			return 0L;
		}
	}

	public static Optional<Long> parseHexId(String id) {
		try {
			return Optional.of(Long.parseLong(id, 16));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public static Optional<String> titleToID(String s) {
		s = s.replace(' ', '_').replaceAll("\\W", "").toLowerCase().trim();

		while (s.startsWith("_")) {
			s = s.substring(1);
		}

		while (s.endsWith("_")) {
			s = s.substring(0, s.length() - 1);
		}

		return s.isEmpty() ? Optional.empty() : Optional.of(s);
	}

	public final String getCodeString() {
		return getCodeString(id);
	}

	public final String toString() {
		return getCodeString();
	}

	public final boolean equals(Object object) {
		return object == this;
	}

	public final int hashCode() {
		return Long.hashCode(id);
	}

	public abstract QuestObjectType getObjectType();

	public abstract BaseQuestFile getQuestFile();

	public Set<String> getTags() {
		if (tags.isEmpty()) {
			return Collections.emptySet();
		} else if (cachedTags == null) {
			cachedTags = new LinkedHashSet<>(tags);
		}

		return cachedTags;
	}

	public boolean hasTag(String tag) {
		return !tags.isEmpty() && getTags().contains(tag);
	}

	public void forceProgress(TeamData teamData, ProgressChange progressChange) {
	}

	public final void forceProgressRaw(TeamData teamData, ProgressChange progressChange) {
		if (teamData.isLocked()) {
			return;
		}

		teamData.clearCachedProgress();
		sendNotifications = progressChange.shouldNotify() ? Tristate.TRUE : Tristate.FALSE;
		forceProgress(teamData, progressChange);
		sendNotifications = Tristate.DEFAULT;
		teamData.clearCachedProgress();
		teamData.markDirty();
	}

	@Nullable
	public Chapter getQuestChapter() {
		return null;
	}

	public long getParentID() {
		return 1L;
	}

	public void writeData(Json5Object json, HolderLookup.Provider provider) {
		if (!rawIcon.isEmpty()) {
			ItemStack.CODEC.encodeStart(Json5Ops.INSTANCE, rawIcon).ifSuccess(t -> json.add("icon", t));
		}
		if (!tags.isEmpty()) {
			Json5Util.store(json, "tags", Codec.STRING.listOf(), tags);
		}
	}

	public void readData(Json5Object json, HolderLookup.Provider provider) {
		Json5Util.getJson5Object(json, "icon").ifPresent(icon -> rawIcon = itemOrMissingFromJson(icon, provider));

		tags = Json5Util.fetch(json, "tags", Codec.STRING.listOf()).orElseGet(ArrayList::new);

		if (json.has("custom_id")) {
			tags.add(json.get("custom_id").getAsString());
		}
	}

	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		int flags = 0;
		flags = Bits.setFlag(flags, 2, !rawIcon.isEmpty());
		flags = Bits.setFlag(flags, 4, !tags.isEmpty());

		buffer.writeVarInt(flags);

		if (!rawIcon.isEmpty()) {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, rawIcon);
		}

		if (!tags.isEmpty()) {
			NetUtils.writeStrings(buffer, tags);
		}
	}

	public void readNetData(RegistryFriendlyByteBuf buffer) {
		int flags = buffer.readVarInt();
		rawIcon = Bits.getFlag(flags, 2) ? ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer) : ItemStack.EMPTY;
		tags = new ArrayList<>(0);

		if (Bits.getFlag(flags, 4)) {
			NetUtils.readStrings(buffer, tags);
		}
	}

	protected boolean hasTitleConfig() {
		return true;
	}

	protected boolean hasIconConfig() {
		return true;
	}

	public void fillConfigGroup(EditableConfigGroup config) {
		if (hasTitleConfig()) {
			config.addString("title", getRawTitle(), this::setRawTitle, "").setNameKey("ftbquests.title").setOrder(-127);
		}

		if (hasIconConfig()) {
			config.add("icon", new EditableIconItemStack(), rawIcon, v -> rawIcon = v, ItemStack.EMPTY).setNameKey("ftbquests.icon").setOrder(-126);
		}

		config.addList("tags", tags, new EditableString(TAG_PATTERN), "").setNameKey("ftbquests.tags").setOrder(-125);
	}

	public abstract Component getAltTitle();

	public abstract Icon<?> getAltIcon();

	public final Component getTitle() {
		if (cachedTitle != null) {
			return cachedTitle.copy();
		}

		if (!getRawTitle().isEmpty()) {
			cachedTitle = TextUtils.parseRawText(getRawTitle(), holderLookup());
		} else {
			cachedTitle = getAltTitle();
		}

		return cachedTitle.copy();
	}

	public final MutableComponent getMutableTitle() {
		return getTitle().copy();
	}

	public final Icon<?> getIcon() {
		if (cachedIcon == null) {
			if (!rawIcon.isEmpty()) {
				cachedIcon = CustomIconItem.getIcon(rawIcon);
			}
			if (cachedIcon == null || cachedIcon.isEmpty()) {
				cachedIcon = ThemeProperties.ICON.get(this);
			}
			if (cachedIcon.isEmpty()) {
				cachedIcon = getAltIcon();
			}
		}
		return cachedIcon;

	}

	public void deleteSelf() {
		getQuestFile().remove(id);
	}

	public void deleteChildren() {
	}

	public void editedFromGUI() {
		ClientQuestFile.getInstance().refreshGui();
	}

	public void editedFromGUIOnServer() {
	}

	public void onCreated() {
	}

	public Optional<String> getPath() {
		return Optional.empty();
	}

	public void clearCachedData() {
		cachedIcon = null;
		cachedTitle = null;
		cachedTags = null;
	}

	public EditableConfigGroup createSubGroup(EditableConfigGroup group) {
		return group.getOrCreateSubgroup(getObjectType().getId());
	}

	public void onEditButtonClicked(Runnable gui) {
		EditableConfigGroup group = new EditableConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
			gui.run();
			if (accepted && validateEditedConfig()) {
				Play2ServerNetworking.send(EditObjectMessage.forQuestObject(this));
			}
		}) {
			@Override
			public Component getName() {
				MutableComponent type = Component.literal(" [").append(Component.translatable("ftbquests." + getObjectType().getId())).append("]").withStyle(getObjectType().getColor());
				return Component.empty().append(getTitle().copy().withStyle(ChatFormatting.UNDERLINE)).append(type);
			}
		};

		fillConfigGroup(createSubGroup(group));

		new EditConfigScreen(group) {
			@Override
			public Component getTitle() {
				return group.getName();
			}
		}.openGui();
	}

	protected boolean validateEditedConfig() {
		return true;
	}

	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.noneOf(RecipeModHelper.Components.class);
	}

	public static <T extends QuestObjectBase> T copy(T orig, Supplier<T> factory) {
		T copied = factory.get();
		Json5Object tag = new Json5Object();
		orig.writeData(tag, orig.holderLookup());
		copied.readData(tag, orig.holderLookup());
		return copied;
	}

	@Override
	public int compareTo(QuestObjectBase other) {
		int typeCmp = Integer.compare(getObjectType().ordinal(), other.getObjectType().ordinal());
		return typeCmp == 0 ?
				getTitle().getString().toLowerCase().compareTo(other.getTitle().getString().toLowerCase()) :
				typeCmp;
	}

	public HolderLookup.Provider holderLookup() {
		return getQuestFile().holderLookup();
	}

//	protected CompoundTag saveItemSingleLine(ItemStack stack) {
//		if (stack.isEmpty()) {
//			return new SNBTCompoundTag();
//		}
//
//		return Util.make(SNBTCompoundTag.of(ItemStack.CODEC.encodeStart(holderLookup().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow()), SNBTCompoundTag::singleLine);
//	}
}
