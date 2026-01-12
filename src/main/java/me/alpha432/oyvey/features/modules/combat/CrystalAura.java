package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancedCrystalAura {
    private final Minecraft mc = Minecraft.getInstance();
    
    // Settings
    private float range = 5.0f;
    private float wallRange = 3.0f;
    private boolean autoSwitch = true;
    private float minDamage = 4.0f; // Minimum damage to deal to enemy
    private float maxSelfDamage = 6.0f; // Maximum damage to deal to yourself

    public void onUpdate() {
        if (mc.player == null || mc.level == null) return;

        // 1. Find Target
        Player target = getBestTarget();
        if (target == null) return;

        // 2. Break Phase (Hit existing crystals)
        EndCrystal crystal = getNearestCrystal();
        if (crystal != null && mc.player.distanceTo(crystal) <= range) {
            if (calculateDamage(crystal.blockPosition(), mc.player) < maxSelfDamage) {
                mc.gameMode.attack(mc.player, crystal);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        // 3. Place Phase
        doPlace(target);
    }

    private void doPlace(Player target) {
        BlockPos bestPos = null;
        double maxDamage = 0;

        // Scan blocks around target
        for (BlockPos pos : getSphere(mc.player.blockPosition(), (int) range)) {
            if (!canPlaceCrystal(pos)) continue;

            double targetDamage = calculateDamage(pos, target);
            double selfDamage = calculateDamage(pos, mc.player);

            if (targetDamage >= minDamage && selfDamage <= maxSelfDamage) {
                if (targetDamage > maxDamage) {
                    maxDamage = targetDamage;
                    bestPos = pos;
                }
            }
        }

        if (bestPos != null) {
            if (autoSwitch && !mc.player.getMainHandItem().is(Items.END_CRYSTAL)) {
                switchToCrystals();
            }
            
            // Send placement packet
            BlockHitResult hitResult = new BlockHitResult(
                Vec3.atCenterOf(bestPos),
                Direction.UP,
                bestPos,
                false
            );
            
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND,
                    hitResult,
                    0
                ));
            }
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // Logic to check if a block is valid for a crystal (Obsidian/Bedrock)
    private boolean canPlaceCrystal(BlockPos pos) {
        if (mc.level == null) return false;
        
        // Check if base block is obsidian or bedrock
        boolean validBase = mc.level.getBlockState(pos).is(Blocks.OBSIDIAN) 
                         || mc.level.getBlockState(pos).is(Blocks.BEDROCK);
        
        // Check if two blocks above are air
        boolean airAbove = mc.level.getBlockState(pos.above()).isAir()
                        && mc.level.getBlockState(pos.above(2)).isAir();
        
        // Check if no crystals already exist there
        boolean noCrystal = mc.level.getEntities(null, 
            new net.minecraft.world.phys.AABB(pos.above())).stream()
            .noneMatch(e -> e instanceof EndCrystal);
        
        return validBase && airAbove && noCrystal;
    }

    private Player getBestTarget() {
        if (mc.level == null) return null;
        
        return mc.level.players().stream()
            .filter(e -> e != mc.player && e.isAlive())
            .filter(e -> mc.player.distanceTo(e) <= range + 2)
            .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
            .orElse(null);
    }

    private EndCrystal getNearestCrystal() {
        if (mc.level == null) return null;
        
        return (EndCrystal) mc.level.getEntities(mc.player, 
            mc.player.getBoundingBox().inflate(range)).stream()
            .filter(e -> e instanceof EndCrystal)
            .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
            .orElse(null);
    }

    // Simple damage estimation (Advanced versions use RayTracing)
    private double calculateDamage(BlockPos crystalPos, Player target) {
        if (mc.level == null) return 0;
        
        // Calculate distance from crystal position to target
        Vec3 crystalVec = Vec3.atCenterOf(crystalPos.above());
        Vec3 targetVec = target.position();
        double distance = crystalVec.distanceTo(targetVec);
        
        // Basic damage calculation (simplified)
        // Real calculation would include: blast protection, armor, totems, terrain
        if (distance > 12.0) return 0;
        
        double damage = (1.0 - (distance / 12.0)) * 12.0;
        
        // Factor in armor (simplified)
        damage *= (1.0 - (target.getArmorValue() * 0.04));
        
        return Math.max(0, damage);
    }

    private void switchToCrystals() {
        if (mc.player == null) return;
        
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.END_CRYSTAL)) {
                mc.player.getInventory().selected = i;
                break;
            }
        }
    }
    
    // Helper method to get sphere of blocks around a position
    private List<BlockPos> getSphere(BlockPos center, int radius) {
        List<BlockPos> sphere = new ArrayList<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (center.distSqr(pos) <= radius * radius) {
                        sphere.add(pos);
                    }
                }
            }
        }
        
        return sphere;
    }
    
    // Getters and setters for configuration
    public void setRange(float range) {
        this.range = range;
    }
    
    public float getRange() {
        return range;
    }
    
    public void setWallRange(float wallRange) {
        this.wallRange = wallRange;
    }
    
    public float getWallRange() {
        return wallRange;
    }
    
    public void setAutoSwitch(boolean autoSwitch) {
        this.autoSwitch = autoSwitch;
    }
    
    public boolean isAutoSwitch() {
        return autoSwitch;
    }
    
    public void setMinDamage(float minDamage) {
        this.minDamage = minDamage;
    }
    
    public float getMinDamage() {
        return minDamage;
    }
    
    public void setMaxSelfDamage(float maxSelfDamage) {
        this.maxSelfDamage = maxSelfDamage;
    }
    
    public float getMaxSelfDamage() {
        return maxSelfDamage;
    }
}
