package net.lepko.easycrafting.core.inventory.gui;

import net.lepko.easycrafting.core.block.TileEntityAutoCrafting;
import net.lepko.easycrafting.core.block.TileEntityAutoCrafting.Mode;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.network.packet.PacketInterfaceChange;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class ButtonMode {

    public final GuiAutoCrafting gui;
    public final int x, y, w, h, u, v;

    public ButtonMode(GuiAutoCrafting gui, int x, int y, int w, int h, int u, int v) {
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.u = u;
        this.v = v;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public void render(int mouseX, int mouseY, Mode mode) {
        int modeOrdinal = mode == null ? 4 : mode.ordinal();
        int offsetV = isMouseOver(mouseX, mouseY) ? 16 : 0;
        gui.drawTexturedModalRect(x, y, u, v + offsetV, w, h);
        GL11.glPushMatrix();
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            gui.drawTexturedModalRect(x, y, u - 16, modeOrdinal * 16, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL11.glPopMatrix();
    }

    public void renderTooltip(int mouseX, int mouseY, Mode mode) {
        if (isMouseOver(mouseX, mouseY) && mode != null) {
            gui.drawHoverText(I18n.format(mode.tooltip), mouseX, mouseY);
        }
    }

    public void click(int mouseX, int mouseY, int mouseButton, TileEntityAutoCrafting tileEntity) {
        if (isMouseOver(mouseX, mouseY)) {
            tileEntity.cycleModes(mouseButton);
            PacketHandler.sendPacket(new PacketInterfaceChange(0, tileEntity.getMode().ordinal()));
        }
    }
}
