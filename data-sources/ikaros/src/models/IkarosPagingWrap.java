package models;


import java.util.Collections;
import java.util.List;

import kotlinx.serialization.Serializable;

@Serializable
public class IkarosPagingWrap<T> {

    private final int page;
    private final int size;
    private final long total;
    private final List<T> items;

    /**
     * new a page result instance.
     *
     * @param page  page
     * @param size  size
     * @param total total
     * @param items item list
     */
    public IkarosPagingWrap(int page, int size, long total, List<T> items) {
        if (total < 0)
            throw new IllegalArgumentException("Total elements must be greater than or equal to 0");
        if (page < 0) {
            page = 0;
        }
        if (size < 0) {
            size = 0;
        }
        if (items == null) {
            items = Collections.emptyList();
        }
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }

    public IkarosPagingWrap(List<T> items) {
        this(0, 0, items.size(), items);
    }

    public boolean isFirstPage() {
        return !hasPrevious();
    }

    public boolean isLastPage() {
        return !hasNext();
    }

    /**
     * if this has next.
     *
     * @return true if this has next
     */
    public boolean hasNext() {
        if (page <= 0) {
            return false;
        }
        return page < getTotalPages();
    }

    public boolean hasPrevious() {
        return page > 1;
    }


    public boolean isEmpty() {
        return items.isEmpty();
    }

    public long getTotalPages() {
        return size == 0 ? 1 : (total + size - 1) / size;
    }


    public static <T> IkarosPagingWrap<T> emptyResult() {
        return new IkarosPagingWrap<>(List.of());
    }

    public List<T> getItems() {
        return items;
    }
    
    public T[] getItemArray() {
        return items.toArray((T[]) new Object[items.size()]);
    }

    public long getTotal() {
        return total;
    }

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }
    
}
