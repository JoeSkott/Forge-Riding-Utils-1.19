package com.joeskott.ridingutils.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RidingUtilsCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue reinsJumpHeight;
    public static final ForgeConfigSpec.DoubleValue reinsRidingCropSpeedBoost;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingCropDuration;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingCropControllableSpeedAmplifier;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingCropCooldownTicks;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ridingCropAnimDamage;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ridingCropBuck;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingCropDangerStart;


    static {
        BUILDER.push("Configs for Riding Utilities");

        reinsJumpHeight = BUILDER.comment("How high do mobs jump when using reins? (Defaults to 0.5)")
                .defineInRange("Reins Jump Height", 0.5, 0.1, 2.0);

        reinsRidingCropSpeedBoost = BUILDER.comment("Speed multiplier for when mobs are using reins and use the riding crop (Defaults to 2.0)")
                .defineInRange("Reins Crop Speed Boost", 2.0, 0.1, 3.0);

        ridingCropDuration = BUILDER.comment("How long does the speed boost last? (Defaults to 140 ticks or 7 seconds)")
                .defineInRange("Riding Crop Speed Duration", 140, 1, 99999999);

        ridingCropControllableSpeedAmplifier = BUILDER.comment("Speed amplifier for default controllable mobs (Defaults to 2)")
                .defineInRange("Riding Crop Speed Amplifier", 2, 0, 99999999);

        ridingCropCooldownTicks = BUILDER.comment("How many ticks before the riding crop can be used again? (Defaults to 60 or 3 seconds)")
                .defineInRange("Riding Crop Cooldown", 60, 1, 99999999);

        ridingCropAnimDamage = BUILDER.comment("Does the riding crop occasionally cause faux damage even when repaired? (Defaults to false)")
                .define("Riding Crop Fake Damage", false);

        ridingCropBuck = BUILDER.comment("Does the riding crop have a chance to buck off the rider when at low durability? (Defaults to true)")
                .define("Riding Crop Buck Chance", true);

        ridingCropDangerStart = BUILDER.comment("When does the risk of side effects begin (at what damage value, higher number = lower durability)? (Defaults to 32)")
                .defineInRange("Riding Crop Buck Danger", 32, 1, 2048);


        BUILDER.pop();
        SPEC = BUILDER.build();

    }




}
