package me.fril.regeneration.proxy;

import me.fril.regeneration.client.RegenKeyBinds;
import me.fril.regeneration.client.gui.InventoryTabRegeneration;
import me.fril.regeneration.client.rendering.LayerFuzz;
import me.fril.regeneration.client.rendering.LayerItemReplace;
import me.fril.regeneration.client.rendering.LayerRegeneration;
import me.fril.regeneration.client.rendering.entity.RenderItemOverride;
import me.fril.regeneration.client.rendering.entity.RenderLindos;
import me.fril.regeneration.client.skinhandling.SkinChangingHandler;
import me.fril.regeneration.common.entity.EntityItemOverride;
import me.fril.regeneration.common.entity.EntityLindos;
import me.fril.regeneration.compat.lucraft.LucraftCoreHandler;
import me.fril.regeneration.util.EnumCompatModids;
import me.fril.regeneration.util.RenderUtil;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.List;
import java.util.Map;

/**
 * Created by Sub
 * on 17/09/2018.
 */
public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(new SkinChangingHandler());
		RenderingRegistry.registerEntityRenderingHandler(EntityItemOverride.class, RenderItemOverride::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLindos.class, RenderLindos::new);
	}
	
	@Override
	public void init() {
		super.init();
		
		// Galacticraft API for TABS ======================
		if (TabRegistry.getTabList().isEmpty()) {
			MinecraftForge.EVENT_BUS.register(new TabRegistry());
			TabRegistry.registerTab(new InventoryTabVanilla());
		}
		TabRegistry.registerTab(new InventoryTabRegeneration());
		
		// LC Core
		if (EnumCompatModids.LCCORE.isLoaded()) {
			LucraftCoreHandler.registerEntry();
		}
		
		// Render layers ===========================================
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		for (RenderPlayer renderPlayer : skinMap.values()) {
			renderPlayer.addLayer(new LayerRegeneration(renderPlayer)); // Add Regeneration Layer
			
			List<LayerRenderer<AbstractClientPlayer>> list = renderPlayer.layerRenderers;
			list.removeIf(layer -> layer.getClass() == LayerHeldItem.class);
			
			renderPlayer.addLayer(new LayerItemReplace(renderPlayer)); // Add new item layer
			renderPlayer.addLayer(new LayerFuzz(renderPlayer));
		}
		
	}
	
	@Override
	public void postInit() {
		super.postInit();
		RegenKeyBinds.init();
		
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		for (RenderPlayer renderPlayer : skinMap.values()) {
			RenderUtil.setupArmorModelOverride(renderPlayer);
		}
		SkinChangingHandler.registerResources();
	}
	
}
