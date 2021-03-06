package kittehmod.morecraft;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import kittehmod.morecraft.block.ModBlocks;
import kittehmod.morecraft.client.ClientRenderSetup;
import kittehmod.morecraft.client.gui.KilnScreen;
import kittehmod.morecraft.container.ModContainerType;
import kittehmod.morecraft.entity.ModEntities;
import kittehmod.morecraft.entity.ai.CatsSitOnChestsHandler;
import kittehmod.morecraft.entity.ai.ModPointOfInterestType;
import kittehmod.morecraft.item.ModItems;
import kittehmod.morecraft.item.ModPotions;
import kittehmod.morecraft.item.crafting.ModBrewingRecipes;
import kittehmod.morecraft.item.crafting.ModRecipes;
import kittehmod.morecraft.item.crafting.conditions.QuarkFlagRecipeCondition;
import kittehmod.morecraft.network.MorecraftPacketHandler;
import kittehmod.morecraft.tileentity.ModTileEntityType;
import kittehmod.morecraft.worldgen.ModBiomes;
import kittehmod.morecraft.worldgen.ModFeatures;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(MoreCraft.MODID)
public class MoreCraft 
{
    public static final String MODID = "morecraft";
    
    //public static Logger LOGGER = LogManager.getLogger(MODID);
    
	public MoreCraft()
    {
    	ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModTileEntityType.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModPotions.POTION_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModFeatures.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModBiomes.BIOMES.register(FMLJavaModLoadingContext.get().getModEventBus());
    	ModContainerType.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
     	ModRecipes.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
   		ModPointOfInterestType.POINTS_OF_INTERESTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient); });
    }
    
    private void setupCommon(final FMLCommonSetupEvent event)
    {	
    	ModBrewingRecipes.registerRecipes();
    	ModFeatures.setupFeatureConfigs();
    	
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MoreCraftConfig.COMMON_CONFIG);
        MoreCraftConfig.loadConfig(MoreCraftConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("morecraft.toml"));
    	
        MorecraftPacketHandler.register();
        
        CraftingHelper.register(new QuarkFlagRecipeCondition.Serializer());
        
    	MinecraftForge.EVENT_BUS.register(new MobEvents());
    	MinecraftForge.EVENT_BUS.register(new PlayerEvents());
    	MinecraftForge.EVENT_BUS.register(new CatsSitOnChestsHandler());
    	MinecraftForge.EVENT_BUS.register(new ModFeatures());
    	MinecraftForge.EVENT_BUS.register(new ModBiomes());
    	
    	ComposterBlock.COMPOSTABLES.put(Items.POISONOUS_POTATO, 0.65F); //Fixes the annoyance.
    	ComposterBlock.COMPOSTABLES.put(ModItems.SWEETBERRY_PIE.get(), 1.0F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.APPLE_PIE.get(), 1.0F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.NETHER_APPLE.get(), 0.65F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.NETHER_APPLE_PIE.get(), 1.0F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.NETHERWOOD_LEAVES.get(), 0.3F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.NETHERWOOD_SAPLING.get(), 0.3F);
    	ComposterBlock.COMPOSTABLES.put(ModItems.NETHERWOOD_LEAF_CARPET.get(), 0.3F);
    }
    
    @OnlyIn(Dist.CLIENT)
	private void setupClient(final FMLClientSetupEvent event)
    {
		ClientRenderSetup.setup();
		ScreenManager.register(ModContainerType.KILN.get(), KilnScreen::new);
    }
    
    /* Dunno what I'll do with this. Maybe later.
    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    */
}