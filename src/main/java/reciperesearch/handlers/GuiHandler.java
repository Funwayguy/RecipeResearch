package reciperesearch.handlers;

import reciperesearch.blocks.TileEntityPrototyping;
import reciperesearch.client.GuiPrototyping;
import reciperesearch.inventory.ContainerPrototyping;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(ID == 0 && tile instanceof TileEntityPrototyping)
		{
			return new ContainerPrototyping(player, (TileEntityPrototyping)tile, x, y, z);
		} else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(ID == 0 && tile instanceof TileEntityPrototyping)
		{
			return new GuiPrototyping(player, (TileEntityPrototyping)tile, x, y, z);
		} else
		{
			return null;
		}
	}
}
