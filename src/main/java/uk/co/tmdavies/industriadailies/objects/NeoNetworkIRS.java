package uk.co.tmdavies.industriadailies.objects;

import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class NeoNetworkIRS {

    private final String apiKey;

    public NeoNetworkIRS(String apiKey) {
        this.apiKey = apiKey;
    }

    public void giveMoney(Player target, int amount, String ref) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String boundary = "----JavaFormBoundary" + UUID.randomUUID();

            String CRLF = "\r\n";

            String bodyBuilder = "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"apikey\"" + CRLF + CRLF +
                    this.apiKey + CRLF +
                    "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"to\"" + CRLF + CRLF +
                    target.getName() + CRLF +
                    "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"amount\"" + CRLF + CRLF +
                    amount + CRLF +
                    "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"reference\"" + CRLF + CRLF +
                    ref + CRLF +
                    "--" + boundary + "--" + CRLF;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://irs.neonetwork.xyz/api/send"))
                    .header("Cookie", "PHPSESSID=4ljck4dbt304a1mveingmj0kvd")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyBuilder, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            IndustriaDailies.LOGGER.info("Response code: " + response.statusCode());
            IndustriaDailies.LOGGER.info("Response body: " + response.body());

            target.sendSystemMessage(Utils.Chat("&a+£%s", amount));
            target.sendSystemMessage(Utils.Chat("&aRef: %s", ref));
        } catch (IOException | InterruptedException exception) {
            IndustriaDailies.LOGGER.error("Error sending HTTP request");
            exception.printStackTrace();
        }
    }

}
