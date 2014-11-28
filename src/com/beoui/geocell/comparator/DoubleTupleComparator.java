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

import java.util.Comparator;

import com.beoui.geocell.model.Tuple;

public class DoubleTupleComparator implements Comparator<Tuple<int[], Double>> {

    public int compare(Tuple<int[], Double> o1, Tuple<int[], Double> o2) {
        if(o1 == null && o2 == null) {
            return 0;
        }
        if(o1 == null) {
            return -1;
        }
        if(o2 == null) {
            return 1;
        }
        return o1.getSecond().compareTo(o2.getSecond());
    }

}
