package com.innowise.authservice.integration.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.auth.test.annotation.WithMockCustomUser;
import com.innowise.authservice.integration.AbstractIntegrationTest;
import com.innowise.authservice.integration.annotation.IT;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@IT
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockCustomUser(
    roles = {
        "MANAGER"
    }
)
class UserControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/auth/users";

  private final MockMvc mockMvc;
  private final MockMvcTester mockMvcTester;
  private final TestEntityManager em;
  private final PasswordEncoder passwordEncoder;

  private FixtureMonkey sut;

  @BeforeAll
  void prepareFixtureMonkey() {
    sut = FixtureMonkey.builder()
        .plugin(new DataFakerPlugin())
        .plugin(new JqwikPlugin())
        .defaultNotNull(true)
        .objectIntrospector(new FailoverIntrospector(
            List.of(
                BuilderArbitraryIntrospector.INSTANCE
            )
        ))
        .register(Credentials.class, fm -> fm.giveMeBuilder(Credentials.class)
            .setLazy("login", () -> FAKER.credentials().username())
            .setLazy("passwordHash", () -> passwordEncoder.encode(FAKER.credentials().password()))
        )
        .register(User.class, fm -> fm.giveMeBuilder(User.class)
            .setNull("id")
            .set("userId", Arbitraries.longs().greaterOrEqual(1L))
            .size("roles", 1)
            .set("roles[0]", Role.USER)
        )
        .register(CredentialDto.class, fm -> fm.giveMeBuilder(CredentialDto.class)
            .setLazy("login", () -> FAKER.credentials().username())
            .setLazy("password", () -> FAKER.credentials().password())
        )
        .build();
  }

  @Test
  void delete_userNotExists_returnNoContent() throws Exception {
    var id = Long.MAX_VALUE;

    mockMvc.perform(delete(BASE_URL + "/{id}", id)
            .contentType("application/json"))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_userExists_returnNoContent() throws Exception {

    var user = sut.giveMeOne(User.class);
    em.persistAndFlush(user);

    userServiceClientServer.stubFor(
        WireMock.delete(WireMock.urlEqualTo("/api/v1/users/" + user.getUserId()))
            .willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value()))
    );

    mockMvc.perform(delete(BASE_URL + "/{id}", user.getId())
            .contentType("application/json"))
        .andExpect(status().isNoContent());

    assertThat(em.find(User.class, user.getId())).isNull();
  }

}
