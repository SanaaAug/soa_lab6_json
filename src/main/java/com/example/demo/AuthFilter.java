package com.example.demo;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthFilter implements Filter {

    @Value("${soap.service.url}")
    private String soapUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // OPTIONS хүсэлтийг алгасах (CORS)
        if ("OPTIONS".equals(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Authorization header авах
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(httpResponse, "Token байхгүй байна!");
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Received token: " + token); // DEBUG

        // SOAP service руу token шалгах
        ValidationResult result = validateToken(token);

        if (!result.valid) {
            sendError(httpResponse, "Token хүчингүй байна!");
            return;
        }

        System.out.println("Token valid! userId: " + result.userId); // DEBUG

        // userId-г request-д хадгалах
        httpRequest.setAttribute("userId", result.userId);
        httpRequest.setAttribute("username", result.username);

        chain.doFilter(request, response);
    }

    private ValidationResult validateToken(String token) {
        try {
            String soapRequest = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:auth="http://example.com/auth">
                    <soapenv:Body>
                        <auth:validateTokenRequest>
                            <auth:token>%s</auth:token>
                        </auth:validateTokenRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(token);

            URL url = new URL(soapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(soapRequest.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            String xml = responseBuilder.toString();
            System.out.println("SOAP Response: " + xml); // DEBUG

            // >true< эсвэл <valid>true шалгах
            boolean valid = xml.contains(">true<") || xml.contains("<valid>true");
            Long userId = null;
            String username = null;

            if (valid) {
                // userId олох - ns2, ns3, эсвэл namespace-гүй
                userId = extractValue(xml, "userId");
                username = extractString(xml, "username");
            }

            return new ValidationResult(valid, userId, username);

        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            e.printStackTrace();
            return new ValidationResult(false, null, null);
        }
    }

    private Long extractValue(String xml, String tag) {
        // Бүх боломжит format-г шалгах
        String[] patterns = {
            "<" + tag + ">", 
            "<ns2:" + tag + ">", 
            "<ns3:" + tag + ">"
        };
        String[] endPatterns = {
            "</" + tag + ">", 
            "</ns2:" + tag + ">", 
            "</ns3:" + tag + ">"
        };
        
        for (int i = 0; i < patterns.length; i++) {
            int start = xml.indexOf(patterns[i]);
            int end = xml.indexOf(endPatterns[i]);
            if (start != -1 && end != -1) {
                try {
                    return Long.parseLong(xml.substring(start + patterns[i].length(), end));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }

    private String extractString(String xml, String tag) {
        String[] patterns = {
            "<" + tag + ">", 
            "<ns2:" + tag + ">", 
            "<ns3:" + tag + ">"
        };
        String[] endPatterns = {
            "</" + tag + ">", 
            "</ns2:" + tag + ">", 
            "</ns3:" + tag + ">"
        };
        
        for (int i = 0; i < patterns.length; i++) {
            int start = xml.indexOf(patterns[i]);
            int end = xml.indexOf(endPatterns[i]);
            if (start != -1 && end != -1) {
                return xml.substring(start + patterns[i].length(), end);
            }
        }
        return null;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"" + message + "\"}");
    }

    private static class ValidationResult {
        boolean valid;
        Long userId;
        String username;

        ValidationResult(boolean valid, Long userId, String username) {
            this.valid = valid;
            this.userId = userId;
            this.username = username;
        }
    }
}