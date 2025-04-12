package net.minecraft.src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class GuiStatsListener implements ActionListener {
	final GuiStatsComponent component;

	GuiStatsListener(GuiStatsComponent var1) {
		this.component = var1;
	}

	public void actionPerformed(ActionEvent var1) {
		GuiStatsComponent.update(this.component);
	}
}
