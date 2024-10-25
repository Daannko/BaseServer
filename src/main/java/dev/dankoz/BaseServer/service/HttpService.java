package dev.dankoz.BaseServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class HttpService {

    private final RestClient restClient;

    public HttpService() {
        this.restClient = RestClient.builder()
                .defaultHeader("Content-Type","application/json")
                .build();
    }

    public JsonNode post(String url,String body) throws JsonProcessingException {
        String response = restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .body(String.class);
        return new ObjectMapper().reader().readTree(response);
    }
    public JsonNode get(String url) {
        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        try {
            return new ObjectMapper().reader().readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
