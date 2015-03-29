package reciperesearch.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import reciperesearch.handlers.ConfigHandler;
import reciperesearch.utils.RecipeHelper;
import reciperesearch.utils.ResearchHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ResearchPacket implements IMessage
{
	NBTTagCompound tags = new NBTTagCompound();
	
	public ResearchPacket()
	{	
	}
	
	public ResearchPacket(NBTTagCompound tags)
	{
		this.tags = tags;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tags = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandleServer implements IMessageHandler<ResearchPacket, IMessage>
	{
		@Override
		public IMessage onMessage(ResearchPacket message, MessageContext ctx)
		{
			return null;
		}
	}
	
	public static class HandleClient implements IMessageHandler<ResearchPacket, IMessage>
	{
		@Override
		public IMessage onMessage(ResearchPacket message, MessageContext ctx)
		{
			int ID = message.tags.hasKey("ID")? message.tags.getInteger("ID") : -1; // Gets the packet ID or returns -1 one if one is missing
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			
			if(ID == 0) // Sync all server side research data and crafting efficiency. Also refreshes the hide list
			{
				NBTTagCompound perTag = ResearchHelper.getPersistentNBT(player);
				
				if(message.tags.hasKey("RecipeResearch"))
				{
					perTag.setTag("RecipeResearch", message.tags.getCompoundTag("RecipeResearch"));
				}
				
				if(message.tags.hasKey("ResearchEff"))
				{
					perTag.setInteger("ResearchEff", message.tags.getInteger("ResearchEff"));
				}
				
				ResearchHelper.setPersistentNBT(player, perTag);
				RecipeHelper.RefreshHidden(player);
			} else if(ID == 1) // Sync necessary server side configuration settings
			{
				ConfigHandler.setServerConfigs(message.tags);
				RecipeHelper.RefreshHidden(player);
			}
			
			return null;
		}
	}
}
