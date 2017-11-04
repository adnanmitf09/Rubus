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

package analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Used for alias relations. Convenient rename for map between LooseStates
 * and sets of CanonicalStates, and also causes the constructor to copy more
 * deeply.
 */
public class AliasMap extends HashMap<LooseState, Set<CanonicalState>> {
  public AliasMap() {
    super();
  }
  
  public AliasMap(AliasMap m) {
    super();

    for(Entry<LooseState, Set<CanonicalState>> entry : m.entrySet()) {
      put(entry.getKey(), new HashSet<CanonicalState>(entry.getValue()));
    }
  }
}
