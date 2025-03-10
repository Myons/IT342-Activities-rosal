package com.rosal.oauth2login.service;

import com.rosal.oauth2login.model.ContactRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = WebClient.builder().build();
    }

    private String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
        return client.getAccessToken().getTokenValue();
    }

    public Object getContacts(OAuth2AuthenticationToken authentication) {
    try {
        String accessToken = getAccessToken(authentication);
        
        // Log that we got the access token (don't log the token itself)
        System.out.println("Successfully retrieved access token");
        
        // Google People API endpoint for contacts
        String url = "https://people.googleapis.com/v1/people/me/connections" +
                "?personFields=names,emailAddresses,phoneNumbers,metadata" +
                "&pageSize=100";
        
        System.out.println("Calling Google People API: " + url);
        
        Object response = webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        
        // Log that we received a response
        System.out.println("Received response from Google People API");
        System.out.println("Response type: " + (response != null ? response.getClass().getName() : "null"));
        
        return response;
        } catch (Exception e) {
            // Log the full exception stack trace
            System.err.println("Error in getContacts: " + e.getMessage());
            e.printStackTrace();
            
            // Return an empty map that matches the expected structure
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("connections", new ArrayList<>());
            return emptyResult;
        }
    }
    
    public Object getContact(OAuth2AuthenticationToken authentication, String resourceName) {
        String accessToken = getAccessToken(authentication);
        
        String url = "https://people.googleapis.com/v1/" + resourceName +
                "?personFields=names,emailAddresses,phoneNumbers";
                
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
    
    public Object createContact(OAuth2AuthenticationToken authentication, ContactRequest contactRequest) {
        String accessToken = getAccessToken(authentication);
        
        Map<String, Object> requestBody = buildContactPayload(contactRequest);
        
        return webClient.post()
                .uri("https://people.googleapis.com/v1/people:createContact")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
    
    /*
    public Object updateContact(OAuth2AuthenticationToken authentication, 
                              String resourceName, 
                              ContactRequest contactRequest) {
        String accessToken = getAccessToken(authentication);
        
        Map<String, Object> requestBody = buildContactPayload(contactRequest);
        
        String updateMask = "names,emailAddresses";
        if (contactRequest.getPhoneNumber() != null && !contactRequest.getPhoneNumber().isEmpty()) {
            updateMask += ",phoneNumbers";
        }
        
        String url = "https://people.googleapis.com/v1/" + resourceName + 
                "?updatePersonFields=" + updateMask;
                
        return webClient.patch()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
    */
    
    /*
    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceName) {
        String accessToken = getAccessToken(authentication);
        
        String url = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";
        
        webClient.delete()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    */

    private Map<String, Object> buildContactPayload(ContactRequest contactRequest) {
        Map<String, Object> contactPerson = new HashMap<>();
        
        // Add name
        Map<String, Object> name = new HashMap<>();
        name.put("givenName", contactRequest.getFirstName());
        name.put("familyName", contactRequest.getLastName());
        
        contactPerson.put("names", new Object[]{name});
        
        // Add email if provided
        if (contactRequest.getEmail() != null && !contactRequest.getEmail().isEmpty()) {
            Map<String, Object> email = new HashMap<>();
            email.put("value", contactRequest.getEmail());
            email.put("type", "home");
            
            contactPerson.put("emailAddresses", new Object[]{email});
        }
        
        // Add phone number if provided
        if (contactRequest.getPhoneNumber() != null && !contactRequest.getPhoneNumber().isEmpty()) {
            Map<String, Object> phone = new HashMap<>();
            phone.put("value", contactRequest.getPhoneNumber());
            phone.put("type", "mobile");
            
            contactPerson.put("phoneNumbers", new Object[]{phone});
        }
        
        return contactPerson;
    }
}