package reciperesearch.utils;

import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;

public class ResearchHelper
{
	public static void changeResult(EntityPlayer player, ItemStack stack)
	{
		String researchID = "";
		
		if(stack != null && player != null)
		{
			researchID = Item.itemRegistry.getNameForObject(stack.getItem());
			
			if(stack.getItem().isDamageable())
			{
				researchID = researchID + ":" + stack.getItemDamage();
			}
		} else
		{
			return; // There is no item to replace...
		}
		
		if(player.worldObj.isRemote || Arrays.asList(RR_Settings.recipeWhitelist).contains(researchID)) // Either no-one is crafting this recipe or the item is white listed
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
			
			/*if(player instanceof EntityPlayerMP)
			{
				((EntityPlayerMP)player).updateHeldItem();
			}*/
		}
	}
	
	public static int getItemResearch(EntityPlayer player, ItemStack stack)
	{
		if(player == null || stack == null)
		{
			return 0;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().isDamageable()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		return allResearch.getInteger(researchID);
	}
	
	public static int getResearchEfficiency(EntityPlayer player)
	{
		if(player == null)
		{
			return 0;
		}
		
		int tmp = getPersistentNBT(player).getInteger("ResearchEff");
		return tmp <= 0? 1 : tmp;
	}
	
	public static void addItemResearch(EntityPlayer player, ItemStack stack, int amount)
	{
		if(player == null || stack == null)
		{
			return;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().isDamageable()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		allResearch.setInteger(researchID, allResearch.getInteger(researchID) + amount);
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setTag("RecipeResearch", allResearch);
		setPersistentNBT(player, perTag);
	}
	
	public static void setItemResearch(EntityPlayer player, ItemStack stack, int amount)
	{
		if(player == null || stack == null)
		{
			return;
		}
		
		String itemID = Item.itemRegistry.getNameForObject(stack.getItem());
		String researchID = !stack.getItem().isDamageable()? itemID : itemID + ":" + stack.getItemDamage();
		NBTTagCompound allResearch = getPersistentNBT(player).getCompoundTag("RecipeResearch");
		allResearch.setInteger(researchID, amount);
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setTag("RecipeResearch", allResearch);
		setPersistentNBT(player, perTag);
	}
	
	public static void setResearchEfficiency(EntityPlayer player, int amount)
	{
		if(player == null)
		{
			return;
		}
		
		NBTTagCompound perTag = getPersistentNBT(player);
		perTag.setInteger("ResearchEff", amount);
		setPersistentNBT(player, perTag);
	}
	
	/**
	 * Resets all crafting knowledge and research
	 * @param player
	 */
	public static void ResetResearch(EntityPlayer player)
	{
		player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag("RecipeResearch");
		player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).removeTag("ResearchEff");
	}
	
	private static NBTTagCompound getPersistentNBT(EntityPlayer player)
	{
		return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
	}
	
	/**
	 * Sets the persistent data tag of the given player.
	 * WARNING: Incorrect use could delete important player data. Always use an edited version of the original to ensure important data is maintained
	 * @param player
	 * @param newTags
	 */
	private static void setPersistentNBT(EntityPlayer player, NBTTagCompound newTags)
	{
		player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, newTags);
	}
}
