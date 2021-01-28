package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.net.MessageMoveQuest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject implements Movable
{
	public Chapter chapter;
	public String subtitle;
	public double x, y;
	public Tristate hide;
	public String shape;
	public final List<String> description;
	public final List<QuestObject> dependencies;
	public final List<Task> tasks;
	public final List<Reward> rewards;
	public DependencyRequirement dependencyRequirement;
	public String guidePage;
	public Tristate hideDependencyLines;
	public int minRequiredDependencies;
	public Tristate hideTextUntilComplete;
	public Tristate disableJEI;
	public double size;
	public boolean optional;
	public int minWidth;

	private MutableComponent cachedDescription = null;
	private MutableComponent[] cachedText = null;

	public Quest(Chapter c)
	{
		chapter = c;
		subtitle = "";
		x = 0;
		y = 0;
		shape = "";
		description = new ArrayList<>(0);
		dependencies = new ArrayList<>(0);
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		guidePage = "";
		hideDependencyLines = Tristate.DEFAULT;
		hide = Tristate.DEFAULT;
		dependencyRequirement = DependencyRequirement.ALL_COMPLETED;
		minRequiredDependencies = 0;
		hideTextUntilComplete = Tristate.DEFAULT;
		disableJEI = Tristate.DEFAULT;
		size = 1D;
		optional = false;
		minWidth = 0;
	}

	@Override
	public String toString()
	{
		return chapter.filename + ":" + getCodeString();
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.QUEST;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return chapter.file;
	}

	@Override
	public Chapter getQuestChapter()
	{
		return chapter;
	}

	@Override
	public int getParentID()
	{
		return chapter.id;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);

		if (!shape.isEmpty())
		{
			nbt.putString("shape", shape);
		}

		if (!subtitle.isEmpty())
		{
			nbt.putString("subtitle", subtitle);
		}

		if (!description.isEmpty())
		{
			ListTag array = new ListTag();

			for (String value : description)
			{
				array.add(StringTag.valueOf(value));
			}

			nbt.put("description", array);
		}

		if (!guidePage.isEmpty())
		{
			nbt.putString("guide_page", guidePage);
		}

		if (hideDependencyLines != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide_dependency_lines", hideDependencyLines.isTrue());
		}

		if (minRequiredDependencies > 0)
		{
			nbt.putInt("min_required_dependencies", (byte) minRequiredDependencies);
		}

		removeInvalidDependencies();

		if (!dependencies.isEmpty())
		{
			int[] ai = new int[dependencies.size()];
			int i = 0;

			for (QuestObject dep : dependencies)
			{
				ai[i] = dep.id;
				i++;
			}

			nbt.putIntArray("dependencies", ai);
		}

		if (hide != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide", hide.isTrue());
		}

		if (dependencyRequirement != DependencyRequirement.ALL_COMPLETED)
		{
			nbt.putString("dependency_requirement", dependencyRequirement.id);
		}

		if (hideTextUntilComplete != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide_text_until_complete", hideTextUntilComplete.isTrue());
		}

		if (size != 1D)
		{
			nbt.putDouble("size", size);
		}

		if (optional)
		{
			nbt.putBoolean("optional", true);
		}

		if (minWidth > 0)
		{
			nbt.putInt("min_width", minWidth);
		}
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		subtitle = nbt.getString("subtitle");
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		shape = nbt.getString("shape");

		if (shape.equals("default"))
		{
			shape = "";
		}

		description.clear();

		ListTag list = nbt.getList("description", NbtType.STRING);

		for (int k = 0; k < list.size(); k++)
		{
			description.add(list.getString(k));
		}

		guidePage = nbt.getString("guide_page");
		hideDependencyLines = Tristate.read(nbt, "hide_dependency_lines");
		minRequiredDependencies = nbt.getInt("min_required_dependencies");

		dependencies.clear();

		for (int i : nbt.getIntArray("dependencies"))
		{
			QuestObject object = chapter.file.get(i);

			if (object != null)
			{
				dependencies.add(object);
			}
		}

		hide = Tristate.read(nbt, "hide");
		dependencyRequirement = DependencyRequirement.NAME_MAP.get(nbt.getString("dependency_requirement"));
		hideTextUntilComplete = Tristate.read(nbt, "hide_text_until_complete");
		size = nbt.contains("size") ? nbt.getDouble("size") : 1D;
		optional = nbt.getBoolean("optional");
		minWidth = nbt.getInt("min_width");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !subtitle.isEmpty());
		flags = Bits.setFlag(flags, 2, !description.isEmpty());
		flags = Bits.setFlag(flags, 4, size != 1D);
		flags = Bits.setFlag(flags, 8, !guidePage.isEmpty());
		//implement others
		//flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		//flags = Bits.setFlag(flags, 64, hideDependencyLines);
		//flags = Bits.setFlag(flags, 128, hideTextUntilComplete);
		flags = Bits.setFlag(flags, 256, optional);
		flags = Bits.setFlag(flags, 512, minWidth > 0);
		buffer.writeVarInt(flags);

		hide.write(buffer);
		hideDependencyLines.write(buffer);
		hideTextUntilComplete.write(buffer);

		if (!subtitle.isEmpty())
		{
			buffer.writeUtf(subtitle, Short.MAX_VALUE);
		}

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeUtf(shape, Short.MAX_VALUE);

		if (!description.isEmpty())
		{
			NetUtils.writeStrings(buffer, description);
		}

		if (!guidePage.isEmpty())
		{
			buffer.writeUtf(guidePage, Short.MAX_VALUE);
		}

		buffer.writeVarInt(minRequiredDependencies);
		DependencyRequirement.NAME_MAP.write(buffer, dependencyRequirement);
		buffer.writeVarInt(dependencies.size());

		for (QuestObject d : dependencies)
		{
			if (d.invalid)
			{
				buffer.writeInt(0);
			}
			else
			{
				buffer.writeInt(d.id);
			}
		}

		if (size != 1D)
		{
			buffer.writeDouble(size);
		}

		if (minWidth > 0)
		{
			buffer.writeVarInt(minWidth);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		int flags = buffer.readVarInt();
		hide = Tristate.read(buffer);
		hideDependencyLines = Tristate.read(buffer);
		hideTextUntilComplete = Tristate.read(buffer);

		subtitle = Bits.getFlag(flags, 1) ? buffer.readUtf(Short.MAX_VALUE) : "";
		x = buffer.readDouble();
		y = buffer.readDouble();
		shape = buffer.readUtf(Short.MAX_VALUE);

		if (Bits.getFlag(flags, 2))
		{
			NetUtils.readStrings(buffer, description);
		}
		else
		{
			description.clear();
		}

		//customClick = Bits.getFlag(flags, 4) ? buffer.readString() : "";
		guidePage = Bits.getFlag(flags, 8) ? buffer.readUtf(Short.MAX_VALUE) : "";
		//customClick = Bits.getFlag(flags, 32) ? data.readString() : "";
		//hideDependencyLines = Bits.getFlag(flags, 64);
		//hideTextUntilComplete = Bits.getFlag(flags, 128);
		optional = Bits.getFlag(flags, 256);

		minRequiredDependencies = buffer.readVarInt();
		dependencyRequirement = DependencyRequirement.NAME_MAP.read(buffer);
		dependencies.clear();
		int d = buffer.readVarInt();

		for (int i = 0; i < d; i++)
		{
			QuestObject object = chapter.file.get(buffer.readInt());

			if (object != null)
			{
				dependencies.add(object);
			}
		}

		size = Bits.getFlag(flags, 4) ? buffer.readDouble() : 1D;
		minWidth = Bits.getFlag(flags, 512) ? buffer.readVarInt() : 0;
	}

	@Override
	public int getRelativeProgressFromChildren(PlayerData data)
	{
		/*if (data.getTimesCompleted(this) > 0)
		{
			return 100;
		}*/

		if (tasks.isEmpty())
		{
			return data.areDependenciesComplete(this) ? 100 : 0;
		}

		int progress = 0;

		for (Task task : tasks)
		{
			progress += data.getRelativeProgress(task);
		}

		if (progress > 0 && !data.areDependenciesComplete(this))
		{
			return 0;
		}

		return getRelativeProgressFromChildren(progress, tasks.size());
	}

	@Override
	public void onCompleted(PlayerData data, List<ServerPlayer> onlineMembers, List<ServerPlayer> notifiedPlayers)
	{
		//data.setTimesCompleted(this, data.getTimesCompleted(this) + 1);
		super.onCompleted(data, onlineMembers, notifiedPlayers);

		if (!disableToast)
		{
			for (ServerPlayer player : notifiedPlayers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		data.checkAutoCompletion(this);
		ObjectCompletedEvent.QUEST.invoker().act(new ObjectCompletedEvent.QuestEvent(data, this, onlineMembers, notifiedPlayers));

		for (Chapter chapter : chapter.file.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.dependencies.contains(this))
				{
					data.checkAutoCompletion(quest);
				}
			}
		}

		if (data.isComplete(chapter))
		{
			chapter.onCompleted(data, onlineMembers, notifiedPlayers);
		}
	}

	@Override
	public void changeProgress(PlayerData data, ChangeProgress type)
	{
		//FIXME: data.setTimesCompleted(this, -1);

		if (type.dependencies)
		{
			for (QuestObject dependency : dependencies)
			{
				if (!dependency.invalid)
				{
					dependency.changeProgress(data, type);
				}
			}
		}

		for (Task task : tasks)
		{
			task.changeProgress(data, type);
		}

		if (type.reset)
		{
			for (Reward r : rewards)
			{
				data.setRewardClaimed(r.id, false);
			}
		}
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (Task task : tasks)
		{
			list.add(task.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public MutableComponent getAltTitle()
	{
		if (!tasks.isEmpty())
		{
			return tasks.get(0).getTitle();
		}

		return new TranslatableComponent("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		chapter.quests.remove(this);
	}

	@Override
	public void deleteChildren()
	{
		for (Task task : tasks)
		{
			task.deleteChildren();
			task.invalid = true;
		}

		for (Reward reward : rewards)
		{
			reward.deleteChildren();
			reward.invalid = true;
		}

		tasks.clear();
		rewards.clear();
	}

	@Override
	public void onCreated()
	{
		chapter.quests.add(this);

		if (!tasks.isEmpty())
		{
			List<Task> l = new ArrayList<>(tasks);
			tasks.clear();
			for (Task task : l)
			{
				task.onCreated();
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("subtitle", subtitle, v -> subtitle = v, "");
		config.addList("description", description, new ConfigString(), "");
		config.addEnum("shape", shape.isEmpty() ? "default" : shape, v -> shape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
		config.addTristate("hide", hide, v -> hide = v);
		config.addDouble("size", size, v -> size = v, 1, 0.0625D, 8D);
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		Predicate<QuestObjectBase> depTypes = object -> object != chapter.file && object != chapter && object instanceof QuestObject;// && !(object instanceof Task);

		config.addList("dependencies", dependencies, new ConfigQuestObject<>(depTypes), null).setNameKey("ftbquests.dependencies");
		config.addEnum("dependency_requirement", dependencyRequirement, v -> dependencyRequirement = v, DependencyRequirement.NAME_MAP);
		config.addInt("min_required_dependencies", minRequiredDependencies, v -> minRequiredDependencies = v, 0, 0, Integer.MAX_VALUE);
		config.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
		config.addString("guide_page", guidePage, v -> guidePage = v, "");
		config.addTristate("hide_text_until_complete", hideTextUntilComplete, v -> hideTextUntilComplete = v);
		config.addEnum("disable_jei", disableJEI, v -> disableJEI = v, Tristate.NAME_MAP);
		config.addBool("optional", optional, v -> optional = v, false);
		config.addInt("min_width", minWidth, v -> minWidth = v, 0, 0, 3000);
	}

	@Override
	public Chapter getChapter()
	{
		return chapter;
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public double getWidth()
	{
		return size;
	}

	@Override
	public double getHeight()
	{
		return size;
	}

	@Override
	public String getShape()
	{
		return shape.isEmpty() ? chapter.getDefaultQuestShape() : shape;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void move(Chapter to, double x, double y)
	{
		new MessageMoveQuest(id, to.id, x, y).sendToServer();
	}

	@Override
	public boolean isVisible(PlayerData data)
	{
		if (dependencies.isEmpty())
		{
			return true;
		}

		if (hide.get(false))
		{
			return data.areDependenciesComplete(this);
		}

		for (QuestObject object : dependencies)
		{
			if (object.isVisible(data))
			{
				return true;
			}
		}

		return false;
	}

	public Task getTask(int index)
	{
		if (tasks.isEmpty())
		{
			throw new IllegalStateException("Quest has no tasks!");
		}
		else if (index <= 0)
		{
			return tasks.get(0);
		}
		else if (index >= tasks.size())
		{
			return tasks.get(tasks.size() - 1);
		}

		return tasks.get(index);
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		cachedDescription = null;
		cachedText = null;

		for (Task task : tasks)
		{
			task.clearCachedData();
		}

		for (Reward reward : rewards)
		{
			reward.clearCachedData();
		}
	}

	@Environment(EnvType.CLIENT)
	public MutableComponent getSubtitle()
	{
		if (cachedDescription != null)
		{
			return cachedDescription;
		}

		MutableComponent textDesc = loadText().getComponent("description");

		if (textDesc != TextComponent.EMPTY)
		{
			cachedDescription = textDesc;
			return cachedDescription;
		}

		String key = String.format("quests.%08x.description", id);
		MutableComponent t = FTBQuestsClient.addI18nAndColors(I18n.get(key));

		if (t == TextComponent.EMPTY || key.equals(t.getString()))
		{
			cachedDescription = FTBQuestsClient.addI18nAndColors(subtitle);
		}
		else
		{
			cachedDescription = t;
		}

		return cachedDescription;
	}

	@Environment(EnvType.CLIENT)
	public MutableComponent[] getDescription()
	{
		if (cachedText != null)
		{
			return cachedText;
		}

		cachedText = loadText().getComponentArray("text");

		if (cachedText.length > 0)
		{
			return cachedText;
		}

		if (description.isEmpty())
		{
			return new MutableComponent[0];
		}

		cachedText = new MutableComponent[description.size()];

		for (int i = 0; i < cachedText.length; i++)
		{
			cachedText[i] = FTBQuestsClient.addI18nAndColors(description.get(i));
		}

		return cachedText;
	}

	public boolean hasDependency(QuestObject object)
	{
		if (object.invalid)
		{
			return false;
		}

		for (QuestObject dependency : dependencies)
		{
			if (dependency == object)
			{
				return true;
			}
		}

		return false;
	}

	public void removeInvalidDependencies()
	{
		if (!dependencies.isEmpty())
		{
			dependencies.removeIf(o -> o == null || o.invalid || o == this);
		}
	}

	public boolean verifyDependencies(boolean autofix)
	{
		try
		{
			verifyDependenciesInternal(id, 0);
			return true;
		}
		catch (DependencyDepthException ex)
		{
			if (autofix)
			{
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				dependencies.clear();
				chapter.file.save();
			}
			else
			{
				FTBQuests.LOGGER.error("Too deep dependencies found in " + this + " (referenced in " + ex.object + ")!");
			}

			return false;
		}
		catch (DependencyLoopException ex)
		{
			if (autofix)
			{
				FTBQuests.LOGGER.error("Looping dependencies found in " + this + " (referenced in " + ex.object + ")! Deleting all dependencies...");
				dependencies.clear();
				chapter.file.save();
			}
			else
			{
				FTBQuests.LOGGER.error("Looping dependencies found in " + this + " (referenced in " + ex.object + ")!");
			}

			return false;
		}
	}

	@Override
	protected void verifyDependenciesInternal(int original, int depth)
	{
		if (depth >= 1000)
		{
			throw new DependencyDepthException(this);
		}

		for (QuestObject dependency : dependencies)
		{
			if (dependency.id == original)
			{
				throw new DependencyLoopException(this);
			}

			dependency.verifyDependenciesInternal(original, depth + 1);
		}
	}

	@Override
	public int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void editedFromGUI()
	{
		GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

		if (gui != null)
		{
			gui.questPanel.refreshWidgets();
			gui.viewQuestPanel.refreshWidgets();
		}
	}

	public void moved(double nx, double ny, int nc)
	{
		x = nx;
		y = ny;

		if (nc != chapter.id)
		{
			QuestFile f = getQuestFile();
			Chapter c = f.getChapter(nc);

			if (c != null)
			{
				chapter.quests.remove(this);
				c.quests.add(this);
				chapter = c;
			}
		}
	}

	public boolean isProgressionIgnored()
	{
		return optional;
	}

	public List<QuestObject> getDependants()
	{
		List<QuestObject> list = new ArrayList<>();

		for (Chapter c : chapter.file.chapters)
		{
			for (Quest q : c.quests)
			{
				if (q.dependencies.contains(this))
				{
					list.add(q);
				}
			}
		}

		return list;
	}
}