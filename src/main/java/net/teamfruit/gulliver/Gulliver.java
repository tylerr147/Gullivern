package net.teamfruit.gulliver;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.teamfruit.gulliver.attributes.Attributes;
import net.teamfruit.gulliver.attributes.AttributesHandler;
import net.teamfruit.gulliver.compatibilities.Capabilities;
import net.teamfruit.gulliver.compatibilities.CapabilitiesHandler;
import net.teamfruit.gulliver.compatibilities.sizeCap.ISizeCap;
import net.teamfruit.gulliver.compatibilities.sizeCap.SizeCapPro;
import net.teamfruit.gulliver.event.PlayNetMoveEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Gulliver.MODID)
public class Gulliver {
    public static final String MODID = "gulliver";
    public static final String NAME = "Gulliver-1.15.2";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public Gulliver() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GulliverConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigEvent);

        Attributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static DamageSource causeCrushingDamage(LivingEntity entity) {
        return new EntityDamageSource(MODID + ".crushing", entity);
    }

    private CapabilitiesHandler caps = new CapabilitiesHandler();
    private AttributesHandler handler = new AttributesHandler();

    public void preInit(FMLCommonSetupEvent event) {
        Capabilities.init();
        MinecraftForge.EVENT_BUS.register(caps);
        MinecraftForge.EVENT_BUS.register(handler);
    }

    public void onConfigEvent(ModConfig.Loading event) {
        // Config.SPEC.setConfig(event.getConfig().getConfigData());
    }

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        new GulliverCommands().register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (GulliverConfig.FEATURE.SCALED_FALL_DAMAGE.get())
                event.setDistance(event.getDistance() / (player.getHeight() * 0.6F));
            if (player.getHeight() < 0.45F) event.setDistance(0);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = event.getEntityLiving().world;

        for (LivingEntity entities : world.getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox())) {
            if (!entity.isSneaking() && GulliverConfig.FEATURE.GIANTS_CRUSH_ENTITIES.get()) {
                if (entity.getHeight() / entities.getHeight() >= 4 && entities.getRidingEntity() != entity) {
                    entities.attackEntityFrom(causeCrushingDamage(entity), entity.getHeight() - entities.getHeight());
                }
            }
        }
    }

    @SubscribeEvent
    public void onTargetEntity(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof PlayerEntity && event.getEntityLiving() instanceof MobEntity && GulliverConfig.FEATURE.SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS.get()) {
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
    public void onPlayNetMove(PlayNetMoveEvent event) {
        ServerPlayerEntity serverPlayer = event.getPlayer();

        if (serverPlayer.getHeight() > 2.0F) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = event.player.world;

        player.stepHeight = player.getHeight() / 3F;
        player.jumpMovementFactor *= (float) Math.pow(player.getHeight() / 1.8f, .4f);

        if (player.getHeight() > 2.0F && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (serverPlayer.connection != null)
                serverPlayer.connection.floating = false;
        }

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
                    && GulliverConfig.FEATURE.PLANTS_SLOW_SMALL_DOWN.get()
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
                    && GulliverConfig.FEATURE.CLIMB_SOME_BLOCKS.get()
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
                        Vector3d motion = player.getMotion();
                        player.setMotion(motion.x, 0.1D, motion.z);
                    }

                    if (player.isSneaking()) {
                        Vector3d motion = player.getMotion();
                        player.setMotion(motion.x, 0.0D, motion.z);
                    }
                }
            }

            for (ItemStack stack : player.getHeldEquipment()) {
                if (stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.getItemFromBlock(Blocks.SLIME_BLOCK) && GulliverConfig.FEATURE.CLIMB_WITH_SLIME.get()) {
                    if (ClimbingHandler.canClimb(player, facing)) {
                        if (player.collidedHorizontally) {
                            if (!player.isSneaking()) {
                                Vector3d motion = player.getMotion();
                                player.setMotion(motion.x, 0.1D, motion.z);
                            }

                            if (player.isSneaking()) {
                                Vector3d motion = player.getMotion();
                                player.setMotion(motion.x, 0.0D, motion.z);
                            }
                        }
                    }
                }

                if (stack.getItem() == Items.PAPER && GulliverConfig.FEATURE.GLIDE_WITH_PAPER.get()) {
                    if (!player.isOnGround()) {
                        player.jumpMovementFactor = 0.02F * 1.75F;
                        player.fallDistance = 0;

                        if (player instanceof ServerPlayerEntity) {
                            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                            if (serverPlayer.connection != null)
                                serverPlayer.connection.floating = false;
                        }

                        Vector3d motion = player.getMotion();
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
                                            GulliverConfig.FEATURE.HOT_BLOCKS_GIVE_LIFT.get()) {
                                player.setMotion(player.getMotion().add(0, MathHelper.clamp(0.07D, Double.MIN_VALUE, 0.1D), 0));
                            }
                        }
                    }
                }
            }
        }

        if (player.isBeingRidden() && player.isSneaking() && player.isSwingInProgress) {
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

            if (target.getHeight() / 2 >= player.getHeight() && GulliverConfig.FEATURE.RIDE_BIG_ENTITIES.get()) {
                for (ItemStack stack : player.getHeldEquipment()) {
                    if (stack.getItem() == Items.STRING) {
                        player.startRiding(target);
                    }
                }
            }

            if (target.getHeight() * 2 <= player.getHeight() && GulliverConfig.FEATURE.PICKUP_SMALL_ENTITIES.get()) {
                target.startRiding(player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && GulliverConfig.MODIFIER.JUMP_MODIFIER.get()) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            float jumpHeight = (float) Math.pow(player.getHeight() / 1.8f, .6f);

            jumpHeight = MathHelper.clamp(jumpHeight, 0.65F, jumpHeight);
            player.setMotion(player.getMotion().mul(1, jumpHeight, 1));

            if (player.isSneaking() || player.isSprinting()) {
                if (player.getHeight() < 1.8F) {
                    Vector3d motion = player.getMotion();
                    player.setMotion(motion.x, 0.42F, motion.z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onHarvest(BreakSpeed event) {
        PlayerEntity player = event.getPlayer();

        if (GulliverConfig.MODIFIER.HARVEST_MODIFIER.get())
            event.setNewSpeed(event.getOriginalSpeed() * (player.getHeight() / 1.8F));
    }

    /*
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
     */

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderWorld(RenderWorldLastEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        float scale = player.getHeight() / 1.8F;

        switch (Minecraft.getInstance().gameSettings.getPointOfView()) {
            case THIRD_PERSON_BACK:
                if (player.getHeight() > 1.8F) event.getMatrixStack().translate(0, 0, -scale * 2);
                break;
            case THIRD_PERSON_FRONT:
                if (player.getHeight() > 1.8F) event.getMatrixStack().translate(0, 0, scale * 2);
                break;
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onEntityRenderPre(RenderLivingEvent.Pre<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        if (GulliverConfig.FEATURE.DO_ADJUSTED_RENDER.get()) {
            final LivingEntity entity = event.getEntity();

            LazyOptional<ISizeCap> capLazy = entity.getCapability(SizeCapPro.sizeCapability);
            capLazy.ifPresent(cap -> {
                if (cap.getTrans()) {
                    float scale = entity.getHeight() / cap.getDefaultHeight();

                    if (scale < 0.01F) {
                        event.getMatrixStack().push();
                        event.getMatrixStack().scale(scale * 2.5F, 1, scale * 2.5F);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onLivingRenderPost(RenderLivingEvent.Post<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        if (GulliverConfig.FEATURE.DO_ADJUSTED_RENDER.get()) {
            final LivingEntity entity = event.getEntity();

            LazyOptional<ISizeCap> capLazy = entity.getCapability(SizeCapPro.sizeCapability);
            capLazy.ifPresent(cap -> {
                if (cap.getTrans()) {
                    float scale = entity.getHeight() / cap.getDefaultHeight();

                    if (scale < 0.01F) {
                        event.getMatrixStack().pop();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // Fetch & Copy Capability
        PlayerEntity playerOld = event.getOriginal();
        PlayerEntity playerNew = event.getPlayer();
        GulliverSize.copySize(playerOld, playerNew);

        // Copy Health on Dimension Change
        if (!event.isWasDeath())
            playerNew.setHealth(playerOld.getHealth());
    }
}
