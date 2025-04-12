package net.minecraft.src;

class MCHashEntry2 {
	final long hashEntry;
	Object valueEntry;
	MCHashEntry2 nextEntry;
	final int slotHash;

	MCHashEntry2(int var1, long var2, Object var4, MCHashEntry2 var5) {
		this.valueEntry = var4;
		this.nextEntry = var5;
		this.hashEntry = var2;
		this.slotHash = var1;
	}

	public final long getHash() {
		return this.hashEntry;
	}

	public final Object getValue() {
		return this.valueEntry;
	}

	public final boolean equals(Object var1) {
		if(!(var1 instanceof MCHashEntry2)) {
			return false;
		} else {
			MCHashEntry2 var2 = (MCHashEntry2)var1;
			Long var3 = Long.valueOf(this.getHash());
			Long var4 = Long.valueOf(var2.getHash());
			if(var3 == var4 || var3 != null && var3.equals(var4)) {
				Object var5 = this.getValue();
				Object var6 = var2.getValue();
				if(var5 == var6 || var5 != null && var5.equals(var6)) {
					return true;
				}
			}

			return false;
		}
	}

	public final int hashCode() {
		return MCHashTable2.getHash(this.hashEntry);
	}

	public final String toString() {
		return this.getHash() + "=" + this.getValue();
	}
}
