package me.dags.plots.support.voxelsniper.mixins;

import com.thevoxelbox.voxelsniper.brush.Brush;
import me.dags.plots.support.voxelsniper.mask.IMaskable;
import me.dags.plots.support.voxelsniper.mask.Mask;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author dags <dags@dags.me>
 */
@Mixin(Brush.class)
public abstract class MixinBrush implements IMaskable {

    private Mask mask = Mask.ALL;

    @Override
    public Mask getMask() {
        return this.mask;
    }

    @Override
    public void setMask(Mask mask) {
        this.mask = mask;
    }

    @Inject(method = "setBlockType", at = @At("HEAD"), cancellable = true, remap = false)
    public void setBlockType(int x, int y, int z, BlockType type, boolean flag, CallbackInfo callbackInfo) {
        if (!getMask().contains(x, y, z)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true, remap = false)
    public void setBlockState(int x, int y, int z, BlockState type, boolean flag, CallbackInfo callbackInfo) {
        if (!getMask().contains(x, y, z)) {
            callbackInfo.cancel();
        }
    }
}
