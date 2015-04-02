package reciperesearch.utils;

import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.network.ResearchPacket;

public class ResearchHelper
{
	/**
	 * Randomly change the result of the stack based on current research values. Edits existing item without creating new instance
	 * @param player
	 * @param stack
	 */
	public static void changeResult(EntityPlayer player, ItemStack stack)
	{
		if(player == null || player.worldObj.isRemote || isWhitelisted(stack)) // Either no-one is crafting this recipe or the item is white listed
		{
			return;
		}
		
		int baseEff = ResearchHelper.getResearchEfficiency(player);
		int num = MathHelper.clamp_int(ResearchHelper.getItemResearch(player, stack), baseEff, 100);
		
		if(num < player.getRNG().nextInt(100))
		{
			ItemStack failStack = new ItemStack(RecipeResearch.failedItem);
			failStack.setStackDisplayName(stack.getDisplayName());
			NBTTagCompound failTags = new NBTTagCompound();
			failStack.writeToNBT(failTags);
			stack.readFromNBT(failTags);
		}
	}
	
	/**
	 * Check if an item is white listed from crafting failure
	 * @param stack
	 * @return
	 */
	public static boolean isWhitelisted(ItemStack stack)
	{
		String researchID = "";
		String researchWild = "";
		
		if(stack != null)
		{
			researchWild = Item.itemRegistry.getNameForObject(stack.getItem());
			researchID = researchWild + ":" + stack.getItemDamage();
		} else
		{
			return false; // There is no item to replace...
		}
		
		for(String listing : RR_Settings.recipeWhitelist)
		{
			if(listing.equalsIgnoreCase(researchID) || listing.equalsIgnoreCase(researchWild)) // Direct match
			{
				return true;
			} else if(Arrays.asList(OreDictionary.getOreNames()).contains(listing.replaceFirst("ore:", ""))) // OreDictionary
			{
				for(ItemStack oreStack : OreDictionary.getOres(listing.replaceFirst("ore:", "")))
				{
					if(RecipeHelper.StackMatch(stack, oreStack))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a number between 0 and 100 representing the percentage of knowledge/research the given players has on this item
	 * @param player
	 * @param stack
	 * @return
	 */
	public static int getItemResearch(EntityPlayer player, ItemStack stack)
	{
		if(player == null || stack == null)
		{
			return 0;
		}
		
		if(isWhitelisted(stack))
		{
			return 100;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().getHasSubtypes()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		return allResearch.getInteger(researchID);
	}
	
	/**
	 * How efficient is the given player's ability to craft/research items (0-100, Should only be 0 if the supplied player is null)
	 * @param player
	 * @return
	 */
	public static int getResearchEfficiency(EntityPlayer player)
	{
		if(player == null)
		{
			return 0;
		}
		
		int tmp = getPersistentNBT(player).getInteger("ResearchEff");
		return tmp <= RR_Settings.startKnowledge? RR_Settings.startKnowledge : tmp;
	}
	
	/**
	 * Increments the players knowledge research on the item by the given amount
	 * @param player
	 * @param stack
	 * @param amount
	 */
	public static void addItemResearch(EntityPlayer player, ItemStack stack, int amount)
	{
		if(player == null || stack == null)
		{
			return;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().getHasSubtypes()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		allResearch.setInteger(researchID, MathHelper.clamp_int(allResearch.getInteger(researchID) + amount, 0, 100));
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setTag("RecipeResearch", allResearch);
		setPersistentNBT(player, perTag);
		
		if(player instanceof EntityPlayerMP && !player.worldObj.isRemote)
		{
			SyncResearch((EntityPlayerMP)player);
		}
	}
	
	/**
	 * Sets the players research value for the given item
	 * @param player
	 * @param stack
	 * @param amount
	 */
	public static void setItemResearch(EntityPlayer player, ItemStack stack, int amount)
	{
		if(player == null || stack == null)
		{
			return;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().getHasSubtypes()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		allResearch.setInteger(researchID, MathHelper.clamp_int(amount, 0, 100));
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setTag("RecipeResearch", allResearch);
		setPersistentNBT(player, perTag);
		
		if(player instanceof EntityPlayerMP && !player.worldObj.isRemote)
		{
			SyncResearch((EntityPlayerMP)player);
		}
	}
	
	/**
	 * Sets the players research/crafting efficiency
	 * @param player
	 * @param amount
	 */
	public static void setResearchEfficiency(EntityPlayer player, int amount)
	{
		if(player == null)
		{
			return;
		}
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setInteger("ResearchEff", MathHelper.clamp_int(amount, 0, 100));
		setPersistentNBT(player, perTag);
		
		if(player instanceof EntityPlayerMP && !player.worldObj.isRemote)
		{
			SyncResearch((EntityPlayerMP)player);
		}
	}
	
	/**
	 * Resets all crafting knowledge and research
	 * @param player
	 */
	public static void ResetResearch(EntityPlayer player)
	{
		player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag("RecipeResearch");
		player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag("ResearchEff");
		
		if(player instanceof EntityPlayerMP && !player.worldObj.isRemote)
		{
			SyncResearch((EntityPlayerMP)player);
		}
	}
	
	/**
	 * Sends all server side research data to client side. Does nothing if called from a client
	 * @param player
	 */
	public static void SyncResearch(EntityPlayerMP player)
	{
		if(player == null || player.worldObj.isRemote || player.isDead)
		{
			return;
		}
		
		NBTTagCompound perTags = getPersistentNBT(player);
		
		NBTTagCompound syncTags = new NBTTagCompound();
		syncTags.setInteger("ID", 0);
		syncTags.setTag("RecipeResearch", perTags.getCompoundTag("RecipeResearch"));
		syncTags.setInteger("ResearchEff", perTags.getInteger("ResearchEff"));
		ResearchPacket syncPacket = new ResearchPacket(syncTags);
		RecipeResearch.instance.network.sendTo(syncPacket, player);
	}
	
	public static ItemStack getBook()
	{
		ItemStack book = new ItemStack(Items.written_book);
		NBTTagCompound bookTags = new NBTTagCompound();
		bookTags.setString("author", "Funwayguy");
		NBTTagList pages = new NBTTagList();
		
		String pageText = " - Researching 101 - \n\n";
		pageText += "To start researching recipe using the prototyping table you must first have a copy of the item you want to research. ";
		pageText += "Ensure you have spare paper in the top right slot of the UI before continuing, you'll need these to begin your research.";
		
		pages.appendTag(new NBTTagString(pageText));
		
		pageText = "Start by inserting the item you wish to research into the top left slot of the UI and wait for the progress bar to complete. ";
		pageText += "This will create the initial research notes which you can now build upon. To finish your notes continue inserting the ingredients ";
		pageText += "required to craft the item.";
		
		pages.appendTag(new NBTTagString(pageText));
		
		pageText = " Note that there is a 10% chance you will lose the ingredient you are using so it's best to have spares. ";
		pageText += "Once all ingredients have been completed you may remove the page from the table and right click to claim it.";
		
		pages.appendTag(new NBTTagString(pageText));
		
		pageText = "The recipe should now be visible in NEI if it was previously hidden and you'll have a 100% success rate crafting it. ";
		pageText += "To speed up research in future you will need to read one of the 3 recipe textbooks. This will also lower your crafting ";
		pageText += "failure rate for undiscovered recipes.";
		
		pages.appendTag(new NBTTagString(pageText));
		
		bookTags.setTag("pages", pages);
		book.setTagCompound(bookTags);
		book.setStackDisplayName("How to Research Recipes");
		return book;
	}
	
	/**
	 * Shortcut to get persistent NBT data contained within the player
	 * @param player
	 * @return
	 */
	public static NBTTagCompound getPersistentNBT(EntityPlayer player)
	{
		return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
	}
	
	/**
	 * Sets the persistent data tag of the given player.
	 * WARNING: Incorrect use could delete important player data. Always use an edited version of the original to ensure important data is maintained
	 * @param player
	 * @param newTags
	 */
	public static void setPersistentNBT(EntityPlayer player, NBTTagCompound newTags)
	{
		player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, newTags);
	}
}
