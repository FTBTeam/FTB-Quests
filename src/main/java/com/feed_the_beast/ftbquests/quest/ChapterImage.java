package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class ChapterImage implements Movable
{
	public Chapter chapter;
	public double x, y;
	public double width, height;
	public double rotation;
	public Icon image;
	public List<String> hover;
	public String click;
	public boolean dev;

	public ChapterImage(Chapter c)
	{
		chapter = c;
		x = y = 0D;
		width = 1D;
		height = 1D;
		rotation = 0D;
		image = Icon.getIcon("minecraft:textures/gui/presets/isles.png");
		hover = new ArrayList<>();
		click = "";
		dev = false;
	}

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setDouble("x", x);
		nbt.setDouble("y", y);
		nbt.setDouble("width", width);
		nbt.setDouble("height", height);
		nbt.setDouble("rotation", rotation);
		nbt.setString("image", image.toString());

		NBTTagList hoverTag = new NBTTagList();

		for (String s : hover)
		{
			hoverTag.appendTag(new NBTTagString(s));
		}

		nbt.setTag("hover", hoverTag);
		nbt.setString("click", click);
		nbt.setBoolean("dev", dev);
	}

	public void readData(NBTTagCompound nbt)
	{
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		width = nbt.getDouble("width");
		height = nbt.getDouble("height");
		rotation = nbt.getDouble("rotation");
		image = Icon.getIcon(nbt.getString("image"));

		hover.clear();
		NBTTagList hoverTag = nbt.getTagList("hover", Constants.NBT.TAG_STRING);

		for (int i = 0; i < hoverTag.tagCount(); i++)
		{
			hover.add(hoverTag.getStringTagAt(i));
		}

		click = nbt.getString("click");
		dev = nbt.getBoolean("dev");
	}

	public void writeNetData(DataOut data)
	{
		data.writeDouble(x);
		data.writeDouble(y);
		data.writeDouble(width);
		data.writeDouble(height);
		data.writeDouble(rotation);
		data.writeIcon(image);
		data.writeCollection(hover, DataOut.STRING);
		data.writeString(click);
		data.writeBoolean(dev);
	}

	public void readNetData(DataIn data)
	{
		x = data.readDouble();
		y = data.readDouble();
		width = data.readDouble();
		height = data.readDouble();
		rotation = data.readDouble();
		image = data.readIcon();
		data.readCollection(hover, DataIn.STRING);
		click = data.readString();
		dev = data.readBoolean();
	}

	@SideOnly(Side.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		config.addDouble("x", () -> x, v -> x = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("y", () -> y, v -> y = v, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		config.addDouble("width", () -> width, v -> width = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("height", () -> height, v -> height = v, 1, 0, Double.POSITIVE_INFINITY);
		config.addDouble("rotation", () -> rotation, v -> rotation = v, 0, -180, 180);
		config.addString("image", () -> image.toString(), v -> image = Icon.getIcon(v), "minecraft:textures/gui/presets/isles.png");
		config.addList("hover", hover, new ConfigString(""), ConfigString::new, ConfigString::getString);
		config.addString("click", () -> click, v -> click = v, "");
		config.addBool("dev", () -> dev, v -> dev = v, false);
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
		return width;
	}

	@Override
	public double getHeight()
	{
		return height;
	}

	@Override
	public String getShape()
	{
		return "square";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void move(Chapter to, double _x, double _y)
	{
		x = _x;
		y = _y;

		if (to != chapter)
		{
			chapter.images.remove(this);
			new MessageEditObject(chapter).sendToServer();

			chapter = to;
			chapter.images.add(this);
		}

		new MessageEditObject(chapter).sendToServer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawMoved()
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, 0.5D, 0);
		GlStateManager.rotate((float) rotation, 0, 0, 1);
		GlStateManager.scale(0.5D, 0.5D, 1);
		image.withColor(Color4I.WHITE.withAlpha(50)).draw(-1, -1, 2, 2);
		GlStateManager.popMatrix();

		QuestShape.get(getShape()).outline.withColor(Color4I.WHITE.withAlpha(30)).draw(0, 0, 1, 1);
	}
}