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

package analysis.loops;

import graph.Block;
import graph.Loop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Tree;

/**
 * Calculates the nesting of a set of loops. Used to prevent attempting to
 * parallelise both an outer loop and one of its descendants.
 */
public class LoopNester {
  /**
   * Nests the given set of loops into a tree structure according to their
   * containments.
   *
   * @param  loops Set of loops to nest.
   * @return       Tree of nesting structure.
   */
  public static <T extends Loop> Set<Tree<T>> nest(Set<T> loops) {
    Set<T> root = new HashSet<T>(loops);
    Map<T, Set<T>> children = new HashMap<T, Set<T>>();
    Map<T, Set<Block>> bodies = new HashMap<T, Set<Block>>();

    // Initialise map and cache bodies.
    for(T loop : root) {
      children.put(loop, new HashSet<T>());
      bodies.put(loop, loop.getBody());
    }

    // Calculate nesting.
    for(T loop : loops) {
      for(T parent : loops) {
        if(bodies.get(parent).contains(loop)) {
          root.remove(loop);
          children.get(parent).add(loop);
          break;
        }
      }
    }

    // Generate tree.
    Set<Tree<T>> result = new HashSet<Tree<T>>();

    for(T loop : root) {
      result.add(createTree(loop, children));
    }

    return result;
  }

  /**
   * Creates a <code>Tree</code> structure based on a given root and description
   * of the tree.
   *
   * @param  loop     Root of tree.
   * @param  children Description of tree (map from node to its children).
   * @return          Tree structure.
   */
  private static <T extends Loop> Tree<T> createTree(T loop, Map<T, Set<T>> children) {
    Set<Tree<T>> childNodes = new HashSet<Tree<T>>();

    // Recurse on children.
    for(T child : children.get(loop)) {
      childNodes.add(createTree(child, children));
    }

    return new Tree<T>(loop, childNodes);
  }
}
