package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class SoapAuthValidator {

    @Value("${soap.validate.url}")
    private String soapValidateUrl;

    public boolean validateToken(String token) {
        try {
            String safeToken = token
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");

            String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                    + "xmlns:auth=\"http://example.com/auth\">"
                    + "<soapenv:Body>"
                    + "<auth:validateTokenRequest>"
                    + "<auth:token>" + safeToken + "</auth:token>"
                    + "</auth:validateTokenRequest>"
                    + "</soapenv:Body>"
                    + "</soapenv:Envelope>";

            HttpURLConnection conn = (HttpURLConnection) new URL(soapValidateUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(xml.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                return resp.contains("<success>true</success>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}