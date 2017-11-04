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

package graph;


/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Class to represent Java types. This class is derived from the
 * org.objectweb.asm.Type class distributed as part of ASM 3.2, but includes
 * modifications so that types can be unified. This is useful as part of array
 * type reconstruction.
 *
 * @author Eric Bruneton
 * @author Chris Nokleberg
 * @author Peter Calvert
 */
public class Type {
  /**
   * Enumeration of basic primitive Java types.
   */
  public enum Sort {
    BOOL, BYTE, CHAR, SHORT, INT, FLOAT, REF, ADDR, LONG, DOUBLE;
  }

  /**
   * The <tt>void</tt> type.
   */
  public static final Type VOID = new Type((Sort) null);

  /**
   * The <tt>boolean</tt> type.
   */
  public static final Type BOOL = new Type(Sort.BOOL);

  /**
   * The <tt>char</tt> type.
   */
  public static final Type CHAR = new Type(Sort.CHAR);

  /**
   * The <tt>byte</tt> type.
   */
  public static final Type BYTE = new Type(Sort.BYTE);

  /**
   * The <tt>short</tt> type.
   */
  public static final Type SHORT = new Type(Sort.SHORT);

  /**
   * The <tt>int</tt> type.
   */
  public static final Type INT = new Type(Sort.INT);

  /**
   * The <tt>float</tt> type.
   */
  public static final Type FLOAT = new Type(Sort.FLOAT);

  /**
   * The <tt>long</tt> type.
   */
  public static final Type LONG = new Type(Sort.LONG);

  /**
   * The <tt>double</tt> type.
   */
  public static final Type DOUBLE = new Type(Sort.DOUBLE);

  /**
   * Reference types.
   */
  public static final Type REF = getFreshRef();

  /**
   * Sort of the type.
   */
  private final Sort sort;

  /**
   * Constructs a primitive type.
   *
   * @param        Sort the primitive type to be constructed.
   */
  private Type(final Sort sort) {
    this.sort = sort;
  }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * A buffer containing the internal name of this Java type.
   */
  private char[] buf = null;

  /**
   * The offset of the internal name of this Java type in {@link #buf buf}.
   */
  private int off = 0;

  /**
   * The length of the internal name of this Java type.
   */
  private int len = 0;

  /**
   * Constructs a reference type.
   *
   * @param buf  Buffer containing the descriptor of the object type.
   * @param off  Offset of this descriptor in the buffer.
   * @param len  Length of this descriptor.
   */
  private Type(final char[] buf, final int off,final int len) {
    this.sort = Sort.REF;
    this.buf  = buf;
    this.off  = off;
    this.len  = len;
  }

  /**
   * Returns the internal name of the class corresponding to this object type.
   * The internal name of a class is its fully qualified name (as returned by
   * Class.getName(), where '.' are replaced by '/'.
   *
   * @return     Internal name of the class corresponding to this object type.
   */
  public String getInternalName() {
    // Objects
    if(buf != null) {
      return new String(buf, off, len);
    // Array
    } else if(element != null) {
      return getDescriptor();
    // Other
    } else {
      throw new RuntimeException("Primitive Types don't have internal name");
    }
  }

  /**
   * Returns the name of the class corresponding to this object type.
   *
   * @return     Fully qualified name of the class corresponding to this type.
   */
  public String getClassName() {
    return new String(buf, off, len).replace('/', '.');
  }

  /**
   * Returns the ClassNode corresponding to this type.
   *
   * @return       ClassNode object.
   */
  public ClassNode getClassNode() {
    if(isArray() || (sort != Sort.REF)) {
      return null;
    } else {
      return ClassNode.getClass(getInternalName());
    }
  }

////////////////////////////////////////////////////////////////////////////////
  /**
   * Type of elements within the array.
   */
  private Type element;

  /**
   * Constructs an array type.
   *
   * @param et   Element type.
   */
  private Type(final Type et) {
    this.sort    = Sort.REF;
    this.element = et;
  }

  /**
   * Returns the number of dimensions of this type. This is defined as 0 for
   * non-array types, and is defined recursively for arrays.
   *
   * @return     Number of dimensions of this array type.
   */
  public int getDimensions() {
    if(element != null) {
      return 1 + element.getDimensions();
    } else {
      return 0;
    }
  }

  /**
   * Returns the type of the elements of this array type.
   *
   * @return     Type of the elements of this array type.
   */
  public Type getElementType() {
    return element;
  }
////////////////////////////////////////////////////////////////////////////////

  /**
   * Determines whether the type is represented by an integer on the operand
   * stack.
   * 
   * @return       <code>true</code> if represented as an integer,
   *               <code>false</code> otherwise.
   */
  public boolean isIntBased() {
    return equals(CHAR) || equals(BYTE) || equals(SHORT) || equals(INT)
        || equals(BOOL);
  }

