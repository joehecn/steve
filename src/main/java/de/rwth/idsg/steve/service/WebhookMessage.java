/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2023 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.service;

import com.google.gson.Gson;
import de.rwth.idsg.steve.utils.PropertiesFileLoader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ocpp.cs._2015._10.MeterValue;
import org.joda.time.DateTime;

/**
 *
 * @author hemiao
 */
@Getter  
@NoArgsConstructor
public class WebhookMessage {
    private String type;
    private String chargeBoxId;
    private String state;
    private Long time;
    private int connectorId;
    private int transactionId;
    private String meterValue;
    private List<MeterValue> list;
    private int connectorPk;
    private DateTime statusTimestamp;
    private String status;
    private String errorCode;
    private String errorInfo;
    private String vendorId;
    private String vendorErrorCode;
    
    public WebhookMessage(String type, Long time, int connectorPk, DateTime statusTimestamp, String status, String errorCode, String errorInfo, String vendorId, String vendorErrorCode) {
        this.type = type;
        this.time = time;
        this.connectorPk = connectorPk;
        this.statusTimestamp = statusTimestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.errorInfo = errorInfo;
        this.vendorId = vendorId;
        this.vendorErrorCode = vendorErrorCode;
    }
    
    public WebhookMessage(String type, String chargeBoxId, String state, Long time) {
        this.type = type;
        this.chargeBoxId = chargeBoxId;
        this.state = state;
        this.time = time;
        
        this.connectorId = -1;
        this.transactionId = -1;
    }
    
    public WebhookMessage(String type, String chargeBoxId, String state, Long time, int connectorId, int transactionId, String meterValue) {
        this.type = type;
        this.chargeBoxId = chargeBoxId;
        this.state = state;
        this.time = time;
        this.connectorId = connectorId;
        this.transactionId = transactionId;
        this.meterValue = meterValue;
    }
    
    public WebhookMessage(String type, String chargeBoxId, String state, Long time, int transactionId, String meterValue) {
        this.type = type;
        this.chargeBoxId = chargeBoxId;
        this.state = state;
        this.time = time;
        this.connectorId = -1;
        this.transactionId = transactionId;
        this.meterValue = meterValue;
    }
    
    public WebhookMessage(String type, Long time, List<MeterValue> list) {
        this.type = type;
        this.time = time;
        this.connectorId = -1;
        this.transactionId = -1;
        this.list = list;
    }
    
    public static void sendMessage(WebhookMessage message) {
        try {
            PropertiesFileLoader p = new PropertiesFileLoader("main.properties");
            String serviceUrl = p.getString("webhook");
            
            Gson gson = new Gson();
            String json = gson.toJson(message);

            System.out.println(json);
            
            String sig = getSignature(json, message.time);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("t", String.valueOf(message.time))
                .header("signature", sig)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private static final String STEVE_KEY = "BwfyIXJVQ2agadsb5zkSjr#abdsETy5f27QIhC6b";
    
    public static String getSignature(String body, long t) throws Exception {

        String text = body + t;

        Mac sha1Hmac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKey = new SecretKeySpec(STEVE_KEY.getBytes(), "HmacSHA1");
        sha1Hmac.init(secretKey);

        byte[] hash = sha1Hmac.doFinal(text.getBytes());

        return hex(hash);
    }
    
    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
