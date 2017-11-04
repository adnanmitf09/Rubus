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

import graph.state.Field;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.ObjectUtil;
import bytecode.ClassImporter;
import cl.Config;

/**
 * Represents any class, and its implementation (if loaded).
 */
public class ClassNode {
  /**
   * Map of all known classes (i.e. those that have been loaded).
   */
  private static Map<String, ClassNode> classes = new HashMap<String, ClassNode>();

  /**
   * Class name
   */
  private String name;

  /**
   * Annotations on the method.
   */
  private Map<Type, Annotation> annotations = new HashMap<Type, Annotation>();

  /**
   * Class modifiers.
   */
  private EnumSet<Modifier> modifiers;

  /**
   * Source file name.
   */
  private String sourceFile;

  /**
   * Superclass.
   */
  private ClassNode superClass;

  /**
   * Interfaces.
   */
  private Set<ClassNode> interfaces = new HashSet<ClassNode>();

  /**
   * Methods contained in the class, keyed by the concatenation of their name
   * and descriptor (as "name:descriptor").
   */
  private Map<String, Method> methods = new HashMap<String, Method>();

  /**
   * Fields (including statics) contained in the class.
   */
  private Map<String, Field> fields = new HashMap<String, Field>();

  /**
   * Returns the ClassNode that represents the given class name.
   *
   * @param  name  Class name.
   * @return       ClassNode for the given name.
   */
  public static ClassNode getClass(String name) {
    if(classes.containsKey(name)) {
      return classes.get(name);
    } else {
      return ClassImporter.getClass(name);
    }
  }

  /**
   * Constructor for class representation.
   *
   * @param name       Name of class (using / rather than . as separator).
   * @param superClass Reference to superclass object.
   * @param modifiers  Set of modifiers applying to the class.
   */
  public ClassNode(String name, ClassNode superClass,
                                                  EnumSet<Modifier> modifiers) {
    this.name       = name;
    this.superClass = superClass;
    this.modifiers  = EnumSet.copyOf(modifiers);

    classes.put(name, this);
  }

  /**
   * Returns the name of the class.
   *
   * @return       Class name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the class
   *
   * @return       Class type.
   */
  public Type getType() {
    return Type.getObjectType(name);
  }

  /**
   * Returns set of modifiers for the class.
   *
   * @return       Modifiers.
   */
  public EnumSet<Modifier> getModifiers() {
    return modifiers;
  }

  /**
   * Returns the superclass of the class, or <code>null</code> if none exist.
   *
   * @return       Superclass.
   */
  public ClassNode getSuperClass() {
    return superClass;
  }

  /**
   * Returns set of interfaces implemented by the class.
   *
   * @return       Set of interfaces.
   */
  public Set<ClassNode> getInterfaces() {
    return interfaces;
  }

