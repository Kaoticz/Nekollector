package com.github.kaoticz.nekollector.api.nekosia.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.nekosia.models.NekosiaResponse;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.scene.image.Image;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NekosiaService implements ApiService {
    private final HttpRequest REQUEST = HttpRequest.newBuilder()
            .uri(URI.create("https://api.nekosia.cat/api/v1/images/catgirl?rating=safe&blacklistedTags=swimwear,swimsuit,bikini,sea,swim-ring"))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

    @Override
    public String getServiceName() {
        return "Nekosia";
    }

    @Override
    public CompletableFuture<Image> getImageAsync() {
        return Statics.HTTP_CLIENT.sendAsync(REQUEST, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    try {
                        var json = httpResponse.body();
                        var nekosiaResponse = Statics.JSON_DESERIALIZER.readValue(json, NekosiaResponse.class);

                        return new Image(nekosiaResponse.getUrl());
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                });
    }
}
