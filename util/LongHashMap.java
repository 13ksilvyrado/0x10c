package util;

public class LongHashMap<V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private transient Entry<V>[] table = extracted1();

	private Entry[] extracted1() {
		return Extracted();
	}

	private Entry[] Extracted() {
		return extracted1();
	}

	private Entry[] extracted() {
		return new Entry[16];
	}
    private transient int size;
    private int threshold = 12;
    private final float loadFactor;
    private volatile transient int modCount;

    public LongHashMap() {
        this.loadFactor = 0.75f;
    }

    private static int hash(long h) {
        return LongHashMap.hash((int)(h ^ h >>> 32));
    }

    private static int hash(int h) {
        h ^= h >>> 20 ^ h >>> 12;
        return h ^ h >>> 7 ^ h >>> 4;
    }

    private static int indexFor(int h, int length) {
        return h & length - 1;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public V get(long key) {
        int hash = LongHashMap.hash(key);
        Entry<V> e = this.table[LongHashMap.indexFor(hash, this.table.length)];
        while (e != null) {
            if (e.key == key) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public boolean containsKey(long key) {
        return this.getEntry(key) != null;
    }

    final Entry<V> getEntry(long key) {
        int hash = LongHashMap.hash(key);
        Entry<V> e = this.table[LongHashMap.indexFor(hash, this.table.length)];
        while (e != null) {
            if (e.key == key) {
                return e;
            }
            e = e.next;
        }
        return null;
    }

    @Deprecated
    public void put(int key, V value) {
    }

    public void put(long key, V value) {
        int hash = LongHashMap.hash(key);
        int i = LongHashMap.indexFor(hash, this.table.length);
        Entry<V> e = this.table[i];
        while (e != null) {
            if (e.key == key) {
                e.value = value;
            }
            e = e.next;
        }
        ++this.modCount;
        this.addEntry(hash, key, value, i);
    }

    private void resize(int newCapacity) {
        Entry<V>[] oldTable = this.table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == 0x40000000) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        Entry[] newTable = new Entry[newCapacity];
        this.transfer(newTable);
        this.table = newTable;
        this.threshold = (int)((float)newCapacity * this.loadFactor);
    }

    private void transfer(Entry<V>[] newTable) {
        Entry<V>[] src = this.table;
        int newCapacity = newTable.length;
        int j = 0;
        while (j < src.length) {
            Entry<V> e = src[j];
            if (e != null) {
                Entry<V> next;
                src[j] = null;
                do {
                    next = e.next;
                    int i = LongHashMap.indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                } while ((e = next) != null);
            }
            ++j;
        }
    }

    public V remove(long key) {
        Entry<V> e = this.removeEntryForKey(key);
        return e == null ? null : e.value;
    }

    final Entry<V> removeEntryForKey(long key) {
        Entry<V> prev;
        int hash = LongHashMap.hash(key);
        int i = LongHashMap.indexFor(hash, this.table.length);
        Entry<V> e = prev = this.table[i];
        while (e != null) {
            Entry<V> next = e.next;
            if (e.key == key) {
                ++this.modCount;
                --this.size;
                if (prev == e) {
                    this.table[i] = next;
                } else {
                    prev.next = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }
        return e;
    }

    public void clear() {
        ++this.modCount;
        Entry<V>[] tab = this.table;
        int i = 0;
        while (i < tab.length) {
            tab[i] = null;
            ++i;
        }
        this.size = 0;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return this.containsNullValue();
        }
        Entry<V>[] tab = this.table;
        int i = 0;
        while (i < tab.length) {
            Entry<V> e = tab[i];
            while (e != null) {
                if (value.equals(e.value)) {
                    return true;
                }
                e = e.next;
            }
            ++i;
        }
        return false;
    }

    private boolean containsNullValue() {
        Entry<V>[] tab = this.table;
        int i = 0;
        while (i < tab.length) {
            Entry<V> e = tab[i];
            while (e != null) {
                if (e.value == null) {
                    return true;
                }
                e = e.next;
            }
            ++i;
        }
        return false;
    }

    private void addEntry(int hash, long key, V value, int bucketIndex) {
        Entry<V> e = this.table[bucketIndex];
        this.table[bucketIndex] = new Entry<V>(hash, key, value, e);
        if (this.size++ >= this.threshold) {
            this.resize(2 * this.table.length);
        }
    }

    public static int getDefaultInitialCapacity() {
		return DEFAULT_INITIAL_CAPACITY;
	}

	public static float getDefaultLoadFactor() {
		return DEFAULT_LOAD_FACTOR;
	}

	public static int getMaximumCapacity() {
		return MAXIMUM_CAPACITY;
	}

	private static class Entry<V> {
        final long key;
        V value;
        Entry<V> next;
        final int hash;

        Entry(int h, long k, V v, Entry<V> n) {
            this.value = v;
            this.next = n;
            this.key = k;
            this.hash = h;
        }

        public final long getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.value;
        }

        public final boolean equals(Object o) {
            V v2;
            V v1;
            Long k2;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?> e = (Entry<?>)o;
            Long k1 = this.getKey();
            return (k1 == (k2 = Long.valueOf(e.getKey())) || k1 != null && ((Object)k1).equals(k2)) && ((v1 = this.getValue()) == (v2 = (V) e.getValue()) || v1 != null && v1.equals(v2));
        }

        public final int hashCode() {
            return LongHashMap.hash(this.key);
        }

        public final String toString() {
            return String.valueOf(this.getKey()) + "=" + this.getValue();
        }
    }
}

