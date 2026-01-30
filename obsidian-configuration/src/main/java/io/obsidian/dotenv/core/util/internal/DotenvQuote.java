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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@ApiStatus.Internal
public class DotenvQuote {

    private static final String  QUOTE         = "\"";
    private static final Pattern QUOTE_PATTERN = Pattern.compile(QUOTE);

    private DotenvQuote() {}

    public static boolean isMultilineStart(String val) {
        return isQuotedStart(val) && !endsWithQuote(val);
    }

    public static boolean hasUnbalancedQuotes(String val) {
        return isQuotedStart(val) && !isValid(val);
    }

    public static String stripQuotes(String input) {
        if (input == null) return null;
        var trimmed = input.trim();
        return isQuoted(trimmed) ? trimmed.substring(1, trimmed.length() - 1) : trimmed;
    }

    private static boolean isQuoted(String s) {
        return s != null && s.length() > 1 && s.startsWith(QUOTE) && s.endsWith(QUOTE);
    }

    private static boolean isQuotedStart(String s) {
        return s != null && s.trim().startsWith(QUOTE);
    }

    private static boolean endsWithQuote(String s) {
        return s != null && s.endsWith(QUOTE);
    }

    private static boolean isValid(String input) {
        if (input == null) return false;
        var trimmed = input.trim();

        if (!trimmed.startsWith(QUOTE) && !trimmed.endsWith(QUOTE)) return true;
        if (trimmed.length() == 1 || !trimmed.startsWith(QUOTE) || !trimmed.endsWith(QUOTE)) return false;

        return !hasUnescapedQuote(trimmed);
    }

    private static boolean hasUnescapedQuote(@NotNull String val) {
        var content = val.substring(1, val.length() - 1);
        var matcher = QUOTE_PATTERN.matcher(content);

        while (matcher.find()) {
            if (matcher.start() == 0 || content.charAt(matcher.start() - 1) != '\\') {
                return true;
            }
        }
        return false;
    }
}
