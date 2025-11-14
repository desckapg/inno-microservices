package com.innowise.orderservice.integration.controller.rest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.test.annotation.WithMockCustomUser;
import com.innowise.common.model.dto.user.UserDto;
import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.integration.annotation.IT;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.enums.OrderStatus;
import com.innowise.orderservice.model.mapper.OrderMapper;
import com.innowise.orderservice.service.client.UserServiceClient;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@IT
@RequiredArgsConstructor
class OrderControllerIT extends AbstractIntegrationTest {

  private final OrderMapper orderMapper;
  private final JsonMapper jsonMapper;
  private final UserServiceClient userServiceClient;
  private final AuthTokenProvider authTokenProvider;
  private final MockMvc mockMvc;
  private final TestEntityManager em;

  private FixtureMonkey itemsSut;
  private FixtureMonkey orderItemSut;
  private FixtureMonkey ordersSut;
  private FixtureMonkey userDtosSut;

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

    orderItemSut = FixtureMonkey.builder()
        .plugin(new JqwikPlugin())
        .defaultNotNull(true)
        .nullableContainer(false)
        .nullableElement(false)
        .register(OrderItem.class, fm -> fm.giveMeBuilder(OrderItem.class)
            .setNull("id")
            .setNull("order")
            .set("quantity", Arbitraries.integers().between(1, 10))
            .setLazy("item", () -> itemsSut.giveMeOne(Item.class))
        )
        .build();

    userDtosSut = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .plugin(new JqwikPlugin())
        .defaultNotNull(true)
        .nullableContainer(false)
        .nullableElement(false)
        .register(UserDto.class, fm -> fm.giveMeBuilder(UserDto.class)
            .set("id", Arbitraries.longs().greaterOrEqual(10000L))
            .set("name", FAKER.name().firstName())
            .set("surname", FAKER.name().lastName())
            .set("birthDate", LocalDate.of(1970, 1, 1))
            .set("email", FAKER.internet().emailAddress())
            .size("cards", 0)
        )
        .build();

    ordersSut = FixtureMonkey.builder()
        .defaultNotNull(true)
        .nullableContainer(false)
        .nullableElement(false)
        .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
            .setNull("id")
            .size("orderItems", 1, 5)
            .setLazy("orderItems[*]", () -> orderItemSut.giveMeOne(OrderItem.class))
        )
        .build();
  }

  @Test
  @WithMockCustomUser
  void findById_orderExists_returnOk() throws Exception {

    var userDto = userDtosSut.giveMeBuilder(UserDto.class)
        .set("id", authTokenProvider.get().getPrincipal().userId())
        .sample();

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", userDto.id())
        .sample();

    em.persist(order);
    em.flush();
    em.clear();

    when(userServiceClient.findById(eq(order.getUserId()), Mockito.anyString()))
        .thenReturn(userDto);

    mockMvc.perform(
        get(URI.create("/api/v1/orders/" + order.getId()))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$.id").value(order.getId())
    );
  }

  @Test
  @WithMockCustomUser
  void findById_orderNotExists_returnNotFound() throws Exception {
    mockMvc.perform(
        get(URI.create("/api/v1/orders/" + Long.MAX_VALUE))
    ).andExpectAll(
        status().isNotFound()
    );
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void delete_orderExists_delete() throws Exception {
    var userDto = userDtosSut.giveMeBuilder(UserDto.class)
        .set("id", authTokenProvider.get().getPrincipal().userId())
        .sample();

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", userDto.id())
        .sample();

    em.persist(order);
    em.flush();
    em.clear();

    when(userServiceClient.findById(eq(order.getUserId()), Mockito.anyString()))
        .thenReturn(userDto);

    mockMvc.perform(
        delete(URI.create("/api/v1/orders/" + order.getId()))
    ).andExpectAll(
        status().isNoContent()
    );

    assertThat(em.find(Order.class, order.getId())).isNull();
  }

  @Test
  @WithMockCustomUser
  void create() throws Exception {
    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sample();

    order.getOrderItems().forEach(item -> item.setOrder(order));

    order.getOrderItems().stream()
        .map(OrderItem::getItem)
        .forEach(em::persist);

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    mockMvc.perform(
        post(URI.create("/api/v1/orders"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(orderMapper.toDto(order, ownedUserDto)))
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
  void update_orderExists_update() throws Exception {
    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .set("status", OrderStatus.NEW)
        .sample();

    order.getOrderItems().forEach(item -> item.setOrder(order));

    em.persist(order);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    var orderUpdateDto = OrderDto.builder()
        .status(OrderStatus.NEW.SHIPPED)
        .build();

    mockMvc.perform(
        put(URI.create("/api/v1/orders/" + order.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(orderUpdateDto))
    ).andExpectAll(
        status().isOk()
    );

    assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
      assertThat(foundOrder).isNotNull();
      assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    });
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void findAll_byIdsOrdersExists_returnOrders() throws Exception {
    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(10);

    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    var requestIds = Arbitraries.of(orders.stream().map(Order::getId).toList())
        .list()
        .uniqueElements()
        .ofMinSize(1)
        .ofMaxSize(5)
        .sample();

    mockMvc.perform(
        get(URI.create("/api/v1/orders"))
            .param("ids", requestIds.stream().map(String::valueOf).toArray(String[]::new))
    ).andExpectAll(
        status().isOk(),
        jsonPath("$").isArray(),
        jsonPath("$").value(Matchers.hasSize(requestIds.size()))
    );
  }

}
