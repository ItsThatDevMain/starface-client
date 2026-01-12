package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.buttons.ModuleButton;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.HudModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;

public class HudEditorScreen extends Screen {
    // Black & White Color Scheme
    public static final int COLOR_BACKGROUND = 0xFF000000;        // Pure black
    public static final int COLOR_PANEL = 0xFF1A1A1A;            // Dark gray
    public static final int COLOR_PANEL_BORDER = 0xFF333333;     // Medium gray
    public static final int COLOR_BUTTON = 0xFF2A2A2A;           // Dark button
    public static final int COLOR_BUTTON_HOVER = 0xFF404040;     // Lighter on hover
    public static final int COLOR_BUTTON_ACTIVE = 0xFFFFFFFF;    // White when active
    public static final int COLOR_TEXT = 0xFFFFFFFF;             // White text
    public static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA;   // Gray text
    public static final int COLOR_ACCENT = 0xFFFFFFFF;           // White accent
    public static final int COLOR_DIVIDER = 0xFF2A2A2A;          // Divider line
    
    private final ArrayList<Widget> components = new ArrayList<>();
    public HudModule currentDragging;
    public boolean anyHover;
    
    public HudEditorScreen() {
        super(Component.literal("oyvey-hudeditor"));
        load();
    }
    
    private void load() {
        Widget hud = new Widget("Hud", 50, 50, true);
        OyVey.moduleManager.stream()
                .filter(m -> m.getCategory() == Module.Category.HUD && !m.hidden)
                .map(ModuleButton::new)
                .forEach(hud::addButton);
        this.components.add(hud);
        this.components.forEach(component -> component.getItems().sort(Comparator.comparing(Feature::getName)));
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Render dark background
        context.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);
        
        // Render subtle grid pattern (optional, for visual depth)
        renderGrid(context);
        
        anyHover = false;
        this.components.forEach(component -> component.drawScreen(context, mouseX, mouseY, delta));
        
        // Render watermark or title
        renderTitle(context);
    }
    
    private void renderGrid(GuiGraphics context) {
        int gridSize = 20;
        int gridColor = 0xFF0A0A0A; // Very subtle grid
        
        // Vertical lines
        for (int x = 0; x < this.width; x += gridSize) {
            context.fill(x, 0, x + 1, this.height, gridColor);
        }
        
        // Horizontal lines
        for (int y = 0; y < this.height; y += gridSize) {
            context.fill(0, y, this.width, y + 1, gridColor);
        }
    }
    
    private void renderTitle(GuiGraphics context) {
        String title = "HUD EDITOR";
        int titleWidth = this.font.width(title);
        int titleX = (this.width - titleWidth) / 2;
        int titleY = 10;
        
        // Shadow effect
        context.drawString(this.font, title, titleX + 1, titleY + 1, 0xFF000000, false);
        // Main text
        context.drawString(this.font, title, titleX, titleY, COLOR_TEXT, false);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.components.forEach(component -> component.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.components.forEach(component -> component.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.components.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.components.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public boolean keyPressed(KeyEvent input) {
        this.components.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }
    
    @Override
    public boolean charTyped(CharacterEvent input) {
        this.components.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
        return super.charTyped(input);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override // ignore 1.21.8 menu blur thing
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
    
    public ArrayList<Widget> getComponents() {
        return components;
    }
}
