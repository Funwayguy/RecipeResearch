package reciperesearch.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import reciperesearch.blocks.TileEntityPrototyping;
import reciperesearch.core.RecipeResearch;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerPrototyping extends Container
{
    /** The crafting matrix inventory (3x3). */
    public TileEntityPrototyping protoTile;
    private EntityPlayer player;
    private int posX;
    private int posY;
    private int posZ;
    
	public ContainerPrototyping(EntityPlayer player, TileEntityPrototyping protoTile, int x, int y, int z)
	{
		this.protoTile = protoTile;
		this.protoTile.user = player;
        this.player = player;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.addSlotToContainer(new Slot(this.protoTile, 0, 16, 8));
        this.addSlotToContainer(new Slot(this.protoTile, 1, 144, 8)
        {
        	public boolean isItemValid(ItemStack stack)
            {
                return stack != null && stack.getItem() != null && stack.getItem() == Items.paper;
            }
        });
        this.addSlotToContainer(new Slot(this.protoTile, 2, 144, 56)
        {
        	public boolean isItemValid(ItemStack stack)
            {
                return stack != null && stack.getItem() != null && stack.getItem() == RecipeResearch.researchPage;
            }
        });
        int l;
        int i1;

        for (l = 0; l < 3; ++l)
        {
            for (i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(player.inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }

        for (l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(player.inventory, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(this.protoTile);
	}

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
	@Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        
        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);
            
            icrafting.sendProgressBarUpdate(this, 0, this.protoTile.progress);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int number)
    {
        if (id == 0)
        {
            this.protoTile.progress = number;
        }
    }

    /**
     * Called when the container is closed.
     */
	public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        protoTile.user = null;
    }

    public boolean canInteractWith(EntityPlayer player)
    {
        return this.player.worldObj.getBlock(this.posX, this.posY, this.posZ) != RecipeResearch.prototypeTable ? false : player.getDistanceSq((double)this.posX + 0.5D, (double)this.posY + 0.5D, (double)this.posZ + 0.5D) <= 64.0D;
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            if (index < 3)
            {
                if (!this.mergeItemStack(itemstack1, 3, 39, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 1, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
