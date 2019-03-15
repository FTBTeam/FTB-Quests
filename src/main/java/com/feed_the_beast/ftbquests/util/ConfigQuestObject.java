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
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Arrays;
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
	private QuestObjectBase object;

	public ConfigQuestObject(@Nullable QuestFile f, @Nullable QuestObjectBase o, Collection<QuestObjectType> t)
	{
		file = f == null ? FTBQuests.PROXY.getQuestFile(null) : f;
		object = o;
		types = new HashSet<>(t);
	}

	public ConfigQuestObject(@Nullable QuestFile f, @Nullable QuestObjectBase o, QuestObjectType... t)
	{
		this(f, o, Arrays.asList(t));
	}

	public boolean isValid(QuestObjectType type)
	{
		return types.contains(type);
	}

	public boolean isValid(@Nullable QuestObjectBase object)
	{
		return isValid(object == null ? QuestObjectType.NULL : object.getObjectType());
	}

	public HashSet<QuestObjectType> getTypes()
	{
		return new HashSet<>(types);
	}

	@Override
	public String getID()
	{
		return ID;
	}

	public void setObject(@Nullable QuestObjectBase v)
	{
		object = v;
	}

	@Nullable
	public QuestObjectBase getObject()
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
		return object == null ? 0 : object.id;
	}

	@Override
	public ConfigQuestObject copy()
	{
		return new ConfigQuestObject(file, object, types);
	}

	@Override
	public ITextComponent getStringForGUI()
	{
		object = getObject();

		if (object == null)
		{
			return new TextComponentString("");
		}

		return object.getDisplayName().createCopy();
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

		data.writeVarInt(i);
		data.writeInt(getInt());
	}

	@Override
	public void readData(DataIn data)
	{
		types.clear();

		int i = data.readVarInt();

		for (QuestObjectType type : QuestObjectType.NAME_MAP)
		{
			if (Bits.getFlag(i, type.getFlag()))
			{
				types.add(type);
			}
		}

		object = file.getBase(data.readInt());

		if (isValid(object))
		{
			setObject(object);
		}
	}

	@Override
	public boolean setValueFromString(@Nullable ICommandSender sender, String string, boolean simulate)
	{
		object = file.getBase(file.getID(string));

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

		if (object != null)
		{
			list.add(TextFormatting.AQUA + "ID: " + TextFormatting.RESET + object);
		}

		if (types.size() == 1)
		{
			list.add(TextFormatting.AQUA + "Type: " + TextFormatting.RESET + I18n.format(types.iterator().next().getTranslationKey()));
		}
		else
		{
			list.add(TextFormatting.AQUA + "Types:");

			for (QuestObjectType type : QuestObjectType.NAME_MAP)
			{
				if (isValid(type))
				{
					list.add("> " + I18n.format(type.getTranslationKey()));
				}
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

		object = file.getBase(value.getInt());

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
			nbt.setInteger(key, object.id);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, String key)
	{
		object = file.getBase(nbt.getInteger(key));

		if (isValid(object))
		{
			setObject(object);
		}
	}
}