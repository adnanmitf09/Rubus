/*
 * Parallelising JVM Compiler -Rubus
 *Copyright 2014 Muhammad Adnan, University of the Punjab
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

import graph.ClassNode;
import graph.Method;
import graph.Modifier;
import graph.state.Field;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Exports Java class representations using the ASM library to a class file.
 * This is only possible where the class is a 'deep' representation.
 */
public class ClassExporter {
  /**
   * Directory to which classes should be exported.
   */
  private static File outputDirectory = new File(".");

  /**
   * Changes the output class directory to that given.
   *
   * @param  directory   New output class path.
   */
  public static void setOutputDirectory(File directory) {
    outputDirectory = directory;
  }
  
  public static File getOutputDirectory() {
	    return outputDirectory;
	  }

  /**
   * Export the given class to a file. The location of this file is determined
   * by the output classpath. If the class is not deeply loaded or does not
   * exist, a <code>RuntimeException</code> is thrown.
   *
   * @param  className Class name.
   * @throws ClassNotFoundException
   */
  public static void export(String className) {
    export(ClassNode.getClass(className));
  }

  /**
   * Exports the given class node to a file. The location of this file is
   * determined by the output classpath. This will cause a
   * <code>RuntimeException</code> if the class is not deeply loaded.
   *
   * @param  c     Class node to be exported.
   */
  //check posibilities to add method and field directlt into this class - adnan
  public static void export(ClassNode c) {
    // Form array of interface names.
    String[] interfaces = new String[c.getInterfaces().size()];
    int index = 0;
    for(ClassNode iface : c.getInterfaces()) {
      interfaces[index++] = iface.getName();
    }

    // Export Class Header
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    cw.visit(
      Opcodes.V1_6,
      Modifier.getBitField(c.getModifiers()),
      c.getName(),
      null, // NOTE: Generic signature (for debugging/reflection).
      (c.getSuperClass() == null) ? null : c.getSuperClass().getName(),
      interfaces
    );

    // TODO: Inner / Outer Classes

    // Debug Information
    if(c.getSourceFile() != null) {
      cw.visitSource(c.getSourceFile(), null);
    }

    // Export Fields
    for(Field f : c.getFields()) {
      cw.visitField(
        Modifier.getBitField(f.getModifiers()),
        f.getName(),
        f.getType().getDescriptor(),
        null,
        f.getDefaultValue()
      );
    }

    // Export Methods
    MethodVisitor mv;

    for(Method m : c.getMethods()) {
      if(!m.getModifiers().contains(Modifier.INHERITED)) {
        mv = cw.visitMethod(
          Modifier.getBitField(m.getModifiers()),
          m.getName(),
          m.getDescriptor(),
          null,             // NOTE: Generic signature (for debugging/reflection).
          null              // TODO: Exceptions
        );

        // Export code unless NATIVE or ABSTRACT.
        if(!m.getModifiers().contains(Modifier.NATIVE) &&
                                  !m.getModifiers().contains(Modifier.ABSTRACT)) {
          mv.visitCode();
          m.getImplementation().accept(new BlockExporter(mv));

          // Arguments are just dummys, the ClassWriter recalculates.
          mv.visitMaxs(0, 0);
        }

        mv.visitEnd();
      }
    }

    cw.visitEnd();

    File f = new File(outputDirectory, c.getName() + ".class");
    try {
      f.getParentFile().mkdirs();
      new FileOutputStream(f).write(cw.toByteArray());
    } catch(IOException e) {
      throw new RuntimeException("Could not create class file: " + f);
    }
  }
}
