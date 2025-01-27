package me.suff.mc.regen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.suff.mc.regen.common.advancement.TriggerManager;
import me.suff.mc.regen.common.entities.TimelordEntity;
import me.suff.mc.regen.common.objects.RBlocks;
import me.suff.mc.regen.common.objects.REntities;
import me.suff.mc.regen.common.objects.RGlobalLoot;
import me.suff.mc.regen.common.objects.RItems;
import me.suff.mc.regen.common.objects.RParticles;
import me.suff.mc.regen.common.objects.RSoundSchemes;
import me.suff.mc.regen.common.objects.RSounds;
import me.suff.mc.regen.common.objects.RTiles;
import me.suff.mc.regen.common.regen.IRegen;
import me.suff.mc.regen.common.regen.RegenCap;
import me.suff.mc.regen.common.regen.RegenStorage;
import me.suff.mc.regen.common.regen.acting.ActingForwarder;
import me.suff.mc.regen.common.traits.RegenTraitRegistry;
import me.suff.mc.regen.common.world.biome.surface.RSurfaceBuilder;
import me.suff.mc.regen.common.world.gen.RStructures;
import me.suff.mc.regen.compat.TardisMod;
import me.suff.mc.regen.config.RegenConfig;
import me.suff.mc.regen.data.AdvancementGen;
import me.suff.mc.regen.data.EnglishLang;
import me.suff.mc.regen.data.LootGen;
import me.suff.mc.regen.data.RBlockLootTableGen;
import me.suff.mc.regen.data.RBlockTags;
import me.suff.mc.regen.data.RItemTags;
import me.suff.mc.regen.data.RRecipeGen;
import me.suff.mc.regen.network.NetworkDispatcher;
import me.suff.mc.regen.util.ClientUtil;
import me.suff.mc.regen.util.DownloadSkinsThread;
import me.suff.mc.regen.util.NBTRecipeIngredient;
import me.suff.mc.regen.util.PlayerUtil;
import me.suff.mc.regen.util.RConstants;
import net.minecraft.data.BiomeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("regen")
public class Regeneration {

    public static final Logger LOG = LogManager.getLogger("Regeneration");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Regeneration() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff));
        modBus.addListener(this::doCommonStuff);
        modBus.register(this);
        MinecraftForge.EVENT_BUS.register(this);
        RBlocks.BLOCKS.register(modBus);
        RSounds.SOUNDS.register(modBus);
        RItems.ITEMS.register(modBus);
        REntities.ENTITIES.register(modBus);
        
        RTiles.TILES.register(modBus);
        RStructures.Structures.STRUCTURES.register(modBus);
        RStructures.FEATURES.register(modBus);
        RParticles.TYPES.register(modBus);
        RGlobalLoot.GLM.register(modBus);
        RSurfaceBuilder.SurfaceBuilders.SURFACE_BUILDERS.register(modBus);
        RegenTraitRegistry.TRAITS.register(modBus);
        
        NetworkDispatcher.init();
        PlayerUtil.setupPotions();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RegenConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RegenConfig.COMMON_SPEC);
    }


    private void doCommonStuff(final FMLCommonSetupEvent event) {

        event.enqueueWork(() ->
        {
            RSurfaceBuilder.registerConfiguredSurfaceBuilders();
            RStructures.setupStructures();
            RStructures.ConfiguredStructures.registerConfiguredStructures();
            RStructures.registerConfiguredFeatures();
        });

        CapabilityManager.INSTANCE.register(IRegen.class, new RegenStorage(), RegenCap::new);
        ActingForwarder.init();
        GlobalEntityTypeAttributes.put(REntities.TIMELORD.get(), TimelordEntity.createAttributes().build());
        GlobalEntityTypeAttributes.put(REntities.WATCHER.get(), TimelordEntity.createAttributes().build());
        DownloadSkinsThread.setup(FMLEnvironment.dist == Dist.CLIENT);
        RSoundSchemes.init();
        TriggerManager.init();

        if(ModList.get().isLoaded("tardis")){
            TardisMod.tardis();
        }
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientUtil::doClientStuff);
    }

    @SubscribeEvent
    public void onGatherData(GatherDataEvent e) {
        DataGenerator generator = e.getGenerator();
        boolean reports = false;
        ExistingFileHelper existingFileHelper = e.getExistingFileHelper();
        generator.addProvider(new EnglishLang(generator));
        generator.addProvider(new RBlockLootTableGen(generator));
        generator.addProvider(new LootGen(generator));
        RBlockTags blockTags = new RBlockTags(generator, existingFileHelper);
        generator.addProvider(blockTags);
        generator.addProvider(new RItemTags(generator, blockTags, existingFileHelper));
        generator.addProvider(new RRecipeGen(generator));
        generator.addProvider(new AdvancementGen(generator));
        if(reports) {
            generator.addProvider(new BiomeProvider(generator));
        }
    }

    @SubscribeEvent
    public void registerRecipeSerialziers(RegistryEvent.Register< IRecipeSerializer< ? > > event) {
        CraftingHelper.register(new ResourceLocation(RConstants.MODID, "nbt"), NBTRecipeIngredient.Serializer.INSTANCE);
    }
}
