/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public class AdvancementTab {
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final Identifier background;
    private final ItemStack icon;
    private final Component title;
    private final AdvancementWidget root;
    private final Map<AdvancementHolder, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;
    private @Nullable AdvancementWidget hovered;

    public AdvancementTab(Minecraft minecraft, AdvancementsScreen screen, AdvancementTabType type, int index, AdvancementWidget root, ItemStackTemplate icon, Component title, Identifier background) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.type = type;
        this.index = index;
        this.background = background;
        this.icon = icon.create();
        this.title = title;
        this.root = root;
        this.addWidget(root);
    }

    public void copyPosition(AdvancementTab source) {
        this.scrollX = source.scrollX;
        this.scrollY = source.scrollY;
        this.minX = source.minX;
        this.minY = source.minY;
        this.maxX = source.maxX;
        this.maxY = source.maxY;
        this.centered = source.centered;
    }

    public AdvancementTabType getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public AdvancementHolder getRootAdvancement() {
        return this.root.getAdvancement();
    }

    public Component getTitle() {
        return this.title;
    }

    public void tick(int relativeMouseX, int relativeMouseY) {
        boolean hovering = false;
        if (relativeMouseX > 0 && relativeMouseX < 234 && relativeMouseY > 0 && relativeMouseY < 113) {
            int intScrollX = Mth.floor(this.scrollX);
            int intScrollY = Mth.floor(this.scrollY);
            for (AdvancementWidget widget : this.widgets.values()) {
                if (!widget.isMouseOver(intScrollX, intScrollY, relativeMouseX, relativeMouseY)) continue;
                hovering = true;
                this.hovered = widget;
                break;
            }
        }
        if (hovering) {
            this.fade = Mth.clamp(this.fade + 0.06f, 0.0f, 0.3f);
        } else {
            this.fade = Mth.clamp(this.fade - 0.12f, 0.0f, 1.0f);
            if (this.hovered != null) {
                this.hovered = null;
            }
        }
    }

    public void extractTab(GuiGraphicsExtractor graphics, int xo, int yo, int mouseX, int mouseY, boolean selected) {
        int tabX = xo + this.type.getX(this.index);
        int tabY = yo + this.type.getY(this.index);
        this.type.extractRenderState(graphics, tabX, tabY, selected, this.index);
        if (!selected && mouseX > tabX && mouseY > tabY && mouseX < tabX + this.type.getWidth() && mouseY < tabY + this.type.getHeight()) {
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    public void extractIcon(GuiGraphicsExtractor graphics, int xo, int yo) {
        this.type.extractIcon(graphics, xo, yo, this.index, this.icon);
    }

    public void extractContents(GuiGraphicsExtractor graphics, int windowLeft, int windowTop) {
        if (!this.centered) {
            this.scrollX = 117 - (this.maxX + this.minX) / 2;
            this.scrollY = 56 - (this.maxY + this.minY) / 2;
            this.centered = true;
        }
        graphics.enableScissor(windowLeft, windowTop, windowLeft + 234, windowTop + 113);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)windowLeft, (float)windowTop);
        int intScrollX = Mth.floor(this.scrollX);
        int intScrollY = Mth.floor(this.scrollY);
        int left = intScrollX % 16;
        int top = intScrollY % 16;
        for (int x = -1; x <= 15; ++x) {
            for (int y = -1; y <= 8; ++y) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, left + 16 * x, top + 16 * y, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }
        this.root.extractConnectivity(graphics, intScrollX, intScrollY, true);
        this.root.extractConnectivity(graphics, intScrollX, intScrollY, false);
        this.root.extractRenderState(graphics, intScrollX, intScrollY);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }

    public void extractTooltips(GuiGraphicsExtractor graphics, int xo, int yo) {
        graphics.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0f) << 24);
        if (this.hovered != null) {
            int intScrollX = Mth.floor(this.scrollX);
            int intScrollY = Mth.floor(this.scrollY);
            this.hovered.extractHover(graphics, intScrollX, intScrollY, this.fade, xo, yo, this.screen.width);
        }
    }

    public boolean isMouseOver(int xo, int yo, double mx, double my) {
        return this.type.isMouseOver(xo, yo, this.index, mx, my);
    }

    public static @Nullable AdvancementTab create(Minecraft minecraft, AdvancementsScreen screen, int index, AdvancementNode root) {
        AdvancementWidget rootWidget = AdvancementWidget.createWidget(minecraft, root);
        if (rootWidget == null) {
            return null;
        }
        DisplayInfo rootDisplay = rootWidget.getDisplay();
        if (rootDisplay.background().isEmpty()) {
            return null;
        }
        Identifier background = rootDisplay.background().get().texturePath();
        for (AdvancementTabType type : AdvancementTabType.values()) {
            if (index >= type.getMax()) {
                index -= type.getMax();
                continue;
            }
            return new AdvancementTab(minecraft, screen, type, index, rootWidget, rootDisplay.icon(), rootDisplay.title(), background);
        }
        return null;
    }

    public void scroll(double x, double y) {
        if (this.canScrollHorizontally()) {
            this.scrollX = Mth.clamp(this.scrollX + x, (double)(-(this.maxX - 234)), 0.0);
        }
        if (this.canScrollVertically()) {
            this.scrollY = Mth.clamp(this.scrollY + y, (double)(-(this.maxY - 113)), 0.0);
        }
    }

    public boolean canScrollHorizontally() {
        return this.maxX - this.minX > 234;
    }

    public boolean canScrollVertically() {
        return this.maxY - this.minY > 113;
    }

    public void addAdvancement(AdvancementNode node) {
        AdvancementWidget widget = AdvancementWidget.createWidget(this.minecraft, node);
        if (widget != null) {
            this.addWidget(widget);
        }
    }

    private void addWidget(AdvancementWidget widget) {
        this.widgets.put(widget.getAdvancement(), widget);
        int x0 = widget.getX();
        int x1 = x0 + 28;
        int y0 = widget.getY();
        int y1 = y0 + 27;
        this.minX = Math.min(this.minX, x0);
        this.maxX = Math.max(this.maxX, x1);
        this.minY = Math.min(this.minY, y0);
        this.maxY = Math.max(this.maxY, y1);
        for (AdvancementWidget other : this.widgets.values()) {
            other.attachToParent(this);
        }
    }

    public @Nullable AdvancementWidget getWidget(AdvancementHolder advancement) {
        return this.widgets.get(advancement);
    }

    public AdvancementsScreen getScreen() {
        return this.screen;
    }
}

