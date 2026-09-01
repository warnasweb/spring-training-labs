package com.warnasweb.training.security.basic;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc
class BasicAuthSecurityTest {
  @Autowired MockMvc mvc;
  @Test void anonymousReadIs401() throws Exception { mvc.perform(get("/api/books")).andExpect(status().isUnauthorized()); }
  @Test void readerCanRead() throws Exception { mvc.perform(get("/api/books").with(httpBasic("reader", "reader-pass"))).andExpect(status().isOk()); }
  @Test void readerCannotWrite() throws Exception { mvc.perform(post("/api/books").with(httpBasic("reader", "reader-pass")).contentType(MediaType.APPLICATION_JSON).content("{\"id\":2,\"title\":\"DDD\"}")).andExpect(status().isForbidden()); }
  @Test void adminCanWrite() throws Exception { mvc.perform(post("/api/books").with(httpBasic("admin", "admin-pass")).contentType(MediaType.APPLICATION_JSON).content("{\"id\":2,\"title\":\"DDD\"}")).andExpect(status().isOk()); }
}
