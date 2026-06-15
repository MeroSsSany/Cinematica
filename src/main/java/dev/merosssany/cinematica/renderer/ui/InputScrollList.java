package dev.merosssany.cinematica.renderer.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class InputScrollList extends ContainerObjectSelectionList<InputScrollEntry> {
    public InputScrollList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }
    
    public void addInputEntry(String label, String initialValue) {
        this.addEntry(new InputScrollEntry(this.minecraft, label, initialValue));
    }

    @Override
    public int getRowWidth() {
        return 260;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + getRowWidth() / 2 + 10;
    }
}