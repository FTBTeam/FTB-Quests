package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.misc.BooleanConsumer;

import javax.swing.*;

/**
 * @author LatvianModder
 */
public class CallbackCheckBox extends JCheckBox
{
	private final BooleanConsumer setter;

	public CallbackCheckBox(boolean v, BooleanConsumer s)
	{
		super(v ? "true" : "false", v);
		setter = s;
	}

	@Override
	protected void fireStateChanged()
	{
		super.fireStateChanged();

		if (setter != null)
		{
			setter.accept(isSelected());
		}

		setText(isSelected() ? "true" : "false");
	}
}