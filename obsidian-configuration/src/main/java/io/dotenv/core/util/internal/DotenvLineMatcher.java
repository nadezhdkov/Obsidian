package io.dotenv.core.util.internal;

import io.dotenv.core.DotenvEntry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class DotenvLineMatcher {

    private static final Pattern PATTERN = Pattern.compile(
            "^\\s*([\\w.\\-]+)\\s*(=)\\s*(['][^']*[']|[\"][^\"]*[\"]|[^#]*)?\\s*(#.*)?$"
    );

    public Optional<DotenvEntry> match(String line) {
        var matcher = PATTERN.matcher(line);

        if (!matcher.matches() || matcher.groupCount() < 3) {
            return Optional.empty();
        }

        return Optional.of(new DotenvEntry(
                matcher.group(1),
                matcher.group(3)
        ));
    }
}
