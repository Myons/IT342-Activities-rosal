package com.rosal.oauth2login.controller;

import com.rosal.oauth2login.model.ContactRequest;
import com.rosal.oauth2login.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    @Autowired
    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    //Get contacts list
    @GetMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        Object contacts = googleContactsService.getContacts(authentication);
        model.addAttribute("contacts", contacts);
        model.addAttribute("contactRequest", new ContactRequest());
        
        return "contacts";
    }
    
    @GetMapping("/contacts/{resourceName}")
    public String getContact(OAuth2AuthenticationToken authentication, 
                           @PathVariable String resourceName, 
                           Model model) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        Object contact = googleContactsService.getContact(authentication, resourceName);
        model.addAttribute("contact", contact);
        model.addAttribute("contactRequest", new ContactRequest());
        
        return "edit-contact";
    }
    
    //Add contacts
    @PostMapping("/contacts/create")
    public String createContact(OAuth2AuthenticationToken authentication,
                              @ModelAttribute ContactRequest contactRequest,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        Object result = googleContactsService.createContact(authentication, contactRequest);
        redirectAttributes.addFlashAttribute("message", "Contact created successfully");
        
        return "redirect:/contacts";
    }

    /* 
    //Edit contact
    @PostMapping("/contacts/{resourceName}/update")
    public String updateContact(OAuth2AuthenticationToken authentication,
                              @PathVariable String resourceName,
                              @ModelAttribute ContactRequest contactRequest,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        Object result = googleContactsService.updateContact(authentication, resourceName, contactRequest);
        redirectAttributes.addFlashAttribute("message", "Contact updated successfully");
        
        return "redirect:/contacts";
    }
    
    //Delete contact
    @DeleteMapping("/contacts/{resourceName}")
    public String deleteContact(OAuth2AuthenticationToken authentication,
                              @PathVariable String resourceName,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/";
        }
        
        googleContactsService.deleteContact(authentication, resourceName);
        redirectAttributes.addFlashAttribute("message", "Contact deleted successfully");
        
        return "redirect:/contacts";
    }
    */
}