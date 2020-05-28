package net.teamfruit.gulliver.attributes;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.teamfruit.gulliver.compatibilities.sizeCap.ISizeCap;
import net.teamfruit.gulliver.compatibilities.sizeCap.SizeCapPro;
import net.teamfruit.gulliver.event.EntitySizeEvent;

public class AttributesHandler {
    @SubscribeEvent
    public void attachAttributes(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof LivingEntity) {
            final LivingEntity entity = (LivingEntity) event.getEntity();
            final AbstractAttributeMap map = entity.getAttributes();

            map.registerAttribute(Attributes.ENTITY_HEIGHT);
            map.registerAttribute(Attributes.ENTITY_WIDTH);
        }
    }

    @SubscribeEvent
    public void onEntityGetSize(EntitySizeEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        final LivingEntity entity = (LivingEntity) event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = entity.getAttribute(Attributes.ENTITY_HEIGHT).func_225505_c_().isEmpty();
            final boolean hasWidthModifier = entity.getAttribute(Attributes.ENTITY_WIDTH).func_225505_c_().isEmpty();

            final double heightAttribute = entity.getAttribute(Attributes.ENTITY_HEIGHT).getValue();
            final double widthAttribute = entity.getAttribute(Attributes.ENTITY_WIDTH).getValue();

            final EntitySize oldSize = event.getOldSize();
            float height = (float) (oldSize.height * heightAttribute);
            float width = (float) (oldSize.width * widthAttribute);

            /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
            if (hasHeightModifier != true || hasWidthModifier != true) {
                /* If the Entity Does have a Modifier get it's size before changing it's size */
                /* Handles Resizing while true */
                width = MathHelper.clamp(width, 0.15F, width);
                height = MathHelper.clamp(height, 0.25F, height);
                event.setNewSize(EntitySize.flexible(width, height));
            }
        });
    }

    @SubscribeEvent
    public void onEyeHeight(EntityEvent.EyeHeight event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        final LivingEntity player = (LivingEntity) event.getEntity();

        LazyOptional<ISizeCap> lazyCap = player.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = player.getAttribute(Attributes.ENTITY_HEIGHT).func_225505_c_().isEmpty();
            final boolean hasWidthModifier = player.getAttribute(Attributes.ENTITY_WIDTH).func_225505_c_().isEmpty();

            final double heightAttribute = player.getAttribute(Attributes.ENTITY_HEIGHT).getValue();
            final double widthAttribute = player.getAttribute(Attributes.ENTITY_WIDTH).getValue();
            float height = (float) (cap.getDefaultHeight() * heightAttribute);
            float width = (float) (cap.getDefaultWidth() * widthAttribute);

            if (cap.getTrans() == true) {
                /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
                if (hasHeightModifier != true || hasWidthModifier != true) {
                    /* If the Entity Does have a Modifier get it's size before changing it's size */
                    /* Handles Resizing while true */
                    float eyeHeight = (float) (event.getOldHeight() * heightAttribute);
                    eyeHeight = (height >= 1.6F) ? eyeHeight : (eyeHeight * 0.9876542F);
                    event.setNewHeight(eyeHeight);
                } else /* If the Entity Does not have any Modifiers */ {
                    /* Returned the Entities Size Back to Normal */
                    player.eyeHeight = event.getOldHeight() * 0.85F;
                }
            }
        });
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        final LivingEntity entity = event.getEntityLiving();
        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = entity.getAttribute(Attributes.ENTITY_HEIGHT).func_225505_c_().isEmpty();
            final boolean hasWidthModifier = entity.getAttribute(Attributes.ENTITY_WIDTH).func_225505_c_().isEmpty();
            final double heightAttribute = entity.getAttribute(Attributes.ENTITY_HEIGHT).getValue();
            final double widthAttribute = entity.getAttribute(Attributes.ENTITY_WIDTH).getValue();
            float height = (float) (cap.getDefaultHeight() * heightAttribute);
            float width = (float) (cap.getDefaultWidth() * widthAttribute);

            /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
            if (hasHeightModifier != true || hasWidthModifier != true) {
                /* If the Entity Does have a Modifier get it's size before changing it's size */
                if (cap.getTrans() != true) {
                    cap.setDefaultHeight(entity.getHeight());
                    cap.setDefaultWidth(entity.getWidth());
                    cap.setTrans(true);
                }

                /* Handles Resizing while true */
                if (cap.getTrans() == true) {
                    width = MathHelper.clamp(width, 0.04F, width);
                    height = MathHelper.clamp(height, 0.08F, height);
                    entity.recalculateSize();
                }
            } else /* If the Entity Does not have any Modifiers */ {
                /* Returned the Entities Size Back to Normal */
                if (cap.getTrans() == true) {
                    entity.recalculateSize();
                    cap.setTrans(false);
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onEntityRenderPre(RenderLivingEvent.Pre event) {
        final LivingEntity entity = event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            if (cap.getTrans() == true) {
                float scaleHeight = entity.getHeight() / cap.getDefaultHeight();
                float scaleWidth = entity.getWidth() / cap.getDefaultWidth();

                if (entity instanceof PlayerEntity) {
                    if (entity.getRidingEntity() instanceof AbstractHorseEntity) {
                        event.getMatrixStack().translate(0, (1 - scaleHeight) * .62F, 0);
                    }
                }

                event.getMatrixStack().push();
                event.getMatrixStack().scale(scaleWidth, scaleHeight, scaleWidth);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onLivingRenderPost(RenderLivingEvent.Post event) {
        final LivingEntity entity = event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            if (cap.getTrans() == true) {
                event.getMatrixStack().pop();
            }
        });
    }
}
