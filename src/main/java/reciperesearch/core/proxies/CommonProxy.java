package reciperesearch.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import reciperesearch.core.RecipeResearch;
import reciperesearch.handlers.EventHandler;
import reciperesearch.handlers.GuiHandler;
import reciperesearch.network.ResearchPacket;
import reciperesearch.handlers.UpdateNotification;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

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
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		NetworkRegistry.INSTANCE.registerGuiHandler(RecipeResearch.instance, new GuiHandler());
		RecipeResearch.instance.network.registerMessage(ResearchPacket.HandleServer.class, ResearchPacket.class, 0, Side.SERVER);
	}
}
