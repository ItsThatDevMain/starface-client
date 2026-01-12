package me.alpha432.oyvey.features.gui.items.buttons;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.gui.OyVeyGui;
import me.alpha432.oyvey.features.gui.Widget;
import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class Button extends Item {
    // Black & White Color Scheme for Buttons
    private static final int COLOR_BUTTON_OFF = 0xFF1A1A1A;          // Dark gray when off
    private static final int COLOR_BUTTON_OFF_HOVER = 0xFF252525;    // Slightly lighter on hover
    private static final int COLOR_BUTTON_ON = 0xFF2A2A2A;           // Medium gray when on
    private static final int COLOR_BUTTON_ON_HOVER = 0xFF353535;     // Lighter when on + hover
    private static final int COLOR_TEXT_ON = 0xFFFFFFFF;             // White text when on
    private static final int COLOR_TEXT_OFF = 0xFF888888;            // Gray text when off
    private static final int COLOR_ACCENT = 0xFFFFFFFF;              // White accent line
    private static final int COLOR_BORDER = 0xFF0F0F0F;              // Dark border
    
    private boolean state;
    private float hoverAnimation = 0f;

    public Button(String name) {
        super(name);
        this.height = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        boolean hovering = this.isHovering(mouseX, mouseY);
        
        // Smooth hover animation
        float targetHover = hovering ? 1f : 0f;
        this.hoverAnimation += (targetHover - this.hoverAnimation) * 0.3f;
        
        // Determine background color based on state and hover
        int backgroundColor;
        if (this.getState()) {
            backgroundColor = interpolateColor(COLOR_BUTTON_ON, COLOR_BUTTON_ON_HOVER, this.hoverAnimation);
        } else {
            backgroundColor = interpolateColor(COLOR_BUTTON_OFF, COLOR_BUTTON_OFF_HOVER, this.hoverAnimation);
        }
        
        // Draw button background
        RenderUtil.rect(context, this.x, this.y, this.x + (float) this.width, 
                       this.y + (float) this.height - 0.5f, backgroundColor);
        
        // Draw left accent line if enabled
        if (this.getState()) {
            RenderUtil.rect(context, this.x, this.y, this.x + 2f, 
                           this.y + (float) this.height - 0.5f, COLOR_ACCENT);
        }
        
        // Draw subtle bottom border
        RenderUtil.rect(context, this.x, this.y + (float) this.height - 1f, 
                       this.x + (float) this.width, this.y + (float) this.height - 0.5f, COLOR_BORDER);
        
        // Draw text with appropriate color
        int textColor = this.getState() ? COLOR_TEXT_ON : COLOR_TEXT_OFF;
        float textX = this.x + (this.getState() ? 5.0f : 2.3f); // Offset if enabled for accent line
        
        drawString(this.getName(), textX, this.y - 2.0f - (float) OyVeyGui.getClickGui().getTextOffset(), textColor);
        
        // Draw enabled indicator (small circle or square on the right)
        if (this.getState()) {
            float indicatorSize = 3f;
            float indicatorX = this.x + (float) this.width - indicatorSize - 4f;
            float indicatorY = this.y + ((float) this.height - indicatorSize) / 2f;
            RenderUtil.rect(context, indicatorX, indicatorY, indicatorX + indicatorSize, 
                           indicatorY + indicatorSize, COLOR_ACCENT);
        }
    }
    
    // Helper method to interpolate between two colors
    private int interpolateColor(int color1, int color2, float factor) {
        if (factor <= 0f) return color1;
        if (factor >= 1f) return color2;
        
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Widget widget : OyVeyGui.getClickGui().getComponents()) {
            if (!widget.drag) continue;
            return false;
        }
        return (float) mouseX >= this.getX() && (float) mouseX <= this.getX() + (float) this.getWidth() 
            && (float) mouseY >= this.getY() && (float) mouseY < this.getY() + (float) this.height;
    }
}
