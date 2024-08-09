package dev.ftb.mods.ftbquests.quest;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.ConfigIconItemStack;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private Icon cachedIcon = null;
	private Component cachedTitle = null;
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

	public static ItemStack itemOrMissingFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
		return tag.isEmpty() ?
				ItemStack.EMPTY :
				ItemStack.parse(provider, tag).orElse(createMissing(tag));
	}

	public static ItemStack singleItemOrMissingFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
		return tag.isEmpty() ?
				ItemStack.EMPTY :
				ItemStack.SINGLE_ITEM_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(createMissing(tag));
	}

	private static ItemStack createMissing(CompoundTag tag) {
		String id = tag.getString("id");
		int count = Math.max(1, tag.getInt("count"));
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
				NetworkManager.sendToServer(SyncTranslationMessageToServer.create(this, locale, translationKey, value));
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
				NetworkManager.sendToServer(SyncTranslationMessageToServer.create(this, locale, translationKey, value));
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

	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		if (!rawIcon.isEmpty()) {
			ItemStack.SINGLE_ITEM_CODEC.encodeStart(NbtOps.INSTANCE, rawIcon).ifSuccess(t -> nbt.put("icon", t));
		}

		if (!tags.isEmpty()) {
			nbt.put("tags", Util.make(new ListTag(), l -> {
				for (String s : tags) {
					l.add(StringTag.valueOf(s));
				}
			}));
		}
	}

	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		rawIcon = singleItemOrMissingFromNBT(nbt.getCompound("icon"), provider);

		ListTag tagsList = nbt.getList("tags", Tag.TAG_STRING);

		tags = new ArrayList<>(tagsList.size());

		for (int i = 0; i < tagsList.size(); i++) {
			tags.add(tagsList.getString(i));
		}

		if (nbt.contains("custom_id")) {
			tags.add(nbt.getString("custom_id"));
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

	@Environment(EnvType.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		if (hasTitleConfig()) {
			config.addString("title", getRawTitle(), this::setRawTitle, "").setNameKey("ftbquests.title").setOrder(-127);
		}

		if (hasIconConfig()) {
			config.add("icon", new ConfigIconItemStack(), rawIcon, v -> rawIcon = v, ItemStack.EMPTY).setNameKey("ftbquests.icon").setOrder(-126);
		}

		config.addList("tags", tags, new StringConfig(TAG_PATTERN), "").setNameKey("ftbquests.tags").setOrder(-125);
	}

	@Environment(EnvType.CLIENT)
	public abstract Component getAltTitle();

	@Environment(EnvType.CLIENT)
	public abstract Icon getAltIcon();

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	public final MutableComponent getMutableTitle() {
		return getTitle().copy();
	}

	@Environment(EnvType.CLIENT)
	public final Icon getIcon() {
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

	@Environment(EnvType.CLIENT)
	public void editedFromGUI() {
		ClientQuestFile.INSTANCE.refreshGui();
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

	public ConfigGroup createSubGroup(ConfigGroup group) {
		return group.getOrCreateSubgroup(getObjectType().getId());
	}

	@Environment(EnvType.CLIENT)
	public void onEditButtonClicked(Runnable gui) {
		ConfigGroup group = new ConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
			gui.run();
			if (accepted && validateEditedConfig()) {
				NetworkManager.sendToServer(EditObjectMessage.forQuestObject(this));
			}
		});
		fillConfigGroup(createSubGroup(group));

		new EditConfigScreen(group).openGui();
	}

	protected boolean validateEditedConfig() {
		return true;
	}

	public Set<RecipeModHelper.Components> componentsToRefresh() {
		return EnumSet.noneOf(RecipeModHelper.Components.class);
	}

	public static <T extends QuestObjectBase> T copy(T orig, Supplier<T> factory) {
		T copied = factory.get();
		if (copied == null) {
			return null;
		}
		CompoundTag tag = new CompoundTag();
		orig.writeData(tag, orig.holderLookup());
		copied.readData(tag, orig.holderLookup());
		return copied;
	}

	@Override
	public int compareTo(@NotNull QuestObjectBase other) {
		int typeCmp = Integer.compare(getObjectType().ordinal(), other.getObjectType().ordinal());
		return typeCmp == 0 ?
				getTitle().getString().toLowerCase().compareTo(other.getTitle().getString().toLowerCase()) :
				typeCmp;
	}

	public HolderLookup.Provider holderLookup() {
		return getQuestFile().holderLookup();
	}

	protected CompoundTag saveItemSingleLine(ItemStack stack) {
		if (stack.isEmpty()) {
			return new SNBTCompoundTag();
		}

		return Util.make(SNBTCompoundTag.of(stack.save(holderLookup())), SNBTCompoundTag::singleLine);
	}

	/**
	 * Build the extra NBT data sent along with a quest object creation request to the server. Default is to include
	 * the initial raw title text for insertion into the translation manager. Override to augment this with any other
	 * extra data that needs to be handled in {@link BaseQuestFile#create(long, QuestObjectType, long, CompoundTag)}.
	 *
	 * @return some nbt data
	 */
	public CompoundTag makeExtraCreationData() {
		CompoundTag tag = new CompoundTag();
		if (getRawTitle() != null && !getRawTitle().isEmpty()) {
			getQuestFile().getTranslationManager().addInitialTranslation(tag, getQuestFile().getLocale(), TranslationKey.TITLE, getRawTitle());
		}
		return tag;
	}
}
