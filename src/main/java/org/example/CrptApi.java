package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private Date timeStamp = new Date();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    @SneakyThrows
    public void createDocument(Document document, String signature) {
        synchronized (this){
            Date timeEntry = new Date();
            long interval = timeStamp.getTime() - timeEntry.getTime();
            if (interval >= timeUnit.toMillis(1)) {
                timeStamp = new Date();
                requestCount.set(0);
            }
            if (requestCount.get() >= requestLimit) {
                Thread.sleep(timeUnit.toMillis(1) - interval);
                timeStamp = new Date();
                requestCount.set(0);
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonDocument = mapper.writeValueAsString(document);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            requestCount.incrementAndGet();
            System.out.println("Document id = " + document.getDoc_id() + " send, response status - " + response.statusCode());
        }

    }

    @Getter
    @AllArgsConstructor
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private ArrayList<Product> products;
        private String reg_date;
        private String reg_number;

        @Getter
        @AllArgsConstructor
        public static class Product {
            private String certificate_document;
            private String certificate_document_date;
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private String production_date;
            private String tnved_code;
            private String uit_code;
            private String uitu_code;
        }
        @Getter
        @AllArgsConstructor
        public static class Description {
            private String participantInn;
        }

    }
}
