package reciperesearch.handlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Level;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.utils.ResearchHelper;

public class RecipeInterceptor implements IRecipe
{
	boolean isCustom = false;
	ArrayList crafters;
	Container container;
	RecipeInfo output = new RecipeInfo(null, null);
	static ArrayList<IRecipe> allRecipes = new ArrayList<IRecipe>();
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(InventoryCrafting invo, World world)
	{
		container = getContainer(invo);
		crafters = container == null? null : getCrafters(container);
		
		if(CraftingManager.getInstance().getRecipeList().get(0) != this) // Just making sure something didn't mess with the intercept position
		{
			CraftingManager.getInstance().getRecipeList().remove(this);
			CraftingManager.getInstance().getRecipeList().add(0, this);
		}
		
		if(RR_Settings.hideRecipes && CraftingManager.getInstance().getRecipeList().size() > 1)
		{
			HideAll();
		} else if(!RR_Settings.hideRecipes && allRecipes.size() > 0)
		{
			UnHideAll();
		}
		
		output = findMatchingRecipe(invo, world);
		
		if(output.recipe == null) // Looks like the recipe we intercepted is a repair recipe and probably shouldn't have been intercepted in the first place
		{
			return false;
		} else if(getIngredients(output.stack).size() <= 0)
		{
			isCustom = true;
			return true;
		}
		
		String researchID = "";
		
		if(output.stack != null)
		{
			researchID = Item.itemRegistry.getNameForObject(output.stack.getItem());
			
			if(output.stack.getItem().isDamageable())
			{
				researchID = researchID + ":" + output.stack.getItemDamage();
			}
		}
		
		if(world != null && !world.isRemote && output.stack != null && crafters != null && crafters.size() > 0 && !Arrays.asList(RR_Settings.recipeWhitelist).contains(researchID))
		{
			int num = 100;
			
			Iterator<ICrafting> iterator = crafters.iterator();
			
			while(iterator.hasNext())
			{
				ICrafting crafter = iterator.next();
				if(crafter instanceof EntityPlayerMP)
				{
					EntityPlayerMP player = (EntityPlayerMP)crafter;
					int baseEff = ResearchHelper.getResearchEfficiency(player);
					num = MathHelper.clamp_int(ResearchHelper.getItemResearch(player, output.stack), baseEff, 100);
				}
			}
			
			if(num < world.rand.nextInt(100))
			{
				ItemStack failStack = new ItemStack(RecipeResearch.failedItem);
				failStack.setStackDisplayName(output.stack.getDisplayName());
				output.stack = failStack;
			}
		}
		
		return true; // This will always result in true because we want this to be the first and only recipe Minecraft checks
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting invo)
	{
		if(container != null)
		{
			container.detectAndSendChanges();
		}
		
		if(isCustom)
		{
			return output.recipe.getCraftingResult(invo);
		} else
		{
			return output.stack;
		}
	}

	@Override
	public int getRecipeSize()
	{
		if(output.recipe != null)
		{
			return output.recipe.getRecipeSize();
		} else
		{
			return 4;
		}
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		if(container != null)
		{
			container.detectAndSendChanges();
		}
		
		if(isCustom)
		{
			return output.recipe.getRecipeOutput();
		} else
		{
			return output.stack;
		}
	}
	
	// -------- MANAGER STUFFS BELOW -------- //
	
	// Helper class for grouping partial recipe data
	public static class RecipeInfo
	{
		public IRecipe recipe;
		public ItemStack stack;
		
		public RecipeInfo(IRecipe recipe, ItemStack stack)
		{
			this.recipe = recipe;
			this.stack = stack;
		}
	}
	
	public static void HideAll()
	{
		for(int i = CraftingManager.getInstance().getRecipeList().size() - 1; i >= 1; i--) // DO NOT remove index 0. That is reserved for the intercepter
		{
			IRecipe recipe = (IRecipe)CraftingManager.getInstance().getRecipeList().get(i);
			if(!allRecipes.contains(recipe))
			{
				allRecipes.add(recipe);
			}
			CraftingManager.getInstance().getRecipeList().remove(i);
		}
	}
	
	public static void UnHideAll()
	{
		for(int i = allRecipes.size() - 1; i >= 0; i--)
		{
			IRecipe recipe = allRecipes.get(i);
			if(!CraftingManager.getInstance().getRecipeList().contains(recipe))
			{
				CraftingManager.getInstance().getRecipeList().add(recipe);
			}
			allRecipes.remove(i);
		}
	}
	
    public static RecipeInfo findMatchingRecipe(InventoryCrafting invo, World world)
    {
        int i = 0;
        ItemStack itemstack = null;
        ItemStack itemstack1 = null;
        int j;

        for (j = 0; j < invo.getSizeInventory(); ++j)
        {
            ItemStack itemstack2 = invo.getStackInSlot(j);

            if (itemstack2 != null)
            {
                if (i == 0)
                {
                    itemstack = itemstack2;
                }

                if (i == 1)
                {
                    itemstack1 = itemstack2;
                }

                ++i;
            }
        }

        if (i == 2 && itemstack.getItem() == itemstack1.getItem() && itemstack.stackSize == 1 && itemstack1.stackSize == 1 && itemstack.getItem().isRepairable())
        {
            Item item = itemstack.getItem();
            int j1 = item.getMaxDamage() - itemstack.getItemDamageForDisplay();
            int k = item.getMaxDamage() - itemstack1.getItemDamageForDisplay();
            int l = j1 + k + item.getMaxDamage() * 5 / 100;
            int i1 = item.getMaxDamage() - l;

            if (i1 < 0)
            {
                i1 = 0;
            }

            return new RecipeInfo(null, new ItemStack(itemstack.getItem(), 1, i1));
        }
        else
        {
        	ArrayList<IRecipe> tmpListing = RR_Settings.hideRecipes? allRecipes : (ArrayList<IRecipe>)CraftingManager.getInstance().getRecipeList();
            for (j = RR_Settings.hideRecipes? 0 : 1; j < tmpListing.size(); ++j) // Note that this now starts at index 1, this is because 0 is reserved for the intercepter
            {
                IRecipe irecipe = tmpListing.get(j);

                if (irecipe.matches(invo, world))
                {
                    return new RecipeInfo(irecipe, irecipe.getCraftingResult(invo));
                }
            }

            return new RecipeInfo(null, null);
        }
    }
    
