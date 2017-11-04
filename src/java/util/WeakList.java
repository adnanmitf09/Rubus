/*
 * Parallelising JVM Compiler
 *
 * Copyright 2010 Peter Calvert, University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a list in which all elements are only weakly referenced. As 
 * elements are garbage collected, they are automatically removed from the list.
 * This automatic removal occurs only before read operations (i.e.
 * <code>get</code> and <code>size</code>).
 */
public class WeakList<T> extends AbstractList<T> {
  /**
   * Internal list for actual store.
   */
  private final List<EquatableWeakReference<T>> internalList;

  /**
   * Reference queue for detecting dead objects.
   */
  private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

  /**
   * Constructs a weak reference list.
   */
  public WeakList() {
    internalList = new ArrayList<EquatableWeakReference<T>>();
  }

  /**
   * Removes any items that have been garbage collected from the underlying
   * list.
   */
  private void removeDead() {
    Reference<? extends T> r;
    
    while((r = queue.poll()) != null) {
      internalList.remove(r);
    }
  }

  /**
   * Adds an element to the list at the specified position. Shifts the element
   * currently at that position (if any) and any subsequent elements to the
   * right (adds one to their indices).
   *
   * @param index  Index at which to add the element
   * @param item   Item to add.
   */
  @Override
  public void add(int index, T item) {
    internalList.add(index, new EquatableWeakReference<T>(item, queue));
  }

  /**
   * Replaces the element at the specified position in this list with the
   * specified element.
   *
   * @param index  Index of the element to replace.
   * @param item   Element to be stored at the specified position.
   * @return       The element previously at the specified position.
   */
  @Override
  public T set(int index, T item) {
    EquatableWeakReference<T> old = internalList.set(
      index,
      new EquatableWeakReference<T>(item, queue)
    );

    if(old != null) {
      return old.get();
    } else {
      return null;
    }
  }

  /**
   * Returns the element at the specified position in this list.
   *
   * @param index  Index of element to be returned.
   * @return       Item at the given index.
   */
  @Override
  public T get(int index) {
    removeDead();
    
    return internalList.get(index).get();
  }

  /**
   * Removes the element at the specified position in this list. Shifts any
   * subsequent elements to the left (subtracts one from their indices). Returns
   * the element that was removed from the list.
   *
   * @param index  Index of element to be removed.
   * @return       Item that was removed.
   */
  @Override
  public T remove(int index) {
    EquatableWeakReference<T> item = internalList.remove(index);

    if(item != null) {
      return item.get();
    } else {
      return null;
    }
  }

  /**
   * Returns the size of the list.
   *
   * @return       Number of elements in the list.
   */
  @Override
  public int size() {
    removeDead();

    return internalList.size();
  }
}
