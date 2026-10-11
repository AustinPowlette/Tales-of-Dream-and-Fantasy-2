package Game;

import java.util.*;
import java.util.function.Predicate;

/** An ArrayList for the lists that belong to a tile (the people there, a city's pool, shelves, boards) and that people on other tiles read while the turns run together on several cores:
 *  every call is synchronized, and iterating (for-each, streams) walks a snapshot, so a reader never meets a ConcurrentModificationException. It is still an ArrayList, so a field that is declared
 *  ArrayList (and a saved game that holds a plain one) keeps working; {@link #of} turns a plain list into one when a world is loaded. */
final class SafeList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 1L;
    SafeList() { super(); }
    SafeList(Collection<? extends E> c) { super(c); }

    /** A plain list (or null) as a SafeList (the same one if it is already). */
    static <T> ArrayList<T> of(ArrayList<T> l) { return l == null || l instanceof SafeList ? l : new SafeList<T>(l); }

    @Override public synchronized int size() { return super.size(); }
    @Override public synchronized boolean isEmpty() { return super.isEmpty(); }
    @Override public synchronized E get(int i) { return super.get(i); }
    @Override public synchronized E set(int i, E e) { return super.set(i, e); }
    @Override public synchronized boolean add(E e) { return super.add(e); }
    @Override public synchronized void add(int i, E e) { super.add(i, e); }
    @Override public synchronized E remove(int i) { return super.remove(i); }
    @Override public synchronized boolean remove(Object o) { return super.remove(o); }
    @Override public synchronized boolean addAll(Collection<? extends E> c) { return super.addAll(c); }
    @Override public synchronized boolean removeAll(Collection<?> c) { return super.removeAll(c); }
    @Override public synchronized boolean retainAll(Collection<?> c) { return super.retainAll(c); }
    @Override public synchronized void clear() { super.clear(); }
    @Override public synchronized boolean contains(Object o) { return super.contains(o); }
    @Override public synchronized int indexOf(Object o) { return super.indexOf(o); }
    @Override public synchronized Object[] toArray() { return super.toArray(); }
    @Override public synchronized <T> T[] toArray(T[] a) { return super.toArray(a); }
    @Override public synchronized boolean removeIf(Predicate<? super E> f) { return super.removeIf(f); }
    @Override public synchronized void sort(Comparator<? super E> c) { super.sort(c); }
    @Override public synchronized ArrayList<E> clone() { return new SafeList<E>(this); }
    @Override @SuppressWarnings("unchecked")
    public Iterator<E> iterator() { return (Iterator<E>) Arrays.asList(toArray()).iterator(); }
    @Override @SuppressWarnings("unchecked")
    public Spliterator<E> spliterator() { return (Spliterator<E>) Arrays.asList(toArray()).spliterator(); }
    @Override public void forEach(java.util.function.Consumer<? super E> a) { for (E e : this) a.accept(e); }
}
