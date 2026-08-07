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
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.config.EditableIconItemStack;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.item.CustomIconItem;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToServer;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.NetUtils;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class QuestObjectBase implements Comparable<QuestObjectBase> {
	private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z0-9_]*$");
	private static Tristate sendNotifications = Tristate.DEFAULT;

	public final long id;

	private boolean invalid = false;
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
		if (json.isEmpty()) {
			return ItemStack.EMPTY;
		}

		var res = ItemStack.CODEC.parse(provider.createSerializationContext(Json5Ops.INSTANCE), json);
		if (res.isSuccess()) {
			return MissingItem.maybeRestoreItem(res.getOrThrow(), provider);
		} else {
			return MissingItem.createFromJson(json);
		}
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

	/**
	 * Strip out all non alphanumeric-characters, replace whitespace with '_', and trim any leading or trailing '_'
	 * characters to create a sanitized filename for this chapter. Called both clientside when a chapter is being
	 * created, and server-side whenever the filename is deserialized from Json5 or the network.
	 *
	 * @param name the unsanitized chapter name
	 * @return the sanitized chapter name
	 */
	public static Optional<String> sanitizeFilename(String name) {
		name = name.replace(' ', '_').replaceAll("\\W", "").toLowerCase().trim();

		while (name.startsWith("_")) {
			name = name.substring(1);
		}

		while (name.endsWith("_")) {
			name = name.substring(0, name.length() - 1);
		}

		return name.isEmpty() ? Optional.empty() : Optional.of(name);
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

		sendNotifications = progressChange.shouldNotify() ? Tristate.TRUE : Tristate.FALSE;
		forceProgress(teamData, progressChange);
		sendNotifications = Tristate.DEFAULT;
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
			ItemStack.CODEC.encodeStart(provider.createSerializationContext(Json5Ops.INSTANCE), rawIcon).ifSuccess(t -> json.add("icon", t));
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
			return cachedTitle;
		}

		if (!getRawTitle().isEmpty()) {
			cachedTitle = getQuestFile().isServerSide() ?
					Component.literal(getRawTitle()) :
					TextUtils.parseRawText(getRawTitle(), holderLookup());
		} else {
			cachedTitle = getAltTitle();
		}

		return cachedTitle;
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

	/**
	 * Called on object deletion. Responsible for cleaning up any self data and also any child objects
	 * (chapter -> quests, quest -> tasks etc.)
	 */
	public void deleteSelf() {
		invalidate();

		if (getQuestFile().removeFromMap(id) == null) {
			FTBQuests.LOGGER.warn("tried to remove quest object {} from ID map, but it wasn't present!", this);
		}
	}

    public void invalidate() {
        invalid = true;
    }

	public void editedFromGUI() {
		ClientQuestFile.getInstance().refreshGui();
	}

	public void editedFromGUIOnServer() {
	}

	public void onCreated() {
		if (getQuestFile().addToMap(this) != null) {
			FTBQuests.LOGGER.warn("quest object {} already in ID map, overwriting!", this);
		}
	}

	/**
	 * For quest object types which are stored in a file, return the path to that file, which is always relative
	 * to the quest book's top-level folder ({@code <instance_dir>/config/ftbquests/quests}).
	 * @return the storage path, or {@code Optional.empty()} for quest object types which aren't stored directly
	 */
	public Optional<Path> getPath() {
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

	public void onEditButtonClicked(Runnable gui, Component title) {
		EditableConfigGroup group = new EditableConfigGroup(FTBQuestsAPI.MOD_ID, accepted -> {
			gui.run();
			if (accepted && validateEditedConfig()) {
				Play2ServerNetworking.send(EditObjectMessage.forQuestObject(this));
			}
		}) {
			@Override
			public Component getName() {
				MutableComponent type = Component.literal(" [").append(Component.translatable("ftbquests." + getObjectType().getId())).append("]").withStyle(getObjectType().getColor());
				return Component.empty().append(title.copy().withStyle(ChatFormatting.UNDERLINE)).append(type);
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

	public final void onEditButtonClicked(Runnable gui) {
		onEditButtonClicked(gui, getTitle());
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
		copied.setRawTitle(orig.getRawTitle());
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

	/**
	 * Build the extra NBT data sent along with a quest object creation request to the server. Default is to include
	 * the initial raw title text for insertion into the translation manager. Override to augment this with any other
	 * extra data that needs to be handled in {@link BaseQuestFile#create(long, QuestObjectType, long, Json5Object)}.
	 *
	 * @return some nbt data
	 */
	public Json5Object makeCreationMetadata() {
		Json5Object json = new Json5Object();
		if (!getRawTitle().isEmpty()) {
			getQuestFile().getTranslationManager().addInitialTranslation(json, getQuestFile().getLocale(), TranslationKey.TITLE, getRawTitle());
		}
		return json;
	}
}
