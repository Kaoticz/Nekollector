package com.github.kaoticz.nekollector.api.nekosia.services;

import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosia.models.NekosiaImage;
import com.github.kaoticz.nekollector.api.nekosia.models.NekosiaResponse;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.scene.image.Image;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * A service that queries the Nekosia API.
 */
public class NekosiaService implements ApiService {
    private final static String SERVICE_NAME = "Nekosia API";

    private final static UUID USER_ID = UUID.randomUUID();

    private final static HttpRequest REQUEST = HttpRequest.newBuilder()
            .uri(URI.create("https://api.nekosia.cat/api/v1/images/catgirl?session=id&id=" + USER_ID + "&count=48&rating=safe&blacklistedTags=swimwear,swimsuit,bikini,sea,swim-ring"))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

    private final ArrayDeque<NekosiaImage> cachedResults = new ArrayDeque<>();

    @Override
    public CompletableFuture<ApiResult> getImageAsync() {
        if (!cachedResults.isEmpty()) {
            var nekosiaImage = cachedResults.remove();
            return CompletableFuture.supplyAsync(() -> new ApiResult(SERVICE_NAME, new Image(nekosiaImage.getUrl())))
                    .orTimeout(15, TimeUnit.SECONDS)
                    .handle((apiResult, ex) -> {
                        if (ex == null) {
                            return apiResult;
                        }

                        cachedResults.add(nekosiaImage);
                        throw new CompletionException(ex);
                    });
        }

        return Statics.HTTP_CLIENT.sendAsync(REQUEST, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    try {
                        var json = httpResponse.body();
                        var nekosiaResponse = Statics.JSON_DESERIALIZER.readValue(json, NekosiaResponse.class);

                        if (!nekosiaResponse.isSuccess()) {
                            throw new IllegalStateException("Request to " + SERVICE_NAME + " has returned HTTP status " + nekosiaResponse.getHttpStatus());
                        }

                        cachedResults.addAll(Arrays.asList(nekosiaResponse.getImages()));

                        var nekosiaImage = cachedResults.remove();
                        return new ApiResult(SERVICE_NAME, new Image(nekosiaImage.getUrl()));
                    } catch (Exception ex) {
                        throw new CompletionException(ex);
                    }
                });
    }
}
