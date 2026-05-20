package dev.merosssany.cinematica.renderer.ui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import java.util.List;

public class InputScrollEntry extends ContainerObjectSelectionList.Entry<InputScrollEntry> {
    private final Minecraft minecraft;
    private final String labelText;
    private final EditBox inputField;
    private final List<GuiEventListener> children;

    public InputScrollEntry(Minecraft minecraft, String labelText, String initialValue) {
        this.minecraft = minecraft;
        this.labelText = labelText;
        
        // Define positioning dimensions relative to the row's bounds
        // Width is split: Label on left, input box on right
        int inputWidth = 140;
        int inputHeight = 18;
        
        this.inputField = new EditBox(
                minecraft.font, 
                0, 0, // X and Y will be adjusted dynamically during render passes
                inputWidth, inputHeight, 
                Component.literal(labelText)
        );
        this.inputField.setValue(initialValue);
        
        // Track the EditBox as a focusable sub-child component
        this.children = ImmutableList.of(this.inputField);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
        // Draw the row description text on the left hand side
        guiGraphics.drawString(
                this.minecraft.font, 
                this.labelText, 
                left, 
                top + (rowHeight / 2) - (this.minecraft.font.lineHeight / 2), 
                0xAAAAAA
        );
        
        // Position and adjust the text input box to sit cleanly on the right hand side of the row
        this.inputField.setX(left + rowWidth - this.inputField.getWidth() - 4);
        this.inputField.setY(top + (rowHeight / 2) - (this.inputField.getHeight() / 2));
        
        this.inputField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    // Fetch value later when compiling inputs
    public String getValue() {
        return this.inputField.getValue();
    }
    
    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of();
    }
}