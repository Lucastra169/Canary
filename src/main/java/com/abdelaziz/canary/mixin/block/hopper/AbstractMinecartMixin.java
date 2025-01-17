package com.abdelaziz.canary.mixin.block.hopper;

import com.abdelaziz.canary.common.entity.tracker.nearby.ToggleableMovementTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity {

    private Vec3 beforeMoveOnRailPos;

    public AbstractMinecartMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    private int beforeMoveOnRailNotificationMask;

    @Inject(
            method = "moveAlongTrack",
            at = @At("HEAD")
    )
    private void avoidNotifyingMovementListeners(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof Container) {
            this.beforeMoveOnRailPos = this.position();
            EntityInLevelCallback changeListener = ((EntityAccessor) this).getLevelCallback();
            if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
                this.beforeMoveOnRailNotificationMask = toggleableMovementTracker.setNotificationMask(0);
            }
        }
    }

    @Inject(
            method = "moveAlongTrack",
            at = @At("RETURN")
    )
    private void notifyMovementListeners(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof Container) {
            EntityInLevelCallback changeListener = ((EntityAccessor) this).getLevelCallback();
            if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
                this.beforeMoveOnRailNotificationMask = toggleableMovementTracker.setNotificationMask(this.beforeMoveOnRailNotificationMask);

                if (!this.beforeMoveOnRailPos.equals(this.position())) {
                    changeListener.onMove();
                }
            }
            this.beforeMoveOnRailPos = null;
        }
    }
}
