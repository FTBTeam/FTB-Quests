package com.feed_the_beast.ftbquests.gui.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * @author LatvianModder
 */
public class RunnableCallback implements EventHandler<ActionEvent>
{
	private final Runnable runnable;

	public RunnableCallback(Runnable r)
	{
		runnable = r;
	}

	@Override
	public void handle(ActionEvent event)
	{
		runnable.run();
	}
}