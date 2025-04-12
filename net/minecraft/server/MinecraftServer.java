package net.minecraft.server;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.ConsoleLogManager;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.EntityTracker;
import net.minecraft.src.ICommandListener;
import net.minecraft.src.IProgressUpdate;
import net.minecraft.src.IUpdatePlayerListBox;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NetworkListenThread;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.Packet4UpdateTime;
import net.minecraft.src.PropertyManager;
import net.minecraft.src.ServerCommand;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.ServerGUI;
import net.minecraft.src.ThreadCommandReader;
import net.minecraft.src.ThreadServerApplication;
import net.minecraft.src.ThreadSleepForeverServer;
import net.minecraft.src.Vec3D;
import net.minecraft.src.WorldManager;
import net.minecraft.src.WorldServer;

public class MinecraftServer implements ICommandListener, Runnable {
	public static Logger logger = Logger.getLogger("Minecraft");
	public static HashMap playerList = new HashMap();
	public NetworkListenThread networkServer;
	public PropertyManager propertyManagerObj;
	public WorldServer worldMngr;
	public ServerConfigurationManager configManager;
	private boolean serverRunning = true;
	public boolean serverStopped = false;
	int deathTime = 0;
	public String currentTask;
	public int percentDone;
	private List playersOnline = new ArrayList();
	private List commands = Collections.synchronizedList(new ArrayList());
	public EntityTracker entityTracker;
	public boolean onlineMode;

	public MinecraftServer() {
		new ThreadSleepForeverServer(this);
	}

