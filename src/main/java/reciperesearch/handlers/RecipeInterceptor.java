package reciperesearch.handlers;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import reciperesearch.core.RecipeResearch;
import reciperesearch.utils.RecipeHelper;
import reciperesearch.utils.RecipeHelper.RecipeInfo;
import reciperesearch.utils.ResearchHelper;

public class RecipeInterceptor implements IRecipe
{
	public static final RecipeInterceptor instance = new RecipeInterceptor();
	
	Random rand = new Random();
	boolean isCustom = false;
	RecipeInfo output = new RecipeInfo(null, null);
	
	private RecipeInterceptor() // Yea it's private, inaccessible and empty. What of it
	{
	}
	
	@Override
	public boolean matches(InventoryCrafting invo, World world)
	{
		output = RecipeHelper.findMatchingRecipe(invo, world);
		
		if(output.recipe == null) // Looks like the recipe we intercepted is a repair recipe and probably shouldn't have been intercepted in the first place
		{
			return false;
		} else if(RecipeHelper.getIngredients(output.stack).size() <= 0) // We can't find any research ingredients for this recipe, including the resulting item, so we're just going to let the recipe handle itself
		{
			isCustom = true;
			return true;
		}
		
		return true; // This will always result in true because we want this to be the first and only recipe Minecraft checks
	}
	
	/**
	 * Ensure the recipe intercept is still in place. Probably not a good idea to call this while something is iterating over the CraftingManager
	 */
	@SuppressWarnings("unchecked")
	public void checkAndResetIntercept()
	{
		if(CraftingManager.getInstance().getRecipeList().get(0) != this)
		{
			CraftingManager.getInstance().getRecipeList().remove(this);
			CraftingManager.getInstance().getRecipeList().add(0, this);
		}
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting invo)
	{
		if(output.recipe == null)
		{
			return null;
		}
		
		ItemStack outStack = output.recipe.getCraftingResult(invo);
		
		if(outStack != null)
		{
			outStack = outStack.copy(); // Just in case
			
			Container tmpCon = RecipeHelper.getContainer(invo);
			ArrayList<?> crafters = tmpCon == null? null : RecipeHelper.getCrafters(tmpCon);
			
			if(crafters != null)
			{
				for(Object obj : crafters)
				{
					if(obj instanceof EntityPlayer)
					{
						if(RecipeResearch.proxy.isClient() && ResearchHelper.getItemResearch((EntityPlayer)obj, outStack) >= 100)
						{
							RecipeHelper.scheduledUnlocks.add(output.recipe);
						}
						
						ResearchHelper.changeResult((EntityPlayer)obj, outStack);
						break;
					}
				}
			}
		}
		
		return outStack;
	}

	@Override
	public int getRecipeSize()
	{
		if(output.recipe == null)
		{
			return 4;
		}
		
		return output.recipe.getRecipeSize();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		if(output.recipe == null)
		{
			return null;
		}
		
		return output.recipe.getRecipeOutput();
	}
}
