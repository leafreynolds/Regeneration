package me.fril.regeneration.client.gui;

import java.awt.Color;

import javax.annotation.Nullable;

import me.fril.regeneration.RegenerationMod;
import me.fril.regeneration.common.capability.CapabilityRegeneration;
import me.fril.regeneration.common.capability.IRegeneration;
import me.fril.regeneration.network.MessageRegenerationStyle;
import me.fril.regeneration.network.NetworkHandler;
import me.fril.regeneration.util.RenderUtil;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;

public class CustomizerGui extends GuiContainer {
	public static final int ID = 0;
	
	private static final ResourceLocation background = new ResourceLocation(RegenerationMod.MODID, "textures/gui/customizer_background.png");
	
	private GuiButtonExt btnDefault, btnReset;
	private GuiColorSlider slidePrimaryRed, slidePrimaryGreen, slidePrimaryBlue, slideSecondaryRed, slideSecondaryGreen, slideSecondaryBlue;
	
	private Color initialPrimary, initialSecondary;

    public CustomizerGui() {
        super(new BlankContainer());
        xSize = 176;
        ySize = 166;
    }
    
    @Override
    public void initGui() {
    	super.initGui();
    	TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabRegeneration.class);
        TabRegistry.addTabsToList(this.buttonList);
        
        int cx = (width - xSize) / 2;
		int cy = (height - ySize) / 2;
		
		IRegeneration cap = CapabilityRegeneration.getForPlayer(mc.player);
		initialPrimary = cap.getPrimaryColor();
		initialSecondary = cap.getSecondaryColor();
		
		float primaryRed = initialPrimary.getRed() / 255F, primaryGreen = initialPrimary.getGreen() / 255F, primaryBlue = initialPrimary.getBlue() / 255F;
		float secondaryRed = initialSecondary.getRed() / 255F, secondaryGreen = initialSecondary.getGreen() / 255F, secondaryBlue = initialSecondary.getBlue() / 255F;
		
		final int btnW = 50, btnH = 18;
		final int sliderW = 70, sliderH = 20;
		
		btnReset   = new GuiButtonExt(1, cx + 34, cy + 135, btnW, btnH, new TextComponentTranslation("regeneration.info.undo").getFormattedText());
		btnDefault = new GuiButtonExt(2, cx + 90, cy + 135, btnW, btnH, new TextComponentTranslation("regeneration.info.default").getFormattedText());
		
		btnReset.enabled = false;
		buttonList.add(btnReset);
		buttonList.add(btnDefault);
		
		slidePrimaryRed     = new GuiColorSlider(3, cx + 10, cy + 65,  sliderW, sliderH, new TextComponentTranslation("regeneration.info.red").getFormattedText(), "", 0, 1, primaryRed, true, true, this::onChangeSliderValue);
		slidePrimaryGreen   = new GuiColorSlider(4, cx + 10, cy + 84,  sliderW, sliderH, new TextComponentTranslation("regeneration.info.green").getFormattedText(), "", 0, 1, primaryGreen, true, true, this::onChangeSliderValue);
		slidePrimaryBlue    = new GuiColorSlider(5, cx + 10, cy + 103, sliderW, sliderH, new TextComponentTranslation("regeneration.info.blue").getFormattedText(), "", 0, 1, primaryBlue, true, true, this::onChangeSliderValue);
		
		slideSecondaryRed   = new GuiColorSlider(7, cx + 96, cy + 65,  sliderW, sliderH, new TextComponentTranslation("regeneration.info.red").getFormattedText(), "", 0, 1, secondaryRed, true, true, this::onChangeSliderValue);
		slideSecondaryGreen = new GuiColorSlider(8, cx + 96, cy + 84,  sliderW, sliderH, new TextComponentTranslation("regeneration.info.green").getFormattedText(), "", 0, 1, secondaryGreen, true, true, this::onChangeSliderValue);
		slideSecondaryBlue  = new GuiColorSlider(9, cx + 96, cy + 103, sliderW, sliderH, new TextComponentTranslation("regeneration.info.blue").getFormattedText(), "", 0, 1, secondaryBlue, true, true, this::onChangeSliderValue);
		
		buttonList.add(slidePrimaryRed);
		buttonList.add(slidePrimaryGreen);
		buttonList.add(slidePrimaryBlue);
		
