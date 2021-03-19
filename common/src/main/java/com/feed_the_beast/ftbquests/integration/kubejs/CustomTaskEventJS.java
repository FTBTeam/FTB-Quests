package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
public class CustomTaskEventJS extends EventJS {
	public final CustomTaskEvent event;

	CustomTaskEventJS(CustomTaskEvent e) {
		event = e;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	public CustomTask getTask() {
		return event.getTask();
	}

	public void setCheck(CustomTaskCheckerJS c) {
		getTask().check = new CheckWrapper(c);
	}

	public void setCheckTimer(int t) {
		getTask().checkTimer = t;
	}

	public void setEnableButton(boolean b) {
		getTask().enableButton = b;
	}

	public void setMaxProgress(long max) {
		getTask().maxProgress = max;
	}
}