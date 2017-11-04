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

import java.util.HashMap;
import java.util.Map;

import util.ObjectUtil;
import cl.Config;

public class Annotation {
  private final Type type;
  private final Map<String, Object> values = new HashMap<String, Object>();

  public Annotation(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void set(String key, Object value) {
    values.put(key, value);
  }

  public Object get(String key) {
    if(values.containsKey(key)) {
      return values.get(key);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    StringBuffer ret = new StringBuffer(type.toString());

    ret.append("(");

    if(values.size() > 0) {
      for(Map.Entry<String, Object> pair : values.entrySet()) {
        ret.append(pair.getKey() + "=" + pair.getValue() + ", ");
      }

      ret.delete(ret.length() - 2, ret.length());
    }
    ret.append(")");

    return ret.toString();
  }
}
