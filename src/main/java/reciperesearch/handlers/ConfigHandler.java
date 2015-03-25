package reciperesearch.handlers;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;

public class ConfigHandler
{
	public static Configuration config;
	
	public static void initConfigs()
	{
		if(config == null)
		{
			RecipeResearch.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		String[] defWhitelist = new String[]
		{
				"minecraft:crafting_table",
				"minecraft:planks",
				"minecraft:stick",
				"minecraft:wooden_sword",
				"minecraft:wooden_pickaxe",
				"minecraft:wooden_shovel",
				"minecraft:wooden_hoe",
				"minecraft:wooden_axe"
		};
		
		config.load();
		
		RR_Settings.shareKnowledge = config.getBoolean("Share Knowledge", Configuration.CATEGORY_GENERAL, false, "Whether or not players should be allowed to share their research notes and textbooks");
		RR_Settings.practiceWorth = config.getInt("Practice Worth", Configuration.CATEGORY_GENERAL, 1, 0, 100, "The increase in a player's knowledge of a recipe by successfully crafting it");
		RR_Settings.practiceCap = config.getInt("Practice Cap", Configuration.CATEGORY_GENERAL, 90, 0, 100, "The limit of research a player can earn through practice without using a textbook");
		RR_Settings.recipeWhitelist = config.getStringList("Item Whitelist", Configuration.CATEGORY_GENERAL, defWhitelist, "Which items can be crafted without knowledge limitations");
		RR_Settings.hideRecipes = config.getBoolean("Hide Recipes", Configuration.CATEGORY_GENERAL, true, "Hides recipes from the recipe listing and NEI, may cause issues"); // WIP
		RR_Settings.persistResearch = config.getBoolean("Persistent Research", Configuration.CATEGORY_GENERAL, false, "Should a player lose all their knowledge when they die? (This option is evil)");
		
		config.save();
		
		System.out.println("Loaded configs...");
	}
}