	private boolean startServer() throws IOException {
		ThreadCommandReader var1 = new ThreadCommandReader(this);
		var1.setDaemon(true);
		var1.start();
		ConsoleLogManager.init();
		logger.info("Starting minecraft server version 0.2.1");
		if(Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
			logger.warning("**** NOT ENOUGH RAM!");
			logger.warning("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
		}

		logger.info("Loading properties");
		this.propertyManagerObj = new PropertyManager(new File("server.properties"));
		String var2 = this.propertyManagerObj.getStringProperty("server-ip", "");
		this.onlineMode = this.propertyManagerObj.getBooleanProperty("online-mode", true);
		InetAddress var3 = null;
		if(var2.length() > 0) {
			var3 = InetAddress.getByName(var2);
		}

		int var4 = this.propertyManagerObj.getIntProperty("server-port", 25565);
		logger.info("Starting Minecraft server on " + (var2.length() == 0 ? "*" : var2) + ":" + var4);

		try {
			this.networkServer = new NetworkListenThread(this, var3, var4);
		} catch (IOException var6) {
			logger.warning("**** FAILED TO BIND TO PORT!");
			logger.log(Level.WARNING, "The exception was: " + var6.toString());
			logger.warning("Perhaps a server is already running on that port?");
			return false;
		}

		if(!this.onlineMode) {
			logger.warning("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
			logger.warning("The server will make no attempt to authenticate usernames. Beware.");
			logger.warning("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
			logger.warning("To change this, set \"online-mode\" to \"true\" in the server.settings file.");
		}

		this.configManager = new ServerConfigurationManager(this);
		this.entityTracker = new EntityTracker(this);
		String var5 = this.propertyManagerObj.getStringProperty("level-name", "world");
		logger.info("Preparing level \"" + var5 + "\"");
		this.initWorld(var5);
		logger.info("Done! For help, type \"help\" or \"?\"");
		return true;
	}

	private void initWorld(String var1) {
		logger.info("Preparing start region");
		this.worldMngr = new WorldServer(new File("."), var1, this.propertyManagerObj.getBooleanProperty("monsters", false));
		this.worldMngr.addWorldAccess(new WorldManager(this));
		this.worldMngr.difficultySetting = 1;
		this.configManager.setPlayerManager(this.worldMngr);
		byte var2 = 10;

		for(int var3 = -var2; var3 <= var2; ++var3) {
			this.outputPercentRemaining("Preparing spawn area", (var3 + var2) * 100 / (var2 + var2 + 1));

			for(int var4 = -var2; var4 <= var2; ++var4) {
				if(!this.serverRunning) {
					return;
				}

				this.worldMngr.chunkProviderServer.loadChunk((this.worldMngr.spawnX >> 4) + var3, (this.worldMngr.spawnZ >> 4) + var4);
			}
		}

		this.clearCurrentTask();
	}

	private void outputPercentRemaining(String var1, int var2) {
		this.currentTask = var1;
		this.percentDone = var2;
		System.out.println(var1 + ": " + var2 + "%");
	}

	private void clearCurrentTask() {
		this.currentTask = null;
		this.percentDone = 0;
	}

	private void save() {
		logger.info("Saving chunks");
		this.worldMngr.saveWorld(true, (IProgressUpdate)null);
	}

	private void stop() {
		logger.info("Stopping server");
		if(this.configManager != null) {
			this.configManager.savePlayerStates();
		}

		if(this.worldMngr != null) {
			this.save();
		}

	}

	public void stopRunning() {
		this.serverRunning = false;
	}

	public void run() {
		try {
			if(this.startServer()) {
				long var1 = System.currentTimeMillis();
				long var3 = 0L;

				while(this.serverRunning) {
					long var5 = System.currentTimeMillis();
					long var7 = var5 - var1;
					if(var7 > 2000L) {
						logger.warning("Can\'t keep up! Did the system time change, or is the server overloaded?");
						var7 = 2000L;
					}

					if(var7 < 0L) {
						logger.warning("Time ran backwards! Did the system time change?");
						var7 = 0L;
					}

					var3 += var7;
					var1 = var5;

					while(var3 > 50L) {
						var3 -= 50L;
						this.doTick();
					}

					Thread.sleep(1L);
				}
			} else {
				while(this.serverRunning) {
					this.commandLineParser();

					try {
						Thread.sleep(10L);
					} catch (InterruptedException var15) {
						var15.printStackTrace();
					}
				}
			}
		} catch (Exception var16) {
			var16.printStackTrace();
			logger.log(Level.SEVERE, "Unexpected exception", var16);

			while(this.serverRunning) {
				this.commandLineParser();

				try {
					Thread.sleep(10L);
				} catch (InterruptedException var14) {
					var14.printStackTrace();
				}
			}
		} finally {
			this.stop();
			this.serverStopped = true;
			System.exit(0);
		}

	}

	private void doTick() throws IOException {
		ArrayList var1 = new ArrayList();
		Iterator var2 = playerList.keySet().iterator();

		while(var2.hasNext()) {
			String var3 = (String)var2.next();
			int var4 = ((Integer)playerList.get(var3)).intValue();
			if(var4 > 0) {
				playerList.put(var3, Integer.valueOf(var4 - 1));
			} else {
				var1.add(var3);
			}
		}

		int var6;
		for(var6 = 0; var6 < var1.size(); ++var6) {
			playerList.remove(var1.get(var6));
		}

		AxisAlignedBB.clearBoundingBoxPool();
		Vec3D.initialize();
		++this.deathTime;
		if(this.deathTime % 20 == 0) {
			this.configManager.sendPacketToAllPlayers(new Packet4UpdateTime(this.worldMngr.worldTime));
		}

		this.worldMngr.tick();

		while(this.worldMngr.updatingLighting()) {
		}

		this.worldMngr.updateEntities();
		this.networkServer.handleNetworkListenThread();
		this.configManager.onTick();
		this.entityTracker.updateTrackedEntities();

		for(var6 = 0; var6 < this.playersOnline.size(); ++var6) {
			((IUpdatePlayerListBox)this.playersOnline.get(var6)).addAllPlayers();
		}

		try {
			this.commandLineParser();
		} catch (Exception var5) {
			logger.log(Level.WARNING, "Unexpected exception while parsing console command", var5);
		}

	}

	public void addCommand(String var1, ICommandListener var2) {
		this.commands.add(new ServerCommand(var1, var2));
	}

	public void commandLineParser() {
		while(this.commands.size() > 0) {
			ServerCommand var1 = (ServerCommand)this.commands.remove(0);
			String var2 = var1.command;
			ICommandListener var3 = var1.commandListener;
			String var4 = var3.getUsername();
			if(!var2.toLowerCase().startsWith("help") && !var2.toLowerCase().startsWith("?")) {
				if(var2.toLowerCase().startsWith("list")) {
					var3.addHelpCommandMessage("Connected players: " + this.configManager.getPlayerList());
				} else if(var2.toLowerCase().startsWith("stop")) {
					this.print(var4, "Stopping the server..");
					this.serverRunning = false;
				} else if(var2.toLowerCase().startsWith("save-all")) {
					this.print(var4, "Forcing save..");
					this.worldMngr.saveWorld(true, (IProgressUpdate)null);
					this.print(var4, "Save complete.");
				} else if(var2.toLowerCase().startsWith("save-off")) {
					this.print(var4, "Disabling level saving..");
					this.worldMngr.levelSaving = true;
				} else if(var2.toLowerCase().startsWith("save-on")) {
					this.print(var4, "Enabling level saving..");
					this.worldMngr.levelSaving = false;
				} else {
					String var11;
					if(var2.toLowerCase().startsWith("op ")) {
						var11 = var2.substring(var2.indexOf(" ")).trim();
						this.configManager.opPlayer(var11);
						this.print(var4, "Opping " + var11);
						this.configManager.sendChatMessageToPlayer(var11, "\u00a7eYou are now op!");
					} else if(var2.toLowerCase().startsWith("deop ")) {
						var11 = var2.substring(var2.indexOf(" ")).trim();
						this.configManager.deopPlayer(var11);
						this.configManager.sendChatMessageToPlayer(var11, "\u00a7eYou are no longer op!");
						this.print(var4, "De-opping " + var11);
					} else if(var2.toLowerCase().startsWith("ban-ip ")) {
						var11 = var2.substring(var2.indexOf(" ")).trim();
						this.configManager.banIP(var11);
						this.print(var4, "Banning ip " + var11);
					} else if(var2.toLowerCase().startsWith("pardon-ip ")) {
						var11 = var2.substring(var2.indexOf(" ")).trim();
						this.configManager.pardonIP(var11);
						this.print(var4, "Pardoning ip " + var11);
					} else {
						EntityPlayerMP var12;
						if(var2.toLowerCase().startsWith("ban ")) {
							var11 = var2.substring(var2.indexOf(" ")).trim();
							this.configManager.banPlayer(var11);
							this.print(var4, "Banning " + var11);
							var12 = this.configManager.getPlayerEntity(var11);
							if(var12 != null) {
								var12.playerNetServerHandler.kickPlayer("Banned by admin");
							}
						} else if(var2.toLowerCase().startsWith("pardon ")) {
							var11 = var2.substring(var2.indexOf(" ")).trim();
							this.configManager.pardonPlayer(var11);
							this.print(var4, "Pardoning " + var11);
						} else if(var2.toLowerCase().startsWith("kick ")) {
							var11 = var2.substring(var2.indexOf(" ")).trim();
							var12 = null;

							for(int var13 = 0; var13 < this.configManager.playerEntities.size(); ++var13) {
								EntityPlayerMP var14 = (EntityPlayerMP)this.configManager.playerEntities.get(var13);
								if(var14.username.equalsIgnoreCase(var11)) {
									var12 = var14;
								}
							}

							if(var12 != null) {
								var12.playerNetServerHandler.kickPlayer("Kicked by admin");
								this.print(var4, "Kicking " + var12.username);
							} else {
								var3.addHelpCommandMessage("Can\'t find user " + var11 + ". No kick.");
							}
						} else {
							String[] var5;
							EntityPlayerMP var7;
							if(var2.toLowerCase().startsWith("tp ")) {
								var5 = var2.split(" ");
								if(var5.length == 3) {
									var12 = this.configManager.getPlayerEntity(var5[1]);
									var7 = this.configManager.getPlayerEntity(var5[2]);
									if(var12 == null) {
										var3.addHelpCommandMessage("Can\'t find user " + var5[1] + ". No tp.");
									} else if(var7 == null) {
										var3.addHelpCommandMessage("Can\'t find user " + var5[2] + ". No tp.");
									} else {
										var12.playerNetServerHandler.teleportTo(var7.posX, var7.posY, var7.posZ, var7.rotationYaw, var7.rotationPitch);
										this.print(var4, "Teleporting " + var5[1] + " to " + var5[2] + ".");
									}
								} else {
									var3.addHelpCommandMessage("Syntax error, please provice a source and a target.");
								}
							} else if(var2.toLowerCase().startsWith("give ")) {
								var5 = var2.split(" ");
								if(var5.length != 3 && var5.length != 4) {
									return;
								}

								String var6 = var5[1];
								var7 = this.configManager.getPlayerEntity(var6);
								if(var7 != null) {
									try {
										int var8 = Integer.parseInt(var5[2]);
										if(Item.itemsList[var8] != null) {
											this.print(var4, "Giving " + var7.username + " some " + var8);
											int var9 = 1;
											if(var5.length > 3) {
												var9 = this.parseInt(var5[3], 1);
											}

											if(var9 < 1) {
												var9 = 1;
											}

											if(var9 > 64) {
												var9 = 64;
											}

											var7.dropPlayerItem(new ItemStack(var8, var9));
										} else {
											var3.addHelpCommandMessage("There\'s no item with id " + var8);
										}
									} catch (NumberFormatException var10) {
										var3.addHelpCommandMessage("There\'s no item with id " + var5[2]);
									}
								} else {
									var3.addHelpCommandMessage("Can\'t find user " + var6);
								}
							} else if(var2.toLowerCase().startsWith("say ")) {
								var2 = var2.substring(var2.indexOf(" ")).trim();
								logger.info("[" + var4 + "] " + var2);
								this.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7d[Server] " + var2));
							} else if(var2.toLowerCase().startsWith("tell ")) {
								var5 = var2.split(" ");
								if(var5.length >= 3) {
									var2 = var2.substring(var2.indexOf(" ")).trim();
									var2 = var2.substring(var2.indexOf(" ")).trim();
									logger.info("[" + var4 + "->" + var5[1] + "] " + var2);
									this.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7d[Server] " + var2));
									var2 = "\u00a77" + var4 + " whispers " + var2;
									logger.info(var2);
									if(!this.configManager.sendPacketToPlayer(var5[1], new Packet3Chat(var2))) {
										var3.addHelpCommandMessage("There\'s no player by that name online.");
									}
								}
							} else {
								logger.info("Unknown console command. Type \"help\" for help.");
							}
						}
					}
				}
			} else {
				var3.addHelpCommandMessage("To run the server without a gui, start it like this:");
				var3.addHelpCommandMessage("   java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui");
				var3.addHelpCommandMessage("Console commands:");
				var3.addHelpCommandMessage("   help  or  ?               shows this message");
				var3.addHelpCommandMessage("   kick <player>             removes a player from the server");
				var3.addHelpCommandMessage("   ban <player>              bans a player from the server");
				var3.addHelpCommandMessage("   pardon <player>           pardons a banned player so that they can connect again");
				var3.addHelpCommandMessage("   ban-ip <ip>               bans an IP address from the server");
				var3.addHelpCommandMessage("   pardon-ip <ip>            pardons a banned IP address so that they can connect again");
				var3.addHelpCommandMessage("   op <player>               turns a player into an op");
				var3.addHelpCommandMessage("   deop <player>             removes op status from a player");
				var3.addHelpCommandMessage("   tp <player1> <player2>    moves one player to the same location as another player");
				var3.addHelpCommandMessage("   give <player> <id> [num]  gives a player a resource");
				var3.addHelpCommandMessage("   tell <player> <message>   sends a private message to a player");
				var3.addHelpCommandMessage("   stop                      gracefully stops the server");
				var3.addHelpCommandMessage("   save-all                  forces a server-wide level save");
				var3.addHelpCommandMessage("   save-off                  disables terrain saving (useful for backup scripts)");
				var3.addHelpCommandMessage("   save-on                   re-enables terrain saving");
				var3.addHelpCommandMessage("   list                      lists all currently connected players");
				var3.addHelpCommandMessage("   say <message>             broadcasts a message to all players");
			}
		}

	}

	private void print(String var1, String var2) {
		String var3 = var1 + ": " + var2;
		this.configManager.sendChatMessageToAllOps("\u00a77(" + var3 + ")");
		logger.info(var3);
	}

	private int parseInt(String var1, int var2) {
		try {
			return Integer.parseInt(var1);
		} catch (NumberFormatException var4) {
			return var2;
		}
	}

	public void addToOnlinePlayerList(IUpdatePlayerListBox var1) {
		this.playersOnline.add(var1);
	}

	public static void main(String[] var0) {
		try {
			MinecraftServer var1 = new MinecraftServer();
			if(!GraphicsEnvironment.isHeadless() && (var0.length <= 0 || !var0[0].equals("nogui"))) {
				ServerGUI.initGui(var1);
			}

			(new ThreadServerApplication("Server thread", var1)).start();
		} catch (Exception var2) {
			logger.log(Level.SEVERE, "Failed to start the minecraft server", var2);
		}

	}

	public File getFile(String var1) {
		return new File(var1);
	}

	public void addHelpCommandMessage(String var1) {
		logger.info(var1);
	}

	public String getUsername() {
		return "CONSOLE";
	}

	public static boolean isServerRunning(MinecraftServer var0) {
		return var0.serverRunning;
	}
}
