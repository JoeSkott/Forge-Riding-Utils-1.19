package com.joeskott.ridingutils.world;

import com.joeskott.ridingutils.RidingUtils;
import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = RidingUtils.MOD_ID)
public class ModWorldEvents {
    static Random random = new Random();
    @SubscribeEvent
    public static void playerTick(final TickEvent.PlayerTickEvent event) {
        Boolean horsesSwim = RidingUtilsCommonConfigs.horsesSwimNaturally.get();

        if(event.phase == TickEvent.Phase.END && horsesSwim) {
            Player player = event.player;
            if (!player.isPassenger()) {
                return;
            }

            Entity playerMount = player.getVehicle();

            if(!(playerMount instanceof Horse)){
                return;
            }

            float chance = 0.4f;
            float roll = random.nextFloat(1.0f);

            if (playerMount.isInWater() && shouldSwim(playerMount, player) && getBlockCollision(playerMount, true)) {
                Vec3 currentVelocity = playerMount.getDeltaMovement();

                double upVelocity = 0.3d;
                Vec3 newVelocity = currentVelocity;

                newVelocity = new Vec3(currentVelocity.x, upVelocity, currentVelocity.z);

                //Vec3 newVelocity = new Vec3(currentVelocity.x, currentVelocity.y + upVelocity, currentVelocity.z);

                playerMount.setDeltaMovement(newVelocity);
            }

            if (!getLiquidBelow(playerMount, true)) {
                return;
            }

            if (shouldSwim(playerMount, player)) {
                Vec3 currentVelocity = playerMount.getDeltaMovement();
                Vec3 lookAngle = playerMount.getLookAngle();


                double upVelocity = currentVelocity.y;
                double sine = getSine(player.level.getGameTime(), 1.0D);

                if (currentVelocity.y < 0.0D) {
                    if (sine > 0.9D && roll < chance) { //check here is to hopefully prevent excessive jumping
                        //upVelocity = (currentVelocity.y * -1D) * 2.1D;
                        upVelocity = currentVelocity.y + 0.1D + random.nextDouble(0.1D);
                        ;
                    } else {
                        //upVelocity = (currentVelocity.y * -1D) * 0.9D;
                        //upVelocity = (currentVelocity.y * -1D) * 2.1D;
                        upVelocity = currentVelocity.y + 0.03D + random.nextDouble(0.1D);
                        //upVelocity += 0.08D;

                    }
                }


                Vec3 newVelocity;// = new Vec3(currentVelocity.x, currentVelocity.y + upVelocity, currentVelocity.z);

                newVelocity = new Vec3(currentVelocity.x, upVelocity, currentVelocity.z);


                playerMount.setDeltaMovement(newVelocity);
            }
        }
    }

    public static boolean shouldSwim(Entity entity, Player player) {
        double boostHeight = 0.0D;
        if(entity instanceof Horse) {
            boostHeight = 0.5D;
        }
        return entity.isInWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getFluidJumpThreshold() + boostHeight; //+ boostHeight;
    }

    private static boolean getBlockCollision(Entity playerMount, boolean belowToo) {
        if(playerMount.isOnGround()) {
            return false;
        }

        Vec3 lookAngle = playerMount.getLookAngle();
        Vec3 position = new Vec3(playerMount.getX(), playerMount.getY(), playerMount.getZ());
        double angleX = lookAngle.x * 1.0f;
        double angleZ = lookAngle.z * 1.0f;
        double offsetY = 0.1f;
        boolean returnValue = false;

        BlockPos collidePos = new BlockPos(angleX + position.x, position.y + offsetY, angleZ + position.z);

        BlockState blockState = playerMount.level.getBlockState(collidePos);

        returnValue = blockState.getMaterial().blocksMotion();

        if(belowToo) {
            BlockPos collidePosAbove = collidePos.above();
            BlockPos collidePosBelow = collidePos.below();
            BlockPos collidePosBelow2 = collidePos.below().below();
            BlockState blockStateAbove = playerMount.level.getBlockState(collidePosAbove);
            BlockState blockStateBelow = playerMount.level.getBlockState(collidePosBelow);
            BlockState blockStateBelow2 = playerMount.level.getBlockState(collidePosBelow2);
            returnValue = blockState.getMaterial().blocksMotion() || blockStateBelow.getMaterial().blocksMotion() || blockStateBelow2.getMaterial().blocksMotion() || blockStateAbove.getMaterial().blocksMotion();
        }

        return returnValue;
    }


    private static boolean getLiquidBelow(Entity playerMount, boolean twoSteps) {
        if(playerMount.isOnGround()) {
            return false;
        }
        Vec3 position = new Vec3(playerMount.getX(), playerMount.getY(), playerMount.getZ());

        BlockPos checkPos = new BlockPos(position.x, position.y, position.z);

        BlockState blockStateBelow = playerMount.level.getBlockState(checkPos.below());
        BlockState blockStateBelow2 = playerMount.level.getBlockState(checkPos.below().below());
        boolean returnValue = blockStateBelow.getMaterial().isLiquid();
        if (twoSteps == true) {
            returnValue = blockStateBelow.getMaterial().isLiquid() && blockStateBelow2.getMaterial().isLiquid();
        }

        return returnValue;
    }

    private static double getSine(double time, double range) {

        double factor = 0.5D;
        //double result = Mth.abs((float) ((time * factor) % (range * 2)));

        double result = Mth.sin((float) (time * factor)) * range;
        result += range;
        result /= 2.0D;

        System.out.println(result);

        return result;
    }
}
