package me.gopro336.zenith.asm.mixin.mixins;

import me.gopro336.zenith.asm.mixin.imixin.ICPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Robeart 25/07/2020
 */
@Mixin(CPacketPlayerTryUseItemOnBlock.class)
public abstract class MixinCPacketPlayerTryUseItemOnBlock implements ICPacketPlayerTryUseItemOnBlock {

    @Accessor(value = "placedBlockDirection")
    public abstract void setDirection(EnumFacing placedBlockDirection);

}
