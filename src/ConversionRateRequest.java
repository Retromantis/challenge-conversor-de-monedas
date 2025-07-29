import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConversionRateRequest {
    private static final String API_KEY = "6e5cc6a4a4a805359e2dcfb7";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private URL buildURL(String baseCurrency) throws MalformedURLException {
        return new URL(API_URL + baseCurrency);
    }

    private URI buildURI(String baseCurrency) throws MalformedURLException {
        return URI.create(API_URL + baseCurrency);
    }

    private String httpRequest(URI uri) throws IOException {
        final String[] result = {null};

        Thread thread = Thread.startVirtualThread(() -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
               .uri(uri)
               .GET()
               .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    result[0] = response.body();
                } else {
                    result[0] = "{\"error\": \"HTTP " + response.statusCode() + "\"}";
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Esperamos a que el virtual thread termine
        try {
            thread.join();
        } catch (InterruptedException e) {
            return "{\"error\": \"Thread interrupted\"}";
        }

        return result[0];
    }

    private ConversionRate parseJsonResponse(String jsonResponse, String targetCurrency) throws CurrencyNotFoundException {
       // System.out.println("Respuesta JSON completa: " + jsonRespuesta); // Imprime la respuesta completa
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonResponse).getAsJsonObject();
           // System.out.println("Objeto JSON parseado: " + jsonObject); // Imprime el objeto JSON

            JsonObject rates = jsonObject.getAsJsonObject("conversion_rates");
            //System.out.println("Objeto 'rates': " + rates); // Imprime el objeto 'rates'

            if (rates != null && rates.has(targetCurrency)) {
                double exchangeRate = rates.get(targetCurrency).getAsDouble();
                return new ConversionRate(targetCurrency, exchangeRate);
            } else {
                throw new CurrencyNotFoundException("El tipo de moneda '" + targetCurrency + "' no fue encontrado en la respuesta.");
            }
        } catch (Exception e) {
            throw new CurrencyNotFoundException("Error al parsear la respuesta de la API: " + e.getMessage(), e);
        }
    }

    public ConversionRate getConversionRate(String baseCurrency, String targetCurrency) throws IOException, CurrencyNotFoundException {
        try {
            return parseJsonResponse(httpRequest(buildURI(baseCurrency)), targetCurrency);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al construir la URL", e);
        }
    }
}