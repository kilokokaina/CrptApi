package org.ismp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class CrptApi {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Document {
        private String participantInn;
        private String doc_id;
        private String doc_status;
        private final String doc_type = "LP_INTRODUCE_GOODS";
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date = new Date();
        private String production_type;
        private Product[] products;
        private Date reg_date = new Date();
        private String reg_number;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        static class Product {
            private String certificate_document;
            private Date certificate_document_date = new Date();
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private Date production_date = new Date();
            private String tnved_code;
            private String uit_code;
            private String uitu_code;
        }
    }

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final int requestLimit;
    private boolean isRequestAllowed = true;

    public CrptApi(long timeRange, int requestLimit) {
        this.requestLimit = requestLimit;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            this.requestCount.set(0);
            this.isRequestAllowed = this.requestCount.get() <= this.requestLimit;
        }, 0, timeRange, TimeUnit.MILLISECONDS);
    }

    public void makeRequest(Document document, String signature) throws IOException {
        System.out.println(Thread.currentThread().getName() + "-" + this.isRequestAllowed + ": " + this.requestCount.get());

        if (isRequestAllowed && this.requestCount.get() < this.requestLimit) {
            this.requestCount.getAndIncrement();

            ObjectMapper mapper = new ObjectMapper();
            StringEntity entity = new StringEntity(mapper.writeValueAsString(document));

            HttpPost httpPost = new HttpPost(signature);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(entity);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(httpPost);
            }
        } else this.isRequestAllowed = false;
    }

}

public class Main {

    public static void main(String[] args) throws IOException {
        CrptApi crptApi = new CrptApi(6000L, 5);
        crptApi.makeRequest(new CrptApi.Document(), "https://ismp.crpt.ru/api/v3/1k/documents/create");

//        Вызов метода из десяти потоков для проверки

//        CrptApi.Document document = new CrptApi.Document();
//        CrptApi.Document.Product product1 = new CrptApi.Document.Product();
//        CrptApi.Document.Product product2 = new CrptApi.Document.Product();
//        CrptApi.Document.Product product3 = new CrptApi.Document.Product();
//
//        document.setParticipantInn("test_pInn");
//        document.setDoc_id("1234");
//        document.setDoc_status("test_status");
//        document.setOwner_inn("test_owner");
//        document.setProducer_inn("test_prod");
//
//        product1.setProducer_inn("test_product_Inn1");
//        product1.setOwner_inn("test_owner1");
//
//        product2.setProducer_inn("test_product_Inn1");
//        product2.setOwner_inn("test_owner1");
//
//        product3.setProducer_inn("test_product_Inn1");
//        product3.setOwner_inn("test_owner1");
//
//        document.setProducts(new CrptApi.Document.Product[]{ product1, product2, product3 });
//
//        Thread[] threads = new Thread[10];
//        for (int i = 0; i < threads.length; i++) {
//            threads[i] = new Thread(() -> {
//                while (true) {
//                    try {
//                        crptApi.makeRequest(document, "http:/localhost:8080/route");
//                        Thread.sleep(1000);
//                    } catch (InterruptedException | IOException e) {
//                        throw new RuntimeException(e.getMessage());
//                    }
//                }
//            }, ("Thread-" + i));
//            threads[i].start();
//        }

    }

}