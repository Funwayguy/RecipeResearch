package reciperesearch.blocks;

import reciperesearch.core.RecipeResearch;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockPrototypeTable extends Block implements ITileEntityProvider
{
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    
	public BlockPrototypeTable()
	{
		super(Material.wood);
		this.setBlockTextureName(RecipeResearch.MODID + ":prototype_table");
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setBlockName("reciperesearch.prototype_table");
	}

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return side == 1 ? this.iconTop : (side == 0 ? Blocks.planks.getBlockTextureFromSide(side) : this.blockIcon);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register)
    {
        this.blockIcon = register.registerIcon(this.getTextureName() + "_side");
        this.iconTop = register.registerIcon(this.getTextureName() + "_top");
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float vecX, float vecY, float vecZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	player.openGui(RecipeResearch.instance, 0, world, x, y, z);
            return true;
        }
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
    	TileEntityPrototyping tileentity = (TileEntityPrototyping)world.getTileEntity(x, y, z);

        if (tileentity != null)
        {
            for (int i1 = 0; i1 < tileentity.getSizeInventory(); ++i1)
            {
                ItemStack itemstack = tileentity.getStackInSlot(i1);

                if (itemstack != null)
                {
                    float f = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem))
                    {
                        int j1 = world.rand.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize)
                        {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);

                        if (itemstack.hasTagCompound())
                        {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }
                    }
                }
            }

            world.func_147453_f(x, y, z, block);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityPrototyping();
	}
}
