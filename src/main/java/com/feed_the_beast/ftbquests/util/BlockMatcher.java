package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class BlockMatcher
{
	public enum Type implements IWithID
	{
		BLOCK("block"),
		ENTITY_ID("entity_id"),
		ENTITY_CLASS("entity_class");

		public static final NameMap<Type> NAME_MAP = NameMap.create(BLOCK, values());

		private final String name;

		Type(String n)
		{
			name = n;
		}

		@Override
		public String getID()
		{
			return name;
		}
	}

	public static class Data
	{
		public final String blockId;
		public final Map<String, String> blockProperties;
		public final String entityClass;
		public final String entityId;

		public Data(IBlockState state, @Nullable TileEntity tileEntity)
		{
			blockProperties = new HashMap<>();
			blockId = String.valueOf(state.getBlock().getRegistryName());

			for (Map.Entry<? extends IProperty, ? extends Comparable> entry : state.getProperties().entrySet())
			{
				blockProperties.put(entry.getKey().getName(), entry.getKey().getName(entry.getValue()));
			}

			if (tileEntity == null)
			{
				entityClass = entityId = "";
			}
			else
			{
				entityClass = tileEntity.getClass().getName();
				entityId = String.valueOf(TileEntity.getKey(tileEntity.getClass()));
			}
		}

		public Data(World world, @Nullable RayTraceResult ray)
		{
			this(ray == null ? BlockUtils.AIR_STATE : world.getBlockState(ray.getBlockPos()), world.getTileEntity(ray.getBlockPos()));
		}

		public Data(EntityPlayer player)
		{
			this(player.world, MathUtils.rayTrace(player, true));
		}
	}

	public Type type = Type.BLOCK;
	public String match = "";
	public final Map<String, String> properties = new HashMap<>();

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", type.getID());
		nbt.setString("match", match);
	}

	public void readData(NBTTagCompound nbt)
	{
		type = Type.NAME_MAP.get(nbt.getString("type"));
		match = nbt.getString("match");
	}

	public void writeNetData(DataOut data)
	{
		Type.NAME_MAP.write(data, type);
		data.writeString(match);
	}

	public void readNetData(DataIn data)
	{
		type = Type.NAME_MAP.read(data);
		match = data.readString();
	}

	public boolean matches(Data data)
	{
		if (match.isEmpty())
		{
			return false;
		}

		switch (type)
		{
			case BLOCK:
				if (match.equals(data.blockId))
				{
					return true;
				}
			case ENTITY_ID:
				if (match.equals(data.entityId))
				{
					return true;
				}
			case ENTITY_CLASS:
				if (match.equals(data.entityClass))
				{
					return true;
				}
			default:
				return false;
		}
	}
}