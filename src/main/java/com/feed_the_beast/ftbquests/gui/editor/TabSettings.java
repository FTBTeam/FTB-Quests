package com.feed_the_beast.ftbquests.gui.editor;

import javax.swing.*;
import java.awt.*;

/**
 * @author LatvianModder
 */
public class TabSettings extends TabBase
{
	public TabSettings(EditorFrame e)
	{
		super(e);
		JScrollPane pane = new JScrollPane();
		pane.setLayout(new GridLayout(6, 1));

		for (int y = 0; y <= 6; y++)
		{
			for (int x = 0; x <= 1; x++)
			{
				JButton button = new JButton("Test!");
				button.setMinimumSize(new Dimension(60, 20));
				pane.add(button);
			}
		}

		add(pane);
	}
}