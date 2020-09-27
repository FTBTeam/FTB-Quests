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
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

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
	public QuestShape shape;
	public final List<String> description;
	public final List<QuestObject> dependencies;
	public final List<Task> tasks;
	public final List<Reward> rewards;
	public DependencyRequirement dependencyRequirement;
	public String guidePage;
	public String customClick;
	public Tristate hideDependencyLines;
	public int minRequiredDependencies;
	public Tristate hideTextUntilComplete;
	public Tristate disableJEI;
	public double size;

	private String cachedDescription = null;
	private String[] cachedText = null;

	public Quest(Chapter c)
	{
		chapter = c;
		subtitle = "";
		x = 0;
		y = 0;
		shape = QuestShape.DEFAULT;
		description = new ArrayList<>(0);
		dependencies = new ArrayList<>(0);
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		guidePage = "";
		customClick = "";
		hideDependencyLines = Tristate.DEFAULT;
		hide = Tristate.DEFAULT;
		dependencyRequirement = DependencyRequirement.ALL_COMPLETED;
		minRequiredDependencies = 0;
		hideTextUntilComplete = Tristate.DEFAULT;
		disableJEI = Tristate.DEFAULT;
		size = 1D;
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
	public void writeData(CompoundNBT nbt)
	{
		super.writeData(nbt);
		nbt.putDouble("x", x);
		nbt.putDouble("y", y);
		nbt.putString("shape", shape.id);
		nbt.putString("subtitle", subtitle);
		nbt.putDouble("size", size);

		ListNBT descriptionArray = new ListNBT();

		for (String value : description)
		{
			descriptionArray.add(StringNBT.valueOf(value));
		}

		nbt.put("description", descriptionArray);

		if (!guidePage.isEmpty())
		{
			nbt.putString("guide_page", guidePage);
		}

		if (!customClick.isEmpty())
		{
			nbt.putString("custom_click", customClick);
		}

		if (hideDependencyLines != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide_dependency_lines", hideDependencyLines.isTrue());
		}

		nbt.putInt("min_required_dependencies", minRequiredDependencies);

		removeInvalidDependencies();

		int[] depArray = new int[dependencies.size()];
		int i = 0;

		for (QuestObject dep : dependencies)
		{
			depArray[i] = dep.id;
			i++;
		}

		nbt.putIntArray("dependencies", depArray);

		if (hide != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide", hide.isTrue());
		}

		nbt.putString("dependency_requirement", dependencyRequirement.id);

		if (hideTextUntilComplete != Tristate.DEFAULT)
		{
			nbt.putBoolean("hide_text_until_complete", hideTextUntilComplete.isTrue());
		}
	}

	@Override
	public void readData(CompoundNBT nbt)
	{
		super.readData(nbt);
		subtitle = nbt.getString("subtitle");
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		shape = QuestShape.NAME_MAP.get(nbt.getString("shape"));
		description.clear();

		ListNBT list = nbt.getList("description", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.size(); k++)
		{
			description.add(list.getString(k));
		}

		guidePage = nbt.getString("guide_page");
		customClick = nbt.getString("custom_click");
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
	}

	@Override
	public void writeNetData(PacketBuffer buffer)
	{
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, !subtitle.isEmpty());
		flags = Bits.setFlag(flags, 2, !description.isEmpty());
		flags = Bits.setFlag(flags, 4, !customClick.isEmpty());
		flags = Bits.setFlag(flags, 8, !guidePage.isEmpty());
		buffer.writeVarInt(flags);

		hide.write(buffer);
		hideDependencyLines.write(buffer);
		hideTextUntilComplete.write(buffer);

		if (!subtitle.isEmpty())
		{
			buffer.writeString(subtitle);
		}

		buffer.writeDouble(x);
		buffer.writeDouble(y);
		QuestShape.NAME_MAP.write(buffer, shape);

		if (!description.isEmpty())
		{
			NetUtils.writeStrings(buffer, description);
		}

		if (!guidePage.isEmpty())
		{
			buffer.writeString(guidePage);
		}

		if (!customClick.isEmpty())
		{
			buffer.writeString(customClick);
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

		buffer.writeDouble(size);
	}

	@Override
	public void readNetData(PacketBuffer buffer)
	{
		super.readNetData(buffer);
		int flags = buffer.readVarInt();
		hide = Tristate.read(buffer);
		hideDependencyLines = Tristate.read(buffer);
		hideTextUntilComplete = Tristate.read(buffer);

		subtitle = Bits.getFlag(flags, 1) ? buffer.readString() : "";
		x = buffer.readDouble();
		y = buffer.readDouble();
		shape = QuestShape.NAME_MAP.read(buffer);

		if (Bits.getFlag(flags, 2))
		{
			NetUtils.readStrings(buffer, description);
		}
		else
		{
			description.clear();
		}

		customClick = Bits.getFlag(flags, 4) ? buffer.readString() : "";
		guidePage = Bits.getFlag(flags, 8) ? buffer.readString() : "";

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

		size = buffer.readDouble();
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
	public void onCompleted(PlayerData data, List<ServerPlayerEntity> onlineMembers, List<ServerPlayerEntity> notifiedPlayers)
	{
		//data.setTimesCompleted(this, data.getTimesCompleted(this) + 1);
		super.onCompleted(data, onlineMembers, notifiedPlayers);

		if (!disableToast)
		{
			for (ServerPlayerEntity player : notifiedPlayers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		data.checkAutoCompletion(this);
		MinecraftForge.EVENT_BUS.post(new ObjectCompletedEvent.QuestEvent(data, this, onlineMembers, notifiedPlayers));

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
	public String getAltTitle()
	{
		if (!tasks.isEmpty())
		{
			return tasks.get(0).getTitle();
		}

		return I18n.format("ftbquests.unnamed");
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
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("subtitle", subtitle, v -> subtitle = v, "");
		config.addList("description", description, new ConfigString(), "");
		config.addEnum("shape", shape, v -> shape = v, QuestShape.NAME_MAP);
		config.addTristate("hide", hide, v -> hide = v);
		config.addDouble("size", size, v -> size = v, 1, 0.5D, 3D);
		config.addDouble("x", x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		Predicate<QuestObjectBase> depTypes = object -> object != chapter.file && object != chapter && object instanceof QuestObject;// && !(object instanceof Task);

		config.addList("dependencies", dependencies, new ConfigQuestObject<>(depTypes), null).setNameKey("ftbquests.dependencies");
		config.addEnum("dependency_requirement", dependencyRequirement, v -> dependencyRequirement = v, DependencyRequirement.NAME_MAP);
		config.addInt("min_required_dependencies", minRequiredDependencies, v -> minRequiredDependencies = v, 0, 0, Integer.MAX_VALUE);
		config.addTristate("hide_dependency_lines", hideDependencyLines, v -> hideDependencyLines = v);
		config.addString("guide_page", guidePage, v -> guidePage = v, "");
		config.addString("custom_click", customClick, v -> customClick = v, "");
		config.addTristate("hide_text_until_complete", hideTextUntilComplete, v -> hideTextUntilComplete = v);
		config.addEnum("disable_jei", disableJEI, v -> disableJEI = v, Tristate.NAME_MAP);
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
	public QuestShape getShape()
	{
		return shape == QuestShape.DEFAULT ? chapter.getDefaultQuestShape() : shape;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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

	@OnlyIn(Dist.CLIENT)
	public String getSubtitle()
	{
		if (cachedDescription != null)
		{
			return cachedDescription;
		}

		String textDesc = loadText().getString("description");

		if (!textDesc.isEmpty())
		{
			cachedDescription = textDesc;
			return cachedDescription;
		}

		String key = String.format("quests.%08x.description", id);
		String t = FTBQuestsClient.addI18nAndColors(I18n.format(key));

		if (t.isEmpty() || key.equals(t))
		{
			cachedDescription = FTBQuestsClient.addI18nAndColors(subtitle);
		}
		else
		{
			cachedDescription = t;
		}

		return cachedDescription;
	}

	@OnlyIn(Dist.CLIENT)
	public String[] getDescription()
	{
		if (cachedText != null)
		{
			return cachedText;
		}

		cachedText = loadText().getStringArray("text");

		if (cachedText.length > 0)
		{
			return cachedText;
		}

		if (description.isEmpty())
		{
			return StringUtils.EMPTY_ARRAY;
		}

		cachedText = new String[description.size()];

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

	private void removeInvalidDependencies()
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
			if (verifyDependenciesInternal(this, true))
			{
				return true;
			}
		}
		catch (StackOverflowError error)
		{
		}

		if (autofix)
		{
			FTBQuests.LOGGER.error("Looping dependencies found in " + this + "! Deleting all dependencies...");
			dependencies.clear();

			if (chapter.file.getSide().isServer())
			{
				ServerQuestFile.INSTANCE.save();
			}
		}
		else
		{
			FTBQuests.LOGGER.error("Looping dependencies found in " + this + "!");
		}

		return false;
	}

	@Override
	public boolean verifyDependenciesInternal(QuestObject original, boolean firstLoop)
	{
		if (this == original && !firstLoop)
		{
			return false;
		}

		removeInvalidDependencies();

		for (QuestObject dependency : dependencies)
		{
			if (!dependency.verifyDependenciesInternal(original, false))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int refreshJEI()
	{
		return FTBQuestsJEIHelper.QUESTS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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
}