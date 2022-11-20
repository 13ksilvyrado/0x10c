
package util;

import java.util.HashSet;
import java.util.Set;

public class IntHashMap<V> {
    private static final Entry[] ENTRIES = new Entry[16];
	private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private transient Entry<V>[] table;
    private transient int size;
    private int threshold = 12;
    private final float loadFactor;
    private volatile transient int modCount;
    private Set<Integer> keys = new HashSet<Integer>();
	private Entry[] newTables;
	private util.IntHashMap.Entry[] newTables2;

    public IntHashMap() {
        this.loadFactor = 0.75f;
        this.table = getEntries();
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

    public V get(int key) {
        int hash = IntHashMap.hash(key);
        Entry<V> e = this.table[IntHashMap.indexFor(hash, this.table.length)];
        while (e != null) {
            if (e.key == key) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public boolean containsKey(int key) {
        return this.getEntry(key) != null;
    }

    final Entry<V> getEntry(int key) {
        int hash = IntHashMap.hash(key);
        Entry<V> e = this.table[IntHashMap.indexFor(hash, this.table.length)];
        while (e != null) {
            if (e.key == key) {
                return e;
            }
            e = e.next;
        }
        return null;
    }

    public void put(int key, V value) {
        this.keys.add(key);
        int hash = IntHashMap.hash(key);
        int i = IntHashMap.indexFor(hash, this.table.length);
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
        setNewTables(new Entry[newCapacity]);
        setNewTables2(getNewTables());
		this.transfer(getNewTables2());
        this.table = getNewTables3();
        this.threshold = (int)((float)newCapacity * this.loadFactor);
    }

    private Entry<V>[] getNewTables3() {
		// TODO Auto-generated method stub
		return null;
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
                    int i = IntHashMap.indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                } while ((e = next) != null);
            }
            ++j;
        }
    }

    public V remove(int key) {
        this.keys.remove(key);
        Entry<V> e = this.removeEntryForKey(key);
        return e == null ? null : e.value;
    }

    final Entry<V> removeEntryForKey(int key) {
        Entry<V> prev;
        int hash = IntHashMap.hash(key);
        int i = IntHashMap.indexFor(hash, this.table.length);
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

    private void addEntry(int hash, int key, V value, int bucketIndex) {
        Entry<V> e = this.table[bucketIndex];
        this.table[bucketIndex] = new Entry<V>(hash, key, value, e);
        if (this.size++ >= this.threshold) {
            this.resize(2 * this.table.length);
        }
    }

    public Set<Integer> keySet() {
        return this.keys;
    }

    public static int getDefaultInitialCapacity() {
		return DEFAULT_INITIAL_CAPACITY;
	}

	public static int getMaximumCapacity() {
		return MAXIMUM_CAPACITY;
	}

	public static float getDefaultLoadFactor() {
		return DEFAULT_LOAD_FACTOR;
	}

	public Entry[] getNewTables() {
		return newTables;
	}

	public void setNewTables(Entry[] newTables) {
		this.newTables = newTables;
	}

	public util.IntHashMap.Entry[] getNewTables2() {
		return newTables2;
	}

	public void setNewTables2(util.IntHashMap.Entry[] newTables2) {
		this.newTables2 = newTables2;
	}

	public static Entry[] getEntries() {
		return ENTRIES;
	}

	private static class Entry<V> {
        final int key;
        V value;
        Entry<V> next;
        final int hash;

        Entry(int h, int k, V v, Entry<V> n) {
            this.value = v;
            this.next = n;
            this.key = k;
            this.hash = h;
        }

        public final int getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.value;
        }

        public final boolean equals(Object o) {
            Object v2;
            V v1;
            Integer k2;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?> e = (Entry<?>)o;
            Integer k1 = this.getKey();
            return (k1 == (k2 = Integer.valueOf(e.getKey())) || k1 != null && ((Object)k1).equals(k2)) && ((v1 = this.getValue()) == (v2 = e.getValue()) || v1 != null && v1.equals(v2));
        }

        public final int hashCode() {
            return IntHashMap.hash(this.key);
        }

        public final String toString() {
            return String.valueOf(this.getKey()) + "=" + this.getValue();
        }
    }
}

