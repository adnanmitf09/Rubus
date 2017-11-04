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

import java.util.Map;

/**
 *
 */
public class MapIterable<S,T> extends TransformIterable<S,T> {
  private Map<S,T> map;

  public MapIterable(Iterable<S> input, Map<S,T> map) {
    super(input);
    this.map = map;
  }

  public static <S,T> MapIterable<S,T> construct(Iterable<S> input, Map<S,T> map) {
    return new MapIterable<S,T>(input, map);
  }

  @Override
  protected T transform(S in) {
    return map.get(in);
  }
}
