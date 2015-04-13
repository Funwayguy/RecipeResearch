package reciperesearch.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.network.ResearchPacket;
import reciperesearch.utils.RecipeHelper;
import reciperesearch.utils.ResearchHelper;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event)
	{
		if(!event.world.isRemote && event.entity instanceof EntityPlayerMP)
		{
			ResearchHelper.SyncResearch((EntityPlayerMP)event.entity);
			NBTTagCompound setTags = ConfigHandler.getServerConfigs();
			setTags.setInteger("ID", 1);
			RecipeResearch.instance.network.sendTo(new ResearchPacket(setTags), (EntityPlayerMP)event.entity);
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.modID.equals(RecipeResearch.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event)
	{
		if(event.entityLiving instanceof EntityPlayer && !RR_Settings.persistResearch)
		{
			ResearchHelper.ResetResearch((EntityPlayer)event.entityLiving);
		}
	}
	
	Container lastCont = null; // Purely to check whether the event below needs to actually be processed
	
	@SubscribeEvent
	public void onContainerOpen(PlayerOpenContainerEvent event) // Refresh interceptor and hidden recipes before we do anything in the container. Prevents concurrent modification error later
	{
		if(event.entityPlayer.openContainer == lastCont)
		{
			return;
		} else
		{
			lastCont = event.entityPlayer.openContainer;
		}
		
		// Because I'm paranoid about things interrupting the recipe interceptor we're going to check it... yes again. Deal with it!
		RecipeInterceptor.instance.checkAndResetIntercept();
		if(RR_Settings.hideRecipes && RecipeResearch.proxy.isClient())
		{
			RecipeHelper.RefreshHidden(event.entityPlayer);
		} else
		{
			RecipeHelper.UnHideAll();
		}
	}
	
	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event) // We could screw with recipe results here but it doesn't work for shift click crafting
	{
		if(event.player.worldObj.isRemote || event.crafting == null || event.crafting.getItem() == null)
		{
			return;
		}
		
		if(event.crafting.getItem() == Item.getItemFromBlock(RecipeResearch.prototypeTable) && !ResearchHelper.getPersistentNBT(event.player).getBoolean("RecipeResearchBook"))
		{
			event.player.dropPlayerItemWithRandomChoice(ResearchHelper.getBook(), false);
			
			NBTTagCompound perTags = ResearchHelper.getPersistentNBT(event.player);
			perTags.setBoolean("RecipeResearchBook", true);
			ResearchHelper.setPersistentNBT(event.player, perTags);
		}
		
		if(event.craftMatrix instanceof InventoryCrafting && RecipeHelper.findMatchingRecipe((InventoryCrafting)event.craftMatrix, event.player.worldObj).recipe == null)
		{
			return;
		}
		
		int num = ResearchHelper.getItemResearch(event.player, event.crafting);
		int resEff = ResearchHelper.getResearchEfficiency(event.player);
		num = num < RR_Settings.practiceCap? MathHelper.clamp_int(num + RR_Settings.practiceWorth, resEff < 10? 10 : resEff, RR_Settings.practiceCap < resEff? resEff : RR_Settings.practiceCap) : num;
		
		ResearchHelper.setItemResearch(event.player, event.crafting, num);
	}
	
	@SubscribeEvent
	public void onToolTip(ItemTooltipEvent event)
	{
		if(event.itemStack == null || event.entityPlayer == null || event.entityPlayer.openContainer == null)
		{
			return;
		}
		
		if(RecipeResearch.proxy.isClient())
		{
			boolean flag = false;
			
			for(StackTraceElement trace : new Exception().getStackTrace())
			{
				if(trace.getMethodName().equals("drawScreen") || trace.getMethodName().equals("func_73863_a"))
				{
					flag = true;
					break;
				}
			}
			
			if(flag)
			{
				int research = ResearchHelper.getItemResearch(event.entityPlayer, event.itemStack);
				event.toolTip.add(StatCollector.translateToLocalFormatted("reciperesearch.tooltip.research", research + "%"));
			}
		}
	}
}
