package net.teamfruit.gulliver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GulliverCommands {
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> node = dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("size")
                        .requires(cs -> cs.hasPermissionLevel(0))
                        .then(
                                RequiredArgumentBuilder.<CommandSource, Float>argument("size", FloatArgumentType.floatArg(0.125F))
                                        .requires(cs -> {
                                            if (!GulliverConfig.GENERAL.REQUIRE_PERMISSION.get())
                                                return true;
                                            if (cs.hasPermissionLevel(2))
                                                return true;
                                            if (cs.getEntity() instanceof ServerPlayerEntity) {
                                                try {
                                                    String playerUUID = cs.asPlayer().getGameProfile().getId().toString();
                                                    if (GulliverConfig.GENERAL.WHITELIST.get().contains(playerUUID))
                                                        return true;
                                                } catch (CommandSyntaxException e) {
                                                }
                                            }
                                            return false;
                                        })
                                        .executes(ctx -> {
                                            PlayerEntity sender = ctx.getSource().asPlayer();
                                            float size = FloatArgumentType.getFloat(ctx, "size");
                                            float maxUserSize = GulliverConfig.GENERAL.MAX_SIZE_USER.get().floatValue();
                                            if (!ctx.getSource().hasPermissionLevel(2) && size > maxUserSize) {
                                                Message message = new StringTextComponent("Need permission for size over " + maxUserSize);
                                                throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
                                            }
                                            GulliverSize.changeSize(sender, size);
                                            ctx.getSource().sendFeedback(
                                                    new StringTextComponent("")
                                                            .append(sender.getDisplayName())
                                                            .append(new StringTextComponent(" set their size to " + size)),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                RequiredArgumentBuilder.<CommandSource, EntitySelector>argument("entities", EntityArgument.entities())
                                                        .requires(cs -> cs.hasPermissionLevel(2))
                                                        .executes(ctx -> {
                                                            Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "entities");
                                                            float size = FloatArgumentType.getFloat(ctx, "size");
                                                            Map<String, List<LivingEntity>> listEntity = entities.stream()
                                                                    .filter(LivingEntity.class::isInstance)
                                                                    .map(LivingEntity.class::cast)
                                                                    .peek(entity -> GulliverSize.changeSize(entity, size))
                                                                    .collect(Collectors.groupingBy(entity -> entity.getDisplayName().getString()));
                                                            ITextComponent text = TextComponentUtils.func_240649_b_(listEntity.entrySet(), entry -> {
                                                                int length = entry.getValue().size();
                                                                if (length <= 0)
                                                                    return new StringTextComponent("");
                                                                ITextComponent name = entry.getValue().get(0).getDisplayName();
                                                                if (length > 1)
                                                                    return new StringTextComponent("")
                                                                            .append(new StringTextComponent(length + "×")
                                                                                    .setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY))))
                                                                            .append(name);
                                                                return name;
                                                            });
                                                            ctx.getSource().sendFeedback(
                                                                    new StringTextComponent("")
                                                                            .append(new StringTextComponent("[").setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY))))
                                                                            .append(text)
                                                                            .append(new StringTextComponent("]").setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY))))
                                                                            .append(new StringTextComponent(" set their size to " + size)),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
        );
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal(Gulliver.MODID)
                        .then(
                                LiteralArgumentBuilder.<CommandSource>literal("size")
                                        .redirect(node)
                        )
                        .then(
                                LiteralArgumentBuilder.<CommandSource>literal("whitelist")
                                        .requires(cs -> cs.hasPermissionLevel(3))
                                        .then(
                                                LiteralArgumentBuilder.<CommandSource>literal("on")
                                                        .executes(ctx -> {
                                                            GulliverConfig.GENERAL.REQUIRE_PERMISSION.set(true);
                                                            GulliverConfig.GENERAL.REQUIRE_PERMISSION.save();
                                                            ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.enabled"), true);
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                LiteralArgumentBuilder.<CommandSource>literal("off")
                                                        .executes(ctx -> {
                                                            GulliverConfig.GENERAL.REQUIRE_PERMISSION.set(false);
                                                            GulliverConfig.GENERAL.REQUIRE_PERMISSION.save();
                                                            ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.disabled"), true);
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                LiteralArgumentBuilder.<CommandSource>literal("list")
                                                        .executes(ctx -> {
                                                            List<String> whitelist = GulliverConfig.GENERAL.WHITELIST.get();
                                                            PlayerList playerList = ctx.getSource().getServer().getPlayerList();
                                                            List<ITextComponent> list = whitelist.stream().map(uuid -> {
                                                                try {
                                                                    return playerList.getPlayerByUUID(UUID.fromString(uuid)).getDisplayName();
                                                                } catch (IllegalArgumentException e) {
                                                                    return new StringTextComponent(uuid);
                                                                }
                                                            }).collect(Collectors.toList());
                                                            if (list.isEmpty())
                                                                ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.none"), false);
                                                            else {
                                                                ITextComponent text = TextComponentUtils.func_240649_b_(list, Function.identity());
                                                                ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.list", list.size(), text), false);
                                                            }
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                LiteralArgumentBuilder.<CommandSource>literal("add")
                                                        .then(
                                                                RequiredArgumentBuilder.<CommandSource, EntitySelector>argument("players", EntityArgument.players())
                                                                        .executes(ctx -> {
                                                                            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(ctx, "players");
                                                                            List<String> whitelist = GulliverConfig.GENERAL.WHITELIST.get();
                                                                            for (ServerPlayerEntity player : players) {
                                                                                String uuid = player.getGameProfile().getId().toString();
                                                                                if (!whitelist.contains(uuid)) {
                                                                                    whitelist.add(uuid);
                                                                                    ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.add.success", TextComponentUtils.getDisplayName(player.getGameProfile())), true);
                                                                                }
                                                                            }
                                                                            GulliverConfig.GENERAL.WHITELIST.set(whitelist);
                                                                            GulliverConfig.GENERAL.WHITELIST.save();
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                LiteralArgumentBuilder.<CommandSource>literal("remove")
                                                        .then(
                                                                RequiredArgumentBuilder.<CommandSource, EntitySelector>argument("players", EntityArgument.players())
                                                                        .executes(ctx -> {
                                                                            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(ctx, "players");
                                                                            List<String> whitelist = GulliverConfig.GENERAL.WHITELIST.get();
                                                                            for (ServerPlayerEntity player : players) {
                                                                                String uuid = player.getGameProfile().getId().toString();
                                                                                if (whitelist.contains(uuid)) {
                                                                                    whitelist.remove(uuid);
                                                                                    ctx.getSource().sendFeedback(new TranslationTextComponent("commands.whitelist.remove.success", TextComponentUtils.getDisplayName(player.getGameProfile())), true);
                                                                                }
                                                                            }
                                                                            GulliverConfig.GENERAL.WHITELIST.set(whitelist);
                                                                            GulliverConfig.GENERAL.WHITELIST.save();
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }
}
