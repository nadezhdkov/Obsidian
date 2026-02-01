package obsidian.json.util;

import obsidian.json.api.Json;
import obsidian.json.api.JsonConfig;
import obsidian.json.api.JsonMapper;
import obsidian.json.api.JsonElement;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public final class JsonSerializer {

    public static String toJson(
            Object obj,
            boolean prettyPrint,
            boolean serializeNulls,
            boolean lenient,
            boolean failOnUnknownFields,
            boolean annotationsEnabled,
            boolean htmlEscaping,
            String dateFormat,
            JsonConfig.AnnotationsMode annotationsMode)
    {
        Objects.requireNonNull(obj, "Object cannot be null.");

        JsonMapper mapper = Json.configure()
                .prettyPrint(prettyPrint)
                .serializeNulls(serializeNulls)
                .lenient(lenient)
                .failOnUnknownFields(failOnUnknownFields)
                .enableAnnotations(annotationsEnabled)
                .htmlEscaping(htmlEscaping)
                .dateFormat(dateFormat)
                .annotationsMode(annotationsMode)
                .buildMapper();

        JsonElement encoded = mapper.encode(obj);
        return mapper.stringify(encoded);
    }

}
