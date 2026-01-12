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
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;

public class SequenceAnchorAura {
    private final Minecraft mc = Minecraft.getInstance();
    
    // Settings
    private int placeDelay = 50; // Milliseconds between each step
    private boolean autoSequence = true;
    private long lastActionTime = 0;
    private int sequenceStep = 0;
    
    public void doAnchorSequence() {
        // 1. Get the block position directly in front of the player's crosshair
        HitResult ray = mc.hitResult;
        if (ray == null || ray.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) ray;
        BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        
        // Find slots in hotbar
        int anchorSlot = findHotbarItem(Items.RESPAWN_ANCHOR);
        int glowstoneSlot = findHotbarItem(Items.GLOWSTONE);
        
        if (anchorSlot == -1 || glowstoneSlot == -1) return;
        
        // --- THE SEQUENCE ---
        // STEP 1: Place the Anchor
        swapAndUse(anchorSlot, targetPos, Direction.UP);
        
        // Small delay for server processing
        try {
            Thread.sleep(placeDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // STEP 2: Charge with Glowstone (The "Charge" phase)
        swapAndUse(glowstoneSlot, targetPos, Direction.UP);
        
        // Small delay for server processing
        try {
            Thread.sleep(placeDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // STEP 3: Explode (Interact again to trigger)
        // We swap back to anchor slot or any non-glowstone item to click it
        swapAndUse(anchorSlot, targetPos, Direction.UP);
    }
    
    // Async version for better performance
    public void doAnchorSequenceAsync() {
        if (System.currentTimeMillis() - lastActionTime < placeDelay) return;
        
        HitResult ray = mc.hitResult;
        if (ray == null || ray.getType() != HitResult.Type.BLOCK) {
            sequenceStep = 0;
            return;
        }
        
        BlockHitResult blockHit = (BlockHitResult) ray;
        BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        
        int anchorSlot = findHotbarItem(Items.RESPAWN_ANCHOR);
        int glowstoneSlot = findHotbarItem(Items.GLOWSTONE);
        
        if (anchorSlot == -1 || glowstoneSlot == -1) {
            sequenceStep = 0;
            return;
        }
        
        switch (sequenceStep) {
            case 0: // Place anchor
                swapAndUse(anchorSlot, targetPos, Direction.UP);
                sequenceStep = 1;
                lastActionTime = System.currentTimeMillis();
                break;
                
            case 1: // Charge with glowstone
                swapAndUse(glowstoneSlot, targetPos, Direction.UP);
                sequenceStep = 2;
                lastActionTime = System.currentTimeMillis();
                break;
                
            case 2: // Explode
                swapAndUse(anchorSlot, targetPos, Direction.UP);
                sequenceStep = 0; // Reset sequence
                lastActionTime = System.currentTimeMillis();
                break;
        }
    }
    
    private void swapAndUse(int slot, BlockPos pos, Direction facing) {
        if (mc.player == null || mc.getConnection() == null) return;
        
        int originalSlot = mc.player.getInventory().selected;
        
        // Silent Swap
        mc.player.getInventory().selected = slot;
        
        // Create hit result
        Vec3 hitVec = Vec3.atCenterOf(pos);
        BlockHitResult hitResult = new BlockHitResult(
            hitVec,
            facing,
            pos,
            false
        );
        
        // Send use packet
        mc.getConnection().send(new ServerboundUseItemOnPacket(
            InteractionHand.MAIN_HAND,
            hitResult,
            0 // Sequence number
        ));
        
        // Visual swing
        mc.player.swing(InteractionHand.MAIN_HAND);
        
        // Swap back
        mc.player.getInventory().selected = originalSlot;
    }
    
    private int findHotbarItem(net.minecraft.world.item.Item item) {
        if (mc.player == null) return -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }
    
    // Configuration methods
    public void setPlaceDelay(int delay) {
        this.placeDelay = delay;
    }
    
    public int getPlaceDelay() {
        return placeDelay;
    }
    
    public void setAutoSequence(boolean auto) {
        this.autoSequence = auto;
    }
    
    public boolean isAutoSequence() {
        return autoSequence;
    }
    
    public void resetSequence() {
        this.sequenceStep = 0;
        this.lastActionTime = 0;
    }
    
    public int getCurrentStep() {
        return sequenceStep;
    }
}
