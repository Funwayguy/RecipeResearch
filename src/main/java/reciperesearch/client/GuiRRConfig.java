package reciperesearch.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import reciperesearch.core.RecipeResearch;
import reciperesearch.handlers.ConfigHandler;
import cpw.mods.fml.client.config.GuiConfig;

public class GuiRRConfig extends GuiConfig
{
	@SuppressWarnings({"rawtypes", "unchecked"})
	public GuiRRConfig(GuiScreen parent)
	{
		super(parent, new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), RecipeResearch.MODID, false, false, RecipeResearch.NAME);
	}
}
