package me.fril.regeneration.common.dna.positive;

import me.fril.regeneration.common.capability.IRegeneration;
import me.fril.regeneration.common.dna.DnaHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class DnaFireResistant implements DnaHandler.IDna {
	
	@Override
	public void onUpdate(IRegeneration cap) {
		EntityPlayer player = cap.getPlayer();
		if (player.isBurning() && cap.dnaAlive()) {
			player.extinguish();
		}
	}
	
	@Override
	public void onAdded(IRegeneration cap) {
	
	}
	
	@Override
	public void onRemoved(IRegeneration cap) {
	
	}
	
	@Override
	public String getLangKey() {
		return null;
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return null;
	}
}
