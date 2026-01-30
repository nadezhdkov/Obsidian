package io.obsidian.promise.api.internal.cancellation;

import io.obsidian.promise.api.CancellationSource;
import io.obsidian.promise.api.CancellationToken;

public class DefaultCancellationSource implements CancellationSource {

    private final DefaultCancellationToken token = new DefaultCancellationToken();

    @Override
    public CancellationToken token() {
        return token;
    }

    @Override
    public void cancel() {
        cancel("Cancelled");
    }

    @Override
    public void cancel(String reason) {
        token.cancel(reason);
    }

    @Override
    public boolean isCancelled() {
        return token.isCancelled();
    }
}