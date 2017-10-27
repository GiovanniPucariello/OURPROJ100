package fr.inrialpes.exmo.ontowrap.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @param <T>
 */
public abstract class FilteredSet<T> extends AbstractSet<T> {
	
	private Set<T> s;
	private int size=-1;
	
	public FilteredSet(Set<T> s) {
	    this.s=s;
	}
	
	protected abstract boolean isFiltered(T obj);

	/**
	 *
	 * @return
	 */
	@Override
	public final Iterator<T> iterator() {
	    return new Iterator<T>() {
		Iterator<T> i=s.iterator();
		T current=null;

			/**
			 *
			 * @return
			 */
		public boolean hasNext() {
		   boolean isFiltered = isFiltered(current);
			while (current==null || isFiltered) {
		       if (!i.hasNext()) return false;
		       current=i.next();
				isFiltered = isFiltered(current);
		   }
		   return true;
		}

			/**
			 *
			 * @return
			 */
		@Override
		public T next() {
		    T r = current;
		    if (hasNext()) {
			current=null;
			return r;
		    }
		    throw new NoSuchElementException();
		}

			/**
			 * void remove()
			 */
		@Override
		public void remove() {
		    throw new UnsupportedOperationException();
		    
		}
	    };
	}

	/**
	 *
	 * @return
	 */
	public final int size() {
	    if (size==-1) {
		Iterator<T> i=this.iterator();
		size=0;
		boolean hasNext = i.hasNext();
		while (hasNext) {
		    i.next();
		    size++;
			hasNext = i.hasNext();
		}
	    }
	    return size;
	}
	
}
