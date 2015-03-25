package reciperesearch.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import reciperesearch.core.RecipeResearch;
import reciperesearch.handlers.EventHandler;
import reciperesearch.handlers.GuiHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
		NetworkRegistry.INSTANCE.registerGuiHandler(RecipeResearch.instance, new GuiHandler());
	}
}
