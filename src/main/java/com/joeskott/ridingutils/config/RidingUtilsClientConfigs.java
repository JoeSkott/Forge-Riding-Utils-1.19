package com.joeskott.ridingutils.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RidingUtilsClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Configs for Riding Utils");

        //Configs defined here

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
