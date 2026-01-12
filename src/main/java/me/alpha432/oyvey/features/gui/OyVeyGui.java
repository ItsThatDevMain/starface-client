package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.gui.items.buttons.ModuleButton;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class OyVeyGui extends Screen {
    // Black & White Color Scheme
    public static final int COLOR_BACKGROUND = 0x78000000;        // Semi-transparent black
    public static final int COLOR_PANEL = 0xFF1A1A1A;            // Dark gray
    public static final int COLOR_PANEL_BORDER = 0xFF333333;     // Medium gray
    public static final int COLOR_BUTTON = 0xFF2A2A2A;           // Dark button
    public static final int COLOR_BUTTON_HOVER = 0xFF404040;     // Lighter on hover
    public static final int COLOR_BUTTON_ACTIVE = 0xFFFFFFFF;    // White when active
    public static final int COLOR_TEXT = 0xFFFFFFFF;             // White text
    public static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA;   // Gray text
    public static final int COLOR_ACCENT = 0xFFFFFFFF;           // White accent
    public static final int COLOR_DIVIDER = 0xFF2A2A2A;          // Divider line
    
    private static OyVeyGui INSTANCE;
    private static Color colorClipboard = null;

    static {
        INSTANCE = new OyVeyGui();
    }

    private final ArrayList<Widget> widgets = new ArrayList<>();

    public OyVeyGui() {
        super(Component.literal("OyVey"));
        setInstance();
        load();
    }

    public static OyVeyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OyVeyGui();
        }
        return INSTANCE;
    }

    public static OyVeyGui getClickGui() {
        return OyVeyGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -84;
        for (Module.Category category : OyVey.moduleManager.getCategories()) {
            if (category == Module.Category.HUD) continue;
            Widget panel = new Widget(category.getName(), x += 90, 4, true);
            OyVey.moduleManager.stream()
                    .filter(m -> m.getCategory() == category && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            this.widgets.add(panel);
        }
        this.widgets.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Item.context = context;
        
        // Render black background with subtle transparency
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), COLOR_BACKGROUND);
        
        // Render subtle grid pattern for visual depth
        renderGrid(context);
        
        // Render all widgets
        this.widgets.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));
        
        // Render client name/watermark
        renderWatermark(context);
    }
    
    private void renderGrid(GuiGraphics context) {
        int gridSize = 25;
        int gridColor = 0x0A0A0A0A; // Very subtle grid with transparency
        
        // Vertical lines
        for (int x = 0; x < context.guiWidth(); x += gridSize) {
            context.fill(x, 0, x + 1, context.guiHeight(), gridColor);
        }
        
        // Horizontal lines
        for (int y = 0; y < context.guiHeight(); y += gridSize) {
            context.fill(0, y, context.guiWidth(), y + 1, gridColor);
        }
    }
    
    private void renderWatermark(GuiGraphics context) {
        String watermark = "OYVEY";
        int watermarkWidth = this.font.width(watermark);
        int watermarkX = context.guiWidth() - watermarkWidth - 5;
        int watermarkY = context.guiHeight() - this.font.lineHeight - 5;
        
        // Shadow effect
        context.drawString(this.font, watermark, watermarkX + 1, watermarkY + 1, 0x50000000, false);
        // Main text
        context.drawString(this.font, watermark, watermarkX, watermarkY, COLOR_TEXT_SECONDARY, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.widgets.forEach(components -> components.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.widgets.forEach(components -> components.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.widgets.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.widgets.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Ignore 1.21.8 blur thing
    }

    public final ArrayList<Widget> getComponents() {
        return this.widgets;
    }

    public int getTextOffset() {
        return -6;
    }

    public static Color getColorClipboard() {
        return colorClipboard;
    }

    public static void setColorClipboard(Color color) {
        colorClipboard = color;
    }
}
