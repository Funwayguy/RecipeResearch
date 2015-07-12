package reciperesearch.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
import reciperesearch.handlers.RecipeInterceptor;

public class RecipeHelper
{
	static ArrayList<IRecipe> allRecipes = new ArrayList<IRecipe>();
	static HashMap<ItemStack, ArrayList<ItemStack>> customIngredients = new HashMap<ItemStack, ArrayList<ItemStack>>();
	
	/**
	 * Helper class for partial recipes
	 */
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
	
	/**
	 * An array of recipes that should be unlocked next refresh pass. Necessary for recipes with conditional outputs
	 */
	public static ArrayList<IRecipe> scheduledUnlocks = new ArrayList<IRecipe>();
	
	/**
	 * Replaces an item's ingredient listing used for research (appends if an entry already exists)
	 * @param recipe
	 * @param ingredients
	 */
	public static void SetCustomIngredients(ItemStack item, ArrayList<ItemStack> ingredients)
	{
		if(customIngredients.containsKey(item))
		{
			customIngredients.get(item).addAll(ingredients);
		} else
		{
			customIngredients.put(item, ingredients);
		}
	}
	
	/**
	 * Refresh all hidden recipes to see if any should be unhidden to the given player (recipes without results are unhidden by default)
	 * @param player
	 */
	@SuppressWarnings("unchecked")
	public static void RefreshHidden(EntityPlayer player)
	{
		if(!RR_Settings.hideRecipes || ResearchHelper.getResearchEfficiency(player) >= 100)
		{
			UnHideAll();
		} else
		{
			HideAll();
			
			for(int i = allRecipes.size() - 1; i >= 0; i--)
			{
				if(i >= allRecipes.size())
				{
					RecipeResearch.logger.log(Level.ERROR, "Something modified the hidden recipe listing mid loop!", new IllegalStateException());
					i = allRecipes.size() - 1;
					
					if(i < 0)
					{
						break;
					}
				}
				
				IRecipe recipe = allRecipes.get(i);
				
				if(scheduledUnlocks.contains(recipe))
				{
					scheduledUnlocks.remove(recipe);
				} else if(recipe != null && recipe.getRecipeOutput() != null && ResearchHelper.getItemResearch(player, recipe.getRecipeOutput()) < 100)
				{
					continue;
				}
				
				if(recipe != null && !CraftingManager.getInstance().getRecipeList().contains(recipe))
				{
					CraftingManager.getInstance().getRecipeList().add(recipe);
				}
				allRecipes.remove(recipe);
			}
		}
	}
	
	/**
	 * Hides the given recipe instance from the vanilla recipe listing
	 * @param recipe
	 */
	public static void HideRecipe(IRecipe recipe)
	{
		if(recipe instanceof RecipeInterceptor)
		{
			return;
		}
		
		if(recipe != null && !allRecipes.contains(recipe))
		{
			allRecipes.add(recipe);
		}
		CraftingManager.getInstance().getRecipeList().remove(recipe);
	}
	
	/**
	 * Unhides recipe instance and restores it to the vanilla listing
	 * @param recipe
	 */
	@SuppressWarnings("unchecked")
	public static void UnhideRecipe(IRecipe recipe)
	{
		if(recipe != null && !CraftingManager.getInstance().getRecipeList().contains(recipe))
		{
			CraftingManager.getInstance().getRecipeList().add(recipe);
		}
		allRecipes.remove(recipe);
	}
	
	/**
	 * Hide all recipes regardless of research values
	 */
	public static void HideAll()
	{
		for(int i = CraftingManager.getInstance().getRecipeList().size() - 1; i >= 0; i--)
		{
			if(i >= CraftingManager.getInstance().getRecipeList().size())
			{
				RecipeResearch.logger.log(Level.ERROR, "Something modified the vanilla recipe listing mid loop!", new IllegalStateException());
				i = CraftingManager.getInstance().getRecipeList().size() - 1;
				
				if(i < 0)
				{
					break;
				}
			}
			
			IRecipe recipe = (IRecipe)CraftingManager.getInstance().getRecipeList().get(i);
			
			if(recipe instanceof RecipeInterceptor)
			{
				continue;
			}
			
			if(recipe != null && !allRecipes.contains(recipe))
			{
				allRecipes.add(recipe);
			}
			
			CraftingManager.getInstance().getRecipeList().remove(i);
		}
	}
	
	/**
	 * Restore all hidden recipes to the vanilla recipe listing
	 */
	@SuppressWarnings("unchecked")
	public static void UnHideAll()
	{
		for(int i = allRecipes.size() - 1; i >= 0; i--)
		{
			if(i >= allRecipes.size())
			{
				RecipeResearch.logger.log(Level.ERROR, "Something modified the hidden recipe listing mid loop!", new IllegalStateException());
				i = allRecipes.size() - 1;
				
				if(i < 0)
				{
					break;
				}
			}
			
			IRecipe recipe = allRecipes.get(i);
			if(recipe != null && !CraftingManager.getInstance().getRecipeList().contains(recipe))
			{
				CraftingManager.getInstance().getRecipeList().add(recipe);
			}
			allRecipes.remove(i);
		}
	}
	
	/**
	 * Modified version of the same method in CraftingManager but also iterates through hidden recipes and ignores intercepter
	 * @param invo
	 * @param world
	 * @return
	 */
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
        	ArrayList<IRecipe> tmpListing = new ArrayList<IRecipe>();
        	tmpListing.addAll((ArrayList<IRecipe>)CraftingManager.getInstance().getRecipeList());
        	tmpListing.addAll(allRecipes);
            for (j = 0; j < tmpListing.size(); ++j) // Note that this now starts at index 1, this is because 0 is reserved for the intercepter
            {
                IRecipe irecipe = tmpListing.get(j);
                
                if(irecipe == null || irecipe instanceof RecipeInterceptor)
                {
                	continue;
                }

                if (irecipe.matches(invo, world))
                {
                    return new RecipeInfo(irecipe, irecipe.getCraftingResult(invo));
                }
            }

