package in.mcxiv.app.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;

public class Pool<T extends Poolable> {

    public static final int DEFAULT_SIZE = 128;
    private final Supplier<T> constructor;
    private final ArrayList<T> list;
    private final PoolIterator iterator;

    public Pool(Supplier<T> constructor) {
        this.constructor = constructor;
        list = new ArrayList<>(DEFAULT_SIZE);
        for (int i = 0; i < DEFAULT_SIZE; i++)
            list.add(constructor.get());
        iterator = new PoolIterator();
    }

    public T acquire() {
        for (int i = 0, s = list.size(); i < s; i++) {
            T t = list.get(i);
            if (t.canBeReset()) {
                t.reset();
                return t;
            }
        }
        T t = constructor.get();
        list.add(t);
        return t;
    }

    void reset(Poolable t) {
        if (t.canBeReset()) t.reset();
    }

    public Iterator<T> iterator() {
        iterator.reset();
        return iterator;
    }

    public class PoolIterator implements Iterator<T> {

        private int index = 0;

        private T next = null;

        public void reset() {
            index = 0;
            next = null;
        }

        @Override
        public boolean hasNext() {
            for (int s = list.size(); index < s; index++) {
                T t = list.get(index);
                if (!t.canBeReset()) {
                    next = t;
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            index++;
            T t = this.next;
            next = null;
            return t;
        }
    }
}
