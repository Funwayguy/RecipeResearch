package reciperesearch.handlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Level;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.utils.ResearchHelper;

public class RecipeInterceptor implements IRecipe
{
	public static final RecipeInterceptor instance = new RecipeInterceptor();
	
	Random rand = new Random();
	boolean isCustom = false;
	RecipeInfo output = new RecipeInfo(null, null);
	static ArrayList<IRecipe> allRecipes = new ArrayList<IRecipe>();
	
	private RecipeInterceptor() // Yea it's private and inaccessible. What of it
	{
	}
	
	@Override
	public boolean matches(InventoryCrafting invo, World world)
	{
		checkAndResetIntercept();
		
		output = findMatchingRecipe(invo, world);
		
		if(output.recipe == null) // Looks like the recipe we intercepted is a repair recipe and probably shouldn't have been intercepted in the first place
		{
			return false;
		} else if(getIngredients(output.stack).size() <= 0) // We can't find any research ingredients for this recipe, including the resulting item, so we're just going to let the recipe handle itself
		{
			isCustom = true;
			return true;
		}
		
		return true; // This will always result in true because we want this to be the first and only recipe Minecraft checks
	}
	
	/**
	 * Ensure the recipe intercept is still in place
	 */
	@SuppressWarnings("unchecked")
	public void checkAndResetIntercept()
	{
		if(CraftingManager.getInstance().getRecipeList().get(0) != this)
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
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting invo)
	{
		checkAndResetIntercept();
		
		if(output.recipe == null)
		{
			return null;
		}
		
		ItemStack outStack = output.recipe.getCraftingResult(invo);
		
		if(outStack != null)
		{
			outStack = outStack.copy(); // Just in case
			
			Container tmpCon = getContainer(invo);
			ArrayList<?> crafters = tmpCon == null? null : getCrafters(tmpCon);
			
			if(crafters != null)
			{
				for(Object obj : crafters)
				{
					if(obj instanceof EntityPlayer)
					{
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
	
	@SuppressWarnings("unchecked")
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
	
    @SuppressWarnings("unchecked")
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
    
    @SuppressWarnings("unchecked")
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
    				if(iStack != null && !ContainsStack(ing, (ItemStack)iStack, false))
    				{
    					ing.add(iStack);
    				}
    			}
    		} else if(recipe instanceof ShapelessRecipes)
    		{
    			ShapelessRecipes sr = (ShapelessRecipes)recipe;
    			
    			for(Object iStack : sr.recipeItems)
    			{
    				if(iStack != null && !ContainsStack(ing, (ItemStack)iStack, false))
    				{
    					ing.add((ItemStack)iStack);
    				}
    			}
    		} else if(recipe instanceof ShapedOreRecipe)
    		{
    			ShapedOreRecipe sor = (ShapedOreRecipe)recipe;
    			
    			for(Object obj : sor.getInput())
    			{
    				if(obj == null)
    				{
    					continue;
    				} else if(obj instanceof ItemStack)
    				{
    					if(!ContainsStack(ing, (ItemStack)obj, false))
    					{
    						ing.add((ItemStack)obj);
    					}
    				} else if(obj instanceof ArrayList)
    				{
    					if(((ArrayList<?>)obj).size() <= 0)
    					{
    						continue;
    					}
    					
    					ItemStack tmpStack = ItemStack.copyItemStack((ItemStack)((ArrayList<?>)obj).get(0)); // We copy the stack because we need to edit it slightly
    				
						if(!ContainsStack(ing, tmpStack, true))
						{
							NBTTagCompound tmpTag = tmpStack.getTagCompound();
							tmpTag = tmpTag != null? tmpTag : new NBTTagCompound();
							tmpTag.setBoolean("UseOreDict", true);
							tmpStack.setTagCompound(tmpTag);
							ing.add(tmpStack);
						}
    				}
    			}
    		} else if(recipe instanceof ShapelessOreRecipe)
    		{
    			ShapelessOreRecipe sor = (ShapelessOreRecipe)recipe;
    			
    			for(Object obj : sor.getInput())
    			{
    				if(obj == null)
    				{
    					continue;
    				} else if(obj instanceof ItemStack)
    				{
    					if(!ContainsStack(ing, (ItemStack)obj, false))
    					{
    						ing.add((ItemStack)obj);
    					}
    				} else if(obj instanceof ArrayList)
    				{
    					if(((ArrayList<?>)obj).size() <= 0)
    					{
    						continue;
    					}
    					
    					ItemStack tmpStack = ItemStack.copyItemStack((ItemStack)((ArrayList<?>)obj).get(0)); // We copy the stack because we need to edit it slightly
    				
						if(!ContainsStack(ing, tmpStack, true))
						{
							NBTTagCompound tmpTag = tmpStack.getTagCompound();
							tmpTag = tmpTag != null? tmpTag : new NBTTagCompound();
							tmpTag.setBoolean("UseOreDict", true);
							tmpStack.setTagCompound(tmpTag);
							ing.add(tmpStack);
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
	    	Field conField = InventoryCrafting.class.getDeclaredField("field_70465_c");
	    	conField.setAccessible(true);
			return (Container)conField.get(invo);
    	} catch(Exception e)
    	{
    		try
    		{
    	    	Field conField = InventoryCrafting.class.getDeclaredField("eventHandler");
    	    	conField.setAccessible(true);
    			return (Container)conField.get(invo);
    		} catch(Exception e1)
    		{
        		RecipeResearch.logger.log(Level.ERROR, "Unable to get container for InventoryCrafting", e1);
        		return null;
    		}
    	}
    }
    
    public static ArrayList<?> getCrafters(Container cont)
    {
    	try
    	{
        	Field craftField = Container.class.getDeclaredField("field_75149_d");
        	craftField.setAccessible(true);
        	return (ArrayList<?>)craftField.get(cont);
    	} catch(Exception e)
    	{
    		try
    		{
	        	Field craftField = Container.class.getDeclaredField("crafters");
	        	craftField.setAccessible(true);
	        	return (ArrayList<?>)craftField.get(cont);
    		} catch(Exception e1)
    		{
        		RecipeResearch.logger.log(Level.ERROR, "Unable to get crafters for Container", e1);
        		return null;
    		}
    	}
    }
    
    public static boolean ContainsStack(ArrayList<ItemStack> list, ItemStack stack, boolean oreDict)
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
    		
    		if(oreDict? AllMatch(entry, stack) : StackMatch(entry, stack))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2)
    {
    	return stack1.getItem() == stack2.getItem() && (stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }
    
    public static boolean OreDictionaryMatch(ItemStack stack, String name)
    {
    	for(ItemStack oreStack : OreDictionary.getOres(name))
    	{
    		if(StackMatch(stack, oreStack))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2)
    {
    	if(StackMatch(stack1, stack2))
    	{
    		return true;
    	}
    	
    	for(int id : OreDictionary.getOreIDs(stack1)) // Search all ore dictionary listings for matches
    	{
    		if(OreDictionaryMatch(stack2, OreDictionary.getOreName(id)))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static ArrayList<ItemStack> getAllOreSiblings(ItemStack stack)
    {
    	ArrayList<ItemStack> list = new ArrayList<ItemStack>();
    	
    	for(int id : OreDictionary.getOreIDs(stack))
    	{
    		list.addAll(OreDictionary.getOres(OreDictionary.getOreName(id)));
    	}
    	
    	return list;
    }
}
