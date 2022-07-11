package com.joeskott.ridingutils.item.custom;

import com.joeskott.ridingutils.config.RidingUtilsCommonConfigs;
import com.joeskott.ridingutils.item.ModItems;
import com.joeskott.ridingutils.sound.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class RidingCropItem extends Item {
    public RidingCropItem(Properties properties) {
        super(properties);
    }

    Random random = new Random();

    boolean ejectPlayer = false;
    boolean offhandIsReins = false;

    int damageOnUse = 1;

    int cooldownTicks = 20;

    int damageCheck = 32;

    int durationOfEffect = 120;

    boolean doBuckPlayer = true;

    boolean showDamage = false;

    int effectAmplifier = 2;


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        if(!player.isPassenger()) {
            return super.use(level, player, usedHand);
        }

        if(isPhysicalVehicle(player.getVehicle())) {
            return super.use(level, player, usedHand);
        }

        updateValuesFromConfig();

        Entity playerMount = player.getVehicle();

        ItemStack itemSelf = player.getItemInHand(usedHand);
        ItemStack itemOffhand = player.getOffhandItem();

        offhandIsReins = itemOffhand.is(ModItems.REINS.get());

        int maxDamage = itemSelf.getMaxDamage();
        int currentDamage = itemSelf.getDamageValue();

        int chanceRange = maxDamage - currentDamage + 1;

        boolean isOnGround = playerMount.isOnGround();
        boolean isInWater = playerMount.isInWater();

        if(isOnGround) {
            addMotion(playerMount);
        } else if(isInWater) {
            addWaterMotion(playerMount);
        } else if(offhandIsReins) {
            playerMount.resetFallDistance();
        }

        if(!level.isClientSide()) {
            if(isOnGround) {
                activateCropSound(playerMount);
                player.getCooldowns().addCooldown(this, cooldownTicks);
                addItemDamage(player, itemSelf, damageOnUse);
                rollForHPDamage(player, playerMount, chanceRange, currentDamage, maxDamage);
            }

            if(ejectPlayer && doBuckPlayer) {
                // Called if bad stuff happened oops
                playerMount.ejectPassengers();
                buckPlayer(player, playerMount);
                ejectPlayer = false;
            } else if(ejectPlayer) {
                ejectPlayer = false;
            }
        }




        return super.use(level, player, usedHand);
    }

    private void activateCropSound(Entity entity) {
        entity.playSound(ModSounds.RIDING_CROP_ACTIVE.get(), 1.0f, getVariablePitch(0.4f));
    }

    private void addItemDamage(Player player, ItemStack item, int damageAmount) {
        item.hurtAndBreak(
                damageAmount,
                player,
                (pPlayer) -> pPlayer.broadcastBreakEvent(pPlayer.getUsedItemHand()));
    }

    private void addWaterMotion(Entity entity) {
        double boost = 0.05d;
        Vec3 lookAngle = entity.getLookAngle();
        Vec3 newMotion = new Vec3(lookAngle.x / 3, boost, lookAngle.z / 3);

        entity.setDeltaMovement(newMotion);
    }

    private void addMotion(Entity entity) {
        double boost = 0.4d;
        Vec3 lookAngle = entity.getLookAngle();
        Vec3 lastMotion = entity.getDeltaMovement();
        Vec3 newMotion = new Vec3(lastMotion.x + lookAngle.x, lastMotion.y + lookAngle.y + boost, lastMotion.z + lookAngle.z);

        entity.setDeltaMovement(newMotion);
    }



    private void buckPlayer(Player player, Entity playerMount) {
        if(player.isPassenger()) {
            return;
        }
        player.stopFallFlying();
    }


    private void rollForHPDamage(Player player, Entity playerMount, int chanceRange, int currentDamage, int maxDamage) {
        int roll = random.nextInt(chanceRange);

        if(currentDamage < damageCheck || roll != 0) {
            doHurt(playerMount, 0.0f);
            addSpeed(playerMount, effectAmplifier, durationOfEffect);
        } else {
            doRealDamageAndSideEffects(player, playerMount);
        }
    }

    private void addSpeed(Entity playerMount, int amplifier, int duration) {
        //playerMount
        if(playerMount instanceof LivingEntity) {
            LivingEntity livingEntity = ((LivingEntity) playerMount);
            MobEffectInstance speedEffect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, false, false);
            livingEntity.addEffect(speedEffect);
        }
    }

    private void doHurt(Entity playerMount, float hurtAmount) {
        if(!playerMount.isOnGround()) {
            return;
        }
        if(playerMount instanceof LivingEntity) {
            LivingEntity livingEntity = ((LivingEntity) playerMount);
            boolean isHorse = playerMount instanceof Horse;

            if (hurtAmount > 0 || !isHorse) {
                if(hurtAmount < 1.0f && !showDamage) {
                    return;
                }
                livingEntity.hurt(DamageSource.GENERIC, hurtAmount);
            } else if (isHorse) {
                int bound = 3;
                if(!showDamage) {
                    bound = 2;
                }
                int choose = random.nextInt(bound);
                float pitch = getVariablePitch(0.3f);


                switch (choose) {
                    case 0 -> playerMount.playSound(SoundEvents.HORSE_ANGRY, 1.0f, pitch);
                    case 1 -> playerMount.playSound(SoundEvents.HORSE_BREATHE, 1.0f, pitch);
                    case 2 -> playerMount.playSound(SoundEvents.HORSE_HURT, 1.0f, pitch);
                }
            }
        }
    }

    private void doRealDamageAndSideEffects(Player player, Entity playerMount) {
        ejectPlayer = random.nextBoolean();
        float hurtAmount = random.nextFloat(2.0f);
        doHurt(playerMount, hurtAmount);
    }


    private boolean isPhysicalVehicle(Entity entity) {
        if(entity instanceof Boat || entity instanceof Minecart) {
            return true;
        }
        return false;
    }

    private float getVariablePitch(float maxVariance) {
        float pitchAdjust = random.nextFloat(maxVariance) - random.nextFloat(maxVariance);
        return 1.2f + pitchAdjust;
    }


    private void updateValuesFromConfig() {
        cooldownTicks = RidingUtilsCommonConfigs.ridingCropCooldownTicks.get();

        damageCheck = RidingUtilsCommonConfigs.ridingCropDangerStart.get();

        durationOfEffect = RidingUtilsCommonConfigs.ridingCropDuration.get();

        doBuckPlayer = RidingUtilsCommonConfigs.ridingCropBuck.get();

        showDamage = RidingUtilsCommonConfigs.ridingCropAnimDamage.get();

        effectAmplifier = RidingUtilsCommonConfigs.ridingCropControllableSpeedAmplifier.get();
    }


}
