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

package com.beoui.geocell.model;

import java.util.List;


/**
 * To run proximity fetch method, entity has to implement LocationCapable.
 * Location and Key are used in the algorithm (to sort and compare entities).
 * Geocells is used in query (entity must have a GEOCELLS column).
 *
 * @author Alexandre Gellibert
 *
 */
@Deprecated
public interface LocationCapable {

    /**
     *
     * @return the location in latitude/longitude
     */
    Point getLocation();

    /**
     *
     * @return the key of the entity used as a String
     */
    String getKeyString();

    List<String> getGeocells();

}
