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

import java.util.EnumSet;

import org.objectweb.asm.Opcodes;

/**
 * Represents modifiers that can be applied to items in a class file. This
 * includes all modifiers for classes, methods and fields.
 */
public enum Modifier {
  INHERITED,

  // Class Modifiers
  PUBLIC, FINAL, SUPER, INTERFACE, ABSTRACT, SYNTHETIC, ANNOTATION, ENUM,

  // Field Modifiers
  /*PUBLIC, FINAL,*/ PRIVATE, PROTECTED, STATIC, VOLATILE, TRANSIENT,

  // Method Modifiers
  /*PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, */ SYNCHRONIZED, BRIDGE,
  VARARGS, NATIVE, /*ABSTRACT,*/ STRICT /*SYNTHETIC*/;

  /**
   * Returns an integer bit field for the EnumSet, where the bit values are
   * defined as in the class file format.
   *
   * @param  set   Set of modifiers to convert to a bit field.
   * @return       Integer bit field.
   */
  public static int getBitField(EnumSet<Modifier> set) {
    int access = 0;

    if(set.contains(PUBLIC)) {
      access |= Opcodes.ACC_PUBLIC;
    }

    if(set.contains(PROTECTED)) {
      access |= Opcodes.ACC_PROTECTED;
    }

    if(set.contains(PRIVATE)) {
      access |= Opcodes.ACC_PRIVATE;
    }

    if(set.contains(STATIC)) {
      access |= Opcodes.ACC_STATIC;
    }

    if(set.contains(FINAL)) {
      access |= Opcodes.ACC_FINAL;
    }

    if(set.contains(SUPER)) {
      access |= Opcodes.ACC_SUPER;
    }

    if(set.contains(INTERFACE)) {
      access |= Opcodes.ACC_INTERFACE;
    }

    if(set.contains(ABSTRACT)) {
      access |= Opcodes.ACC_ABSTRACT;
    }

    if(set.contains(SYNTHETIC)) {
      access |= Opcodes.ACC_SYNTHETIC;
    }

    if(set.contains(ANNOTATION)) {
      access |= Opcodes.ACC_ANNOTATION;
    }

    if(set.contains(ENUM)) {
      access |= Opcodes.ACC_ENUM;
    }

    if(set.contains(VOLATILE)) {
      access |= Opcodes.ACC_VOLATILE;
    }

    if(set.contains(TRANSIENT)) {
      access |= Opcodes.ACC_TRANSIENT;
    }

    if(set.contains(SYNCHRONIZED)) {
      access |= Opcodes.ACC_SYNCHRONIZED;
    }

    if(set.contains(BRIDGE)) {
      access |= Opcodes.ACC_BRIDGE;
    }

    if(set.contains(VARARGS)) {
      access |= Opcodes.ACC_VARARGS;
    }

    if(set.contains(NATIVE)) {
      access |= Opcodes.ACC_NATIVE;
    }

    return access;
  }

  /**
   * Returns an EnumSet of the modifiers, based on the given bit field (where
   * the bit values are defined as in the class file format).
   *
   * @param access Integer bit field giving access modifiers.
   * @return       Set of modifiers.
   */
  public static EnumSet<Modifier> getSet(int access) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

    if((access & Opcodes.ACC_PUBLIC) != 0) {
      modifiers.add(Modifier.PUBLIC);
    }

    if((access & Opcodes.ACC_PROTECTED) != 0) {
      modifiers.add(Modifier.PROTECTED);
    }

    if((access & Opcodes.ACC_PRIVATE) != 0) {
      modifiers.add(Modifier.PRIVATE);
    }

    if((access & Opcodes.ACC_STATIC) != 0) {
      modifiers.add(Modifier.STATIC);
    }

    if((access & Opcodes.ACC_FINAL) != 0) {
      modifiers.add(Modifier.FINAL);
    }

    if((access & Opcodes.ACC_SUPER) != 0) {
      modifiers.add(Modifier.SUPER);
    }

    if((access & Opcodes.ACC_INTERFACE) != 0) {
      modifiers.add(Modifier.INTERFACE);
    }

    if((access & Opcodes.ACC_ABSTRACT) != 0) {
      modifiers.add(Modifier.ABSTRACT);
    }

    if((access & Opcodes.ACC_SYNTHETIC) != 0) {
      modifiers.add(Modifier.SYNTHETIC);
    }
    
    if((access & Opcodes.ACC_ANNOTATION) != 0) {
      modifiers.add(Modifier.ANNOTATION);
    }

    if((access & Opcodes.ACC_ENUM) != 0) {
      modifiers.add(Modifier.ENUM);
    }

    if((access & Opcodes.ACC_VOLATILE) != 0) {
      modifiers.add(Modifier.VOLATILE);
    }

    if((access & Opcodes.ACC_TRANSIENT) != 0) {
      modifiers.add(Modifier.TRANSIENT);
    }

    if((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
      modifiers.add(Modifier.SYNCHRONIZED);
    }

    if((access & Opcodes.ACC_BRIDGE) != 0) {
      modifiers.add(Modifier.BRIDGE);
    }

    if((access & Opcodes.ACC_VARARGS) != 0) {
      modifiers.add(Modifier.VARARGS);
    }

    if((access & Opcodes.ACC_NATIVE) != 0) {
      modifiers.add(Modifier.NATIVE);
    }

    if((access & Opcodes.ACC_STRICT) != 0) {
      modifiers.add(Modifier.STRICT);
    }
    
    return modifiers;
  }
}
