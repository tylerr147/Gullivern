package com.camellias.gulliverreborn;

import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.UUID;

public class GulliverCommands {
    private static UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
    private static UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
    private static UUID uuidReach1 = UUID.fromString("854e0004-c218-406c-a9e2-590f1846d80b");
    private static UUID uuidReach2 = UUID.fromString("216080dc-22d3-4eff-a730-190ec0210d5c");
    private static UUID uuidHealth = UUID.fromString("3b901d47-2d30-495c-be45-f0091c0f6fb2");
    private static UUID uuidStrength = UUID.fromString("558f55be-b277-4091-ae9b-056c7bc96e84");
    private static UUID uuidSpeed = UUID.fromString("f2fb5cda-3fbe-4509-a0af-4fc994e6aeca");

    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> node = dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal(GulliverReborn.MODID)
                        .requires(cs -> cs.hasPermissionLevel(0))
                        .then(
                                RequiredArgumentBuilder.<CommandSource, Float>argument("size", FloatArgumentType.floatArg(0.125F))
                                        .executes(ctx -> {
                                            PlayerEntity sender = ctx.getSource().asPlayer();
                                            float size = FloatArgumentType.getFloat(ctx, "size");
                                            changeSize(sender, size);
                                            return 1;
                                        })
                        )
                        .then(
                                RequiredArgumentBuilder.<CommandSource, Float>argument("size", FloatArgumentType.floatArg(0.125F))
                                        .then(RequiredArgumentBuilder.<CommandSource, EntitySelector>argument("players", EntityArgument.players()))
                                        .executes(ctx -> {
                                            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(ctx, "players");
                                            float size = FloatArgumentType.getFloat(ctx, "size");
                                            players.forEach(player -> changeSize(player, size));
                                            return 1;
                                        })
                        )
        );
    }

	/*
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "gulliverreborn.commands.mysize.usage";
	}
	 */

    public void changeSize(PlayerEntity sender, float size) {
        Multimap<String, AttributeModifier> attributes = HashMultimap.create();
        Multimap<String, AttributeModifier> removeableAttributes = HashMultimap.create();
        Multimap<String, AttributeModifier> removeableAttributes2 = HashMultimap.create();

        attributes.put(ArtemisLibAttributes.ENTITY_HEIGHT.getName(), new AttributeModifier(uuidHeight, "Player Height", size - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(ArtemisLibAttributes.ENTITY_WIDTH.getName(), new AttributeModifier(uuidWidth, "Player Width", MathHelper.clamp(size - 1, 0.4 - 1, Config.GENERAL.MAX_SIZE.get()), AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (Config.SPEED_MODIFIER)
            attributes.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(uuidSpeed, "Player Speed", (size - 1) / 2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (Config.REACH_MODIFIER)
            removeableAttributes.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach1, "Player Reach 1", size - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (Config.REACH_MODIFIER)
            removeableAttributes2.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(uuidReach2, "Player Reach 2", -MathHelper.clamp(size - 1, 0.33, Double.MAX_VALUE), AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (Config.STRENGTH_MODIFIER)
            attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(uuidStrength, "Player Strength", size - 1, AttributeModifier.Operation.ADDITION));
        if (Config.HEALTH_MODIFIER)
            attributes.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(uuidHealth, "Player Health", (size - 1) * Config.GENERAL.HEALTH_MULTIPLIER.get(), AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (size > 1) {
            sender.getAttributes().applyAttributeModifiers(removeableAttributes);
        } else {
            sender.getAttributes().removeAttributeModifiers(removeableAttributes);
        }

        if (size < 1) {
            sender.getAttributes().applyAttributeModifiers(removeableAttributes2);
        } else {
            sender.getAttributes().removeAttributeModifiers(removeableAttributes2);
        }

        GulliverReborn.LOGGER.info(sender.getDisplayName() + " set their size to " + size);
    }
}
