package net.minecraft.src;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.swing.JTextArea;

public class GuiLogOutputHandler extends Handler {
	private int[] allNums = new int[1024];
	private int currentNum = 0;
	Formatter formatter = new GuiLogFormatter(this);
	private JTextArea textArea;

	public GuiLogOutputHandler(JTextArea var1) {
		this.setFormatter(this.formatter);
		this.textArea = var1;
	}

	public void close() {
	}

	public void flush() {
	}

	public void publish(LogRecord var1) {
		int var2 = this.textArea.getDocument().getLength();
		this.textArea.append(this.formatter.format(var1));
		this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
		int var3 = this.textArea.getDocument().getLength() - var2;
		if(this.allNums[this.currentNum] != 0) {
			this.textArea.replaceRange("", 0, this.allNums[this.currentNum]);
		}

		this.allNums[this.currentNum] = var3;
		this.currentNum = (this.currentNum + 1) % 1024;
	}
}
