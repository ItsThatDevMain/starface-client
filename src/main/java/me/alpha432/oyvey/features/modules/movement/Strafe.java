package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.material.Fluids;

public class Strafe {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    // 1. ADVANCED SPEED (Strafe / Bhop)
    public void onUpdateSpeed() {
        if (mc.player == null) return;
        
        if (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0) {
            if (mc.player.onGround()) {
                mc.player.jumpFromGround();
                // Apply a 20% boost on the initial jump to bypass friction
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x * 1.2,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * 1.2
                );
            } else {
                // Mid-air strafe control
                double speed = Math.sqrt(
                    mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x + 
                    mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z
                );
                double yaw = Math.toRadians(mc.player.getYRot());
                mc.player.setDeltaMovement(
                    -Math.sin(yaw) * speed,
                    mc.player.getDeltaMovement().y,
                    Math.cos(yaw) * speed
                );
            }
        }
    }
    
    // 2. JESUS / WATERWALK (Solid Surface Mode)
    public void onUpdateJesus() {
        if (mc.player == null || mc.level == null) return;
        
        if (mc.level.getFluidState(mc.player.blockPosition().below()).is(Fluids.WATER)) {
            if (!mc.player.isInWater()) {
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x,
                    0,
                    mc.player.getDeltaMovement().z
                );
                mc.player.setOnGround(true); // Spoofs ground to allow sprinting on water
            }
        }
    }
    
    // 3. NOFALL (Packet Mode)
    public void onUpdateNoFall() {
        if (mc.player == null || mc.getConnection() == null) return;
        
        if (mc.player.fallDistance > 2.0F) {
            // Sends a ground packet to reset fall distance on server-side
            mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true));
        }
    }
    
    // 4. SPIDER & STEP
    public void onUpdateSpiderStep() {
        if (mc.player == null) return;
        
        // Spider Logic
        if (mc.player.horizontalCollision) {
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x,
                0.2,
                mc.player.getDeltaMovement().z
            );
        }
        
        // Step Logic (1.0 block height)
        mc.player.maxUpStep = 1.0F;
    }
    
    // 5. ANTI-KNOCKBACK (Velocity)
    // This would be called from your Packet Receiving Event
    public boolean onPacketReceive(Object packet) {
        if (mc.player == null) return false;
        
        if (packet instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket motionPacket = (ClientboundSetEntityMotionPacket) packet;
            if (motionPacket.getId() == mc.player.getId()) {
                // Advanced: Cancel horizontal, keep vertical (looks more natural)
                return true; // Return true to cancel the packet
            }
        }
        return false;
    }
    
    // 6. HIGHJUMP & LONGJUMP
    public void executeLongJump() {
        if (mc.player == null) return;
        
        if (mc.player.onGround()) {
            double yaw = Math.toRadians(mc.player.getYRot());
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x - Math.sin(yaw) * 0.6, // Forward burst
                0.45, // Jump height
                mc.player.getDeltaMovement().z + Math.cos(yaw) * 0.6
            );
        }
    }
    
    // 7. GLIDE / AIRJUMP
    public void onUpdateGlide() {
        if (mc.player == null) return;
        
        if (!mc.player.onGround() && mc.player.getDeltaMovement().y < 0) {
            // Slows fall to a crawl
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x,
                -0.05,
                mc.player.getDeltaMovement().z
            );
            
            // AirJump: Reset ground state if space is pressed
            if (mc.options.keyJump.isDown()) {
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x,
                    0.42,
                    mc.player.getDeltaMovement().z
                );
            }
        }
    }
    
    // 8. NOSLOWDOWN
    public void onUpdateNoSlow() {
        if (mc.player == null) return;
        
        // Removes slowdown from using items, sneaking, etc.
        if (mc.player.isUsingItem()) {
            mc.player.input.leftImpulse *= 5.0F;
            mc.player.input.forwardImpulse *= 5.0F;
        }
    }
    
    // 9. SAFEWALK (Prevents falling off edges)
    public void onUpdateSafeWalk() {
        if (mc.player == null || mc.level == null) return;
        
        if (mc.player.onGround()) {
            double x = mc.player.getX();
            double y = mc.player.getY() - 1;
            double z = mc.player.getZ();
            
            // Check if there's a block below the next position
            if (mc.level.isEmptyBlock(mc.player.blockPosition().below())) {
                // Stop horizontal movement
                mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
            }
        }
    }
    
    // 10. TIMER (Speed up game tick)
    public void setTimer(float speed) {
        if (mc.timer != null) {
            // Note: timer field might be different in your version
            // You may need to use reflection or mixins to access this
            // This is a simplified example
        }
    }
}
