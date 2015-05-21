package reciperesearch.blocks;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.utils.RecipeHelper;
import reciperesearch.utils.RecipeHelper.RecipeInfo;
import reciperesearch.utils.ResearchHelper;

public class TileEntityPrototyping extends TileEntity implements IInventory
{
	public ItemStack[] stackList = new ItemStack[3];
	public EntityPlayer user;
	public int progress = 0;
	
	@Override
    public void readFromNBT(NBTTagCompound tags)
    {
    	super.readFromNBT(tags);
    	
        NBTTagList nbttaglist = tags.getTagList("Items", 10);
        this.stackList = new ItemStack[this.getSizeInventory()];
        
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound itemTag = nbttaglist.getCompoundTagAt(i);
            byte slot = itemTag.getByte("Slot");

            if (slot >= 0 && slot < this.stackList.length)
            {
                this.stackList[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tags)
    {
    	super.writeToNBT(tags);
    	
        NBTTagList nbttaglist = new NBTTagList();
        
        for (int i = 0; i < this.stackList.length; ++i)
        {
            if (this.stackList[i] != null)
            {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte)i);
                this.stackList[i].writeToNBT(itemTag);
                nbttaglist.appendTag(itemTag);
            }
        }
        
        tags.setTag("Items", nbttaglist);
    }
    
    @Override
    public void updateEntity()
    {
    	if(this.worldObj.isRemote || user == null)
    	{
    		return;
    	}
    	
    	if(getStackInSlot(0) != null && getStackInSlot(0).getItem() != null && (getStackInSlot(1) != null || getStackInSlot(2) != null))
    	{
    		if(progress >= 60)
    		{
    			progress = 0;
    		} else
    		{
    			progress += 1;
    			return;
    		}
    		
    		ItemStack input = getStackInSlot(0);
    		ItemStack paper = getStackInSlot(1);
    		ItemStack output = getStackInSlot(2);
    		
    		if(output == null && paper != null && paper.stackSize >= 1)
    		{
    			if(!RR_Settings.reverseMode)
    			{
    				RecipeInfo recipe = RecipeHelper.getRecipeFromIngredient(input, this.worldObj.rand, user);
    				if(recipe == null || recipe.stack == null)
    				{
    	    			decrStackSize(0, 1);
    					progress = 0;
    					return;
    				} else
    				{
    					input = recipe.stack;
    				}
    			}
    			
    			output = new ItemStack(RecipeResearch.researchPage);
    			
    			NBTTagCompound resTags = new NBTTagCompound();
    			NBTTagCompound outTags = new NBTTagCompound();
    			new ItemStack(input.getItem(), 1, input.getItemDamage()).writeToNBT(outTags);
    			resTags.setTag("Output", outTags);
    			
    			NBTTagList inTags = new NBTTagList();
    			
    			ArrayList<ItemStack> ingredients = RecipeHelper.getIngredients(input);
    			
    			if(ingredients.size() > 0)
    			{
	    			for(ItemStack ingStack : ingredients)
	    			{
	    				if(ingStack == null)
	    				{
	    					continue;
	    				}
	    				NBTTagCompound inputTag = new NBTTagCompound();
	    				ingStack.writeToNBT(inputTag);
	    				inputTag.setInteger("Research", 0);
	    				inputTag.setBoolean("UseOreDict", inputTag.getCompoundTag("tag").getBoolean("UseOreDict"));
	    				inTags.appendTag(inputTag);
	    			}
	    			
	    			resTags.setTag("Materials", inTags);
	    			output.setTagCompound(resTags);
	    			output.setStackDisplayName(input.getDisplayName() + " Recipe");
	    			
	    			setInventorySlotContents(2, output);
	    			decrStackSize(1, 1);
    			}
    			
    			if(RR_Settings.reverseMode)
    			{
    				decrStackSize(0, 1);
    			} else
    			{
    				progress = 60;
    				return;
    			}
    		} else if(output != null)
    		{
    			NBTTagCompound resTags = output.getTagCompound();
    			resTags = resTags != null? resTags : new NBTTagCompound();
    			
    			ItemStack outStack = ItemStack.loadItemStackFromNBT(resTags.getCompoundTag("Output"));
    			NBTTagList inTags = resTags.getTagList("Materials", 10);
    			
    			for(int i = 0; i < inTags.tagCount(); i++)
    			{
    				NBTTagCompound ingTag = inTags.getCompoundTagAt(i);
    				ItemStack ingStack = ItemStack.loadItemStackFromNBT(ingTag);
    				int research = ingTag.getInteger("Research");
    				boolean oreDict = ingTag.getBoolean("UseOreDict");
    				
    				if(ingStack != null && (oreDict? RecipeHelper.AllMatch(ingStack, input) : RecipeHelper.StackMatch(ingStack, input)))
    				{
    					research = MathHelper.clamp_int(research + ResearchHelper.getResearchEfficiency(user), 0, 100);
    					inTags.getCompoundTagAt(i).setInteger("Research", research);
    					//break;
    				} else if(ingStack != null && RecipeHelper.StackMatch(input, outStack))
    				{
    					research = MathHelper.clamp_int(research + (ResearchHelper.getResearchEfficiency(user)/2), 0, 100);
    					inTags.getCompoundTagAt(i).setInteger("Research", research);
    				}
    			}
    			
    			if(this.worldObj.rand.nextInt(10) == 0)
    			{
    				this.decrStackSize(0, 1);
    			}
    		}
    	} else
    	{
    		progress = 0;
    	}
    }
	
	@Override
	public int getSizeInventory()
	{
		return stackList.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(slot < this.getSizeInventory())
		{
			return stackList[slot];
		} else
		{
			return null;
		}
	}
	
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
        if (this.stackList[slot] != null)
        {
            ItemStack itemstack;

            if (this.stackList[slot].stackSize <= amount)
            {
                itemstack = this.stackList[slot];
                this.stackList[slot] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.stackList[slot].splitStack(amount);

                if (this.stackList[slot].stackSize == 0)
                {
                    this.stackList[slot] = null;
                }
                
                return itemstack;
            }
        }
        else
        {
            return null;
        }
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
        if (this.stackList[slot] != null)
        {
            ItemStack itemstack = this.stackList[slot];
            this.stackList[slot] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
        this.stackList[slot] = stack;
	}
	
	@Override
	public String getInventoryName()
	{
		return "container.prototyping";
	}
	
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return user == null || user == player;
	}
	
	@Override
	public void openInventory()
	{
	}
	
	@Override
	public void closeInventory()
	{
		user = null;
	}
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return true;
	}
}
