package com.joeskott.ridingutils.item.custom;

import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import com.joeskott.ridingutils.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ReinsItem extends Item {
    public ReinsItem(Properties properties) {
        super(properties);
    }

    Random random = new Random();
    boolean cancelMotion = false;
    double jumpHeight = 0.5d;
    double speedEffectMultiplier = 2.0d;

    int damageOnUse = 1;


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!player.isPassenger()) {
            return super.use(level, player, usedHand);
        }

        updateValuesFromConfig();

        Entity playerMount = player.getVehicle();
        ItemStack itemSelf = player.getItemInHand(usedHand);
        ItemStack itemOffhand = player.getOffhandItem();

        boolean offhandIsWhip = itemOffhand.is(ModItems.RIDING_CROP.get());
        boolean offhandIsSelf = itemOffhand.is(this);
        boolean isControllable = playerMount instanceof Saddleable;

        cancelMotion = !itemSelf.is(ModItems.REINS.get()) || offhandIsWhip || offhandIsSelf || isPhysicalVehicle(playerMount) || isControllable;

        if(playerMount instanceof Horse) { // Don't run code if this is a horse, but still negate fall damage
            playerMount.resetFallDistance();

            return super.use(level, player, usedHand);
        }

        if(!level.isClientSide()) {
            if(random.nextInt(10) == 0) {
                addItemDamage(player, itemSelf, damageOnUse);
                playerMount.playSound(SoundEvents.LEASH_KNOT_BREAK, 0.2f, getVariablePitch(0.5f));
            }
        }

        if(playerMount.isInWater()) {
            addWaterMotion(player, playerMount);
        } else {
            addMotion(player, playerMount);
        }
        return super.use(level, player, usedHand);
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if(!player.level.isClientSide()) {
            if(!player.isPassenger()) {

                boolean isAdult = true;
                boolean ageable = interactionTarget instanceof AgeableMob;

                if(ageable) {
                    if(((AgeableMob) interactionTarget).getAge() < 0) {
                        isAdult = false;
                    }
                }
                if(isAdult) {
                    player.startRiding(interactionTarget);
                    interactionTarget.playSound(SoundEvents.PIG_SADDLE, 1.0f, 1.0f);
                    updateValuesFromConfig();
                }
            }
        }
        return super.interactLivingEntity(itemStack, player, interactionTarget, usedHand);
    }


    private void addItemDamage(Player player, ItemStack item, int damageAmount) {
        item.hurtAndBreak(
                damageAmount,
                player,
                (pPlayer) -> pPlayer.broadcastBreakEvent(pPlayer.getUsedItemHand()));
    }


    private void addMotion(Player player, Entity playerMount) {
        if(cancelMotion) {
            return;
        }

        boolean climbingMob = (playerMount instanceof Spider);

        if(getBlockCollision(playerMount)) {
            addJumpMotion(player, playerMount, climbingMob);
        }

        Vec3 lookAngle = player.getLookAngle();
        Vec3 lastMotion = playerMount.getDeltaMovement();


        boolean offGroundCheck = !playerMount.isOnGround() && lastMotion.y < -0.1f;
        boolean inWaterCheck = playerMount.isInWater();
        boolean flightCheck = (playerMount instanceof FlyingMob);
        if((offGroundCheck || inWaterCheck) && !flightCheck && !climbingMob) {
            return;
        }

        Vec3 newMotion = new Vec3(lastMotion.x + (lookAngle.x/2), lastMotion.y, lastMotion.z + (lookAngle.z/2));
        Vec3 newFastMotion = new Vec3(lastMotion.x + (lookAngle.x * speedEffectMultiplier), lastMotion.y, lastMotion.z + (lookAngle.z * speedEffectMultiplier));
        Vec3 newJumpMotion = new Vec3(lookAngle.x/4, lastMotion.y, lookAngle.z/4);
        Vec3 newFlightMotion = new Vec3(lastMotion.x + (lookAngle.x * 1.5f), lastMotion.y + (lookAngle.y * 1.5f), lastMotion.z + (lookAngle.z * 2f));

        setLookAngle(playerMount, player);

        if(flightCheck) {
            playerMount.setDeltaMovement(newFlightMotion);
        } else if (!playerMount.isOnGround()) {
            playerMount.setDeltaMovement(newJumpMotion);
        } else {
            if(hasBeenSpedUp(playerMount)) {
                playerMount.setDeltaMovement(newFastMotion);
            } else {
                playerMount.setDeltaMovement(newMotion);
            }
        }
    }

    private void addWaterMotion(Player player, Entity playerMount) {
        if(cancelMotion) {
            return;
        }

        boolean offGroundCheck = (!playerMount.isOnGround() && !playerMount.isInWater());
        boolean flightCheck = (playerMount instanceof FlyingMob);
        boolean waterMob = (playerMount instanceof WaterAnimal);

        if(offGroundCheck && !flightCheck) {
            return;
        }
        Vec3 lookAngle = player.getLookAngle();
        Vec3 lastMotion = playerMount.getDeltaMovement();

        double boost = (waterMob) ? 0.01d : lastMotion.y;

        Vec3 newMotion = new Vec3(lastMotion.x + (lookAngle.x/4), boost, lastMotion.z + (lookAngle.z/4));
        playerMount.setDeltaMovement(newMotion);

        setLookAngle(playerMount, player);
    }


    private void addJumpMotion(Player player, Entity playerMount, boolean climbingMob) {
        // Method must be executed inside addMotion
        if((!playerMount.isOnGround() && !climbingMob) || getBlockCeilingCollision(player)) {
            return;
        }

        playerMount.resetFallDistance();

        Vec3 lastMotion = playerMount.getDeltaMovement();
        //Vec3 lookAngle = playerMount.getLookAngle();
        setLookAngle(playerMount, player);

        Vec3 newMotion = new Vec3(lastMotion.x, jumpHeight, lastMotion.z);
        playerMount.setDeltaMovement(newMotion);
    }


    private boolean isPhysicalVehicle(Entity entity) {
        if(entity instanceof Boat || entity instanceof Minecart) {
            return true;
        }
        return false;
    }


    private boolean hasBeenSpedUp(Entity entity) {
        if(entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            return livingEntity.hasEffect(MobEffects.MOVEMENT_SPEED);
        }
        return false;
    }


    private boolean getBlockCollision(Entity playerMount) {
        Vec3 lookAngle = playerMount.getLookAngle();
        Vec3 position = new Vec3(playerMount.getX(), playerMount.getY(), playerMount.getZ());
        double angleX = lookAngle.x * 1.0f;
        double angleZ = lookAngle.z * 1.0f;
        double offsetY = 0.1f;

        BlockPos collidePos = new BlockPos(angleX + position.x, position.y + offsetY, angleZ + position.z);
        BlockState blockState = playerMount.level.getBlockState(collidePos);

        return blockState.getMaterial().isSolid();
    }


    private boolean getBlockCeilingCollision(Entity entity) {
        BlockPos collidePos = entity.blockPosition().above();
        BlockState blockState = entity.level.getBlockState(collidePos);

        return blockState.getMaterial().isSolid();
    }

    private void setLookAngle(Entity entity, Player player) {
        float yRot = player.getYRot();
        entity.setYRot(yRot);
    }


    private float getVariablePitch(float maxVariance) {
        float pitchAdjust = random.nextFloat(maxVariance) - random.nextFloat(maxVariance);
        return 1.0f + pitchAdjust;
    }

    private void updateValuesFromConfig() {
        jumpHeight = RidingUtilsCommonConfigs.reinsJumpHeight.get();
        speedEffectMultiplier = RidingUtilsCommonConfigs.reinsRidingCropSpeedBoost.get();
    }
}
