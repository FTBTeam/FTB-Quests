package com.feed_the_beast.ftbquests.block.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * @author LatvianModder
 */
public class BannerBlockEntity extends BlockEntity {
	public ResourceLocation texture = new ResourceLocation("minecraft:textures/misc/unknown_pack.png");
	public float rotationX = 0F;
	public float rotationY = 0F;
	public float rotationZ = 0F;
	public float offsetX = 0F;
	public float offsetY = 0F;
	public float offsetZ = 0F;
	public float sizeX = 0F;
	public float sizeZ = 0F;
	public String bannerId = "";

	public BannerBlockEntity() {
		super(FTBQuestsBlockEntities.BANNER.get());
	}
}
