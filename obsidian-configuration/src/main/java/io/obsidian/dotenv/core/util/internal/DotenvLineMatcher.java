/*
 * Copyright 2026 Rick M. Viana
 *
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

package io.obsidian.dotenv.core.util.internal;

import io.obsidian.dotenv.core.DotenvEntry;
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
