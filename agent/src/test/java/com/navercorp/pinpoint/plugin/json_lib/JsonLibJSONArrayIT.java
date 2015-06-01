/**
 * Copyright 2014 NAVER Corp.
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
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.json_lib;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import java.lang.reflect.Method;

import net.sf.json.JSONArray;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 *@author Sangyoon Lee
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"net.sf.json-lib:json-lib:jar:jdk15:[1.0,)"})
public class JsonLibJSONArrayIT {

    private static final String test = "[{'string':'JSON'}]";
    private static final String json = JSONArray.fromObject(test).toString();
    private static final String serviceType = "JSON-LIB";
    private static final String annotationKeyName = "json-lib.json.length";
    // "Pinpoint"
    private static final PluginTestVerifier.ExpectedAnnotation fromObjectAnnotation = annotation(annotationKeyName, json.length());
    // "Pinpoint"
    private static final PluginTestVerifier.ExpectedAnnotation toStringAnnotation = annotation(annotationKeyName, json.length());

    @Test
    public void jsonToArrayTest() throws Exception {
        Method fromObject = JSONArray.class.getMethod("fromObject", Object.class);
        Method toArray = JSONArray.class.getMethod("toArray", JSONArray.class);
        Method toString = JSONArray.class.getMethod("toString");
	// JSONArray.toCollection() is added in json-lib 2.2. so check toCollection in JSONArray 
	Method toCollection = null;
        try{
	        toCollection = JSONArray.class.getMethod("toCollection", JSONArray.class);
	} catch (NoSuchMethodException e) {}
        Method toList = JSONArray.class.getMethod("toList", JSONArray.class);

        String test = "[{'string':'JSON'}]";

        JSONArray jsn = JSONArray.fromObject(test);
        // JSONArray.toArray() of json-lib 2.0 and below have different return type. so we invoke it by reflection to avoid NoSuchMethodError
        toArray.invoke(null, jsn);
        jsn.toString();
        if (toCollection != null){
            toCollection.invoke(null, jsn);
        }
        toList.invoke(null, jsn);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

	verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, fromObject, null, null, null, null, fromObjectAnnotation);
        verifier.verifyApi("JSON-LIB", toArray);
        verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, toString, null, null, null, null, toStringAnnotation);
        if (toCollection != null){
            verifier.verifyApi("JSON-LIB", toCollection);
        }
        verifier.verifyApi("JSON-LIB", toList);
        
        verifier.verifyTraceBlockCount(0);
    }
}