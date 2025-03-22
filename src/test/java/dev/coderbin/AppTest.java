package dev.coderbin;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

class AppTest {

    private static final EmbeddedPostgres pg;
    private static final App app;
    private static final HttpClient client;

    static {
        try {
            pg = EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        app = App.start(pg.getPostgresDatabase());
        client = HttpClient.newHttpClient();
    }

    @Test
    public void canPostANewRestaurantAndQueryItBack() throws Exception {

        var restaurant = new Restaurant(UUID.randomUUID(), "Wok and Rolls",
                "All the best rolls");

        var resp = createRestaurant(restaurant.toJSON().toString());
        assertThat(resp.statusCode(), equalTo(201));
        assertThat(Restaurant.fromJSON(new JSONObject(resp.body())), equalTo(restaurant));

        var lookupResp = send(request(app.uri().resolve("/api/v1/restaurants/" + restaurant.id()))
                .header("accept", "application/json")
        );
        assertThat(lookupResp.statusCode(), equalTo(200));
        assertThat(Restaurant.fromJSON(new JSONObject(lookupResp.body())), equalTo(restaurant));


    }

    @Test
    public void a400IsReturnedIfThereIsNoNameWhenCreatingARestaurant() throws Exception {
        var restaurant = new Restaurant(UUID.randomUUID(), "Wok and Rolls",
                "All the best rolls");
        var requestBody = restaurant.toJSON();
        requestBody.remove("name");

        var resp = createRestaurant(requestBody.toString());
        assertThat(resp.statusCode(), equalTo(400));
        assertThat(resp.body(), containsString("Restaurant name cannot be blank."));
    }

    @Test
    public void a400IsReturnedIfTheJSONIsInvalid() throws Exception {
        var resp = createRestaurant("{ invalid json");
        assertThat(resp.statusCode(), equalTo(400));
        assertThat(resp.body(), containsString("Invalid JSON format."));
    }

    private static HttpResponse<String> createRestaurant(String body) throws IOException, InterruptedException {
        return send(request(app.uri().resolve("/api/v1/restaurants"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
        );
    }


    private static HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest.Builder request(URI uri) {
        return HttpRequest.newBuilder(uri);
    }


    @AfterAll
    public static void tearDown() throws Exception {
        client.close();
        app.close();
        pg.close();
    }

}
























