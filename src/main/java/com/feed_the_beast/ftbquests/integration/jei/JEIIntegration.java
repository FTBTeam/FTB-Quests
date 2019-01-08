package com.feed_the_beast.ftbquests.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IFocus;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
@JEIPlugin
public class JEIIntegration implements IModPlugin
{
	public static IJeiRuntime RUNTIME;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r)
	{
		RUNTIME = r;
	}

	public static void openFocus(boolean recipe, @Nullable Object object)
	{
		if (object != null && RUNTIME != null)
		{
			RUNTIME.getRecipesGui().show(RUNTIME.getRecipeRegistry().createFocus(recipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT, object));
		}
	}
}