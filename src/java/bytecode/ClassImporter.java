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
import graph.ClassNode;
import graph.Method;
import graph.Modifier;
import graph.Type;
import graph.state.Field;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Imports Java class files using the ASM library. The depth of the import (i.e.
 * whether method implementations are considered, depends on the defined
 * configuration, but can be overidden by annotations on classes).
 */
public class ClassImporter implements ClassVisitor {
  /**
   * Whether to perform portable imports (false means that core classes will be
   * fully loaded).
   */
  private static boolean portable = true;

  /**
   * Class object that should be populated by the visitor.
   */
  private ClassNode classNode;

  /**
   * Whether the import should be deep or not.
   */
  private boolean deep;

  /**
   * Sets whether the class imports must be portable between different JVMs and
   * class libraries. Setting this to false allows standard classes to be
   * considered, but may break code when run on a different JVM.
   * 
   * @param portable Whether portability is required.
   */
  public static void setPortable(boolean portable) {
    ClassImporter.portable = portable;
  }

  /**
   * Causes the named class to be imported.
   *
   * @param  name  Class name.
   * @return       Node representing the class.
   */
  public static ClassNode getClass(String name) {
    // Load actual class.
    try {
      InputStream input = ClassFinder.findClass(name);

      // Outline import (using classes accessible by compiler). Full import if
      // portability is not required.
      if(input == null) {
        ClassImporter ci = new ClassImporter(!portable);

        new ClassReader(name).accept(ci, 0);

        return ci.classNode;
      // Full class import (within input classpath).
      } else {
        ClassImporter ci = new ClassImporter(true);

        new ClassReader(input).accept(ci, 0);

        return ci.classNode;
      }
    // Empty class (allows analysis to continue).
    } catch(IOException e) {
      return new ClassNode(name, null, EnumSet.noneOf(Modifier.class));
    }
  }

  /**
   * Despite being a public class, it should not be possible to manually create
   * an instance (since its usage is very rigid).
   */
  private ClassImporter(boolean deep) {
    this.deep = deep;
  }

  /**
   * Only called a single time, which internally creates the ClassNode object
   * that will be populated by the import.
   *
   * @param version    JVM version that the class file was compiled for.
   * @param access     Bit field of access modifiers applying to the class.
   * @param name       Name of the class.
   * @param signature  Generic signature (for debugging/reflection).
   * @param superName  Name of the class inherited.
   * @param interfaces Array of interfaces implemented.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
                                        String superName, String[] interfaces) {
    // Create EnumSet of access modifiers.
    EnumSet<Modifier> modifiers = Modifier.getSet(access);

    // Instantiate ClassNode
    if(superName == null) {
      // NOTE: This should only occur for java/lang/Object.
      classNode = new ClassNode(name, null, modifiers);
    } else {
      classNode = new ClassNode(name, ClassNode.getClass(superName), modifiers);
    }

    // Add interfaces.
    for(String iface : interfaces) {
      classNode.getInterfaces().add(ClassNode.getClass(iface));
    }
    
    // NOTE: signature:  Generic signature (for debugging/reflection).
  }

  /**
   * Uses source file debug information so that the source can be referenced
   * when warnings are produced.
   *
   * @param source  Filename of source file (does not include directory).
   * @param debug
   */
  @Override
  public void visitSource(String source, String debug) {
    classNode.setSourceFile(source);
  }

  /**
   * Gives details of the enclosing class and (optionally) method.
   * 
   * @param owner   Enclosing class name.
   * @param name    Enclosing method name (or null for none).
   * @param desc    Encloding method descriptor (or null if none).
   */
  @Override
  public void visitOuterClass(String owner, String name, String desc) {
    // TODO: Should import simply to allow export.
  }

  /**
   * Visited for each inner class, contained in the class being imported.
   *
   * @param name   Fully qualified name for the class.
   * @param outer  ???
   * @param inner  Name that could be used within this class.
   * @param access Access modifiers.
   */
  @Override
  public void visitInnerClass(String name, String outer, String inner, int access) {
    // TODO: getClass(name), store result with modifiers in the class
  }

  /**
   * Visited for each annotation on the class. The annotation is added to the
   * class and imported.
   *
   * @param desc    Name of the annotation class.
   * @param visible Whether the annotation is visible at runtime.
   * @return        An ASM annotation visitor, if further details are required.
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    Annotation a = new Annotation(Type.getType(desc));

    classNode.addAnnotation(a);

    return new AnnotationImporter(a);
  }

  
  /**
   * For each class method, we construct a flow graph using MethodImporter, and
   * also a containing Method object.
   *
   * @param access      Access modifiers for the method.
   * @param name        Name of the method.
   * @param descriptor  Desriptor of argument and return types.
   * @param signature   Generic signature (for debugging/reflection).
   * @param exceptions  Array of exceptions thrown by the method.
   * @return            MethodVisitor to read the method (i.e. MethodImporter).
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor,
                                        String signature, String[] exceptions) {
    // Get the method from the class.
    Method m = classNode.getMethod(name, descriptor);

    m.setImplementation(null);
    m.getModifiers().clear();
    m.getModifiers().addAll(Modifier.getSet(access));

    // NOTE: Generic signature (for debugging/reflection).
    // TODO: Exceptions

    // Full import: create MethodImporter that builds from the entry block.
    if(deep) {
      Logger.getLogger("bytecode.Import").trace(
        "Importing method " + classNode.getName() + "." + name + " with code."
      );

      return new MethodImporter(m);
    // Shallow import
    } else {
      Logger.getLogger("bytecode.Import").trace(
        "Importing method " + classNode.getName() + "." + name + " without code."
      );

      return null;
    }
  }

  /**
   * For each field, a representative State object is created and added to the
   * class.
   *
   * @param access     Modifers applied to the field (including STATICness).
   * @param name       Name of the field.
   * @param descriptor Type description.
   * @param signature  Generic Signature (for debugging/reflection).
   * @param value      Default value (for static variables only).
   * @return           FieldVisitor (for annotations of the field) - null here.
   */
  @Override
  public FieldVisitor visitField(int access, String name, String descriptor,
                                               String signature, Object value) {
    // NOTE: signature: Generic Signature (for debugging/reflection).

    // Get the field from the class.
    Field f  = classNode.getField(name, descriptor);

    f.getModifiers().addAll(Modifier.getSet(access));
    f.setDefaultValue(value);

    // TODO: Anonymous inner class extension of EmptyVisitor that imports
    //       annotations.
    return null;
  }

  /**
   * Visited at the end of an import. Not used for import.
   */
  @Override
  public void visitEnd() {
    // Nothing
  }

  /**
   * Visited for attributes. Unclear what these are, and aren't needed.
   *
   * @param attr   Attribute.
   */
  @Override
  public void visitAttribute(Attribute attr) {
    // Nothing
  }
}
