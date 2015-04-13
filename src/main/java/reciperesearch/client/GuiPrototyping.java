package reciperesearch.client;

import java.util.ArrayList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import reciperesearch.blocks.TileEntityPrototyping;
import reciperesearch.inventory.ContainerPrototyping;
import reciperesearch.utils.RecipeHelper;

public class GuiPrototyping extends GuiContainer
{
    private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("reciperesearch","textures/gui/prototyping_gui.png");
    private EntityPlayer player;
    
    ItemStack toolTipStack = null;
    
	public GuiPrototyping(EntityPlayer player, TileEntityPrototyping protoTile, int x, int y, int z)
	{
		super(new ContainerPrototyping(player, protoTile, x, y, z));
		this.player = player;
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		GL11.glPushMatrix();
		super.drawScreen(mx, my, partialTick);
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	if(toolTipStack != null)
    	{
    		this.renderToolTip(toolTipStack, mx, my);
    	}
        
        GL11.glPopMatrix();
	}

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mx, int my)
    {
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 4, 4210752);
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int mx, int my)
	{
		GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        this.mc.getTextureManager().bindTexture(craftingTableGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        
        int progress = ((ContainerPrototyping)this.inventorySlots).protoTile.progress;
        this.drawTexturedModalRect(k + 16, l + 24, 96, 168, 24, MathHelper.ceiling_float_int(48F * (float)progress/60F));
        
        if(this.inventorySlots.getSlot(2).getHasStack())
        {
            this.drawTexturedModalRect(k + 40, l + 8, 0, 168, 96, 64);
            NBTTagCompound stackTags = this.inventorySlots.getSlot(2).getStack().getTagCompound();
            stackTags = stackTags != null? stackTags : new NBTTagCompound();
            
            ItemStack outStack = ItemStack.loadItemStackFromNBT(stackTags.getCompoundTag("Output"));
            
            if(outStack == null) // Well there's no output to research so why render the rest
            {
                GL11.glPopMatrix(); // DO NOT exclude this! Always pop before return.
                return;
            }
        	
        	toolTipStack = null;
            
            this.drawItemStack(outStack, k + 112, l + 32, "");
            if(this.func_146978_c(112, 32, 16, 16, mx, my))
            {
            	toolTipStack = outStack;
            }
            
            NBTTagList inputs = stackTags.getTagList("Materials", 10);
            
            for(int i = 0; i < inputs.tagCount() && i < 9; i++)
            {
            	int n = 0;
            	if(inputs.tagCount() > 9)
            	{
            		n += (int)((player.worldObj.getTotalWorldTime()%24000)/50)%MathHelper.ceiling_float_int((float)(inputs.tagCount() - 6)/3F) * 3;
            	}
            	
            	if(i + n > inputs.tagCount())
            	{
            		break;
            	}
            	
        		int rx = i%3 * 17;
        		int ry = (i - i%3)/3 * 17;
        		
            	NBTTagCompound inTag = inputs.getCompoundTagAt(i + n);
            	ItemStack inStack = ItemStack.loadItemStackFromNBT(inTag);
            	
            	if(inStack != null & inStack.getItem() != null)
            	{
            		int research = inTag.getInteger("Research");
            		if(research <= 0 && !(RecipeHelper.StackMatch(inStack, outStack) && inputs.tagCount() == 1))
            		{
            	        this.fontRendererObj.drawString("?", k + 53 + rx, l + 20 + ry, 4210752);
            		} else
            		{
            			ItemStack tmpStack = inStack.copy();
            			
            			if(inTag.getBoolean("UseOreDict"))
            			{
            				ArrayList<ItemStack> oreList = RecipeHelper.getAllOreSiblings(inStack);
            				int cycle = (int)((player.worldObj.getTotalWorldTime()%24000)/20)%oreList.size();
            				tmpStack = ItemStack.copyItemStack(oreList.get(cycle));
            			} else if(inStack.getItemDamage() == Short.MAX_VALUE)
            			{
            				int cycle = (int)((player.worldObj.getTotalWorldTime()%24000)/20)%16;
            				tmpStack.setItemDamage(cycle);
            			}
            			
        				this.drawItemStack(tmpStack, k + 47 + rx, l + 15 + ry, (research < 25? EnumChatFormatting.RED : (research < 75? EnumChatFormatting.YELLOW : EnumChatFormatting.GREEN)) + (research < 100? research + "%" : ""));
        	            if(this.func_146978_c(47 + rx, 15 + ry, 16, 16, mx, my))
        	            {
        	            	toolTipStack = tmpStack;
        	            }
            		}
            	}
            }
        }
        
        GL11.glPopMatrix();
	}

    private void drawItemStack(ItemStack p_146982_1_, int p_146982_2_, int p_146982_3_, String p_146982_4_) // Drawing stacks without needing a slot
    {
    	GL11.glPushMatrix();
        
        try
        {
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        RenderHelper.enableGUIStandardItemLighting();
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        
	        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
	        this.zLevel = 200.0F;
	        itemRender.zLevel = 200.0F;
	        FontRenderer font = null;
	        if (p_146982_1_ != null) font = p_146982_1_.getItem().getFontRenderer(p_146982_1_);
	        if (font == null) font = fontRendererObj;
	        itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_);
	        itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_, p_146982_4_);
	        this.zLevel = 0.0F;
	        itemRender.zLevel = 0.0F;
        } catch(Exception e)
        {
        }
        
        GL11.glPopMatrix();
    }
}
