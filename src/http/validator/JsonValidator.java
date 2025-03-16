package http.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public final class JsonValidator {

    private JsonValidator() {

    }

    public static boolean isJsonValid(JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            System.out.println("Тело запроса не является объектом JSON");
            return false;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Set<String> requiredFields = new HashSet<>();
        requiredFields.add("name");
        requiredFields.add("description");
        requiredFields.add("status");
        requiredFields.add("startTime");
        requiredFields.add("duration");

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