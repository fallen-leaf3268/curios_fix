package com.example.curiosfix.mixin;

import com.example.curiosfix.DynamicSlotNbtRepair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;

import java.util.Map;

@Mixin(value = CurioInventoryCapability.CurioInventoryWrapper.class, remap = false)
public abstract class MixinCurioInventoryWrapper {
    @Shadow
    private Map<String, ICurioStacksHandler> curios;

    @Inject(method = "clearCachedSlotModifiers", at = @At("HEAD"), cancellable = true)
    private void curiosFix$clearCachedSlotModifiers(CallbackInfo ci) {
        ICuriosItemHandler handler = (ICuriosItemHandler) (Object) this;
        DynamicSlotNbtRepair.clearCachedSlotModifiers(this.curios, handler.getWearer());
        ci.cancel();
    }
}