  /**
   * Determines whether this class is a superclass of that given.
   *
   * @param  clazz Class to compare with.
   * @return       <code>true</code> if this class is a superclass of that given
   *               or <code>false</code> otherwise.
   */
  public boolean isSuperClass(ClassNode clazz) {
    // Super Class
    if(clazz.superClass != null) {
      if(clazz.superClass.equals(this) || isSuperClass(clazz.superClass)) {
        return true;
      }
    }

    // Interfaces
    for(ClassNode iface : clazz.interfaces) {
      if(iface.equals(this) || isSuperClass(iface)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Sets the filename from which the class was compiled.
   *
   * @param file   Filename
   */
  public void setSourceFile(String file) {
    sourceFile = file;
  }

  /**
   * Returns the filename from which the class was compiled.
   *
   * @return       Filename
   */
  public String getSourceFile() {
    return sourceFile;
  }

  /**
   * Returns a collection of the annotations defined on the class.
   *
   * @return       Immutable set of annotations.
   */
  public Collection<Annotation> getAnnotations() {
    return new HashSet<Annotation>(annotations.values());
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
   * @param  type       sType of the annotation.
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
   * Returns a collection of the methods contained in the class.
   *
   * @return     Immutable set of methods.
   */
  public Collection<Method> getMethods() {
    return new HashSet<Method>(methods.values());
  }

  /**
   * Adds the specified method to the class, overwriting any other method with
   * the same name and descriptor.
   *
   * @param  method     New method.
   */
  public void addMethod(Method method) {
    // Check not already associated with another class.
    if(method.getOwner() != null)
      throw new RuntimeException("Method already associated with a class.");

    methods.put(method.getName() + ":" + method.getDescriptor(), method);

    method.setOwner(this);
  }

  /**
   * Returns the specified method from the class, or inherited from a
   * superclass. If none exists by the given name and descriptor, a new method
   * is added to the class defined as such.
   *
   * @param  name       Name of the method.
   * @param  descriptor Descriptor of the method.
   * @return            Relevant method object.
   */
  public Method getMethod(String name, String descriptor) {
    final String key = name + ":" + descriptor;

    if(methods.containsKey(key)) {
      return methods.get(key);
    } else {
      ClassNode ancestor = superClass;

      // Check for inherited methods.
      while(ancestor != null) {
        if(ancestor.methods.containsKey(key)) {
          Method method = ancestor.methods.get(key);

          // TODO: Any other modifiers that prevent inheritance?
          if(!method.getModifiers().contains(Modifier.STATIC)
                        && !method.getModifiers().contains(Modifier.PRIVATE)) {
            Method m = new Method(method.getName(), method.getDescriptor());

            m.setOwner(this);
            m.setImplementation(method.getImplementation());
            m.getModifiers().addAll(method.getModifiers());
            m.getModifiers().add(Modifier.INHERITED);

            methods.put(key, m);

            return m;
          }
        }

        ancestor = ancestor.superClass;
      }

      // Create new.
      Method m = new Method(name, descriptor);

      m.setOwner(this);

      methods.put(key, m);

      return m;
    }
  }

  /**
   * Removes the given method from the class.
   *
   * @param m    Method to remove.
   */
  public void removeMethod(Method m) {
    methods.remove(m.getName() + ":" + m.getDescriptor());

    m.setOwner(null);
  }

  /**
   * Returns a collection of the fields defined for the class.
   *
   * @return     Immutable set of fields.
   */
  public Collection<Field> getFields() {
    return new HashSet<Field>(fields.values());
  }

  /**
   * Returns the field of the given name and type in the class, or if none exist
   * a new field is created and added to the class.
   *
   * @param name       Field name.
   * @param descriptor Field descriptor.
   * @return           Field object.
   */
  public Field getField(String name, String descriptor) {
    if(fields.containsKey(name)) {
      Field f = fields.get(name);

      f.getType().unify(Type.getType(descriptor));

      return fields.get(name);
    } else {
      Field f = new Field(name, descriptor);

      f.setOwner(this);

      fields.put(name, f);

      return f;
    }
  }

  /**
   * Adds the given field to the class.
   *
   * @param field  Field to add.
   */
  public void addField(Field field) {
    // Check not already associated with another class.
    if(field.getOwner() != null)
      throw new RuntimeException("Field already associated with a class.");

    fields.put(field.getName(), field);

    field.setOwner(this);
  }

  /**
   * Removes the given field from the class.
   *
   * @param field  Field to remove.
   */
  public void removeField(Field field) {
    fields.remove(field.getName());

    field.setOwner(null);
  }

  /**
   * Returns a textual description of the class (its name).
   *
   * @return     Textual representation of class.
   */
  @Override
  public String toString() { if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    return name;
  }
  
//  public String toStringRep(){
//	 
//		    StringBuilder result = new StringBuilder();
//		    String newLine = System.getProperty("line.separator");
//
//		    result.append(this.getClass().getName());
//		    result.append(" Object {");
//		    result.append(newLine);
//
//		    //determine fields declared in this class only (no fields of superclass)
//		    java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
//
//		    //print field names paired with their values
//		    for (java.lang.reflect.Field field : fields) {
//		    	field.setAccessible(true);
//		      result.append("  ");
//		      try {
//		        result.append(field.getName());
//		        result.append(": ");
//		        //requires access to private field:
//		        result.append(field.get(this));
//		      }
//		      catch (IllegalAccessException ex) {
//		        System.out.println(ex);
//		      }
//		      result.append(newLine);
//		    }
//		    result.append("}");
//
//		    return result.toString();
//		  } 
	  
  
}
