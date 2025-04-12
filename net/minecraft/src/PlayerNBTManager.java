package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;

public class PlayerNBTManager {
	public static Logger logger = Logger.getLogger("Minecraft");
	private File playerNBT;

	public PlayerNBTManager(File var1) {
		this.playerNBT = var1;
		var1.mkdir();
	}

	public void writePlayerNBT(EntityPlayerMP var1) {
		try {
			NBTTagCompound var2 = new NBTTagCompound();
			var1.writeToNBT(var2);
			File var3 = new File(this.playerNBT, "_tmp_.dat");
			File var4 = new File(this.playerNBT, var1.username + ".dat");
			CompressedStreamTools.writeCompressed(var2, new FileOutputStream(var3));
			if(var4.exists()) {
				var4.delete();
			}

			var3.renameTo(var4);
		} catch (Exception var5) {
			logger.warning("Failed to save player data for " + var1.username);
		}

	}

	public void readPlayerNBT(EntityPlayerMP var1) {
		try {
			File var2 = new File(this.playerNBT, var1.username + ".dat");
			if(var2.exists()) {
				NBTTagCompound var3 = CompressedStreamTools.readCompressed(new FileInputStream(var2));
				if(var3 != null) {
					var1.readFromNBT(var3);
				}
			}
		} catch (Exception var4) {
			logger.warning("Failed to load player data for " + var1.username);
		}

	}
}
