package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class RayMatcher
{
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+)(\\=|\\=\\=|\\>\\=|\\<\\=|\\>|\\<|\\!\\=)(\\w+)");

	public enum Type implements IWithID
	{
		BLOCK_ID("block_id", true),
		BLOCK_ENTITY_ID("block_entity_id", true),
		BLOCK_ENTITY_CLASS("block_entity_class", true),
		ENTITY_ID("entity_id", false),
		ENTITY_CLASS("entity_class", false);

		public static final NameMap<Type> NAME_MAP = NameMap.createWithBaseTranslationKey(BLOCK_ID, "ftbquests.raymatcher", values());

		private final String name;
		public final boolean block;

		Type(String n, boolean b)
		{
			name = n;
			block = b;
		}

		@Override
		public String getID()
		{
			return name;
		}
	}

	public static class Data
	{
		public static final Data EMPTY = new Data();

		public static Data get(IBlockState state, @Nullable TileEntity tileEntity, @Nullable Entity entity)
		{
			if (state == BlockUtils.AIR_STATE && tileEntity == null && entity == null)
			{
				return EMPTY;
			}

			return new Data(state, tileEntity, entity);
		}

		public static Data get(World world, @Nullable RayTraceResult ray)
		{
			if (ray == null || ray.typeOfHit == RayTraceResult.Type.MISS)
			{
				return EMPTY;
			}
			else if (ray.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				return get(world.getBlockState(ray.getBlockPos()), world.getTileEntity(ray.getBlockPos()), ray.entityHit);
			}
			else if (ray.entityHit != null)
			{
				return get(BlockUtils.AIR_STATE, null, ray.entityHit);
			}

			return EMPTY;
		}

		public static Data get(EntityPlayer player)
		{
			return get(player.world, MathUtils.rayTrace(player, true));
		}

		public final String blockId;
		public final Map<String, String> blockProperties;
		public final String blockEntityClass;
		public final String blockEntityId;
		public final String entityClass;
		public final String entityId;

		private Data()
		{
			blockId = "";
			blockProperties = Collections.emptyMap();
			blockEntityClass = "null";
			blockEntityId = "null";
			entityClass = "null";
			entityId = "null";
		}

		private Data(IBlockState state, @Nullable TileEntity tileEntity, @Nullable Entity entity)
		{
			blockProperties = new HashMap<>();
			blockId = String.valueOf(state.getBlock().getRegistryName());

			for (Map.Entry<? extends IProperty, ? extends Comparable> entry : state.getProperties().entrySet())
			{
				blockProperties.put(entry.getKey().getName(), entry.getKey().getName(entry.getValue()));
			}

			if (tileEntity == null)
			{
				blockEntityClass = blockEntityId = "null";
			}
			else
			{
				blockEntityClass = tileEntity.getClass().getName();
				blockEntityId = String.valueOf(TileEntity.getKey(tileEntity.getClass()));
			}

			if (entity == null)
			{
				entityClass = entityId = "";
			}
			else
			{
				entityClass = entity.getClass().getName();
				EntityEntry entityEntry = EntityRegistry.getEntry(entity.getClass());
				entityId = entityEntry == null ? "null" : String.valueOf(entityEntry.getRegistryName());
			}
		}
	}

	public Type type = Type.BLOCK_ID;
	public String match = "";
	public final Map<String, String> properties = new HashMap<>();

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("match_type", type.getID());
		nbt.setString("match", match);

		if (!properties.isEmpty())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();

			for (Map.Entry<String, String> entry : properties.entrySet())
			{
				nbt1.setString(entry.getKey(), entry.getValue());
			}

			nbt.setTag("properties", nbt1);
		}
	}

	public void readData(NBTTagCompound nbt)
	{
		type = Type.NAME_MAP.get(nbt.getString("match_type"));
		match = nbt.getString("match");
		properties.clear();

		NBTTagCompound nbt1 = nbt.getCompoundTag("properties");

		for (String s : nbt1.getKeySet())
		{
			properties.put(s, nbt1.getString(s));
		}
	}

	public void writeNetData(DataOut data)
	{
		Type.NAME_MAP.write(data, type);
		data.writeString(match);
		data.writeMap(properties, DataOut.STRING, DataOut.STRING);
	}

	public void readNetData(DataIn data)
	{
		type = Type.NAME_MAP.read(data);
		match = data.readString();
		data.readMap(properties, DataIn.STRING, DataIn.STRING);
	}

	public boolean stringMatches(Data data)
	{
		if (match.isEmpty())
		{
			return false;
		}

		switch (type)
		{
			case BLOCK_ID:
				return match.equals(data.blockId);
			case BLOCK_ENTITY_ID:
				return match.equals(data.blockEntityId);
			case BLOCK_ENTITY_CLASS:
				return match.equals(data.blockEntityClass);
			case ENTITY_ID:
				return match.equals(data.entityId);
			case ENTITY_CLASS:
				return match.equals(data.entityClass);
			default:
				return false;
		}
	}

	public boolean propertiesMatch(Data data)
	{
		if (properties.isEmpty() || !type.block)
		{
			return true;
		}
		else if (properties.size() > data.blockProperties.size())
		{
			return false;
		}

		for (Map.Entry<String, String> entry : properties.entrySet())
		{
			String s = data.blockProperties.get(entry.getKey());

			if (s == null || !s.equals(entry.getValue()))
			{
				return false;
			}
		}

		return true;
	}

	public boolean matches(Data data)
	{
		return data != Data.EMPTY && stringMatches(data) && propertiesMatch(data);
	}

	public String getPropertyString()
	{
		if (properties.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for (Map.Entry<String, String> entry : properties.entrySet())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				builder.append(',');
			}

			builder.append(entry.getKey());
			builder.append('=');
			builder.append(entry.getValue());
		}

		return builder.toString();
	}

	public void setPropertyString(String s)
	{
		properties.clear();

		for (String s1 : s.split(","))
		{
			String[] s2 = s1.split("=", 2);

			if (s2.length == 2)
			{
				properties.put(s2[0], s2[1]);
			}
		}
	}
}