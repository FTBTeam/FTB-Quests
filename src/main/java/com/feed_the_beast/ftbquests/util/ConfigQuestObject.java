package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject extends ConfigValue
{
	public static final String ID = "ftbquests_object";

	private final QuestFile file;
	private final HashSet<QuestObjectType> types;
	private QuestObject object;

	public ConfigQuestObject(@Nullable QuestFile f, @Nullable QuestObject o, Collection<QuestObjectType> t)
	{
		file = f == null ? FTBQuests.PROXY.getQuestFile(null) : f;
		object = o;
		types = new HashSet<>(t);
	}

	public boolean isValid(QuestObjectType type)
	{
		return types.contains(type);
	}

	public boolean isValid(@Nullable QuestObject object)
	{
		return isValid(object == null ? QuestObjectType.NULL : object.getObjectType());
	}

	@Override
	public String getID()
	{
		return ID;
	}

	public void setObject(@Nullable QuestObject v)
	{
		object = v;
	}

	@Nullable
	public QuestObject getObject()
	{
		return object;
	}

	@Override
	public String getString()
	{
		object = getObject();
		return object == null ? "" : object.toString();
	}

	@Override
	public boolean getBoolean()
	{
		return getObject() != null;
	}

	@Override
	public int getInt()
	{
		object = getObject();
		return object == null ? 0 : object.uid;
	}

	@Override
	public ConfigQuestObject copy()
	{
		return new ConfigQuestObject(file, object, types);
	}

	@Override
	public void onClicked(IOpenableGui gui, ConfigValueInstance inst, MouseButton button, Runnable callback)
	{
		if (inst.getCanEdit())
		{
			new GuiSelectQuestObject(this, gui, callback).openGui();
		}
	}

	@Override
	public void writeData(DataOut data)
	{
		int i = 0;

		for (QuestObjectType type : types)
		{
			i |= type.getFlag();
		}

		data.writeByte(i);
		data.writeInt(getInt());
	}

	@Override
	public void readData(DataIn data)
	{
		types.clear();

		int i = data.readUnsignedByte();

		for (QuestObjectType type : QuestObjectType.VALUES)
		{
			if (Bits.getFlag(i, type.getFlag()))
			{
				types.add(type);
			}
		}

		object = file.get(data.readInt());

		if (isValid(object))
		{
			setObject(object);
		}
	}

	@Override
	public boolean setValueFromString(@Nullable ICommandSender sender, String string, boolean simulate)
	{
		object = file.get(file.getID(string));

		if (isValid(object))
		{
			if (!simulate)
			{
				setObject(object);
			}

			return true;
		}

		return false;
	}

	@Override
	public void addInfo(ConfigValueInstance inst, List<String> list)
	{
		if (inst.getCanEdit() && !inst.getDefaultValue().isNull())
		{
			list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + inst.getDefaultValue().getStringForGUI().getFormattedText());
		}

		if (types.size() == 1)
		{
			list.add(TextFormatting.AQUA + "Type: " + TextFormatting.RESET + I18n.format(types.iterator().next().getTranslationKey()));
		}
		else
		{
			list.add(TextFormatting.AQUA + "Types:");

			for (QuestObjectType type : types)
			{
				list.add("> " + I18n.format(type.getTranslationKey()));
			}
		}
	}

	@Override
	public void setValueFromOtherValue(ConfigValue value)
	{
		if (value instanceof ConfigQuestObject)
		{
			types.clear();
			types.addAll(((ConfigQuestObject) value).types);
		}

		object = file.get(value.getInt());

		if (isValid(object))
		{
			setObject(object);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, String key)
	{
		object = getObject();

		if (object != null)
		{
			nbt.setInteger(key, object.uid);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, String key)
	{
		object = file.get(nbt.getInteger(key));

		if (isValid(object))
		{
			setObject(object);
		}
	}
}