/*
Copyright 2010 Alexandre Gellibert

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/
LICENSE-2.0 Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an "AS IS"
BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package com.beoui.geocell.comparator;

import com.beoui.geocell.model.LocationCapable;
import com.beoui.geocell.model.Tuple;

/**
 * This class is used to merge lists of Tuple<T, Double>. Lists are sorted following Double value but are equals only if T.key (same entity) are equals.
 *
 * @author Alexandre Gellibert
 *
 * @param <T>
 */
@Deprecated
public class LocationComparableTuple<T extends LocationCapable> extends Tuple<T ,Double> implements Comparable<LocationComparableTuple<T>>{

    public LocationComparableTuple(T first, Double second) {
        super(first, second);
    }

    public int compareTo(LocationComparableTuple<T> o) {
        if(o == null) {
            return -1;
        }
        int doubleCompare = this.getSecond().compareTo(o.getSecond());
        if(doubleCompare == 0) {
            return this.getFirst().getKeyString().compareTo(o.getFirst().getKeyString());
        } else {
            return doubleCompare;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocationComparableTuple<LocationCapable> other = (LocationComparableTuple<LocationCapable>) obj;
        if (getFirst() == null) {
            if (other.getFirst() != null) {
                return false;
            }
        } else if (!getFirst().getKeyString().equals(other.getFirst().getKeyString())) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        return getFirst().getKeyString().hashCode();
    }

}