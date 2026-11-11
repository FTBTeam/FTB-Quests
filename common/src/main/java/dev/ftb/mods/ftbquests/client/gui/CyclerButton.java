package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleButton;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CyclerButton<V> extends SimpleButton {
    private final NameMap<V> nameMap;
    private final Consumer<V> onCycled;
    private V value;

    public CyclerButton(Panel panel, NameMap<V> nameMap, V value, Consumer<V> onCycled) {
        super(panel, Component.empty(), nameMap.getIcon(value), (_, _) -> {});
        this.nameMap = nameMap;
        this.value = value;
        this.onCycled = onCycled;
        setConsumer((_, mb) -> cycleValue(mb));
    }

    private void cycleValue(MouseButton mb) {
        value = mb.isLeft() ? nameMap.getNext(value) : nameMap.getPrevious(value);
        setIcon(nameMap.getIcon(value));
        onCycled.accept(value);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        list.add(nameMap.getDisplayName(value));
    }
}
