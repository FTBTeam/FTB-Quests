package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject extends ConfigValue
{
	public static final String ID = "ftbquests_object";

	public final QuestFile file;
	private final IntSet validObjects;
	private int object;

	public ConfigQuestObject(QuestFile f, int o, IntSet t)
	{
		file = f;
		object = o;
		validObjects = new IntOpenHashSet(t);
	}

	public ConfigQuestObject(QuestFile f, int o, Predicate<QuestObjectBase> t)
	{
		this(f, o, IntSets.EMPTY_SET);

		if (t.test(null))
		{
			validObjects.add(0);
		}

		for (QuestObjectBase objectBase : f.getAllObjects())
		{
			if (t.test(objectBase))
			{
				validObjects.add(objectBase.id);
			}
		}
	}

	public boolean isValid(int id)
	{
		return validObjects.contains(id);
	}

	public boolean isValid(@Nullable QuestObjectBase object)
	{
		return isValid(object == null ? 0 : object.id);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	public void setObject(int v)
	{
		object = v;
	}

	public int getObject()
	{
		return object;
	}

	@Override
	public String getString()
	{
		QuestObjectBase o = file.getBase(object);
		return o == null ? "" : o.toString();
	}

	@Override
	public boolean getBoolean()
	{
		return file.getBase(object) != null;
	}

	@Override
	public int getInt()
	{
		return object;
	}

	@Override
	public ConfigQuestObject copy()
	{
		return new ConfigQuestObject(file, object, validObjects);
	}

	@Override
	public ITextComponent getStringForGUI()
	{
		QuestObjectBase o = file.getBase(object);

		if (o == null)
		{
			return new TextComponentString("");
		}

		return new TextComponentString(o.getUnformattedTitle());
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
		data.writeIntList(validObjects);
		data.writeInt(getInt());
	}

	@Override
	public void readData(DataIn data)
	{
		validObjects.clear();
		validObjects.addAll(data.readIntList());

		int o = data.readInt();

		if (isValid(o))
		{
			setObject(o);
		}
	}

	@Override
	public boolean setValueFromString(@Nullable ICommandSender sender, String string, boolean simulate)
	{
		int o = file.getID(string);

		if (isValid(o))
		{
			if (!simulate)
			{
				setObject(o);
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

		QuestObjectBase o = file.getBase(object);

		if (o != null)
		{
			list.add(TextFormatting.AQUA + "ID: " + TextFormatting.RESET + o);
		}
	}

	@Override
	public void setValueFromOtherValue(ConfigValue value)
	{
		if (value instanceof ConfigQuestObject)
		{
			validObjects.clear();
			validObjects.addAll(((ConfigQuestObject) value).validObjects);
		}

		int o = value.getInt();

		if (isValid(o))
		{
			setObject(o);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, String key)
	{
		nbt.setInteger(key, getObject());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, String key)
	{
		int o = nbt.getInteger(key);

		if (isValid(o))
		{
			setObject(o);
		}
	}
}