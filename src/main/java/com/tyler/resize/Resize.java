package com.tyler.resize;

import com.tyler.resize.attributes.Attributes;
import com.tyler.resize.attributes.AttributesHandler;
import com.tyler.resize.compatibilities.Capabilities;
import com.tyler.resize.compatibilities.CapabilitiesHandler;
import com.tyler.resize.compatibilities.sizeCap.ISizeCap;
import com.tyler.resize.compatibilities.sizeCap.SizeCapPro;
import com.tyler.resize.event.PlayNetMoveEvent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Resize.MODID)
public class Resize {
	public static final String MODID = "resize";
	public static final String NAME = "Resize";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	
	public Resize() {
		LOGGER.info("Resize constructor called");
		MinecraftForge.EVENT_BUS.register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ResizeConfig.SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigEvent);
		Attributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	//apply damage when crushed by a large entity
	public static DamageSource causeCrushingDamage(LivingEntity entity) {
		return new EntityDamageSource(MODID + ".crushing", entity);
	}
	
	private final CapabilitiesHandler caps = new CapabilitiesHandler();
	private final AttributesHandler handler = new AttributesHandler();
	
	public void preInit(FMLCommonSetupEvent event) {
		Capabilities.init();
		MinecraftForge.EVENT_BUS.register(caps);
		MinecraftForge.EVENT_BUS.register(handler);
	}
	
	public void onConfigEvent(ModConfig.Loading event) {
		// Config.SPEC.setConfig(event.getConfig().getConfigData());
		//TODO: set up configs
	}
	
	@SubscribeEvent
	public void onServerStarting(RegisterCommandsEvent event) {
		new ResizeCommands().register(event.getDispatcher());
	}
	
	
	@SubscribeEvent //factor size into fall damage
	public void onPlayerFall(LivingFallEvent event) {
		if (event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if (ResizeConfig.FEATURE.SCALED_FALL_DAMAGE.get())
				event.setDistance(event.getDistance() / (player.getHeight() * 0.6F));
			if (player.getHeight() < 0.45F) event.setDistance(0);
		}
	}
	
	//TODO: move to separate mod
	@SubscribeEvent //check and assure if crushing damage should be applied
	public void onLivingTick(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World world = event.getEntityLiving().world;
		
		for (LivingEntity entities : world.getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox())) {
			if (!entity.isSneaking() && ResizeConfig.FEATURE.GIANTS_CRUSH_ENTITIES.get()) {
				if (entity.getHeight() / entities.getHeight() >= 4 && entities.getRidingEntity() != entity) {
					entities.attackEntityFrom(causeCrushingDamage(entity), entity.getHeight() - entities.getHeight());
				}
			}
		}
	}
	
	//TODO: move to separate mod
	@SubscribeEvent //mobs don't notice small players
	public void onTargetEntity(LivingSetAttackTargetEvent event) {
		if (event.getTarget() instanceof PlayerEntity && event.getEntityLiving() instanceof MobEntity && ResizeConfig.FEATURE.SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS.get()) {
			PlayerEntity player = (PlayerEntity) event.getTarget();
			MobEntity entity = (MobEntity) event.getEntityLiving();
			
			if (!(entity instanceof SpiderEntity || entity instanceof OcelotEntity)) {
				if (player.getHeight() <= 0.45F) {
					entity.setAttackTarget(null);
				}
			}
		}
	}
	
	@SubscribeEvent //TODO: figure this out
	// why cancel movement if size is greater than 2?
	public void onPlayNetMove(PlayNetMoveEvent event) {
		ServerPlayerEntity serverPlayer = event.getPlayer();
		
		if (serverPlayer.getHeight() > 2.0F) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent //multi-tick movements?
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
			
			if (!player.abilities.isFlying
					&& ResizeConfig.FEATURE.PLANTS_SLOW_SMALL_DOWN.get()
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
			
			if (ClimbingHandler.canClimb(player, facing)
					&& ResizeConfig.FEATURE.CLIMB_SOME_BLOCKS.get()
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
				doPlayerClimb(player);
			}
			
			for (ItemStack stack : player.getHeldEquipment()) {
				if (stack.getItem() == Items.SLIME_BALL || stack.getItem() == Item.getItemFromBlock(Blocks.SLIME_BLOCK) && ResizeConfig.FEATURE.CLIMB_WITH_SLIME.get()) {
					if (ClimbingHandler.canClimb(player, facing)) {
						doPlayerClimb(player);
					}
				}
				
				if (stack.getItem() == Items.PAPER && ResizeConfig.FEATURE.GLIDE_WITH_PAPER.get()) {
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
											ResizeConfig.FEATURE.HOT_BLOCKS_GIVE_LIFT.get()) {
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
	
	//TODO: move to separate mod
	private void doPlayerClimb(PlayerEntity player) {
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
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event) {
		if (event.getTarget() instanceof LivingEntity) {
			LivingEntity target = (LivingEntity) event.getTarget();
			PlayerEntity player = event.getPlayer();
			
			if (target.getHeight() / 2 >= player.getHeight() && ResizeConfig.FEATURE.RIDE_BIG_ENTITIES.get()) {
				for (ItemStack stack : player.getHeldEquipment()) {
					if (stack.getItem() == Items.STRING) {
						player.startRiding(target);
					}
				}
			}
			
			if (target.getHeight() * 2 <= player.getHeight() && ResizeConfig.FEATURE.PICKUP_SMALL_ENTITIES.get()) {
				target.startRiding(player);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event) {
		if (event.getEntityLiving() instanceof PlayerEntity && ResizeConfig.MODIFIER.JUMP_MODIFIER.get()) {
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
		
		if (ResizeConfig.MODIFIER.HARVEST_MODIFIER.get())
			event.setNewSpeed(event.getOriginalSpeed() * (player.getHeight() / 1.8F));
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderWorld(RenderWorldLastEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		
		assert player != null;
		float playerHeight = player.getHeight();
		float scale = playerHeight / 1.8F;
		
		switch (Minecraft.getInstance().gameSettings.getPointOfView()) {
			case THIRD_PERSON_BACK:
				if (playerHeight > 1.8F) event.getMatrixStack().translate(0, 0, -scale * 2);
				break;
			case THIRD_PERSON_FRONT:
				if (playerHeight > 1.8F) event.getMatrixStack().translate(0, 0, scale * 2);
				break;
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onEntityRenderPre(RenderLivingEvent.Pre<PlayerEntity, PlayerModel<PlayerEntity>> event) {
		if (ResizeConfig.FEATURE.DO_ADJUSTED_RENDER.get()) {
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
		if (ResizeConfig.FEATURE.DO_ADJUSTED_RENDER.get()) {
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
	
	//TODO: what is this?
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		// Fetch & Copy Capability
		PlayerEntity playerOld = event.getOriginal();
		PlayerEntity playerNew = event.getPlayer();
		SizeManager.copySize(playerOld, playerNew);
		
		// Copy Health on Dimension Change
		if (!event.isWasDeath())
			playerNew.setHealth(playerOld.getHealth());
	}
}
