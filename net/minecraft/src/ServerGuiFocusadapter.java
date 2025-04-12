package net.minecraft.src;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

class ServerGuiFocusadapter extends FocusAdapter {
	final ServerGUI mcServerGui;

	ServerGuiFocusadapter(ServerGUI var1) {
		this.mcServerGui = var1;
	}

	public void focusGained(FocusEvent var1) {
	}
}
