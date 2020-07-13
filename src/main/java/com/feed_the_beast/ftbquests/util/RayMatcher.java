package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
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
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class RayMatcher
{
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+)(\\=|\\=\\=|\\>\\=|\\<\\=|\\>|\\<|\\!\\=)(\\w+)");

	public enum Type implements IWithID
	{
		BLOCK_ID("block_id", true, false),
		BLOCK_ENTITY_ID("block_entity_id", true, true),
		BLOCK_ENTITY_CLASS("block_entity_class", true, true),
		ENTITY_ID("entity_id", false, true),
		ENTITY_CLASS("entity_class", false, true);

		public static final NameMap<Type> NAME_MAP = NameMap.createWithBaseTranslationKey(BLOCK_ID, "ftbquests.raymatcher", values());

		private final String name;
		public final boolean block;
		public final boolean nbt;

		Type(String n, boolean b, boolean nb)
		{
			name = n;
			block = b;
			nbt = nb;
		}

		@Override
		public String getId()
		{
			return name;
		}
	}

	@FunctionalInterface
	public interface NBTSupplier
	{
		@Nullable
		NBTTagCompound get();
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

		private final IBlockState state;
		private final TileEntity tileEntity;
		private final Entity entity;

		public final String blockId;
		public final String blockEntityClass;
		public final String blockEntityId;
		public final String entityClass;
		public final String entityId;

		private boolean readProperties, readNBT;
		private Map<String, String> blockProperties;
		private NBTTagCompound nbt;

		private Data()
		{
			state = Blocks.AIR.getDefaultState();
			tileEntity = null;
			entity = null;

			blockId = "";
			blockProperties = Collections.emptyMap();
			blockEntityClass = "null";
			blockEntityId = "null";
			entityClass = "null";
			entityId = "null";
		}

		private Data(IBlockState s, @Nullable TileEntity te, @Nullable Entity e)
		{
			state = s;
			tileEntity = te;
			entity = e;

			blockId = String.valueOf(state.getBlock().getRegistryName());

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

		public Map<String, String> getBlockProperties()
		{
			if (readProperties)
			{
				return blockProperties;
			}

			blockProperties = new HashMap<>();

			for (Map.Entry<? extends IProperty, ? extends Comparable> entry : state.getProperties().entrySet())
			{
				blockProperties.put(entry.getKey().getName(), entry.getKey().getName(entry.getValue()));
			}

			readProperties = true;
			return blockProperties;
		}

		@Nullable
		public NBTTagCompound getNBT()
		{
			if (readNBT)
			{
				return nbt;
			}

			nbt = null;

			if (tileEntity != null)
			{
				nbt = tileEntity.serializeNBT();
			}
			else if (entity != null)
			{
				nbt = entity.serializeNBT();
			}

			return nbt;
		}

		@Override
		public String toString()
		{
			NBTTagCompound nbt = new SNBTTagCompound();

			nbt.setString("state", state.toString());
			nbt.setBoolean("tileEntity", tileEntity != null);
			nbt.setBoolean("entity", entity != null);
			nbt.setString("blockId", blockId);
			nbt.setString("blockEntityClass", blockEntityClass);
			nbt.setString("blockEntityId", blockEntityId);
			nbt.setString("entityClass", entityClass);
			nbt.setString("entityId", entityId);
			nbt.setString("blockProperties", getBlockProperties().toString());

			if (getNBT() != null)
			{
				nbt.setTag("nbt", getNBT());
			}
			else
			{
				nbt.setString("nbt", "null");
			}

			return NBTUtils.getColoredNBTString(nbt);
		}
	}

	public Type type = Type.BLOCK_ID;
	public String match = "";
	public final Map<String, String> properties = new HashMap<>();
	public NBTTagCompound nbtData = null;

	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("match_type", type.getId());
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

		if (nbtData != null && !nbtData.isEmpty())
		{
			nbt.setTag("nbt", nbtData);
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

		nbtData = nbt.getCompoundTag("nbt");

		if (nbtData.isEmpty())
		{
			nbtData = null;
		}
	}

	public void writeNetData(DataOut data)
	{
		Type.NAME_MAP.write(data, type);
		data.writeString(match);
		data.writeMap(properties, DataOut.STRING, DataOut.STRING);
		data.writeNBT(nbtData);
	}

	public void readNetData(DataIn data)
	{
		type = Type.NAME_MAP.read(data);
		match = data.readString();
		data.readMap(properties, DataIn.STRING, DataIn.STRING);
		nbtData = data.readNBT();
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
		if (type.nbt)
		{
			if (nbtData == null || nbtData.isEmpty())
			{
				return true;
			}

			NBTTagCompound n = data.getNBT();

			if (n == null || n.isEmpty())
			{
				return false;
			}

			for (String key : nbtData.getKeySet())
			{
				if (!Objects.equals(nbtData.getTag(key), n.getTag(key)))
				{
					return false;
				}
			}

			return true;
		}

		if (properties.isEmpty() || !type.block)
		{
			return true;
		}
		else if (properties.size() > data.getBlockProperties().size())
		{
			return false;
		}

		for (Map.Entry<String, String> entry : properties.entrySet())
		{
			String s = data.getBlockProperties().get(entry.getKey());

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
		if (type.nbt)
		{
			return nbtData == null ? "" : nbtData.toString();
		}

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
		nbtData = null;

		if (type.nbt)
		{
			if (!s.isEmpty())
			{
				try
				{
					nbtData = JsonToNBT.getTagFromJson(s);
				}
				catch (Exception ex)
				{
				}
			}

			return;
		}

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