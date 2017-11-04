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

import graph.state.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.ObjectUtil;
import cl.Config;

/**
 * Stores method descriptor (name and type), along with optional information
 * about the class such as modifiers and its implementation.
 */
public class Method {
  /**
   * Owner class.
   */
  private ClassNode owner = null;

  /**
   * Method name.
   */
  private String name;

  /**
   * Method source.
   */
  private String source;

  public String getSource() {
	return source;
}

public void setSource(String source) {
	this.source = source;
}

/**
   * Method parameter types.
   */
  private Type[] parameters;

  /**
   * Annotations on the method.
   */
  private Map<Type, Annotation>[] paramAnnotations;

  /**
   * Return type of the method.
   */
  private Type returnType;

  /**
   * Annotations on the method.
   */
  private Map<Type, Annotation> annotations = new HashMap<Type, Annotation>();

  /**
   * Modifiers.
   */
  private EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

  /**
   * Method implementation (first block).
   */
  private Block implementation = null;

  /**
   * Initialises the method object, setting its name and types from those given.
   *
   * @param name       Method name.
   * @param parameters Method parameter types.
   * @param returnType Method return type.
   */
  public Method(String name, Type[] parameters, Type returnType) {
    // Simple initialisation.
    this.name       = name;
    this.parameters = parameters;
    this.returnType = returnType;

    // Initialise parameter annotation maps.
    if(modifiers.contains(Modifier.STATIC)) {
      paramAnnotations = new HashMap[parameters.length];
    } else {
      paramAnnotations = new HashMap[parameters.length + 1];
    }

    for(int i = 0; i < paramAnnotations.length; i++) {
      paramAnnotations[i] = new HashMap<Type, Annotation>();
    }
  }

  /**
   * Initialises the method object, setting its name as given, and the types
   * based on the descriptor given.
   *
   * @param name       Method name.
   * @param descriptor Method type descriptor.
   */
  public Method(String name, String descriptor) {
    this(
      name,
      Type.getParameterTypes(descriptor),
      Type.getReturnType(descriptor)
    );
  }

  /**
   * Sets the owner class of the method. This should only be used from within
   * <code>ClassNode</code>.
   *
   * @param classNode  Owner class.
   */
  public void setOwner(ClassNode classNode) {
    this.owner = classNode;
  }

  /**
   * Returns the owner class of the method.
   * 
   * @return           Owner class.
   */
  public ClassNode getOwner() {
    return owner;
  }

  /**
   * Returns the name of the method.
   *
   * @return           Method name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns method parameter types (in order).
   *
   * @return           Parameter types.
   */
  public List<Type> getParameters() {
    return Arrays.asList(parameters);
  }

  /**
   * Returns the variables defined by method parameters.
   *
   * @return           Parameter variables.
   */
  public List<Variable> getParameterVariables() {
    List<Variable> result = new LinkedList<Variable>();
    int number = 0;

    // 'this' argument for non-static methods.
    if(!modifiers.contains(Modifier.STATIC)) {
      result.add(new Variable(number++, owner.getType()));
    }

    // Arguments
    for(Type type : parameters) {
      result.add(new Variable(number, type));
      number += type.getSize();
    }

    return Collections.unmodifiableList(result);
  }



  /**
   * Returns number of parameters taken by the method.
   *
   * @return           Number of parameters.
   */
  public int getParameterCount() {
    return parameters.length;
  }

  /**
   * Returns the return type of the method.
   *
   * @return           Return type.
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Returns the Java internal method descriptor string.
   *
   * @return           Internal method descriptor.
   */
  public String getDescriptor() {
    return Type.getMethodDescriptor(parameters, returnType);
  }

  /**
   * Returns the set of modifiers specified on the method. This is a modifiable
   * set.
   *
   * @return           Modifier set for method.
   */
  public EnumSet<Modifier> getModifiers() {
    return modifiers;
  }

  /**
   * Sets the implementation block for the method.
   *
   * @param b          Start block of implementation.
   */
  public void setImplementation(Block b) {
    implementation = b;
  }

  /**
   * Returns the first block of the method implementation.
   *
   * @return           First block of implementation.
   */
  public Block getImplementation() {
    return implementation;
  }

  /**
   * Returns a collection of the annotations defined on the class.
   *
   * @return     Immutable set of annotations.
   */
  public Collection<Annotation> getAnnotations() {
    return Collections.unmodifiableCollection(annotations.values());
  }

  /**
   * Adds the given annotation to the class, overwriting any other annotation
   * with the same type.
   *
   * @param annotation  New annotation.
   */
  public void addAnnotation(Annotation annotation) {
    annotations.put(annotation.getType(), annotation);
  }

  /**
   * Returns the specified annotation on the class, or <code>null</code> if no
   * annotation of the given type is defined for the class.
   *
   * @param  type       Type of the annotation.
   * @return            Relevant annotation object.
   */
  public Annotation getAnnotation(Type type) {
    return annotations.get(type);
  }

  /**
   * Removes the given annotation from the class.
   *
   * @param a    Annotation to remove.
   */
  public void removeAnnotation(Annotation a) {
    annotations.remove(a.getType());
  }

  /**
   * Returns a collection of the annotations defined on the specified parameter.
   *
   * @param  i   Index of the parameter.
   * @return     Immutable set of annotations.
   */
  public Collection<Annotation> getParameterAnnotations(int i) {
    return Collections.unmodifiableCollection(paramAnnotations[i].values());
  }

  /**
   * Adds the given annotation to the class, overwriting any other annotation
   * with the same type.
   *
   * @param i           Index of the parameter.
   * @param annotation  New annotation.
   */
  public void addParameterAnnotation(int i, Annotation annotation) {
    paramAnnotations[i].put(annotation.getType(), annotation);
  }

  /**
   * Returns the specified annotation on the class, or <code>null</code> if no
   * annotation of the given type is defined for the class.
   *
   * @param  i          Index of the parameter.
   * @param  type       Type of the annotation.
   * @return            Relevant annotation object.
   */
  public Annotation getParameterAnnotation(int i, Type type) {
    return paramAnnotations[i].get(type);
  }

  /**
   * Removes the given annotation from the class.
   *
   * @param i    Index of the parameter.
   * @param a    Annotation to remove.
   */
  public void removeParameterAnnotation(int i, Annotation a) {
    paramAnnotations[i].remove(a.getType());
  }

  /**
   * Returns a textual description of the method (the concatenation of its name
   * and its descriptor).
   *
   * @return           Textual description.
   */
  @Override
  public String toString() { if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    return owner + "." + name + getDescriptor();
  }
}
