package com.github.kaoticz.nekollector.api.nekosia.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kaoticz.nekollector.api.nekosia.models.NekosiaResponse;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.scene.image.Image;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class NekosiaService {
    public CompletableFuture<Image> getRandomCatgirlAsync() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.nekosia.cat/api/v1/images/catgirl?rating=safe&blacklistedTags=swimwear,swimsuit,bikini,sea,swim-ring"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return Statics.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    try {
                        var json = httpResponse.body();
                        var nekosiaResponse = Statics.JSON_DESERIALIZER.readValue(json, NekosiaResponse.class);
                        return new Image(nekosiaResponse.getUrl());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
