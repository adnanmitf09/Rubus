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

import java.util.AbstractList;
import java.util.List;

/**
 * 'Cons' constructor for lists. Allows creation of read-only lists from head
 * elements and tail lists. Changes to the tail list will be exhibited in the
 * resultant list.
 */
public class ConsList<T> extends AbstractList<T> {
  /**
   * Head of list.
   */
  private T head;

  /**
   * Tail of list.
   */
  private List<T> tail;

  /**
   * 'Cons' Constructor.
   *
   * @param  head  Head of list.
   * @param  tail  Tail of list.
   */
  public ConsList(T head, List<T> tail) {
    this.head = head;
    this.tail = tail;
  }

  /**
   * Returns the element at the given index (head for 0, recursion on tail
   * otherwise).
   *
   * @param  index Index of element.
   * @return       Element.
   */
  @Override
  public T get(int index) {
    if(index == 0) {
      return head;
    } else {
      return tail.get(index - 1);
    }
  }

  /**
   * Returns the length of the list.
   *
   * @return       Length of the list.
   */
  @Override
  public int size() {
    return 1 + tail.size();
  }
}
