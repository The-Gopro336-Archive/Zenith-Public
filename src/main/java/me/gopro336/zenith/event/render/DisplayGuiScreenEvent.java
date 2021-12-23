package me.gopro336.zenith.event.render;

import me.gopro336.zenith.event.EventCancellable;
import net.minecraft.client.gui.GuiScreen;

/**
 * @author Robeart
 */
public class DisplayGuiScreenEvent extends EventCancellable {
	
	private GuiScreen guiScreen;
	
	public DisplayGuiScreenEvent(GuiScreen guiScreen) {
		this.guiScreen = guiScreen;
	}
	
	public GuiScreen getGuiScreen() {
		return guiScreen;
	}
	
	public void setGuiScreen(GuiScreen guiScreen) {
		this.guiScreen = guiScreen;
	}
}
