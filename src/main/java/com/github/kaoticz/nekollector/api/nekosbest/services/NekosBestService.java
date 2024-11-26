package com.github.kaoticz.nekollector.api.nekosbest.services;

import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosbest.models.NekosBestImage;
import com.github.kaoticz.nekollector.api.nekosbest.models.NekosBestResponse;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.scene.image.Image;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * A service that queries the nekos.best API.
 */
public class NekosBestService implements ApiService {
    private final static String SERVICE_NAME = "nekos.best API";

    private final static HttpRequest REQUEST = HttpRequest.newBuilder()
            .uri(URI.create("https://nekos.best/api/v2/neko?category=neko&amount=50"))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

    private final ArrayDeque<NekosBestImage> cachedResults = new ArrayDeque<>();

    @Override
    public CompletableFuture<ApiResult> getImageAsync() {
        if (!cachedResults.isEmpty()) {
            var nekosImage = cachedResults.remove();
            return CompletableFuture.supplyAsync(() -> new ApiResult(SERVICE_NAME, new Image(nekosImage.getUrl())))
                    .orTimeout(15, TimeUnit.SECONDS)
                    .handle((apiResult, ex) -> {
                        if (ex == null) {
                            return apiResult;
                        }

                        cachedResults.add(nekosImage);
                        throw new CompletionException(ex);
                    });
        }

        return Statics.HTTP_CLIENT.sendAsync(REQUEST, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    try {
                        var json = httpResponse.body();
                        var nekosResponse = Statics.JSON_DESERIALIZER.readValue(json, NekosBestResponse.class);
                        cachedResults.addAll(Arrays.asList(nekosResponse.getImages()));

                        var nekosImage = cachedResults.remove();
                        return new ApiResult(SERVICE_NAME, new Image(nekosImage.getUrl()));
                    } catch (Exception ex) {
                        throw new CompletionException(ex);
                    }
                });
    }
}