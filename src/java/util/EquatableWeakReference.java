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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Extends the standard WeakReference class so that two EquatableWeakReferences
 * are equal if (and only if) they point to the same object. This equality does
 * not include references that point to equal objects.
 */
public class EquatableWeakReference<T> extends WeakReference<T> {
  /**
   * Internal store of the referent's system hashcode (stored since the referent
   * could go away at any time).
   */
  private int hashCode;

  /**
   * Creates a new equatable weak reference that refers to the given object. The
   * new reference is not registered with any queue.
   *
   * @param referent  Object the new weak reference will refer to.
   */
  public EquatableWeakReference(T referent) {
    super(referent);
    hashCode = System.identityHashCode(referent);
  }

  /**
   * Creates a new weak reference that refers to the given object and is
   * registered with the given queue.
   *
   * @param referent  Object the new weak reference will refer to.
   * @param queue     Queue with which the reference is to be registered,
   *                  or <code>null</code> if registration is not required.
   */
  public EquatableWeakReference(T referent, ReferenceQueue<? super T> queue) {
    super(referent, queue);
    hashCode = System.identityHashCode(referent);
  }

  /**
   * Checks for equality with the given reference. Equality requires that
   * <code>ref</code> is also an equatable weak reference that was initialised
   * pointing to the same object.
   *
   * @param   ref     Object to compare to.
   * @return          <code>true</code> if this object is the equal to the
   *                  <code>ref</code> argument; <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object ref) {
    if(ref == null) {
      return false;
    } else if(ref instanceof EquatableWeakReference) {
      return hashCode == ref.hashCode();
    } else {
      return false;
    }
  }

  /**
   * Returns the system hash code for the referent.
   *
   * @return          Hash code.
   * @see             System#identityHashCode(Object)
   */
  @Override
  public int hashCode() {
    return hashCode;
  }
}
