package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class RewardKey {
	private final String title;
	private final Icon icon;
	private final ItemStack stack;

	public RewardKey(String title, Icon icon) {
		this(title, icon, ItemStack.EMPTY);
	}

	public RewardKey(String title, Icon icon, ItemStack stack) {
		this.title = title;
		this.icon = icon;
		this.stack = stack;
	}

	public String getTitle() {
		return title;
	}

	public Icon getIcon() {
		return icon;
	}

	public int hashCode() {
		return stack.isEmpty() ? Objects.hash(title, icon) : Objects.hash(stack.getItem(), stack.getTag());
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof RewardKey key) {
			if (!stack.isEmpty()) {
				return stack.getItem() == key.stack.getItem() && Objects.equals(stack.getTag(), key.stack.getTag());
			} else {
				return title.equals(key.title) && icon.equals(key.icon);
			}
		}

		return false;
	}
}