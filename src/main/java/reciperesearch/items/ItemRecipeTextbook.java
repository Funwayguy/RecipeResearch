package reciperesearch.items;

import java.util.List;
import reciperesearch.core.RR_Settings;
import reciperesearch.utils.ResearchHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemRecipeTextbook extends Item
{
	IIcon[] icons;
	int[] bookLvls = new int[]{25, 50, 90, 100};
	
	public ItemRecipeTextbook()
	{
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setTextureName("reciperesearch:textbook");
		this.setUnlocalizedName("reciperesearch.textbook");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
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
		
		int rEff = ResearchHelper.getResearchEfficiency(player);
		int bLvl = bookLvls[stack.getItemDamage()%bookLvls.length];
		
		if(rEff < bLvl)
		{
			ResearchHelper.setResearchEfficiency(player, bLvl);
			
			player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocalFormatted("reciperesearch.chat.efficiency", bLvl + "%")));
			
			if(!player.capabilities.isCreativeMode && !RR_Settings.shareKnowledge)
			{
				--stack.stackSize;
			}
		}
		
		return stack;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
    	icons = new IIcon[4];
    	
    	for(int i = 0; i < 3; i++)
    	{
            this.icons[i] = register.registerIcon(this.getIconString() + "_" + i);
    	}
    	
        this.icons[3] = register.registerIcon(this.getIconString() + "_" + 2);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName(stack) + "_" + stack.getItemDamage();
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
	@SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List listing)
    {
        for (int i = 0; i < 4; ++i)
        {
        	listing.add(new ItemStack(item, 1, i));
        }
    }
    
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
        return stack.isItemEnchanted() || stack.getItemDamage() == 3;
    }

    /**
     * Gets an icon index based on an item's damage value
     */
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta)
    {
        return this.icons[meta%icons.length];
    }
}
