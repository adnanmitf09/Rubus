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

import java.util.Collections;
import java.util.Set;

import cl.Config;

/**
 * A generic tree data structure. The number of children on each node is
 * unconstrained, and every node holds a value.
 */
public class Tree<T> {
  /**
   * Node Value.
   */
  private T value;

  /**
   * Children.
   */
  private Set<Tree<T>> children;

  /**
   * Constructor for a leaf node (i.e. no children).
   *
   * @param value    Value for the node.
   */
  public Tree(T value) {
    this.value    = value;
    this.children = Collections.emptySet();
  }

  /**
   * Constructor for a node with children.
   *
   * @param value    Value for the node.
   * @param children Children nodes.
   */
  public Tree(T value, Set<Tree<T>> children) {
    this.value    = value;
    this.children = Collections.unmodifiableSet(children);
  }

  /**
   * Accessor for value.
   *
   * @return       Value at this node.
   */
  public T getValue() {
    return value;
  }

  /**
   * Accessor for children of node.
   *
   * @return       Child nodes.
   */
  public Set<Tree<T>> getChildren() {
    return children;
  }

  /**
   * Returns a string representation of the tree.
   *
   * @return       String representation of the tree.
   */
  @Override
  public String toString() { if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    return getValue().toString() + "->" + getChildren().toString();
  }
}