  /**
   * Determines whether the array is an array type.
   *
   * @return       <code>true</code> if an array, <code>false</code> otherwise.
   */
  public boolean isArray() {
    return (element != null);
  }

  /**
   * Returns <code>true</code> if the type is a supertype of that given.
   *
   * @param  type  Type to compare to.
   * @return       <code>true</code> if the type is a supertype,
   *               <code>false</code> otherwise.
   */
  public boolean isSuperType(Type type) {
    // Equal.
    if(equals(type)) {
      return true;
    }

    // Only possible for reference types.
    if((sort != Sort.REF) || (type.sort != Sort.REF)) {
      return false;
    }

    // java.lang.Object is a supertype of everything
    if(equals(REF)) {
      return true;
    // If both arrays, recurse
    } else if(isArray() && type.isArray()) {
      return element.isSuperType(type.element);
    // If both objects, check superclass sets.
    } else if(!isArray() && !type.isArray()) {
      return getClassNode().isSuperClass(type.getClassNode());
    }

    return false;
  }

  public void unify(Type type) {
    unify(type, true);
  }

  /**
   * Unifies the type with the given type. This is the sole way of altering a
   * type object. A parameter specifies whether the given type can also be
   * modified. If so, this function ensures that the two types are
   * interchangable, if not, then it ensures that the type can be used as if it
   * were of the argument type (i.e. that the argument type is a super type of
   * it).
   *
   * @param type   Type to unify with.
   * @param modify Whether to allow modification of type argument.
   */
  public void unify(Type type, boolean modify) {
    // Already equal.
    if(equals(type)) {
      return;
    }

    // Both integer based, so can be used interchangibly.
    if(isIntBased() && type.isIntBased()) {
      return;
    }

    // Only possible for reference types.
    if((sort != Sort.REF) || (type.sort != Sort.REF)) {
      throw new RuntimeException("Can only unify reference types");
    }

    // Perform unification in correct direction.
    if(isSuperType(type)) {
      // Don't modify REF
      if(this == REF) {
        throw new RuntimeException("Can't modify REF, use getFreshRef()");
      }

      this.buf = type.buf;
      this.off = type.off;
      this.len = type.len;
      this.element = type.element;
    } else if(type.isSuperType(this)) {
      if(modify) {
        // Don't modify REF
        if(type == REF) {
          throw new RuntimeException("Can't modify REF, use getFreshRef()");
        }

        type.buf = this.buf;
        type.off = this.off;
        type.len = this.len;
        type.element = this.element;
      }
    } else {
      throw new RuntimeException("Unification failed (" + this + " with " + type + ")");
    }
  }

  /**
   * Returns the sort of this Java type.
   *
   * @return {@link #VOID VOID}, {@link #BOOLEAN BOOLEAN},
   *         {@link #CHAR CHAR}, {@link #BYTE BYTE}, {@link #SHORT SHORT},
   *         {@link #INT INT}, {@link #FLOAT FLOAT}, {@link #LONG LONG},
   *         {@link #DOUBLE DOUBLE}, {@link #ARRAY ARRAY} or
   *         {@link #OBJECT OBJECT}.
   */
  public Sort getSort() {
    return sort;
  }

  /**
   * Returns the size of values of this type.
   *
   * @return       Size of values of this type (i.e., 2 for <tt>long</tt> and
   *               <tt>double</tt>), and 1 otherwise.
   */
  public int getSize() {
    if((sort == Sort.LONG) || (sort == Sort.DOUBLE)) {
      return 2;
    } else {
      return 1;
    }
  }

  /**
   * Returns the descriptor corresponding to this Java type.
   *
   * @return the descriptor corresponding to this Java type.
   */
  public String getDescriptor() {
      StringBuffer sbuf = new StringBuffer();
      getDescriptor(sbuf);
      return sbuf.toString();
  }

  /**
   * Appends the descriptor corresponding to this Java type to the given
   * string buffer.
   *
   * @param buf    String buffer to which the descriptor must be appended.
   */
  protected void getDescriptor(final StringBuffer sbuf) {
    if(sort == null) {
      sbuf.append('V');
    } else {
      switch(sort) {
        // Primitive Types
        case BOOL:  sbuf.append('Z'); return;
        case CHAR:  sbuf.append('C'); return;
        case BYTE:  sbuf.append('B'); return;
        case SHORT: sbuf.append('S'); return;
        case INT:   sbuf.append('I'); return;
        case FLOAT: sbuf.append('F'); return;
        case LONG:  sbuf.append('J'); return;
        case DOUBLE:sbuf.append('D'); return;
        // Reference Types
        case REF:
          // Object
          if(element == null) {
            sbuf.append('L');
            sbuf.append(buf, off, len);
            sbuf.append(';');
          // Array
          } else {
            sbuf.append('[');
            element.getDescriptor(sbuf);
          }
      }
    }
  }

