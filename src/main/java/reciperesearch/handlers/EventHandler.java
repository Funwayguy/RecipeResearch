package reciperesearch.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import reciperesearch.core.RR_Settings;
import reciperesearch.core.RecipeResearch;
import reciperesearch.utils.ResearchHelper;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class EventHandler
{
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
	
	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event) // We could screw with recipe results here but it doesn't work for shift click crafting
	{
		// Because I'm paranoid about things interrupting the recipe interceptor we're going to check the recipe handler... yes again. Deal with it!
		RecipeInterceptor.instance.checkAndResetIntercept();
		
		if(event.player.worldObj.isRemote)
		{
			return;
		}
		
		if(event.craftMatrix instanceof InventoryCrafting && RecipeInterceptor.findMatchingRecipe((InventoryCrafting)event.craftMatrix, event.player.worldObj).recipe == null)
		{
			return;
		}
		
		int num = ResearchHelper.getItemResearch(event.player, event.crafting);
		int resEff = ResearchHelper.getResearchEfficiency(event.player);
		num = num < RR_Settings.practiceCap? MathHelper.clamp_int(num + RR_Settings.practiceWorth, resEff < 10? 10 : resEff, RR_Settings.practiceCap < resEff? resEff : RR_Settings.practiceCap) : num;
		
		ResearchHelper.setItemResearch(event.player, event.crafting, num);
	}
}
