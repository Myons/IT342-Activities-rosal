package com.rosal.oauth2login.controller;

import com.rosal.oauth2login.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    @Autowired
    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        Object contacts = googleContactsService.getContacts(authentication);
        model.addAttribute("contacts", contacts);
        
        return "contacts";
    }
}