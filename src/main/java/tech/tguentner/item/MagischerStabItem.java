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

    private static final int[] COOLDOWNS = {20, 10, 0};

    public MagischerStabItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        int currentMode = itemStack.getOrDefault(ModDataComponents.MODE, 0);

        if (user.isSneaking()) {
            int nextMode = (currentMode + 1) % 3;
            itemStack.set(ModDataComponents.MODE, nextMode);

            // 1.21.0: Cooldown-API erwartet das Item (nicht den Stack)
            user.getItemCooldownManager().set(this, 5);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL,
                    0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClient()) {
                user.sendMessage(Text.literal("Modus " + nextMode + " ist aktiv!"), false);
            }
        } else {
            switch (currentMode) {
                case 0:
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                            0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    if (!world.isClient()) {
                        // 1.21.0: Konstruktor (world, user) vorhanden
                        SnowballEntity snowballEntity = new SnowballEntity(world, user);
                        snowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
                        world.spawnEntity(snowballEntity);
                    }
                    user.getItemCooldownManager().set(this, COOLDOWNS[currentMode]);
                    break;
                case 1:
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    user.getItemCooldownManager().set(this, COOLDOWNS[currentMode]);

                    if (!world.isClient()) {
                        Vec3d[] offsets = new Vec3d[]{
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
                    break;
                case 2:
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                            0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                    if (!world.isClient()) {
                        HomingSnowballEntity homingSnowball = new HomingSnowballEntity(world, user);
                        homingSnowball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
                        world.spawnEntity(homingSnowball);
                    }
                    user.getItemCooldownManager().set(this, COOLDOWNS[currentMode]);
                    break;
            }
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    // 1.21.0 Tooltip-Override: List<Text>-Variante
    @Override
    public void appendTooltip(ItemStack stack,
                              Item.TooltipContext context,
                              List<Text> tooltip,
                              TooltipType type) {
        int mode = stack.getOrDefault(ModDataComponents.MODE, 0);
        tooltip.add(
                Text.translatable("item.assasin-endboss.magischer_stab.tooltip.mode", mode)
                        .formatted(Formatting.GRAY)
        );
        super.appendTooltip(stack, context, tooltip, type);
    }
}