		buttonList.add(slideSecondaryRed);
		buttonList.add(slideSecondaryGreen);
		buttonList.add(slideSecondaryBlue);
    }
    
    private void onChangeSliderValue(@Nullable GuiSlider slider) {
    	btnReset.enabled = true;
    	sendStyleNBTTagToServer();
    }
    
    private void sendStyleNBTTagToServer() {
    	NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("PrimaryRed",   (float) slidePrimaryRed.getValue());
		nbt.setFloat("PrimaryGreen", (float) slidePrimaryGreen.getValue());
		nbt.setFloat("PrimaryBlue",  (float) slidePrimaryBlue.getValue());
		
		nbt.setFloat("SecondaryRed",   (float) slideSecondaryRed.getValue());
		nbt.setFloat("SecondaryGreen", (float) slideSecondaryGreen.getValue());
		nbt.setFloat("SecondaryBlue",  (float) slideSecondaryBlue.getValue());
		
		NetworkHandler.INSTANCE.sendToServer(new MessageRegenerationStyle(nbt));
	}
    
    @Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == btnReset.id) {
			slidePrimaryRed.setValue(initialPrimary.getRed() / 255F);
			slidePrimaryGreen.setValue(initialPrimary.getGreen() / 255F);
			slidePrimaryBlue.setValue(initialPrimary.getBlue() / 255F);
			
			slideSecondaryRed.setValue(initialSecondary.getRed() / 255F);
			slideSecondaryGreen.setValue(initialSecondary.getGreen() / 255F);
			slideSecondaryBlue.setValue(initialSecondary.getBlue() / 255F);
			
			btnReset.enabled = false;
		} else if (button.id == btnDefault.id) {
			slidePrimaryRed.setValue(0.93F);
			slidePrimaryGreen.setValue(0.61F);
			slidePrimaryBlue.setValue(0F);
			
			slideSecondaryRed.setValue(1F);
			slideSecondaryGreen.setValue(0.11F);
			slideSecondaryBlue.setValue(0.18F);
			
			onChangeSliderValue(null);
		}
	}

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
        int cx = (width - xSize) / 2;
		int cy = (height - ySize) / 2;
        
		RenderUtil.drawRect(cx + 10,  cy + 44, cx + 81, cy + 61, 0.1F, 0.1F, 0.1F, 1);
        RenderUtil.drawRect(cx + 11,  cy + 45, cx + 80, cy + 60, (float) slidePrimaryRed.getValue(),   (float) slidePrimaryGreen.getValue(),   (float) slidePrimaryBlue.getValue(), 1);
        
        RenderUtil.drawRect(cx + 95, cy + 44, cx + 166, cy + 61, 0.1F, 0.1F, 0.1F, 1);
		RenderUtil.drawRect(cx + 96, cy + 45, cx + 165, cy + 60, (float) slideSecondaryRed.getValue(), (float) slideSecondaryGreen.getValue(), (float) slideSecondaryBlue.getValue(), 1);
		
		
		Color primaryColor = new Color((float) slidePrimaryRed.getValue(),   (float) slidePrimaryGreen.getValue(),   (float) slidePrimaryBlue.getValue()),
				secondaryColor = new Color((float) slideSecondaryRed.getValue(),   (float) slideSecondaryGreen.getValue(),   (float) slideSecondaryBlue.getValue());
		
		String str = new TextComponentTranslation("regeneration.info.primary").getFormattedText();
		int length = mc.fontRenderer.getStringWidth(str);
		fontRenderer.drawString(str, cx + 45 - length / 2, cy + 49, RenderUtil.calculateColorBrightness(primaryColor) > 0.179 ? 0x0 : 0xFFFFFF);
		
		str = new TextComponentTranslation("regeneration.info.secondary").getFormattedText();
		length = mc.fontRenderer.getStringWidth(str);
		fontRenderer.drawString(str, cx + 131 - length / 2, cy + 49, RenderUtil.calculateColorBrightness(secondaryColor) > 0.179 ? 0x0 : 0xFFFFFF);
		
		str = new TextComponentTranslation("regeneration.messages.remaining_regens.status").getFormattedText() + " " + CapabilityRegeneration.getForPlayer(Minecraft.getMinecraft().player).getLivesLeft();
		length = mc.fontRenderer.getStringWidth(str);
		fontRenderer.drawString(str, cx + 86 - length / 2, cy + 21, Color.DARK_GRAY.getRGB());
    }
    
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
    
}
