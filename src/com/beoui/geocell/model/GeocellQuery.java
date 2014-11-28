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
 * GeocellQuery splits the traditional query in 3 parts:
 * the base query string,
 * the declared parameters
 * and the list of object parameters.
 *
 * Additional information on http://code.google.com/appengine/docs/java/datastore/queriesandindexes.html
 *
 * This allows us to create new queries and adding conditions/filters like in the proximity fetch.
 *
 * @author Alexandre Gellibert
 *
 */
public class GeocellQuery {

    /**
     * Base query string without the declared parameters and without the entity name. Ex: "lastName == lastNameParam"
     *
     * CAREFUL: must not contain "order" clauses!
     */
    private String baseQuery;

    /**
     * (Optional)
     * Declared parameters. Ex: "String lastNameParam"
     */
    private String declaredParameters;

    /**
     * (Optional)
     * List of parameters. Ex: Arrays.asList("Smith")
     */
    private List<Object> parameters;
    
    // Use this constructor to build empty base queries.
    public GeocellQuery() {
        this.baseQuery = null;
        this.declaredParameters = null;
        this.parameters = null;
    }

    public GeocellQuery(String baseQuery) {
        this.baseQuery = baseQuery;
        this.declaredParameters = null;
        this.parameters = null;
    }

    public GeocellQuery(String baseQuery, List<Object> parameters) {
        this.baseQuery = baseQuery;
        this.declaredParameters = null;
        this.parameters = parameters;
    }

    public GeocellQuery(String baseQuery, String declaredParameters,
            List<Object> parameters) {
        this.baseQuery = baseQuery;
        this.declaredParameters = declaredParameters;
        this.parameters = parameters;
    }

    public String getBaseQuery() {
        return baseQuery;
    }

    public String getDeclaredParameters() {
        return declaredParameters;
    }

    public List<Object> getParameters() {
        return parameters;
    }

}
