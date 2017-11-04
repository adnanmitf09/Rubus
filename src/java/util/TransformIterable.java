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

import java.util.Iterator;

/**
 *
 */
public abstract class TransformIterable<S,T> implements Iterable {
  private Iterable<S> input;

  protected abstract T transform(S obj);

  public TransformIterable(Iterable<S> input) {
    this.input = input;
  }

  public Iterator<T> iterator() {
    final Iterator<S> inIter = input.iterator();

    return new Iterator<T>() {
      public boolean hasNext() {
        return inIter.hasNext();
      }

      public T next() {
        return transform(inIter.next());
      }

      public void remove() {
        inIter.remove();
      }
    };
  }
}
