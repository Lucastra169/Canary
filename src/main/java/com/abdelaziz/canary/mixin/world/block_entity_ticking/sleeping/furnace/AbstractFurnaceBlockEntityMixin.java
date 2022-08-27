package com.abdelaziz.canary.mixin.world.block_entity_ticking.sleeping.furnace;

import com.abdelaziz.canary.common.block.entity.SleepingBlockEntity;
import com.abdelaziz.canary.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    @Shadow
    protected abstract boolean isBurning();

    @Shadow
    int cookTime;
    private WrappedBlockEntityTickInvokerAccessor tickWrapper = null;
    private TickingBlockEntity sleepingTicker = null;

    public AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public WrappedBlockEntityTickInvokerAccessor getTickWrapper() {
        return tickWrapper;
    }

    @Override
    public void setTickWrapper(WrappedBlockEntityTickInvokerAccessor tickWrapper) {
        this.tickWrapper = tickWrapper;
        this.setSleepingTicker(null);
    }

    @Override
    public TickingBlockEntity getSleepingTicker() {
        return sleepingTicker;
    }

    @Override
    public void setSleepingTicker(TickingBlockEntity sleepingTicker) {
        this.sleepingTicker = sleepingTicker;
    }

    @Inject(method = "tick", at = @At("RETURN" ))
    private static void checkSleep(Level world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        ((AbstractFurnaceBlockEntityMixin) (Object) blockEntity).checkSleep();
    }

    private void checkSleep() {
        if (!this.isBurning() && this.cookTime == 0 && this.level != null) {
            this.startSleeping();
        }
    }

    @Inject(method = "readNbt", at = @At("RETURN" ))
    private void wakeUpAfterFromTag(CallbackInfo ci) {
        if (this.isSleeping() && this.level != null && !this.level.isClientSide) {
            this.wakeUpNow();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.isSleeping() && this.level != null && !this.level.isClientSide) {
            this.wakeUpNow();
        }
    }

}