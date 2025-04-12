package net.minecraft.src;

public class MCHashTable2 {
	private transient MCHashEntry2[] slots = new MCHashEntry2[16];
	private transient int count;
	private int threshold = 12;
	private final float growFactor = 12.0F / 16.0F;
	private transient volatile int versionStamp;

	private static int computeHash(long var0) {
		return computeHash((int)(var0 ^ var0 >>> 32));
	}

	private static int computeHash(int var0) {
		var0 ^= var0 >>> 20 ^ var0 >>> 12;
		return var0 ^ var0 >>> 7 ^ var0 >>> 4;
	}

	private static int getSlotIndex(int var0, int var1) {
		return var0 & var1 - 1;
	}

	public Object lookup(long var1) {
		int var3 = computeHash(var1);

		for(MCHashEntry2 var4 = this.slots[getSlotIndex(var3, this.slots.length)]; var4 != null; var4 = var4.nextEntry) {
			if(var4.hashEntry == var1) {
				return var4.valueEntry;
			}
		}

		return null;
	}

	public void addKey(long var1, Object var3) {
		int var4 = computeHash(var1);
		int var5 = getSlotIndex(var4, this.slots.length);

		for(MCHashEntry2 var6 = this.slots[var5]; var6 != null; var6 = var6.nextEntry) {
			if(var6.hashEntry == var1) {
				var6.valueEntry = var3;
			}
		}

		++this.versionStamp;
		this.insert(var4, var1, var3, var5);
	}

	private void grow(int var1) {
		MCHashEntry2[] var2 = this.slots;
		int var3 = var2.length;
		if(var3 == 1073741824) {
			this.threshold = Integer.MAX_VALUE;
		} else {
			MCHashEntry2[] var4 = new MCHashEntry2[var1];
			this.copyTo(var4);
			this.slots = var4;
			this.threshold = (int)((float)var1 * this.growFactor);
		}
	}

	private void copyTo(MCHashEntry2[] var1) {
		MCHashEntry2[] var2 = this.slots;
		int var3 = var1.length;

		for(int var4 = 0; var4 < var2.length; ++var4) {
			MCHashEntry2 var5 = var2[var4];
			if(var5 != null) {
				var2[var4] = null;

				MCHashEntry2 var6;
				do {
					var6 = var5.nextEntry;
					int var7 = getSlotIndex(var5.slotHash, var3);
					var5.nextEntry = var1[var7];
					var1[var7] = var5;
					var5 = var6;
				} while(var6 != null);
			}
		}

	}

	public Object removeObject(long var1) {
		MCHashEntry2 var3 = this.removeEntry(var1);
		return var3 == null ? null : var3.valueEntry;
	}

	final MCHashEntry2 removeEntry(long var1) {
		int var3 = computeHash(var1);
		int var4 = getSlotIndex(var3, this.slots.length);
		MCHashEntry2 var5 = this.slots[var4];

		MCHashEntry2 var6;
		MCHashEntry2 var7;
		for(var6 = var5; var6 != null; var6 = var7) {
			var7 = var6.nextEntry;
			if(var6.hashEntry == var1) {
				++this.versionStamp;
				--this.count;
				if(var5 == var6) {
					this.slots[var4] = var7;
				} else {
					var5.nextEntry = var7;
				}

				return var6;
			}

			var5 = var6;
		}

		return var6;
	}

	private void insert(int var1, long var2, Object var4, int var5) {
		MCHashEntry2 var6 = this.slots[var5];
		this.slots[var5] = new MCHashEntry2(var1, var2, var4, var6);
		if(this.count++ >= this.threshold) {
			this.grow(2 * this.slots.length);
		}

	}

	static int getHash(long var0) {
		return computeHash(var0);
	}
}