    public static ArrayList<ItemStack> getIngredients(ItemStack stack)
    {
    	ArrayList<ItemStack> ing = new ArrayList<ItemStack>();
    	
    	if(stack == null)
    	{
    		return ing;
    	}
    	
    	boolean flag = false; // Should the normal recipes not return anything then we shall add the item itself as a research item

    	ArrayList<IRecipe> tmpListing = RR_Settings.hideRecipes? allRecipes : (ArrayList<IRecipe>)CraftingManager.getInstance().getRecipeList();
    	for(int i = 0; i < tmpListing.size(); i ++)
    	{
    		IRecipe recipe = tmpListing.get(i);
    		
    		if(recipe == null || recipe.getRecipeOutput() == null || recipe.getRecipeOutput().getItem() != stack.getItem() || recipe.getRecipeOutput().getItemDamage() != stack.getItemDamage() || recipe.getRecipeOutput().getItemDamage() == Short.MAX_VALUE)
    		{
    			continue;
    		}
    		
    		if(recipe instanceof ShapedRecipes)
    		{
    			ShapedRecipes sr = (ShapedRecipes)recipe;
    			
    			for(ItemStack iStack : sr.recipeItems)
    			{
    				if(!ContainsStack(ing, (ItemStack)iStack))
    				{
    					ing.add(iStack);
    				}
    			}
    		} else if(recipe instanceof ShapelessRecipes)
    		{
    			ShapelessRecipes sr = (ShapelessRecipes)recipe;
    			
    			for(Object iStack : sr.recipeItems)
    			{
    				if(!ContainsStack(ing, (ItemStack)iStack))
    				{
    					ing.add((ItemStack)iStack);
    				}
    			}
    		} else if(recipe instanceof ShapedOreRecipe)
    		{
    			ShapedOreRecipe sor = (ShapedOreRecipe)recipe;
    			
    			for(Object obj : sor.getInput())
    			{
    				if(obj instanceof ItemStack)
    				{
    					if(!ContainsStack(ing, (ItemStack)obj))
    					{
    						ing.add((ItemStack)obj);
    					}
    				} else if(obj instanceof ArrayList)
    				{
    					for(Object obj1 : (ArrayList)obj)
    					{
    						if(!ContainsStack(ing, (ItemStack)obj1))
    						{
    							ing.add((ItemStack)obj1);
    						}
    					}
    				}
    			}
    		} else if(recipe instanceof ShapelessOreRecipe)
    		{
    			ShapelessOreRecipe sor = (ShapelessOreRecipe)recipe;
    			
    			for(Object obj : sor.getInput())
    			{
    				if(obj instanceof ItemStack)
    				{
    					if(!ContainsStack(ing, (ItemStack)obj))
    					{
    						ing.add((ItemStack)obj);
    					}
    				} else if(obj instanceof ArrayList)
    				{
    					for(Object obj1 : (ArrayList)obj)
    					{
    						if(!ContainsStack(ing, (ItemStack)obj1))
    						{
    							ing.add((ItemStack)obj1);
    						}
    					}
    				}
    			}
    		} else // Extend on more supported recipe types here... probably a good place for additional NEI support
    		{
    			flag = true;
    		}
    	}
    	
    	if(flag && ing.size() <= 0) // Emergency fall back for unsupported recipe handlers
    	{
    		ing.add(stack);
    	}
    	
    	return ing;
    }
    
    public static Container getContainer(InventoryCrafting invo)
    {
    	try
    	{
	    	Field conField = InventoryCrafting.class.getDeclaredField("eventHandler");
	    	conField.setAccessible(true);
			return (Container)conField.get(invo);
    	} catch(Exception e)
    	{
    		RecipeResearch.logger.log(Level.ERROR, "Unable to get container for InventoryCrafting", e);
    		return null;
    	}
    }
    
    public static ArrayList getCrafters(Container cont)
    {
    	try
    	{
        	Field craftField = Container.class.getDeclaredField("crafters");
        	craftField.setAccessible(true);
        	return (ArrayList)craftField.get(cont);
    	} catch(Exception e)
    	{
    		RecipeResearch.logger.log(Level.ERROR, "Unable to get crafters for Container", e);
    		return null;
    	}
    }
    
    public static boolean ContainsStack(ArrayList<ItemStack> list, ItemStack stack)
    {
    	if(stack == null)
    	{
    		return false;
    	}
    	
    	for(ItemStack entry : list)
    	{
    		if(entry == null)
    		{
    			continue;
    		}
    		
    		if(entry.getItem() == stack.getItem() && (entry.getItemDamage() == stack.getItemDamage() || entry.getItem().isDamageable()))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
