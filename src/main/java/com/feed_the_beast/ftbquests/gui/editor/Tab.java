package com.feed_the_beast.ftbquests.gui.editor;

import javax.swing.*;

/**
 * @author LatvianModder
 */
public class Tab extends JPanel
{
	public final EditorFrame editor;

	public Tab(EditorFrame e)
	{
		editor = e;
	}

	public boolean scrollPage()
	{
		return true;
	}
}