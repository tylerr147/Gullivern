package com.tyler.resize.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import com.tyler.resize.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "registerAttributes", at = @At("RETURN"), cancellable = true)
    private static void registerAttributes(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cir) {
        cir.setReturnValue(
                cir.getReturnValue()
                        .createMutableAttribute(Attributes.ENTITY_HEIGHT.get())
                        .createMutableAttribute(Attributes.ENTITY_WIDTH.get())
        );
    }
}
