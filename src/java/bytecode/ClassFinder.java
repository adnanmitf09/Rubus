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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Finds classes within a class path.
 */
public class ClassFinder {
  /**
   * Class Paths (in order of precedence).
   */
  private static List<File> paths = new LinkedList<File>();

  /**
   * Static initialiser (imports java.class.path).
   */
  static {
    String[] parts = System.getProperty("java.class.path", ".")
                           .split(File.pathSeparator);

    for(int i = 0; i < parts.length; i++) {
      paths.add(new File(parts[i]));
    }
  }

  /**
   * Replaces the current class path.
   *
   * @param paths  List of paths that make up new class path.
   */
  public static void setClassPath(List<File> paths) {
    ClassFinder.paths = paths;
  }
  


  /**
   * Finds the <code>.class</code> file for a given class.
   *
   * @param  className Class name
   * @return           File for the class.
   */
  public static InputStream findClass(String className) {
    for(File path : paths) {
      // JAR File
      if(path.isFile()) {
        try {
          JarFile  jar   = new JarFile(path);
          JarEntry entry = jar.getJarEntry(className + ".class");

          if(entry != null) {
            return jar.getInputStream(entry);
          }
        } catch(IOException e) {
          // Just go to next path.
        }
      // Directory
      } else {
        try {
          return new FileInputStream(new File(path, className + ".class"));
        } catch (FileNotFoundException ex) {
          // Just go to next path.
        }
      }
    }

    return null;
  }

  public static Set<String> listClasses(String pattern) {
    Set<String> result = new HashSet<String>();

    for(File path : paths) {
      // JAR File
      if(path.isFile()) {
        try {
        	
        	
        	
          for(Enumeration<JarEntry> e = new JarFile(path).entries(); e.hasMoreElements();) {
            File entry = new File(e.nextElement().getName());

            // Classes
            if(entry.getPath().equals(pattern + ".class")) {
              result.add(pattern);
            }else  if(entry.getPath().endsWith(".class")) {
                result.add(entry.getPath().replace(".class",""));
              }

            // Packages
            File dir = new File(pattern);

            if((entry.getParentFile() != null) && entry.getParentFile().equals(dir)) {
              if(entry.getPath().endsWith(".class")) {
                result.add(entry.getPath().replace(".class", ""));
              }
            }
          }
        } catch(IOException e) {
          // Just go to next path.
        }
      // Directory
      } else {
        // Classes
        if(new File(path, pattern + ".class").canRead()) {
          result.add(pattern);
        }

        // Packages
        File dir = new File(path, pattern);

        if(dir.isDirectory()) {
          for(File file : dir.listFiles()) {
            if(file.getName().endsWith(".class")) {
              result.add(pattern + "/" + file.getName().replace(".class", ""));
            }
          }
        }
      }
    }

    return result;
  }
}
