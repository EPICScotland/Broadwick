/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.utils;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * HashMap compatible Pair class (i.e. a
 * <code>Collection</code> that stores exactly 2 non-null objects and is not mutable). They respect
 * <code>equals</code> and may be used as indices or map keys. <p> Note that they do not protect from malevolent
 * behavior: if one or another object in the tuple is mutable, then it can be changed with the usual bad effects.
 * <p/>
 * @param <A> the type of the first element in pair.
 * @param <B> the type of the second element in the pair.
 */
@EqualsAndHashCode
public class Pair<A, B> implements Serializable {

    @Getter
    @Setter
    private A first;
    @Getter
    @Setter
    private B second;
    /**
     * Create the pair object with the given coordinates.
     * @param first  the first element in the pair
     * @param second the second element in the pair
     */
    public Pair(final A first, final B second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Pair cannot contain null values");
        }
        this.first = first;
        this.second = second;
    }

    /**
     * Generate a string describing this pair object.
     * @return the description of this object.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('<').append(first);
        sb.append(':');
        sb.append(second).append('>');
        return sb.toString();
    }

}
