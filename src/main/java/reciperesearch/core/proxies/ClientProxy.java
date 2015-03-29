package reciperesearch.core.proxies;

import reciperesearch.core.RecipeResearch;
import reciperesearch.network.ResearchPacket;
import cpw.mods.fml.relauncher.Side;


public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		RecipeResearch.instance.network.registerMessage(ResearchPacket.HandleClient.class, ResearchPacket.class, 1, Side.CLIENT);
	}
}
