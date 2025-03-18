package http.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public final class JsonValidator {

    private JsonValidator() {

    }

    public static boolean isTaskJsonValid(JsonElement jsonElement) {
        Set<String> requiredFields = new HashSet<>();
        requiredFields.add("name");
        requiredFields.add("description");
        requiredFields.add("status");
        requiredFields.add("startTime");
        requiredFields.add("duration");

        return isJsonValid(jsonElement, requiredFields);
    }

    public static boolean isSubTaskJsonValid(JsonElement jsonElement) {
        Set<String> requiredFields = new HashSet<>();
        requiredFields.add("name");
        requiredFields.add("description");
        requiredFields.add("status");
        requiredFields.add("epicId");
        requiredFields.add("startTime");
        requiredFields.add("duration");

        return isJsonValid(jsonElement, requiredFields);
    }

    public static boolean isEpicJsonValid(JsonElement jsonElement) {
        Set<String> requiredFields = new HashSet<>();
        requiredFields.add("name");
        requiredFields.add("description");

        return isJsonValid(jsonElement, requiredFields);
    }

    private static boolean isJsonValid(JsonElement jsonElement, Set<String> requiredFields) {
        if (!jsonElement.isJsonObject()) {
            System.out.println("Тело запроса не является объектом JSON");
            return false;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        for (String field : requiredFields) {
            if (!jsonObject.has(field)) {
                System.out.println("Отсутствует обязательное поле: " + field);
                return false;
            }
            if (jsonObject.get(field).isJsonNull()) {
                System.out.println("Обязательное поле '" + field + "' имеет значение null");
                return false;
            }
        }

        System.out.println("JSON корректен: все обязательные поля присутствуют.");
        return true;
    }
}