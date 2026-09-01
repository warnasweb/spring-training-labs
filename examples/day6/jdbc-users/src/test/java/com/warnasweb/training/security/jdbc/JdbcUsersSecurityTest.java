package com.warnasweb.training.security.jdbc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc
class JdbcUsersSecurityTest {
  @Autowired MockMvc mvc;
  @Test void databaseUserCanAuthenticate() throws Exception { mvc.perform(get("/api/books").with(httpBasic("reader", "reader-pass"))).andExpect(status().isOk()); }
  @Test void wrongPasswordIs401() throws Exception { mvc.perform(get("/api/books").with(httpBasic("reader", "wrong"))).andExpect(status().isUnauthorized()); }
}
