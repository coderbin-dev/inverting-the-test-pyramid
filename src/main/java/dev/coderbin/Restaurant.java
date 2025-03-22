package dev.coderbin;

import org.json.JSONObject;

import java.util.UUID;

public record Restaurant(UUID id, String name, String description) implements Jsonable {

    public static Restaurant fromJSON(JSONObject jsonObject) {
        String name = jsonObject.optString("name");
        if (name.isBlank()) throw new IllegalArgumentException("Restaurant name cannot be blank.");
        return new Restaurant(UUID.fromString(jsonObject.optString("id")),
                name, jsonObject.optString("description"));
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("name", name)
                .put("description", description);
    }
}
