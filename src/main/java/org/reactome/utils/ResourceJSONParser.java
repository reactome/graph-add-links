package org.reactome.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 4/11/2022
 */
public class ResourceJSONParser {

    public static List<String> convertJSONArrayToStringList(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }
        return jsonArray.toList().stream().map(Object::toString).collect(Collectors.toList());
    }

    public static Map<String, JSONObject> getResourceJSONObjects() {
        JSONObject resourceJSONObject = getIdentifierResourcesAsJSONObject();
        return resourceJSONObject.keySet().stream().collect(Collectors.toMap(
            resourceName -> resourceName,
            resourceName -> resourceJSONObject.getJSONObject(resourceName)
        ));
    }

    public static JSONObject getResourceJSONObject(String referenceName) {
        JSONObject resourceJSONObject = getIdentifierResourcesAsJSONObject().getJSONObject(referenceName);
        return resourceJSONObject != null ? resourceJSONObject : new JSONObject();
    }

    private static JSONObject getIdentifierResourcesAsJSONObject() {
        return getIdentifierResourcesJSON().getJSONObject("resources");
    }

    private static JSONObject getIdentifierResourcesJSON() {
        JSONTokener jsonTokener = new JSONTokener(getIdentifierResourcesJSONInputStream());
        return new JSONObject(jsonTokener);
    }

    private static InputStream getIdentifierResourcesJSONInputStream() {
        return ResourceJSONParser.class.getClassLoader().getResourceAsStream("identifier-resources.json");
    }
}
