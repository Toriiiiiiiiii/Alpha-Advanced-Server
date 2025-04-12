package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet23VehicleSpawn extends Packet {
	public int entityId;
	public int xPosition;
	public int yPosition;
	public int zPosition;
	public int type;

	public Packet23VehicleSpawn() {
	}

	public Packet23VehicleSpawn(Entity var1, int var2) {
		this.entityId = var1.entityID;
		this.xPosition = MathHelper.floor_double(var1.posX * 32.0D);
		this.yPosition = MathHelper.floor_double(var1.posY * 32.0D);
		this.zPosition = MathHelper.floor_double(var1.posZ * 32.0D);
		this.type = var2;
	}

	public void readPacketData(DataInputStream var1) throws IOException {
		this.entityId = var1.readInt();
		this.type = var1.readByte();
		this.xPosition = var1.readInt();
		this.yPosition = var1.readInt();
		this.zPosition = var1.readInt();
	}

	public void writePacket(DataOutputStream var1) throws IOException {
		var1.writeInt(this.entityId);
		var1.writeByte(this.type);
		var1.writeInt(this.xPosition);
		var1.writeInt(this.yPosition);
		var1.writeInt(this.zPosition);
	}

	public void processPacket(NetHandler var1) {
		var1.handleVehicleSpawn(this);
	}

	public int getPacketSize() {
		return 17;
	}
}