  /**
   * Returns a 'fresh' reference type. This is identical to Type.REF, except
   * that it can be modified using unify().
   *
   * @return       Fresh reference type.
   */
  public static Type getFreshRef() {
    return getType("Ljava/lang/Object;");
  }

  /**
   * Returns the Java type corresponding to the given type descriptor.
   *
   * @param  descriptor Type descriptor.
   * @return            Corresponding type.
   */
  public static Type getType(final String descriptor) {
    return getType(descriptor.toCharArray(), 0);
  }

  /**
   * Returns the Java type corresponding to the given type descriptor.
   *
   * @param buf  Buffer containing a type descriptor.
   * @param off  Offset of this descriptor in the buffer.
   * @return     Type corresponding to the given type descriptor.
   */
  private static Type getType(final char[] buf, final int off) {
    switch(buf[off]) {
      // Primitive Types
      case 'V': return VOID;
      case 'Z': return BOOL;
      case 'C': return CHAR;
      case 'B': return BYTE;
      case 'S': return SHORT;
      case 'I': return INT;
      case 'F': return FLOAT;
      case 'J': return LONG;
      case 'D': return DOUBLE;
      // Array Types
      case '[': return new Type(getType(buf, off + 1));
      // Object Types
      case 'L':
      default:
        int len = 1;
        while(buf[off + len] != ';') {
          ++len;
        }
        return new Type(buf, off + 1, len - 1);
    }
  }

  /**
   * Returns the Java type corresponding to the given internal name.
   *
   * @param internalName an internal name.
   * @return the Java type corresponding to the given internal name.
   */
  public static Type getObjectType(final String internalName) {
    char[] buf = internalName.toCharArray();

    // Arrays
    if(buf[0] == '[') {
      return getType(internalName);
    // Objects
    } else {
      return new Type(buf, 0, buf.length);
    }
  }

  /**
   * Returns the array type where each element is of the given type.
   *
   * @param  type  Element type.
   * @return       Array type.
   */
  public static Type getArrayOf(Type type) {
    return new Type(type);
  }

  /**
   * Returns the Java types corresponding to the parameter types of the given
   * method descriptor.
   *
   * @param  desc  Method descriptor.
   * @return       Types corresponding to the parameter types of the given
   *               method descriptor.
   */
  public static Type[] getParameterTypes(final String desc) {
    char[] buf = desc.toCharArray();
    int off = 1;
    int size = 0;
    while (true) {
        char car = buf[off++];
        if (car == ')') {
            break;
        } else if (car == 'L') {
            while(buf[off++] != ';') {}
            ++size;
        } else if (car != '[') {
            ++size;
        }
    }
    Type[] args = new Type[size];
    off = 1;
    size = 0;
    while (buf[off] != ')') {
        args[size] = getType(buf, off);

        off += args[size].getDescriptor().length();
        size += 1;
    }
    return args;
  }

  /**
   * Returns the Java type corresponding to the return type of the given
   * method descriptor.
   *
   * @param  desc  Method descriptor.
   * @return       Type corresponding to the return type of the given method
   *               descriptor.
   */
  public static Type getReturnType(final String desc) {
    char[] buf = desc.toCharArray();
    return getType(buf, desc.indexOf(')') + 1);
  }

  /**
   * Returns the descriptor corresponding to the given argument and return
   * types.
   *
   * @param  args  Argument types of the method.
   * @param  ret   Return type of the method.
   * @return       Descriptor corresponding to the given argument and return
   *               types.
   */
  public static String getMethodDescriptor(final Type[] args, final Type ret) {
    StringBuffer buf = new StringBuffer();
    buf.append('(');
    for(final Type arg : args) {
      arg.getDescriptor(buf);
    }
    buf.append(')');
    ret.getDescriptor(buf);
    return buf.toString();
  }

  /**
   * Tests if the given object is equal to this type.
   *
   * @param o      Object to be compared to this type.
   * @return       <tt>true</tt> if the given object is equal to this type.
   */
  @Override
  public boolean equals(final Object o) {
    if(o instanceof Type) {
      return getDescriptor().equals(((Type) o).getDescriptor());
    } else {
      return false;
    }
  }

  /**
   * Returns a hash code value for this type.
   *
   * @return       Hash code value for this type.
   */
  @Override
  public int hashCode() {
    return getDescriptor().hashCode();
  }

  /**
   * Returns the textual descriptor of the type, as its standard string
   * representation.
   *
   * @return       Type descriptor.
   */
  @Override
  public String toString() {
	 
    return getDescriptor();
  }
}
