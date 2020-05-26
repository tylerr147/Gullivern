package com.camellias.gulliverreborn;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final General GENERAL = new General(BUILDER);
    public static final Modifier MODIFIER = new Modifier(BUILDER);
    public static final Feature FEATURE = new Feature(BUILDER);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Double> MAX_SIZE;
        public final ForgeConfigSpec.ConfigValue<Double> MAX_SIZE_USER;
        public final ForgeConfigSpec.ConfigValue<Double> HEALTH_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_PERMISSION;
        public final ForgeConfigSpec.ConfigValue<List<String>> WHITELIST;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            MAX_SIZE = builder
                    .comment("Set the maximum player size")
                    .defineInRange("MAX_SIZE", Float.MAX_VALUE, 1F, 100F);
            MAX_SIZE_USER = builder
                    .comment("Set the maximum player size (non-op)")
                    .defineInRange("MAX_SIZE_USER", Float.MAX_VALUE, 1F, 20F);
            HEALTH_MULTIPLIER = builder
                    .comment("Set the health multiplier")
                    .defineInRange("HEALTH_MULTIPLIER", 1.0F, Float.MIN_VALUE, Float.MAX_VALUE);
            REQUIRE_PERMISSION = builder
                    .comment("Set true to require permission for own size changing")
                    .define("REQUIRE_PERMISSION", false);
            WHITELIST = builder
                    .comment("UUID of members can ignore permission for own size changing")
                    .define("WHITELIST", new ArrayList<>());
            builder.pop();
        }
    }

    public static class Modifier {
        public final ForgeConfigSpec.ConfigValue<Boolean> SPEED_MODIFIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> REACH_MODIFIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> STRENGTH_MODIFIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> HEALTH_MODIFIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> HARVEST_MODIFIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> JUMP_MODIFIER;

        public Modifier(ForgeConfigSpec.Builder builder) {
            builder.push("Modifier");
            SPEED_MODIFIER = builder
                    .comment("Enable/disable the speed modifier")
                    .define("SPEED_MODIFIER", true);
            REACH_MODIFIER = builder
                    .comment("Enable/disable the reach modifier")
                    .define("REACH_MODIFIER", true);
            STRENGTH_MODIFIER = builder
                    .comment("Enable/disable the strength modifier")
                    .define("STRENGTH_MODIFIER", true);
            HEALTH_MODIFIER = builder
                    .comment("Enable/disable the health modifier")
                    .define("HEALTH_MODIFIER", true);
            HARVEST_MODIFIER = builder
                    .comment("Enable/disable the harvest speed modifier")
                    .define("HARVEST_MODIFIER", true);
            JUMP_MODIFIER = builder
                    .comment("Enable/disable the jump height modifier")
                    .define("JUMP_MODIFIER", true);
            builder.pop();
        }
    }

    public static class Feature {
        public final ForgeConfigSpec.ConfigValue<Boolean> DO_ADJUSTED_RENDER;
        public final ForgeConfigSpec.ConfigValue<Boolean> PICKUP_SMALL_ENTITIES;
        public final ForgeConfigSpec.ConfigValue<Boolean> RIDE_BIG_ENTITIES;
        public final ForgeConfigSpec.ConfigValue<Boolean> CLIMB_SOME_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<Boolean> CLIMB_WITH_SLIME;
        public final ForgeConfigSpec.ConfigValue<Boolean> GLIDE_WITH_PAPER;
        public final ForgeConfigSpec.ConfigValue<Boolean> HOT_BLOCKS_GIVE_LIFT;
        public final ForgeConfigSpec.ConfigValue<Boolean> ROSES_HURT;
        public final ForgeConfigSpec.ConfigValue<Boolean> PLANTS_SLOW_SMALL_DOWN;
        public final ForgeConfigSpec.ConfigValue<Boolean> SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS;
        public final ForgeConfigSpec.ConfigValue<Boolean> GIANTS_CRUSH_ENTITIES;
        public final ForgeConfigSpec.ConfigValue<Boolean> SCALED_FALL_DAMAGE;

        public Feature(ForgeConfigSpec.Builder builder) {
            builder.push("Feature");
            DO_ADJUSTED_RENDER = builder
                    .comment("Player render is more normal at small sizes, but may cause problems with other mods")
                    .define("DO_ADJUSTED_RENDER", true);
            PICKUP_SMALL_ENTITIES = builder
                    .comment("Enable/disable the ability to pick up small entities")
                    .define("PICKUP_SMALL_ENTITIES", true);
            RIDE_BIG_ENTITIES = builder
                    .comment("Enable/disable the ability to ride large entities with String")
                    .define("RIDE_BIG_ENTITIES", true);
            CLIMB_SOME_BLOCKS = builder
                    .comment("Enable/disable the ability to climb some blocks (dirt, grass, leaves, etc)")
                    .define("CLIMB_SOME_BLOCKS", true);
            CLIMB_WITH_SLIME = builder
                    .comment("Enable/disable the ability to climb with Slimeballs or Slime Blocks")
                    .define("CLIMB_WITH_SLIME", true);
            GLIDE_WITH_PAPER = builder
                    .comment("Enable/disable the ability to glide with paper")
                    .define("GLIDE_WITH_PAPER", true);
            HOT_BLOCKS_GIVE_LIFT = builder
                    .comment("Enable/disable hot blocks giving gliding players lift (requires the ability to glide with paper to be enabled)")
                    .define("HOT_BLOCKS_GIVE_LIFT", true);
            ROSES_HURT = builder
                    .comment("Enable/disable rose/poppy thorns")
                    .define("ROSES_HURT", true);
            PLANTS_SLOW_SMALL_DOWN = builder
                    .comment("Enable/disable plants slowing down small players")
                    .define("PLANTS_SLOW_SMALL_DOWN", true);
            SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS = builder
                    .comment("Enable/disable the ability for small players to be unnoticed by non-ocelots and non-spiders")
                    .define("SMALL_IS_INVISIBLE_TO_NONCATS_OR_NONSPIDERS", true);
            GIANTS_CRUSH_ENTITIES = builder
                    .comment("Enable/disable the ability for giants to crush small entities")
                    .define("GIANTS_CRUSH_ENTITIES", true);
            SCALED_FALL_DAMAGE = builder
                    .comment("Enable/disable scaled fall damage")
                    .define("SCALED_FALL_DAMAGE", true);
            builder.pop();
        }
    }
}
