package me.suff.mc.regen.common.regen.transitions;

import me.suff.mc.regen.client.rendering.transitions.FieryTransitionRenderer;
import me.suff.mc.regen.common.objects.RSounds;
import me.suff.mc.regen.common.regen.IRegen;
import me.suff.mc.regen.config.RegenConfig;
import me.suff.mc.regen.network.NetworkDispatcher;
import me.suff.mc.regen.network.messages.POVMessage;
import me.suff.mc.regen.util.PlayerUtil;
import me.suff.mc.regen.util.RConstants;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Iterator;

public class FieryTransition implements TransitionType< FieryTransitionRenderer > {


    @Override
    public void onUpdateMidRegen(IRegen capability) {

        LivingEntity livingEntity = capability.getLiving();
        livingEntity.clearFire();

        if (!livingEntity.level.isClientSide) {
            if (capability.getLiving() instanceof ServerPlayerEntity) {
                NetworkDispatcher.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) capability.getLiving()), new POVMessage(RConstants.THIRD_PERSON_FRONT));
            }
        }

        if (livingEntity.level.isClientSide) return;

        if (livingEntity.getType() == EntityType.PLAYER) {
            BlockPos livingPos = new BlockPos(livingEntity.position());
            if (livingEntity.level.getBlockState(livingPos).getBlock() instanceof FireBlock)
                livingEntity.level.removeBlock(livingPos, false);


            PlayerUtil.regenerationExplosion(livingEntity);
            double x = livingEntity.getX() + livingEntity.getRandom().nextGaussian() * 2;
            double y = livingEntity.getY() + 0.5 + livingEntity.getRandom().nextGaussian() * 2;
            double z = livingEntity.getZ() + livingEntity.getRandom().nextGaussian() * 2;
            if (!PlayerUtil.isPlayerAboveZeroGrid(livingEntity)) {
                livingEntity.level.explode(livingEntity, x, y, z, 0.1F, RegenConfig.COMMON.fieryRegen.get(), Explosion.Mode.NONE);
            }
            Iterator< BlockPos > iterator = BlockPos.betweenClosedStream(new BlockPos(livingEntity.position()).north().west(), new BlockPos(livingEntity.position()).south().east()).iterator();
            while (iterator.hasNext()) {
                iterator.forEachRemaining((blockPos -> {
                    if (livingEntity.level.getBlockState(blockPos).getBlock() instanceof FireBlock) {
                        livingEntity.level.removeBlock(blockPos, false);
                    }
                }));
            }
        }

    }

    @Override
    public void onFinishRegeneration(IRegen capability) {
        if (capability.getLiving() instanceof ServerPlayerEntity) {
            NetworkDispatcher.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) capability.getLiving()), new POVMessage(RConstants.FIRST_PERSON));
        }
        capability.setUpdateTicks(0);
        capability.syncToClients(null);
    }

    @Override
    public int getAnimationLength() {
        return 280; // 14 seconds of 20 ticks
    }

    @Override
    public SoundEvent[] getRegeneratingSounds() {
        return new SoundEvent[]{RSounds.REGENERATION_0.get(), RSounds.REGENERATION_1.get(), RSounds.REGENERATION_2.get(), RSounds.REGENERATION_3.get(), RSounds.REGENERATION_4.get(), RSounds.REGENERATION_5.get(), RSounds.REGENERATION_6.get(), RSounds.REGENERATION_7.get()};
    }

    @Override
    public Vector3d getDefaultPrimaryColor() {
        return new Vector3d(0.69411767f, 0.74509805f, 0.23529412f);
    }

    @Override
    public Vector3d getDefaultSecondaryColor() {
        return new Vector3d(0.7137255f, 0.75686276f,0.25490198f);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(RConstants.MODID, "fiery");
    }

    @Override
    public FieryTransitionRenderer getRenderer() {
        return FieryTransitionRenderer.INSTANCE;
    }

}
