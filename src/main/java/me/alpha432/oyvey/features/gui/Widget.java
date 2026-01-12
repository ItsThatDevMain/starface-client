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
    // Black & White Color Scheme
    private static final int COLOR_HEADER = 0xFF1A1A1A;          // Dark gray header
    private static final int COLOR_HEADER_BORDER = 0xFF333333;   // Medium gray border
    private static final int COLOR_BODY = 0xE6000000;            // Semi-transparent black body
    private static final int COLOR_BODY_BORDER = 0xFF1A1A1A;     // Dark border
    private static final int COLOR_TEXT = 0xFFFFFFFF;            // White text
    private static final int COLOR_ACCENT = 0xFFFFFFFF;          // White accent
    private static final int COLOR_HOVER = 0x30FFFFFF;           // Subtle white hover
    
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

    public Widget(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width = 88;
        this.height = 18;
        this.open = open;
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
        float totalItemHeight = this.open ? this.getTotalItemHeight() - 2.0f : 0.0f;
        
        boolean isHovering = this.isHovering(mouseX, mouseY);
        
        // Draw header background
        context.fill(this.x, this.y - 1, this.x + this.width, this.y + this.height - 6, 
                     isHovering ? COLOR_HEADER_BORDER : COLOR_HEADER);
        
        // Draw header top accent line (white)
        context.fill(this.x, this.y - 1, this.x + this.width, this.y, COLOR_ACCENT);
        
        // Draw header bottom border
        context.fill(this.x, this.y + this.height - 7, this.x + this.width, this.y + this.height - 6, 
                     COLOR_HEADER_BORDER);
        
        if (this.open) {
            // Draw body background
            RenderUtil.rect(context, this.x, (float) this.y + 12.5f, this.x + this.width, 
                          (float) (this.y + this.height) + totalItemHeight, COLOR_BODY);
            
            // Draw body border
            // Left border
            context.fill(this.x, this.y + this.height - 6, this.x + 1, 
                        (int)((float) (this.y + this.height) + totalItemHeight), COLOR_BODY_BORDER);
            // Right border
            context.fill(this.x + this.width - 1, this.y + this.height - 6, this.x + this.width, 
                        (int)((float) (this.y + this.height) + totalItemHeight), COLOR_BODY_BORDER);
            // Bottom border
            context.fill(this.x, (int)((float) (this.y + this.height) + totalItemHeight) - 1, 
                        this.x + this.width, (int)((float) (this.y + this.height) + totalItemHeight), 
                        COLOR_BODY_BORDER);
        }
        
        // Draw category name
        drawString(this.getName().toUpperCase(), (float) this.x + 3.0f, 
                  (float) this.y - 4.0f - (float) OyVeyGui.getClickGui().getTextOffset(), COLOR_TEXT);
        
        // Draw expand/collapse indicator
        String indicator = this.open ? "-" : "+";
        int indicatorWidth = mc.font.width(indicator);
        drawString(indicator, (float) (this.x + this.width - indicatorWidth - 3), 
                  (float) this.y - 4.0f - (float) OyVeyGui.getClickGui().getTextOffset(), COLOR_TEXT);
        
        ScissorUtil.enable(context, x, 0, x + width, mc.getWindow().getGuiScaledHeight());

        if (this.open) {
            float y = (float) (this.getY() + this.getHeight()) - 3.0f;
            for (Item item : this.getItems()) {
                if (item.isHidden()) continue;
                item.setLocation((float) this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
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
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
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
