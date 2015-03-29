package reciperesearch.core;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import reciperesearch.blocks.BlockPrototypeTable;
import reciperesearch.blocks.TileEntityPrototyping;
import reciperesearch.core.proxies.CommonProxy;
import reciperesearch.handlers.ConfigHandler;
import reciperesearch.handlers.RecipeInterceptor;
import reciperesearch.items.ItemRecipeTextbook;
import reciperesearch.items.ItemResearch;
import reciperesearch.utils.RecipeHelper;
import reciperesearch.utils.ResearchHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = RecipeResearch.MODID, version = RecipeResearch.VERSION, name = RecipeResearch.NAME, guiFactory = "reciperesearch.handlers.ConfigGuiFactory")
public class RecipeResearch
{
    public static final String MODID = "reciperesearch";
    public static final String VERSION = "RR_VER_KEY";
    public static final String NAME = "RecipeResearch";
    public static final String PROXY = "reciperesearch.core.proxies";
    public static final String CHANNEL = "RR_NET_CHAN";
	
	@Instance(MODID)
	public static RecipeResearch instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static Block prototypeTable;
	
	public static Item researchPage;
	public static Item failedItem;
	public static Item textbook;
    
    @SuppressWarnings("unchecked")
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	CraftingManager.getInstance().getRecipeList().add(0, RecipeInterceptor.instance);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	prototypeTable = new BlockPrototypeTable();
    	GameRegistry.registerBlock(prototypeTable, "prototype_table");
    	
    	researchPage = new ItemResearch();
    	GameRegistry.registerItem(researchPage, "research_page");
    	
    	textbook = new ItemRecipeTextbook();
    	GameRegistry.registerItem(textbook, "textbook");
    	
    	failedItem = new Item().setTextureName("reciperesearch:failed_item").setCreativeTab(CreativeTabs.tabMisc).setUnlocalizedName("reciperesearch.failed_item");
    	GameRegistry.registerItem(failedItem, "failed_item");
    	
    	GameRegistry.registerTileEntity(TileEntityPrototyping.class, "tile.prototyping");
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(prototypeTable), new ItemStack(Blocks.crafting_table), new ItemStack(Items.writable_book));
    	GameRegistry.addShapelessRecipe(ResearchHelper.getBook(), new ItemStack(prototypeTable), new ItemStack(Items.book));
    	GameRegistry.addShapedRecipe(new ItemStack(textbook, 1, 0), "xxx", "xox", "xxx", 'x', new ItemStack(Items.book), 'o', new ItemStack(Blocks.crafting_table));
    	GameRegistry.addShapedRecipe(new ItemStack(textbook, 1, 1), "xxx", "xox", "xxx", 'x', new ItemStack(textbook, 1, 0), 'o', new ItemStack(Blocks.crafting_table));
    	GameRegistry.addShapedRecipe(new ItemStack(textbook, 1, 2), "xxx", "xox", "xxx", 'x', new ItemStack(textbook, 1, 1), 'o', new ItemStack(Blocks.crafting_table));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	if(RR_Settings.hideRecipes && proxy.isClient())
    	{
    		RecipeHelper.HideAll();
    	}
    }
}
