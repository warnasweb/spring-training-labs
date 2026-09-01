package com.warnasweb.training.security.form;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc
class FormLoginSecurityTest {
  @Autowired MockMvc mvc;
  @Test void anonymousBrowserIsRedirectedToLogin() throws Exception { mvc.perform(get("/books")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login")); }
  @Test void loggedInUserCanOpenBooks() throws Exception { mvc.perform(get("/books").with(user("reader").roles("READER"))).andExpect(status().isOk()).andExpect(view().name("books")); }
}
