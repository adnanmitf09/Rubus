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

package bytecode;

import graph.Annotation;
import graph.Type;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;

/**
 * Imports annotations into instances of <code>graph.Annotation</code>.
 */
public class AnnotationImporter implements AnnotationVisitor {
  /**
   * Annotation being populated.
   */
  final private Annotation annotation;

  /**
   * Constructs importer on the given annotation.
   *
   * @param a      Annotation to populate.
   */
  public AnnotationImporter(Annotation a) {
    annotation = a;
  }

  /**
   * Sets the value of a simple method/value in the annotation.
   *
   * @param key    Method to set.
   * @param value  Value for method.
   */
  @Override
  public void visit(String key, Object value) {
    annotation.set(key, value);
  }

  /**
   * Sets the value for enum methods/values.
   *
   * @param key    Method to set.
   * @param type   Name of the enum class type.
   * @param value  Value for method.
   */
  @Override
  public void visitEnum(String key, String type, String value) {
    try {
      annotation.set(key, Enum.valueOf(
        (Class<? extends Enum>) Class.forName(Type.getType(type).getClassName()),
        value
      ));
    } catch (ClassNotFoundException ex) {
      Logger.getLogger("annotation").log(Level.FATAL, null, ex);
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String key, String descriptor) {
    Annotation nested = new Annotation(Type.getType(descriptor));

    annotation.set(key, nested);

    return new AnnotationImporter(nested);
  }

  /**
   * Prepares to set the value for array methods/values.
   *
   * @param  key   Method to set.
   * @return       AnnotationVisitor to import the array.
   */
  @Override
  public AnnotationVisitor visitArray(String key) {
    List<Object> arr = new LinkedList<Object>();

    annotation.set(key, arr);

    return new ArrayImporter(arr);
  }

  /**
   * End of annotation import. Nothing in our case.
   */
  @Override
  public void visitEnd() {
    // Nothing
  }

  /**
   * Imports array methods/values into a list.
   */
  private class ArrayImporter implements AnnotationVisitor {
    private List<Object> arr;

    ArrayImporter(List<Object> arr) {
      this.arr = arr;
    }

    @Override
    public void visit(String dummy, Object value) {
      arr.add(value);
    }
    
    @Override
    public void visitEnum(String dummy, String type, String value) {
      try {
        arr.add(Enum.valueOf(
          (Class<? extends Enum>) Class.forName(Type.getType(type).getClassName()),
          value
        ));
      } catch (ClassNotFoundException ex) {
        Logger.getLogger("annotation").log(Level.FATAL, null, ex);
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String dummy, String descriptor) {
      Annotation nested = new Annotation(Type.getType(descriptor));

      arr.add(nested);

      return new AnnotationImporter(nested);
    }

    @Override
    public AnnotationVisitor visitArray(String key) {
      List<Object> arr2 = new LinkedList<Object>();

      arr.add(arr2);

      return new ArrayImporter(arr2);
    }

    /**
     * End of array import. Nothing in our case.
     */
    @Override
    public void visitEnd() {
      // Nothing
    }
  }
}
