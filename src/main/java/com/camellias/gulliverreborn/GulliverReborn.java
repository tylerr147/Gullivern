package com.camellias.gulliverreborn;

import com.artemis.artemislib.attributes.AttributesHandler;
import com.artemis.artemislib.compatibilities.Capabilities;
import com.artemis.artemislib.compatibilities.CapabilitiesHandler;
import com.artemis.artemislib.compatibilities.sizeCap.ISizeCap;
import com.artemis.artemislib.compatibilities.sizeCap.SizeCapPro;
import net.minecraft.block.*;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GulliverReborn.MODID)
public class GulliverReborn {
    public static final String MODID = "gulliver";
    public static final String NAME = "Gulliver-1.15.2";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public GulliverReborn() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigEvent);
    }

    public static DamageSource causeCrushingDamage(LivingEntity entity) {
        return new EntityDamageSource(MODID + ".crushing", entity);
    }

    public void preInit(FMLCommonSetupEvent event) {
        Capabilities.init();
        MinecraftForge.EVENT_BUS.register(new CapabilitiesHandler());
        MinecraftForge.EVENT_BUS.register(new AttributesHandler());
    }

    public void onConfigEvent(ModConfig.Loading event) {
        // Config.SPEC.setConfig(event.getConfig().getConfigData());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        new GulliverCommands().register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (Config.FEATURE.SCALED_FALL_DAMAGE.get())
                event.setDistance(event.getDistance() / (player.getHeight() * 0.6F));
            if (player.getHeight() < 0.45F) event.setDistance(0);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = event.getEntityLiving().world;

        for (LivingEntity entities : world.getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox())) {
            if (!entity.isSneaking() && Config.FEATURE.GIANTS_CRUSH_ENTITIES.get()) {
                if (entity.getHeight() / entities.getHeight() >= 4 && entities.getRidingEntity() != entity) {
                    entities.attackEntityFrom(causeCrushingDamage(entity), entity.getHeight() - entities.getHeight());
                }
            }
        }
    }

    @SubscribeEvent
    public void onTargetEntity(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof PlayerEntity && event.getEntityLiving() instanceof MobEntity && Config.FEATURE.SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS.get()) {
            PlayerEntity player = (PlayerEntity) event.getTarget();
            MobEntity entity = (MobEntity) event.getEntityLiving();

            if (!(entity instanceof SpiderEntity || entity instanceof OcelotEntity)) {
                if (player.getHeight() <= 0.45F) {
                    entity.setAttackTarget(null);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = event.player.world;

        player.stepHeight = player.getHeight() / 3F;
        player.jumpMovementFactor *= (player.getHeight() / 1.8F);

        if (player.getHeight() < 0.9F) {
            BlockPos pos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            float ratio = (player.getHeight() / 1.8F) / 2;

			/*
			if(block instanceof BlockRedFlower
				|| state == Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.ROSE)
				&& Config.ROSES_HURT)
			{
				player.attackEntityFrom(DamageSource.CACTUS, 1);
			}
			 */

            if (!player.abilities.isFlying
                    && Config.FEATURE.PLANTS_SLOW_SMALL_DOWN.get()
                    && (block instanceof BushBlock)
                    || (block instanceof CarpetBlock)
                    || (block instanceof FlowerBlock)
                    || (block instanceof SugarCaneBlock)
                    || (block instanceof SnowBlock)
                    || (block instanceof WebBlock)
                    || (block instanceof SoulSandBlock)) {
                player.setMotion(player.getMotion().mul(ratio, (block instanceof WebBlock) ? ratio : 1d, ratio));
            }
        }

        if (player.getHeight() <= 0.45F) {
            Direction facing = player.getHorizontalFacing();
            BlockPos pos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
            BlockState state = world.getBlockState(pos.add(0, 0, 0).offset(facing));
            Block block = state.getBlock();
            boolean canPass = state.allowsMovement(world, pos.offset(facing), PathType.LAND);

            if (ClimbingHandler.canClimb(player, facing)
                    && Config.FEATURE.CLIMB_SOME_BLOCKS.get()
                    && (block == Blocks.DIRT)
                    || (block instanceof GrassBlock)
                    || (block instanceof MyceliumBlock)
                    || (block instanceof LeavesBlock)
                    || (block instanceof SandBlock)
                    || (block instanceof SoulSandBlock)
                    || (block instanceof ConcretePowderBlock)
                    || (block instanceof FarmlandBlock)
                    || (block instanceof GrassPathBlock)
                    || (block instanceof GravelBlock)
                    || (block == Blocks.CLAY)) {
                if (player.collidedHorizontally) {
                    if (!player.isSneaking()) {
                        Vec3d motion = player.getMotion();
                        player.setMotion(motion.x, 0.1D, motion.z);
                    }

                    if (player.isSneaking()) {
                        Vec3d motion = player.getMotion();
                        player.setMotion(motion.x, 0.0D, motion.z);
                    }
                }
            }

            for (ItemStack stack : player.getHeldEquipment()) {
                if (stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.getItemFromBlock(Blocks.SLIME_BLOCK) && Config.FEATURE.CLIMB_WITH_SLIME.get()) {
                    if (ClimbingHandler.canClimb(player, facing)) {
                        if (player.collidedHorizontally) {
                            if (!player.isSneaking()) {
                                Vec3d motion = player.getMotion();
                                player.setMotion(motion.x, 0.1D, motion.z);
                            }

                            if (player.isSneaking()) {
                                Vec3d motion = player.getMotion();
                                player.setMotion(motion.x, 0.0D, motion.z);
                            }
                        }
                    }
                }

                if (stack.getItem() == Items.PAPER && Config.FEATURE.GLIDE_WITH_PAPER.get()) {
                    if (!player.onGround) {
                        player.jumpMovementFactor = 0.02F * 1.75F;
                        player.fallDistance = 0;

                        Vec3d motion = player.getMotion();
                        if (motion.getY() < 0D) {
                            player.setMotion(motion.mul(1, 0.6D, 1));
                        }

                        if (player.isSneaking()) {
                            player.jumpMovementFactor *= 3.50F;
                        }

                        for (double blockY = player.getPosY(); !player.isSneaking() &&
                                ((world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.AIR) ||
                                        (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.LAVA) ||
                                        (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.FIRE) ||
                                        (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.FURNACE) ||
                                        (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.MAGMA_BLOCK)) &&
                                player.getPosY() - blockY < 25;
                             blockY--) {
                            if ((world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.LAVA) ||
                                    (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.FIRE) ||
                                    (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.FURNACE) ||
                                    (world.getBlockState(new BlockPos(player.getPosX(), blockY, player.getPosZ())).getBlock() == Blocks.MAGMA_BLOCK) &&
                                            Config.FEATURE.HOT_BLOCKS_GIVE_LIFT.get()) {
                                player.setMotion(player.getMotion().add(0, MathHelper.clamp(0.07D, Double.MIN_VALUE, 0.1D), 0));
                            }
                        }
                    }
                }
            }
        }

        if (player.isBeingRidden() && player.isSneaking()) {
            for (Entity entity : player.getPassengers()) {
                entity.stopRiding();
                entity.addVelocity(0, -1, 0);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getTarget();
            PlayerEntity player = event.getPlayer();

            if (target.getHeight() / 2 >= player.getHeight() && Config.FEATURE.RIDE_BIG_ENTITIES.get()) {
                for (ItemStack stack : player.getHeldEquipment()) {
                    if (stack.getItem() == Items.STRING) {
                        player.startRiding(target);
                    }
                }
            }

            if (target.getHeight() * 2 <= player.getHeight() && Config.FEATURE.PICKUP_SMALL_ENTITIES.get()) {
                target.startRiding(player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && Config.MODIFIER.JUMP_MODIFIER.get()) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            float jumpHeight = (player.getHeight() / 1.8F);

            jumpHeight = MathHelper.clamp(jumpHeight, 0.65F, jumpHeight);
            player.setMotion(player.getMotion().mul(1, jumpHeight, 1));

            if (player.isSneaking() || player.isSprinting()) {
                if (player.getHeight() < 1.8F) {
                    Vec3d motion = player.getMotion();
                    player.setMotion(motion.x, 0.42F, motion.z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHarvest(BreakSpeed event) {
        PlayerEntity player = event.getPlayer();

        if (Config.MODIFIER.HARVEST_MODIFIER.get())
            event.setNewSpeed(event.getOriginalSpeed() * (player.getHeight() / 1.8F));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFOVChange(FOVUpdateEvent event) {
        if (event.getEntity() != null) {
            PlayerEntity player = event.getEntity();
            GameSettings settings = Minecraft.getInstance().gameSettings;
            EffectInstance speed = player.getActivePotionEffect(Effects.SPEED);
            float fov = (float) settings.fov;

            if (player.isSprinting()) {
                event.setNewfov(speed != null ? fov + ((0.1F * (speed.getAmplifier() + 1)) + 0.15F) : fov + 0.1F);
            } else {
                event.setNewfov(speed != null ? fov + (0.1F * (speed.getAmplifier() + 1)) : fov);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderWorld(RenderWorldLastEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        float scale = player.getHeight() / 1.8F;

        if (Minecraft.getInstance().gameSettings.thirdPersonView == 1) {
            if (player.getHeight() > 1.8F) event.getMatrixStack().translate(0, 0, -scale * 2);
        }

        if (Minecraft.getInstance().gameSettings.thirdPersonView == 2) {
            if (player.getHeight() > 1.8F) event.getMatrixStack().translate(0, 0, scale * 2);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onEntityRenderPre(RenderLivingEvent.Pre event) {
        if (Config.FEATURE.DO_ADJUSTED_RENDER.get()) {
            final LivingEntity entity = event.getEntity();

            LazyOptional<ISizeCap> capLazy = entity.getCapability(SizeCapPro.sizeCapability);
            capLazy.ifPresent(cap -> {
                if (cap.getTrans() == true) {
                    float scale = entity.getHeight() / cap.getDefaultHeight();

                    if (scale < 0.4F) {
                        event.getMatrixStack().push();
                        event.getMatrixStack().scale(scale * 2.5F, 1, scale * 2.5F);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onLivingRenderPost(RenderLivingEvent.Post event) {
        if (Config.FEATURE.DO_ADJUSTED_RENDER.get()) {
            final LivingEntity entity = event.getEntity();

            LazyOptional<ISizeCap> capLazy = entity.getCapability(SizeCapPro.sizeCapability);
            capLazy.ifPresent(cap -> {
                if (cap.getTrans() == true) {
                    float scale = entity.getHeight() / cap.getDefaultHeight();

                    if (scale < 0.4F) {
                        event.getMatrixStack().pop();
                    }
                }
            });
        }
    }
}
