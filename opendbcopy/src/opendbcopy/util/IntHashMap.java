/*
 * Copyright (C) 2004 Anthony Smith
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * ----------------------------------------------------------------------------
 * TITLE $Id$
 * ---------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.util;

/**
 * A hash map mapping int values to objects. This offers the benefit of not having to use objects as keys, which can result in performance benefits.
 */
public class IntHashMap {
    /** The default capacity for hash map instances. */
    public static final int DEFAULT_CAPACITY = 17;

    /** The maximum allowed capacity for hash map instances. */
    public static final int MAXIMUM_CAPACITY = 1 << 30;

    /** The default load factor for hash map instances. */
    public static final float DEFAULT_LOADFACTOR = 0.75f;
    private MapElement[]      map = null; // The first bucket for each key
    private int[]             count = null; // The count of buckets in each chain
    private int               contents = 0;
    private int               objectCounter = 0; // Counter for objects created
    private int               capacity = DEFAULT_CAPACITY;
    private int               initialCap = DEFAULT_CAPACITY;
    private float             loadFactor = DEFAULT_LOADFACTOR;
    private int               maxLoad = 0;
    private boolean           rehashing = true;

    /**
     * Constructs an empty instance with the default initial capacity and the default load factor
     */
    public IntHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
    }

    /**
     * Constructs an empty instance with the given initial capacity and the default load factor
     * 
     * <p></p>
     *
     * @param initialCapacity The initial capacity for this hash map.
     */
    public IntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOADFACTOR);
    }

    /**
     * Constructs an empty instance with the given initial capacity and the given load factor
     * 
     * <p></p>
     *
     * @param initialCapacity The initial capacity for this hash map.
     * @param loadFactor The load factor for this hash map.
     */
    public IntHashMap(int   initialCapacity,
                      float loadFactor) {
        construct(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new HashMap with the same mappings as the specified Map. The HashMap  is created with default load factor and an initial capacity
     * sufficient to hold the  mappings in the specified Map.
     * 
     * <p></p>
     *
     * @param m The map whose mappings are to be placed in this map. Throws: <p>
     *
     * @throws IllegalArgumentException if the specified map is<code>null</code>.
     */
    public IntHashMap(IntHashMap m) {
        if (m == null) {
            throw new IllegalArgumentException("m may not be null");
        }

        //.... Determine parameters
        loadFactor     = DEFAULT_LOADFACTOR; // As per standard API for java.util.HashMap
        capacity       = (int) (m.size() / loadFactor);

        if (capacity < DEFAULT_CAPACITY) { // Avoid underflow
            capacity = DEFAULT_CAPACITY;
        } else if ((capacity % 2) == 0) { // Make sure we have an odd value
            capacity++;
        }

        //.... Standard initialization for the internal map elements
        maxLoad        = (int) ((loadFactor * capacity) + 0.5f); // Max. number of elements before a rehash occurs
        initialCap     = capacity;

        objectCounter += 2;
        map       = new MapElement[capacity];
        count     = new int[capacity];

        //.... Copy the elements to the new map 
        int[] keys = m.keySet();

        for (int i = 0; i < m.size(); i++) {
            put(keys[i], m.get(keys[i]));
        }
    }

    /**
     * Return the current number of mappings in the hash map.
     * 
     * <p></p>
     *
     * @return The current number of mappings in the hash map.
     */
    public int size() {
        return contents;
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return DOCUMENT ME!
     */
    public boolean isEmpty() {
        return (contents == 0) ? true : false;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        construct(initialCap, loadFactor);
    }

    /**
     * Return the number of objects created in / by this instance
     * 
     * <p></p>
     *
     * @return The number of objects created
     */
    public int getObjectCounter() {
        return objectCounter;
    }

    /**
     * Return the current capacity of the instance. If rehashing is enabled (which it is per default), the capacity may have been increased as
     * necessary from the initial value.
     * 
     * <p></p>
     *
     * @return The current capacity for this hash map.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Return the load factor of the instance.
     * 
     * <p></p>
     *
     * @return The load factor for this hash map.
     */
    public float getLoadFactor() {
        return loadFactor;
    }

    /**
     * Return the keys in the hash map
     * 
     * <p></p>
     *
     * @return An array containing the keys for which mappings are stored in this hash map.
     */
    public int[] keySet() {
        objectCounter++;

        int[]      keys = new int[contents];
        int        cnt = 0;
        MapElement me = null;

        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                me = map[i];

                for (int j = 0; j < count[i]; j++) {
                    keys[cnt++]     = me.getKey();
                    me              = me.getNext();
                }
            }
        }

        return keys;
    }

    /**
     * Enable/disable rehashing (defaults to <code>true</code>).
     * 
     * <p></p>
     *
     * @param rehashing A boolean indicating the desired rehashing status.
     */
    public void setRehash(boolean rehashing) {
        this.rehashing = rehashing;
    }

    /**
     * Associates the specified value with the specified key in this map. If  the map previously contained a mapping for this key, the old value is
     * replaced.
     * 
     * <p></p>
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.  <p>
     *
     * @return Previous value associated with specified key, or <code>null</code> if there was no mapping  for key. A <code>null</code> return can
     *         also indicate that the HashMap previously associated <code>null</code>  with the specified key.
     */
    public Object put(int    key,
                      Object value) {
        int index = key % capacity;

        if (index < 0) {
            index = -index;
        }

        //.... This is a new key since no bucket exists
        if (map[index] == null) {
            objectCounter++;
            map[index] = new MapElement(key, value);
            count[index]++;
            contents++;

            if (contents > maxLoad) {
                rehash();
            }

            return null;

            //.... A bucket already exists for this index: check whether we already have a mapping for this key
        } else {
            MapElement me = map[index];

            while (true) {
                if (me.getKey() == key) { // We have a mapping: just replace the value for this element

                    Object previous = me.getValue(); // Return the current value - same as for java.util.HashMap.put()
                    me.setValue(value);

                    return previous;
                } else {
                    if (me.getNext() == null) { // No next element: so we have no mapping for this key
                        objectCounter++;
                        me.setNext(new MapElement(key, value));
                        count[index]++;
                        contents++;

                        if (contents > maxLoad) {
                            rehash();
                        }

                        return null;
                    } else {
                        me = me.getNext();
                    }
                }
            }
        }
    }

    /**
     * Returns the value to which the specified key is mapped in this identity  hash map, or <code>null</code> if the map contains no mapping for
     * this key. A return  value of <code>null</code> does not necessarily indicate that the map contains no  mapping for the key; it is also
     * possible that the map explicitly maps  the key to <code>null</code>. The <code>containsKey</code>  method may be used to distinguish these
     * two cases.
     * 
     * <p></p>
     *
     * @param key The key whose associated value is to be returned.  <p>
     *
     * @return The value to which this map maps the specified key, or  <code>null</code> if the map contains no mapping for this key.
     */
    public Object get(int key) {
        MapElement me = exists(key);

        if (me == null) {
            return null;
        } else {
            return me.getValue();
        }
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key.
     * 
     * <p></p>
     *
     * @param key The key whose presence in this map is to be tested. <p>
     *
     * @return <code>true</code> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(int key) {
        if (exists(key) == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     * 
     * <p></p>
     *
     * @param key The key whose mapping is to be removed from the map. <p>
     *
     * @return Previous value associated with specified key, or  <code>null</code>  if there was no mapping for key. A <code>null</code> return can
     *         also indicate that the map previously associated <code>null</code>  with the specified key.
     */
    public Object remove(int key) {
        int index = key % capacity;

        if (index < 0) {
            index = -index;
        }

        if (map[index] == null) {
            return null;
        } else {
            MapElement me = map[index];
            MapElement prev = null;

            while (true) {
                if (me.getKey() == key) { // Keys match

                    if (prev == null) { // The first element in the chain matches
                        map[index] = me.getNext();
                    } else { // An element further down in the chain matches - delete it from the chain
                        prev.setNext(me.getNext());
                    }

                    count[index]--;
                    contents--;

                    return me.getValue();
                } else { // Keys don't match, try the next element
                    prev     = me;
                    me       = me.getNext();

                    if (me == null) {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * Helper method: returns the element matching the key, or <code>null</code> if no such element exists
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private MapElement exists(int key) {
        int index = key % capacity;

        if (index < 0) {
            index = -index;
        }

        if (map[index] == null) {
            return null;
        } else {
            MapElement me = map[index];

            while (true) {
                if (me.getKey() == key) {
                    return me;
                } else {
                    me = me.getNext();

                    if (me == null) {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * Increase the capacity of the map to improve performance
     */
    private void rehash() {
        if (rehashing) {
            int newCapacity = (2 * capacity) + 1;

            if (newCapacity > MAXIMUM_CAPACITY) {
                return;
            }

            objectCounter += 2;

            MapElement[] newMap = new MapElement[newCapacity];
            int[]        newCount = new int[newCapacity];

            MapElement   me = null;
            MapElement   t = null;
            MapElement   next = null;
            int          newIndex = 0;

            for (int index = 0; index < capacity; index++) {
                me = map[index];

                while (me != null) {
                    next         = me.getNext();
                    newIndex     = me.getKey() % newCapacity;

                    if (newIndex < 0) {
                        newIndex = -newIndex;
                    }

                    newCount[newIndex]++;

                    if (newMap[newIndex] == null) { // No element yet for this new index
                        newMap[newIndex] = me;
                        me.setNext(null);
                    } else { // Hook the element into the beginning of the chain
                        t                    = newMap[newIndex];
                        newMap[newIndex]     = me;
                        me.setNext(t);
                    }

                    me = next;
                }
            }

            map          = newMap;
            count        = newCount;
            capacity     = newCapacity;
            maxLoad      = (int) ((loadFactor * capacity) + 0.5f); // Max. number of elements before a rehash occurs

            newMap = null;
        }
    }

    /**
     * Construction helper method
     *
     * @param initialCapacity DOCUMENT ME!
     * @param loadFactor DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void construct(int   initialCapacity,
                           float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Invalid initial capacity: " + initialCapacity);
        }

        if (initialCapacity < DEFAULT_CAPACITY) {
            initialCapacity = DEFAULT_CAPACITY;
        }

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if ((loadFactor <= 0.0f) || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Invalid load factor: " + loadFactor);
        }

        this.initialCap     = initialCapacity;
        this.capacity       = initialCapacity;
        this.loadFactor     = loadFactor;

        objectCounter += 2;
        maxLoad      = (int) ((loadFactor * capacity) + 0.5f); // Max. number of elements before a rehash occurs
        map          = new MapElement[capacity];
        count        = new int[capacity];
        contents     = 0;
    }

    /**
     * Statistical output for this map.
     * 
     * <p></p>
     *
     * @param full A boolean indicating whether just a short of the full information should be printed.
     */
    public void printStatistics(boolean full) {
        if (full) {
            for (int i = 0; i < capacity; i++) {
                System.out.println("Count[" + i + "] = " + count[i]);
            }
        }

        System.out.println("Initial capacity   = " + initialCap);
        System.out.println("Capacity           = " + capacity);
        System.out.println("Number of elements = " + contents);
    }

    /**
         *
         */
    class MapElement {
        private int        key = 0;
        private Object     value = null;
        private MapElement next = null;

        /**
         * Constructor
         *
         * @param key DOCUMENT ME!
         * @param value DOCUMENT ME!
         */
        public MapElement(int    key,
                          Object value) {
            this.key       = key;
            this.value     = value;
        }

        /**
         * Getter method for <code>key</code> property
         * 
         * <p></p>
         *
         * @return The value for the <code>key</code> property
         */
        public int getKey() {
            return key;
        }

        /**
         * Setter method for <code>value</code> property
         * 
         * <p></p>
         *
         * @param value The value for the <code>value</code> property
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Getter method for <code>value</code> property
         * 
         * <p></p>
         *
         * @return The value for the <code>value</code> property
         */
        public Object getValue() {
            return value;
        }

        /**
         * Setter method for <code>next</code> property
         * 
         * <p></p>
         *
         * @param next The value for the <code>next</code> property
         */
        public void setNext(MapElement next) {
            this.next = next;
        }

        /**
         * Getter method for <code>next</code> property
         * 
         * <p></p>
         *
         * @return The value for the <code>next</code> property
         */
        public MapElement getNext() {
            return next;
        }
    }
}
