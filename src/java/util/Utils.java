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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 */
public class Utils {

  public static int count(String needle, String haystack) {
    int count = 0, index = 0;
if(haystack!=null)
    while((index = haystack.indexOf(needle, index) + 1) != 0) count++;

    return count;
  }

  /**
   *
   * @param iterable
   * @param separator
   * @return
   */
  public static String join(Iterable iterable, String separator) {
    Iterator i = iterable.iterator();

    if(i.hasNext()) {
      StringBuilder str = new StringBuilder().append(i.next());

      while(i.hasNext()) {
        str.append(separator).append(i.next());
      }

      return str.toString();
    } else {
      return "";
    }
  }

  /**
   *
   * @param <T>
   * @param input
   * @return
   */
  public static <T> Set<T> union(Iterable<Set<T>> input) {
    Set<T> set = new HashSet<T>();

    for(Set<T> s : input) {
      set.addAll(s);
    }

    return set;
  }

  /**
   * 
   * @param <T>
   * @param input
   * @return
   */
  public static <T> Set<T> intersect(Iterable<Set<T>> input) {
    Set<T> set = null;

    for(Set<T> s : input) {
      if(set == null) {
        set = new HashSet<T>(s);
      } else {
        set.retainAll(s);
      }
    }

    if(set == null) {
      set = new HashSet<T>();
    }

    return set;
  }

  public static <T> T getSingleElement(Set<T> set) {
    if(set.size() == 1) {
      return set.iterator().next();
    } else {
      return null;
    }
  }
  public static boolean deleteDirectory(File directory) {
	  if(directory == null) return false;
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
}