            return new RecipeInfo(null, null);
        }
    }
    
    /**
     * Obtains a random recipe that uses the given item as an ingredient. If given a player, it will exclude recipes already learned<br>
     * <b>WARNING: Lots of loops in this method, call sparingly
     */
    @SuppressWarnings("unchecked")
	public static RecipeInfo getRecipeFromIngredient(ItemStack stack, Random rand, EntityPlayer player)
    {
    	ArrayList<IRecipe> tmpListing = new ArrayList<IRecipe>();
    	tmpListing.addAll((ArrayList<IRecipe>)CraftingManager.getInstance().getRecipeList());
    	tmpListing.addAll(allRecipes);
    	
    	ArrayList<RecipeInfo> possibleRecipes = new ArrayList<RecipeInfo>();
    	
    	RecipeLoop:
        for (int j = 0; j < tmpListing.size(); ++j) // Note that this now starts at index 1, this is because 0 is reserved for the intercepter
        {
            IRecipe irecipe = tmpListing.get(j);
            
            if(irecipe == null || irecipe instanceof RecipeInterceptor || irecipe.getRecipeOutput() == null)
            {
            	continue RecipeLoop;
            }
            
            ArrayList<ItemStack> ingredients = getIngredients(irecipe.getRecipeOutput());
            
            if(player != null && RR_Settings.progressive)
            {
	            for(ItemStack ingStack : ingredients)
	            {
	            	if(ResearchHelper.getItemResearch(player, ingStack) < 100F)
	            	{
	            		continue RecipeLoop;
	            	}
	            }
            }
            
            if(ContainsStack(ingredients, stack, true) && (player == null || ResearchHelper.getItemResearch(player, irecipe.getRecipeOutput()) < 100F))
            {
            	possibleRecipes.add(new RecipeInfo(irecipe, irecipe.getRecipeOutput()));
            }
        }
        
        if(possibleRecipes.size() <= 0)
        {
        	return new RecipeInfo(null, null);
        } else
        {
        	return possibleRecipes.get(rand.nextInt(possibleRecipes.size()));
        }
    }
    
    /**
     * Attempt to pull all known ingredients from all recipes
     * @param stack
     * @return
     */
    @SuppressWarnings("unchecked")
	public static ArrayList<ItemStack> getIngredients(ItemStack stack)
    {
    	ArrayList<ItemStack> ing = new ArrayList<ItemStack>();
    	
    	if(stack == null)
    	{
    		return ing;
    	}
    	
    	for(ItemStack customStack : customIngredients.keySet())
    	{
    		if(StackMatch(customStack, stack))
    		{
    			return customIngredients.get(customStack);
    		}
    	}
    	
    	boolean flag = false; // Should the normal recipes not return anything then we shall add the item itself as a research item
    	
    	ArrayList<IRecipe> tmpListing = new ArrayList<IRecipe>();
    	tmpListing.addAll((ArrayList<IRecipe>)CraftingManager.getInstance().getRecipeList());
    	tmpListing.addAll(allRecipes);
    	
    	for(int i = 0; i < tmpListing.size(); i ++)
    	{
    		IRecipe recipe = tmpListing.get(i);
    		
    		if(recipe == null || recipe.getRecipeOutput() == null || recipe.getRecipeOutput().getItem() == null || recipe.getRecipeOutput().getItem() != stack.getItem() || recipe.getRecipeOutput().getItemDamage() != stack.getItemDamage() || recipe.getRecipeOutput().getItemDamage() == Short.MAX_VALUE)
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
    
    /**
     * Force get container instance of the given crafting inventory
     * @param invo
     * @return
     */
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
    
    /**
     * Force get list of crafters using this container
     * @param cont
     * @return
     */
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
    
    /**
     * Check whether the list contains the given stack. Can also use ore dictionary matches
     * @param list
     * @param stack
     * @param oreDict
     * @return
     */
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
    
    /**
     * Check whether two stacks match
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2)
    {
    	if(stack1 == null || stack2 == null || stack1.getItem() == null || stack2.getItem() == null) // NPE checks
    	{
    		return false;
    	}
    	
    	return stack1.getItem() == stack2.getItem() && (stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }
    
    /**
     * Check if the item stack is part of the ore dictionary listing with the given name
     * @param stack
     * @param name
     * @return
     */
    public static boolean OreDictionaryMatch(ItemStack stack, String name)
    {
    	if(stack == null || stack.getItem() == null) // NPE checks
    	{
    		return false;
    	}
    	
    	for(ItemStack oreStack : OreDictionary.getOres(name))
    	{
    		if(StackMatch(stack, oreStack))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Check if the two stacks match directly or through ore dictionary listings
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2)
    {
    	if(stack1 == null || stack2 == null || stack1.getItem() == null || stack2.getItem() == null) // NPE checks
    	{
    		return false;
    	}
    	
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
    
    /**
     * Get associated item stacks from all related ore dictionary listings
     * @param stack
     * @return
     */
    public static ArrayList<ItemStack> getAllOreSiblings(ItemStack stack)
    {
    	ArrayList<ItemStack> list = new ArrayList<ItemStack>();
    	
    	if(stack == null || stack.getItem() == null) // NPE checks
    	{
    		return list;
    	}
    	
    	for(int id : OreDictionary.getOreIDs(stack))
    	{
    		list.addAll(OreDictionary.getOres(OreDictionary.getOreName(id)));
    	}
    	
    	return list;
    }
}
