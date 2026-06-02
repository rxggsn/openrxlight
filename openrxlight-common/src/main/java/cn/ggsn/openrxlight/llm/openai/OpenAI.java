package cn.ggsn.openrxlight.llm.openai;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.ggsn.openrxlight.llm.openai.request.ChatCompletionRequest;
import cn.ggsn.openrxlight.llm.openai.response.ChoiceChunk;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OpenAI {
    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient httpClient;

    public OpenAI(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient()
                .newBuilder()
                .callTimeout(java.time.Duration.ofSeconds(5))
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .readTimeout(java.time.Duration.ofMinutes(15))
                .writeTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    public Stream<ChoiceChunk> chatCompletion(ChatCompletionRequest requestBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String url = baseUrl + "/v1/chat/completions";
        // Ensure stream=true for SSE
        requestBody.setStream(true);
        String json = mapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "text/event-stream")
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Unexpected code " + response);
        }

        // SSE: Each line starting with "data: " is a JSON object, except "data: [DONE]"
        java.io.BufferedReader reader = new java.io.BufferedReader(response.body().charStream());
        AtomicBoolean done = new AtomicBoolean(false);

        Iterable<ChoiceChunk> iterable = () -> new java.util.Iterator<>() {
            String nextLine = null;
            ChoiceChunk nextObj = null;

            @Override
            public boolean hasNext() {
                if (done.get())
                    return false;
                try {
                    while ((nextLine = reader.readLine()) != null) {
                        nextLine = nextLine.trim();
                        if (nextLine.isEmpty())
                            continue;
                        if (nextLine.startsWith("data: ")) {
                            String data = nextLine.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                done.set(true);
                                response.close();
                                return false;
                            }
                            nextObj = mapper.readValue(data, ChoiceChunk.class);
                            return true;
                        }
                    }
                } catch (IOException e) {
                    done.set(true);
                    response.close();
                    throw new RuntimeException(e);
                }
                done.set(true);
                response.close();
                return false;
            }

            @Override
            public ChoiceChunk next() {
                if (nextObj != null || hasNext()) {
                    ChoiceChunk obj = nextObj;
                    nextObj = null;
                    return obj;
                }
                throw new java.util.NoSuchElementException();
            }
        };

        return StreamSupport.stream(iterable.spliterator(), false)
                .onClose(() -> {
                    done.set(true);
                    response.close();
                });
    }
}