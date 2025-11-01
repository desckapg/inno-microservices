package com.innowise.orderservice.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.auth.test.annotation.WithMockCustomUser;
import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.integration.annotation.IT;
import com.innowise.orderservice.model.dto.item.ItemDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.mapper.ItemMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import java.math.BigDecimal;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@IT
@RequiredArgsConstructor
class ItemControllerIT extends AbstractIntegrationTest {

  private final ItemMapper itemMapper;
  private final JsonMapper jsonMapper;
  private final MockMvc mockMvc;
  private final MockMvcTester mockMvcTester;
  private final TestEntityManager em;

  private FixtureMonkey itemsSut;

  @BeforeAll
  void prepareItemsAndOrders() {
    itemsSut = FixtureMonkey.builder()
        .plugin(new JqwikPlugin())
        .defaultNotNull(true)
        .nullableContainer(false)
        .nullableElement(false)
        .register(Item.class, fm -> fm.giveMeBuilder(Item.class)
            .setNull("id")
            .setLazy("name", () -> FAKER.commerce().productName())
            .setLazy("price",
                () -> BigDecimal.valueOf(Double.parseDouble(FAKER.commerce().price())))
        )
        .build();

  }

  @Test
  @WithMockCustomUser
  void findById_itemExists_returnOk() throws Exception {
    var item = itemsSut.giveMeOne(Item.class);

    em.persist(item);
    em.flush();
    em.clear();

    mockMvc.perform(
        get(URI.create("/api/v1/orders/items/" + item.getId()))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$.id").value(item.getId())
    );
  }

  @Test
  @WithMockCustomUser
  void findById_itemNotExists_returnNotFound() throws Exception {
    mockMvc.perform(
        get(URI.create("/api/v1/orders/items/" + Long.MAX_VALUE))
    ).andExpectAll(
        status().isNotFound()
    );
  }


  @Test
  @WithMockCustomUser
  void findAll() throws Exception {
    var items = itemsSut.giveMe(Item.class, 5);

    items.forEach(em::persist);
    em.flush();
    em.clear();

    mockMvc.perform(
        get(URI.create("/api/v1/orders/items"))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$").isArray()
    );
  }

  @Test
  @WithMockCustomUser
  void findAll_byIdIn() throws Exception {
    var items = itemsSut.giveMe(Item.class, 5);

    items.forEach(em::persist);
    em.flush();
    em.clear();

    var findingItems = Arbitraries.of(items).list().ofSize(3).sample();

    mockMvc.perform(
        get(URI.create("/api/v1/orders/items"))
            .param("ids",
                findingItems.stream().map(i -> i.getId().toString()).toArray(String[]::new))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$").isArray(),
        jsonPath("$").value(Matchers.hasSize(findingItems.size()))
    );
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void create() throws Exception {
    var item = itemsSut.giveMeOne(Item.class);

    em.persist(item);
    em.flush();
    em.clear();

    mockMvc.perform(
        post(URI.create("/api/v1/orders/items"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(itemMapper.toDto(item)))
    ).andExpectAll(
        status().isCreated()
    );
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void delete_itemExists_returnNoContent() throws Exception {
    var item = itemsSut.giveMeOne(Item.class);

    em.persist(item);
    em.flush();
    em.clear();

    mockMvc.perform(
        delete(URI.create("/api/v1/orders/items/" + item.getId()))
    ).andExpectAll(
        status().isNoContent()
    );

    assertThat(em.find(Item.class, item.getId())).isNull();
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void update_itemExists_returnOk() throws Exception {
    var item = itemsSut.giveMeOne(Item.class);

    em.persist(item);
    em.flush();
    em.clear();

    var updateItemDto = ItemDto.builder()
        .name("Updated name")
        .build();

    mockMvc.perform(
        put(URI.create("/api/v1/orders/items/" + item.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(updateItemDto))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$.name").value("Updated name")
    );

    assertThat(em.find(Item.class, item.getId())).satisfies(foundedItem -> {
      assertThat(foundedItem).isNotNull();
      assertThat(foundedItem.getName()).isEqualTo("Updated name");
    });
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void update_itemNotExists_returnNotFound() throws Exception {
    var updateItemDto = ItemDto.builder()
        .name("Updated name")
        .build();

    mockMvc.perform(
        put(URI.create("/api/v1/orders/items/" + Long.MAX_VALUE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(updateItemDto))
    ).andExpectAll(
        status().isNotFound()
    );
  }

}
