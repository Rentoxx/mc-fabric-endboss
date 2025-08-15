package tech.tguentner.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tech.tguentner.component.ModDataComponents;
import tech.tguentner.entity.HomingSnowballEntity;
import tech.tguentner.entity.OrbitingProjectileEntity;

import java.util.List;

public class MagischerStabItem extends Item {

    // Die Cooldowns sind jetzt die "Source of Truth" für die Anzahl der Modi.
    private static final int[] COOLDOWNS = {20, 10, 0};
    private static final int MODE_COUNT = COOLDOWNS.length;

    public MagischerStabItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            switchMode(world, user, itemStack);
        } else {
            executeModeAction(world, user, itemStack);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    /**
     * Wechselt den Modus des Items und gibt Feedback an den Spieler.
     */
    private void switchMode(World world, PlayerEntity user, ItemStack stack) {
        int currentMode = stack.getOrDefault(ModDataComponents.MODE, 0);
        int nextMode = (currentMode + 1) % MODE_COUNT;
        stack.set(ModDataComponents.MODE, nextMode);

        // Feedback-Sound für den Moduswechsel.
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL,
                0.5F, 1.5F); // Fester Pitch für konsistenten Sound.

        // Feedback-Nachricht im Chat.
        if (!world.isClient()) {
            user.sendMessage(Text.literal("Modus " + (nextMode + 1) + " ist aktiv!"), true); // true für die Action Bar
        }

        // Cooldown nur für den Moduswechsel
        user.getItemCooldownManager().set(this, 5);
    }

    /**
     * Führt die Aktion des aktuell ausgewählten Modus aus.
     */
    private void executeModeAction(World world, PlayerEntity user, ItemStack stack) {
        int currentMode = stack.getOrDefault(ModDataComponents.MODE, 0);

        // Die Logik für jeden Modus ist jetzt in einer eigenen, kleinen Methode.
        // Das macht den Code viel übersichtlicher.
        if (!world.isClient()) {
            switch (currentMode) {
                case 0 -> fireSimpleSnowball(world, user);
                case 1 -> spawnOrbitingProjectiles(world, user);
                case 2 -> fireHomingSnowball(world, user);
            }
        }

        // Der Cooldown wird nur einmal am Ende gesetzt, da er für alle Aktionen gilt.
        user.getItemCooldownManager().set(this, COOLDOWNS[currentMode]);
    }

    private void fireSimpleSnowball(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1.0F);

        SnowballEntity snowballEntity = new SnowballEntity(world, user);
        snowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
        world.spawnEntity(snowballEntity);
    }

    private void fireHomingSnowball(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1.0F);

        HomingSnowballEntity homingSnowball = new HomingSnowballEntity(world, user);
        homingSnowball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
        world.spawnEntity(homingSnowball);
    }

    private void spawnOrbitingProjectiles(World world, PlayerEntity user) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR, SoundCategory.PLAYERS, 1.0F, 1.0F);

        Vec3d[] offsets = {
                new Vec3d(0.0, 0.7, 0.5),
                new Vec3d(-0.8, 0.4, 0.4),
                new Vec3d(0.8, 0.4, 0.4)
        };

        for (Vec3d offset : offsets) {
            OrbitingProjectileEntity projectile = new OrbitingProjectileEntity(world, user, offset);
            Vec3d rotatedOffset = offset.rotateY((float) -Math.toRadians(user.getYaw()));
            Vec3d startPosition = user.getEyePos().add(rotatedOffset);
            projectile.setPosition(startPosition);
            world.spawnEntity(projectile);
        }
    }


    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        int mode = stack.getOrDefault(ModDataComponents.MODE, 0);
        tooltip.add(
                Text.translatable("item.assasin-endboss.magischer_stab.tooltip.mode", mode + 1)
                        .formatted(Formatting.GRAY)
        );
        super.appendTooltip(stack, context, tooltip, type);
    }
}