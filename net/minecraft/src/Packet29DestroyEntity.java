package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet29DestroyEntity extends Packet {
	public int entityId;

	public Packet29DestroyEntity() {
	}

	public Packet29DestroyEntity(int var1) {
		this.entityId = var1;
	}

	public void readPacketData(DataInputStream var1) throws IOException {
		this.entityId = var1.readInt();
	}

	public void writePacket(DataOutputStream var1) throws IOException {
		var1.writeInt(this.entityId);
	}

	public void processPacket(NetHandler var1) {
		var1.handleDestroyEntity(this);
	}

	public int getPacketSize() {
		return 4;
	}
}
