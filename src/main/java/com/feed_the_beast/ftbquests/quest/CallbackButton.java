package com.feed_the_beast.ftbquests.quest;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author LatvianModder
 */
public class CallbackButton extends JButton
{
	private final Runnable callback;

	public CallbackButton(String title, Runnable r)
	{
		super(title);
		callback = r;
	}

	@Override
	protected void fireActionPerformed(ActionEvent event)
	{
		super.fireActionPerformed(event);
		callback.run();
	}
}
