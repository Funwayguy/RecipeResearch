package reciperesearch.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reciperesearch.core.RR_Settings;
import reciperesearch.utils.ResearchHelper;

public class ItemResearch extends Item
{
	public ItemResearch()
	{
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setTextureName("reciperesearch:research_page");
		this.setUnlocalizedName("reciperesearch.research_page");
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(world.isRemote)
		{
			return stack;
		}
		
		NBTTagCompound tags = stack.getTagCompound();
		
		if(tags == null)
		{
			return stack;
		}
		
		ItemStack outStack = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("Output"));
		
		if(outStack == null)
		{
			return stack;
		}
		
		NBTTagList matList = tags.getTagList("Materials", 10);
		
		if(matList.tagCount() <= 0)
		{
			return stack;
		}
		
		float total = matList.tagCount() * 100F;
		int current = 0;
		
		for(int i = 0; i < matList.tagCount(); i++)
		{
			NBTTagCompound ingredient = matList.getCompoundTagAt(i);
			current += ingredient.getInteger("Research");
		}
		
		if(current < total)
		{
			int pageResearch = MathHelper.floor_float((float)current/total * 100F);
			player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal("reciperesearch.chat.incomplete") + " (" + pageResearch + "%)"));
			
			if(RR_Settings.claimIncomplete && ResearchHelper.getItemResearch(player, outStack) < pageResearch)
			{
				ResearchHelper.setItemResearch(player, outStack, pageResearch);
				
				if(!player.capabilities.isCreativeMode && !RR_Settings.shareKnowledge)
				{
					--stack.stackSize;
				}
			}
			
			return stack;
		}
		
		player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocalFormatted("reciperesearch.chat.perfected", outStack.getDisplayName())));
		
		ResearchHelper.setItemResearch(player, outStack, MathHelper.floor_float((float)current/total * 100F));
		
		if(!player.capabilities.isCreativeMode && !RR_Settings.shareKnowledge)
		{
			--stack.stackSize;
		}
		
        return stack;
    }
}
