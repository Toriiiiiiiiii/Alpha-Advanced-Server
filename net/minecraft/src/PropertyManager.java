package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyManager {
	public static Logger logger = Logger.getLogger("Minecraft");
	private Properties serverProperties = new Properties();
	private File propertiesFile;

	public PropertyManager(File var1) {
		this.propertiesFile = var1;
		if(var1.exists()) {
			try {
				this.serverProperties.load(new FileInputStream(var1));
			} catch (Exception var3) {
				logger.log(Level.WARNING, "Failed to load " + var1, var3);
				this.generateAndSaveProperties();
			}
		} else {
			logger.log(Level.WARNING, var1 + " does not exist");
			this.generateAndSaveProperties();
		}

	}

	public void generateAndSaveProperties() {
		logger.log(Level.INFO, "Generating new properties file");
		this.saveProperties();
	}

	public void saveProperties() {
		try {
			this.serverProperties.store(new FileOutputStream(this.propertiesFile), "Minecraft server properties");
		} catch (Exception var2) {
			logger.log(Level.WARNING, "Failed to save " + this.propertiesFile, var2);
			this.generateAndSaveProperties();
		}

	}

	public String getStringProperty(String var1, String var2) {
		if(!this.serverProperties.containsKey(var1)) {
			this.serverProperties.setProperty(var1, var2);
			this.saveProperties();
		}

		return this.serverProperties.getProperty(var1, var2);
	}

	public int getIntProperty(String var1, int var2) {
		try {
			return Integer.parseInt(this.getStringProperty(var1, "" + var2));
		} catch (Exception var4) {
			this.serverProperties.setProperty(var1, "" + var2);
			return var2;
		}
	}

	public boolean getBooleanProperty(String var1, boolean var2) {
		try {
			return Boolean.parseBoolean(this.getStringProperty(var1, "" + var2));
		} catch (Exception var4) {
			this.serverProperties.setProperty(var1, "" + var2);
			return var2;
		}
	}
}
