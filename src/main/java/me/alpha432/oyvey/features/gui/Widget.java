package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.gui.items.buttons.Button;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.render.RenderUtil;
import me.alpha432.oyvey.util.render.ScissorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Widget extends Feature {
    // Improved Black & White Color Scheme
    private static final int COLOR_HEADER = 0xFF1F1F1F;          // Slightly lighter dark gray
    private static final int COLOR_HEADER_HOVER = 0xFF2A2A2A;    // Lighter on hover
    private static final int COLOR_BODY = 0xF0000000;            // Semi-transparent black
    private static final int COLOR_TEXT = 0xFFFFFFFF;            // White text
    private static final int COLOR_TEXT_DIM = 0xFFB0B0B0;        // Dimmed white
    private static final int COLOR_ACCENT = 0xFFFFFFFF;          // White accent
    private static final int COLOR_SHADOW = 0x40000000;          // Soft shadow
    
    protected GuiGraphics context;
    private final List<Item> items = new ArrayList<>();
    public boolean drag;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    private boolean hidden = false;
    
    // Animation
    private float animationProgress = 0f;

    public Widget(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width = 100;  // Slightly wider for better spacing
        this.height = 20;  // Taller header
        this.open = open;
        this.animationProgress = open ? 1f : 0f;
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) {
            return;
        }
        this.x = this.x2 + mouseX;
        this.y = this.y2 + mouseY;
    }

    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);
        
        // Smooth animation
        float targetProgress = this.open ? 1f : 0f;
        this.animationProgress += (targetProgress - this.animationProgress) * 0.2f;
        
        float totalItemHeight = this.getTotalItemHeight() - 2.0f;
        float animatedHeight = totalItemHeight * this.animationProgress;
        
        boolean isHovering = this.isHovering(mouseX, mouseY);
        
        // Draw shadow for depth
        drawRoundedRect(context, this.x - 1, this.y - 1, this.width + 2, this.height + 2, 
                       3, COLOR_SHADOW);
        
        // Draw header with rounded corners
        int headerColor = isHovering ? COLOR_HEADER_HOVER : COLOR_HEADER;
        drawRoundedRect(context, this.x, this.y, this.width, this.height, 3, headerColor);
        
        // Draw thin accent line at top
        drawRoundedRect(context, this.x, this.y, this.width, 2, 3, COLOR_ACCENT);
        
        // Draw body if open
        if (this.animationProgress > 0.01f) {
            int bodyHeight = (int)(this.height + animatedHeight);
            
            // Body shadow
            drawRoundedRect(context, this.x - 1, this.y + this.height - 1, 
                          this.width + 2, (int)animatedHeight + 3, 3, COLOR_SHADOW);
            
            // Body background
            drawRoundedRect(context, this.x, this.y + this.height, 
                          this.width, (int)animatedHeight + 1, 3, COLOR_BODY);
        }
        
        // Draw category name
        String displayName = this.getName().toUpperCase();
        int textY = this.y + (this.height - mc.font.lineHeight) / 2;
        drawString(displayName, (float) this.x + 8.0f, (float) textY, COLOR_TEXT);
        
        // Draw expand/collapse arrow
        String arrow = this.open ? "▼" : "▶";
        int arrowWidth = mc.font.width(arrow);
        drawString(arrow, (float) (this.x + this.width - arrowWidth - 8), 
                  (float) textY, COLOR_TEXT_DIM);
        
        ScissorUtil.enable(context, x, 0, x + width, mc.getWindow().getGuiScaledHeight());

        if (this.animationProgress > 0.01f) {
            float y = (float) (this.getY() + this.getHeight()) + 4.0f;
            for (Item item : this.getItems()) {
                if (item.isHidden()) continue;
                
                // Calculate alpha based on animation
                int alpha = (int)(255 * this.animationProgress);
                
                item.setLocation((float) this.x + 4.0f, y);
                item.setWidth(this.getWidth() - 8);
                
                if (item.isHovering(mouseX, mouseY)) {
                    ScissorUtil.disable(context);
                }
                
                item.drawScreen(context, mouseX, mouseY, partialTicks);

                if (item.isHovering(mouseX, mouseY)) {
                    ScissorUtil.enable(context);
                }
                y += (float) item.getHeight() + 2f;
            }
        }

        ScissorUtil.disable(context);
    }
    
    // Helper method to draw rounded rectangles
    private void drawRoundedRect(GuiGraphics context, int x, int y, int width, int height, int radius, int color) {
        // For now, just draw regular rectangles
        // You can implement proper rounded corners if you have a RenderUtil method for it
        context.fill(x, y, x + width, y + height, color);
        
        // Optional: Add corner smoothing with small fills
        // This creates a pseudo-rounded effect
        if (radius > 0) {
            // Top-left corner
            context.fill(x, y, x + 1, y + 1, 0x00000000);
            // Top-right corner  
            context.fill(x + width - 1, y, x + width, y + 1, 0x00000000);
            // Bottom-left corner
            context.fill(x, y + height - 1, x + 1, y + height, 0x00000000);
            // Bottom-right corner
            context.fill(x + width - 1, y + height - 1, x + width, y + height, 0x00000000);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            OyVeyGui.getClickGui().getComponents().forEach(component -> {
                if (component.drag) {
                    component.drag = false;
                }
            });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            return;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            this.drag = false;
        }
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(String typedChar, int keyCode) {
        if (!this.open) {
            return;
        }
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!open) return;
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(Button button) {
        this.items.add(button);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final List<Item> getItems() {
        return this.items;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && 
               mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight();
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (Item item : this.getItems()) {
            height += (float) item.getHeight() + 2;
        }
        return height;
    }

    protected void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        context.drawString(mc.font, text, (int) x, (int) y, color);
    }
}
