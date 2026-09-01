package com.warnasweb.training.security.method;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
class MethodSecurityTest {
  @Autowired BookService service;
  @Test @WithMockUser(username="reader", roles="READER") void readerCanRead() { service.findAll(); }
  @Test @WithMockUser(username="reader", roles="READER") void readerCannotCreate() { assertThatThrownBy(() -> service.create(new Book(2, "DDD"))).isInstanceOf(AccessDeniedException.class); }
  @Test @WithMockUser(username="reader", roles="READER") void userCanReadOwnProfile() { service.profile("reader"); }
  @Test @WithMockUser(username="reader", roles="READER") void userCannotReadAnotherProfile() { assertThatThrownBy(() -> service.profile("admin")).isInstanceOf(AccessDeniedException.class); }
}
