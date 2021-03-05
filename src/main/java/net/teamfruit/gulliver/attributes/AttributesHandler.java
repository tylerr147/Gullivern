package net.teamfruit.gulliver.attributes;

import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
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
    public void onEntityGetSize(EntitySizeEvent event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        final LivingEntity entity = (LivingEntity) event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = entity.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_HEIGHT.get());
            final boolean hasWidthModifier = entity.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_WIDTH.get());

            final double heightAttribute = entity.getAttribute(Attributes.ENTITY_HEIGHT.get()) == null ? 1 : entity.getAttributeValue(Attributes.ENTITY_HEIGHT.get());
            final double widthAttribute = entity.getAttribute(Attributes.ENTITY_WIDTH.get()) == null ? 1 : entity.getAttributeValue(Attributes.ENTITY_WIDTH.get());

            final EntitySize oldSize = event.getOldSize();
            float height = (float) (oldSize.height * heightAttribute);
            float width = (float) (oldSize.width * widthAttribute);

            /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
            if (hasHeightModifier || hasWidthModifier) {
                /* If the Entity Does have a Modifier get it's size before changing it's size */
                /* Handles Resizing while true */
                width = MathHelper.clamp(width, 0.005F, width);
                height = MathHelper.clamp(height, 0.025F, height);
                event.setNewSize(EntitySize.flexible(width, height));
            }
        });
    }

    @SubscribeEvent
    public void onEyeHeight(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        final LivingEntity player = (LivingEntity) event.getEntity();

        LazyOptional<ISizeCap> lazyCap = player.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = player.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_HEIGHT.get());
            final boolean hasWidthModifier = player.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_WIDTH.get());

            final double heightAttribute = player.getAttribute(Attributes.ENTITY_HEIGHT.get()) == null ? 1 : player.getAttributeValue(Attributes.ENTITY_HEIGHT.get());
            final double widthAttribute = player.getAttribute(Attributes.ENTITY_WIDTH.get()) == null ? 1 : player.getAttributeValue(Attributes.ENTITY_WIDTH.get());
            float height = (float) (cap.getDefaultHeight() * heightAttribute);
            float width = (float) (cap.getDefaultWidth() * widthAttribute);

            if (cap.getTrans()) {
                /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
                if (hasHeightModifier || hasWidthModifier) {
                    /* If the Entity Does have a Modifier get it's size before changing it's size */
                    /* Handles Resizing while true */
                    float eyeHeight = (float) (event.getOldEyeHeight() * heightAttribute);
                    eyeHeight = (height >= 1.6F) ? eyeHeight : (eyeHeight * 0.9876542F);
                    event.setNewEyeHeight(eyeHeight);
                } else /* If the Entity Does not have any Modifiers */ {
                    /* Returned the Entities Size Back to Normal */
                    player.eyeHeight = event.getOldEyeHeight() * 0.85F;
                }
            }
        });
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        final LivingEntity entity = event.getEntityLiving();
        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            final boolean hasHeightModifier = entity.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_HEIGHT.get());
            final boolean hasWidthModifier = entity.getAttributeManager().hasAttributeInstance(Attributes.ENTITY_WIDTH.get());

            /* Makes Sure to only Run the Code IF the Entity Has Modifiers */
            if (hasHeightModifier || hasWidthModifier) {
                /* If the Entity Does have a Modifier get it's size before changing it's size */
                if (!cap.getTrans()) {
                    cap.setDefaultHeight(entity.getHeight());
                    cap.setDefaultWidth(entity.getWidth());
                    cap.setTrans(true);
                }

                /* Handles Resizing while true */
                else {
                    entity.recalculateSize();
                }
            } else /* If the Entity Does not have any Modifiers */ {
                /* Returned the Entities Size Back to Normal */
                if (cap.getTrans()) {
                    entity.recalculateSize();
                    cap.setTrans(false);
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onEntityRenderPre(RenderLivingEvent.Pre<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        final LivingEntity entity = event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);

        if (entity.getRidingEntity() instanceof LivingEntity) {
            LivingEntity entityOther = (LivingEntity) entity.getRidingEntity();
            LazyOptional<ISizeCap> lazyCapOther = entityOther.getCapability(SizeCapPro.sizeCapability);

            float scaleHeight = lazyCap.filter(ISizeCap::getTrans).map(e -> entity.getHeight() / e.getDefaultHeight()).orElse(0f);
            float scaleHeightOther = lazyCapOther.filter(ISizeCap::getTrans).map(e -> entityOther.getHeight() / e.getDefaultHeight()).orElse(0f);
            float diff = scaleHeight - scaleHeightOther;
            event.getMatrixStack().translate(0, (1 - diff) * .42F, 0);
        }

        lazyCap.ifPresent(cap -> {
            if (cap.getTrans()) {
                float scaleHeight = entity.getHeight() / cap.getDefaultHeight();
                float scaleWidth = entity.getWidth() / cap.getDefaultWidth();

                event.getMatrixStack().push();
                event.getMatrixStack().scale(scaleWidth, scaleHeight, scaleWidth);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onLivingRenderPost(RenderLivingEvent.Post<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        final LivingEntity entity = event.getEntity();

        LazyOptional<ISizeCap> lazyCap = entity.getCapability(SizeCapPro.sizeCapability);
        lazyCap.ifPresent(cap -> {
            if (cap.getTrans()) {
                event.getMatrixStack().pop();
            }
        });
    }
}
