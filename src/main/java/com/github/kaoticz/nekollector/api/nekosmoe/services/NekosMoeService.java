package com.github.kaoticz.nekollector.api.nekosmoe.services;

import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosmoe.models.NekosMoeImage;
import com.github.kaoticz.nekollector.api.nekosmoe.models.NekosMoeResponse;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.scene.image.Image;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * A service that queries the nekos.moe API.
 */
public class NekosMoeService implements ApiService {
    private final static String SERVICE_NAME = "nekos.moe API";

    private final static Set<String> BLACKLISTED_TAGS = Set.of(
            "all fours",
            "arched back",
            "ass",
            "bare legs",
            "backless outfit",
            "breasts",
            "cleavage",
            "collar",
            "dog collar",
            "cowboy shot",
            "covered nipples",
            "dog girl",
            "dog ears",
            "legs up",
            "fox girl",
            "fox ears",
            "from behind",
            "garter straps",
            "knees up",
            "large breasts",
            "midriff",
            "miniskirt",
            "naked overalls",
            "mouse ears",
            "mouse girl",
            "open clothes",
            "off-shoulder shirt",
            "panties",
            "side-tie panties",
            "sideboob",
            "short shorts",
            "strap slip",
            "striped",
            "swimsuit",
            "tail censor",
            "underboob",
            "underwear",
            "waist apron",
            "wolf ears",
            "wolf girl"
    );

    private final static HttpRequest REQUEST = HttpRequest.newBuilder()
            .uri(URI.create("https://nekos.moe/api/v1/random/image?count=100&nsfw=false"))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

    private final ArrayDeque<NekosMoeImage> cachedResults = new ArrayDeque<>();

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
                        var nekosResponse = Statics.JSON_DESERIALIZER.readValue(json, NekosMoeResponse.class);
                        var validImages = nekosResponse.getImages()
                                .stream()
                                .filter(image -> image.getTags().stream().noneMatch(BLACKLISTED_TAGS::contains))
                                .toList();

                        cachedResults.addAll(validImages);

                        var nekosImage = cachedResults.remove();
                        return new ApiResult(SERVICE_NAME, new Image(nekosImage.getUrl()));
                    } catch (Exception ex) {
                        throw new CompletionException(ex);
                    }
                });
    }
}