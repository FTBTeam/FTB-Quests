package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.world.item.ItemStack;

import dev.ftb.mods.ftblibrary.icon.Icon;

import java.util.Objects;

public class RewardKey {
	private final String title;
	private final Icon<?> icon;
	private final ItemStack stack;
	private final boolean disableBlur;

	public RewardKey(String title, Icon<?> icon, boolean disableBlur) {
		this(title, icon, ItemStack.EMPTY, disableBlur);
	}

	public RewardKey(String title, Icon<?> icon, ItemStack stack, boolean disableBlur) {
		this.title = title;
		this.icon = icon;
		this.stack = stack;
		this.disableBlur = disableBlur;
	}

	public String getTitle() {
		return title;
	}

	public Icon<?> getIcon() {
		return icon;
	}

	public boolean disableBlur() {
		return disableBlur;
	}

	public int hashCode() {
		return stack.isEmpty() ? Objects.hash(title, icon) : ItemStack.hashItemAndComponents(stack);
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof RewardKey key) {
			if (!stack.isEmpty()) {
				return ItemStack.isSameItemSameComponents(stack, key.stack);
			} else {
				return title.equals(key.title) && icon.equals(key.icon);
			}
		}

		return false;
	}
}
