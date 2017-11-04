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

package graph.state;

import graph.ClassNode;
import graph.Modifier;
import graph.Type;
import graph.instructions.Producer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 *
 */
public class Field implements State {
  private ClassNode         owner;
  private String            name;
  private EnumSet<Modifier> access;
  private Type              type;
  private Object            value;

  public Field(String name, Type type) {
    this.name  = name;
    this.type  = type;
    this.value = null;
  }

  public Field(String name, Type type, Object value) {
    this.name  = name;
    this.type  = type;
    this.value = value;
  }

  public Field(String name, String descriptor, Object value) {
    this.name  = name;
    this.type  = Type.getType(descriptor);
    this.value = value;
  }

  public Field(String name, String descriptor) {
    this.owner  = null;
    this.name   = name;
    this.access = EnumSet.noneOf(Modifier.class);
    this.type   = Type.getType(descriptor);
    this.value  = null;
  }

  public void setOwner(ClassNode clazz) {
    owner = clazz;
  }

  public ClassNode getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public EnumSet<Modifier> getModifiers() {
    return access;
  }

  @Override
  public Type getType() {
    return type;
  }

  public void setDefaultValue(Object value) {
    this.value = value;
  }

  public Object getDefaultValue() {
    return value;
  }

  /**
   * Returns the static variable itself.
   *
   * @return       The static variable (<code>this</code>).
   */
  @Override
  public State getBase() {
    return this;
  }

  @Override
  public Producer[] getOperands() {
    return new Producer[0];
  }

  @Override
  public List<Producer> getIndicies() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return owner + "." + name + " [" + type + "]";
  }
}
