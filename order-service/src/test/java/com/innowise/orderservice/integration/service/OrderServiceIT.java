package com.innowise.orderservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.test.annotation.WithMockCustomUser;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.integration.annotation.IT;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.dto.order.OrderSpecsDto;
import com.innowise.orderservice.model.dto.user.UserDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.mapper.OrderMapper;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.client.UserServiceClient;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.support.TransactionTemplate;

@IT
@RequiredArgsConstructor
class OrderServiceIT extends AbstractIntegrationTest {

  private final OrderService orderService;
  private final OrderMapper orderMapper;
  private final UserServiceClient userServiceClient;
  private final AuthTokenProvider authTokenProvider;
  private final TransactionTemplate tt;
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


  @AfterAll
  void clearItemsAndOrders() {
    tt.executeWithoutResult(_ -> {
      em.getEntityManager().createQuery("DELETE FROM Item").executeUpdate();
      em.getEntityManager().createQuery("DELETE FROM Order").executeUpdate();
    });
  }

  @Test
  void delete_orderExists_delete() {
    var order = ordersSut.giveMeOne(Order.class);
    em.persist(order);

    assertThat(em.find(Order.class, order.getId())).isNotNull();
    orderService.delete(order.getId());
    assertThat(em.find(Order.class, order.getId())).isNull();
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findById_orderNotExists_throwResourceNotFoundException() {
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> orderService.findById(Long.MAX_VALUE));
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findById_orderExistsAndUserHadUserAuthorityAndRequestOwnedOrder_returnOrder() {
    var userDto = userDtosSut.giveMeBuilder(UserDto.class)
        .set("id", authTokenProvider.get().getPrincipal().userId())
        .sample();

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", userDto.id())
        .sample();

    em.persist(order);
    em.flush();

    when(userServiceClient.findById(eq(order.getUserId()), Mockito.anyString()))
        .thenReturn(userDto);

    assertThat(orderService.findById(order.getId()))
        .isEqualTo(orderMapper.toDto(order, userDto));
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findById_orderExistsAndUserHasUserAuthorityAndRequestNowOwnedOrder_accessDenied() {
    var userDto = userDtosSut.giveMeBuilder(UserDto.class)
        .set("id", authTokenProvider.get().getPrincipal().userId())
        .sample();

    var order = ordersSut.giveMeBuilder(Order.class)
        .sample();

    em.persist(order);
    em.flush();

    when(userServiceClient.findById(eq(order.getUserId()), Mockito.anyString()))
        .thenReturn(userDto);

    var orderId = order.getId();

    assertThatExceptionOfType(AuthorizationDeniedException.class)
        .isThrownBy(() -> orderService.findById(orderId));
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void findById_orderExistsAndUserHasManagerAuthorityAndRequestNowOwnedOrder_returnOrder() {
    var orderOwnerUserDto = userDtosSut.giveMeOne(UserDto.class);

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", orderOwnerUserDto.id())
        .sample();

    em.persist(order);
    em.flush();

    when(userServiceClient.findById(eq(orderOwnerUserDto.id()), Mockito.anyString()))
        .thenReturn(orderOwnerUserDto);

    assertThat(orderService.findById(order.getId()))
        .isEqualTo(orderMapper.toDto(order, orderOwnerUserDto));
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void findAll_byIdsAndUserHasManagerAuthority_returnOrders() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(5);
    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    assertThat(orderService.findAll(OrderSpecsDto.builder()
        .ids(orders.stream().map(Order::getId).toList())
        .build())
    ).containsAll(orders.stream().map(order -> orderMapper.toDto(order, ownedUserDto)).toList());
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findAll_byIdsAndUserHasUserAuthority_accessDenied() {

    var orderSpecsDto = OrderSpecsDto.builder()
        .ids(Arbitraries.longs().list().ofSize(5).sample())
        .build();

    assertThatExceptionOfType(AuthorizationDeniedException.class)
        .isThrownBy(() -> orderService.findAll(orderSpecsDto));

  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void findAll_byStatusesAndUserHasManagerAuthority_returnOrders() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(1);
    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    assertThat(orderService.findAll(OrderSpecsDto.builder()
        .statuses(orders.stream().map(Order::getStatus).distinct().toList())
        .build())
    ).containsExactlyInAnyOrderElementsOf(
        orders.stream()
            .map(order -> orderMapper.toDto(order, ownedUserDto))
            .toList()
    );
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findAll_byStatusesAndUserHasUserAuthority_accessDenied() {

    var orderSpecsDto = OrderSpecsDto.builder()
        .statuses(Arbitraries.of(Order.Status.values()).list().sample())
        .build();

    assertThatExceptionOfType(AuthorizationDeniedException.class)
        .isThrownBy(() -> orderService.findAll(orderSpecsDto));

  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void findAll_byUserIdAndUserHasManagerAuthorityAndNotOwnedOrders_returnOrders() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(5);

    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    assertThat(orderService.findAll(OrderSpecsDto.builder()
        .userId(ownedUserDto.id())
        .build())
    ).containsAll(orders.stream().map(order -> orderMapper.toDto(order, ownedUserDto)).toList());
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findAll_byUserIdAndUserHasUserAuthorityAndOwnedOrders_returnOrders() {
    var ownedUserDto = userDtosSut.giveMeBuilder(UserDto.class)
        .set("id", authTokenProvider.get().getPrincipal().userId())
        .sample();

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(5);

    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    assertThat(orderService.findAll(OrderSpecsDto.builder()
        .userId(ownedUserDto.id())
        .build())
    ).containsAll(orders.stream().map(order -> orderMapper.toDto(order, ownedUserDto)).toList());
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "USER"
      }
  )
  void findAll_byUserIdAndUserHasUserAuthorityAndOwnedOrders_accessDenied() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var orders = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .sampleList(5);

    orders.forEach(order -> order.getOrderItems().forEach(item -> item.setOrder(order)));

    orders.forEach(em::persist);
    em.flush();
    em.clear();

    when(userServiceClient.findById(
            anyLong(),
            anyString()
        )
    ).thenReturn(ownedUserDto);

    var orderSpecsDto = OrderSpecsDto.builder()
        .userId(ownedUserDto.id())
        .build();

    assertThatExceptionOfType(AuthorizationDeniedException.class)
        .isThrownBy(() -> orderService.findAll(orderSpecsDto));

  }

  @Test
  @WithMockCustomUser
  void create() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
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

    assertThatNoException()
        .isThrownBy(() -> orderService.create(orderMapper.toDto(order, ownedUserDto)));

    assertThat(em.find(Order.class, order.getId())).isEqualTo(order);
  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void update_orderExists_update() {

    var ownedUserDto = userDtosSut.giveMeOne(UserDto.class);

    var order = ordersSut.giveMeBuilder(Order.class)
        .set("userId", ownedUserDto.id())
        .set("status", Order.Status.NEW)
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
        .status(OrderDto.Status.SHIPPED)
        .build();

    assertThatNoException().isThrownBy(() -> orderService.update(order.getId(), orderUpdateDto));

    assertThat(em.find(Order.class, order.getId())).satisfies(foundedOrder -> {
      assertThat(foundedOrder).isNotNull();
      assertThat(foundedOrder.getStatus()).isEqualTo(Order.Status.SHIPPED);
    });

  }

  @Test
  @WithMockCustomUser(
      roles = {
          "MANAGER"
      }
  )
  void update_orderNotExists_throwResourceNotFoundException() {
    var orderUpdateDto = OrderDto.builder()
        .status(OrderDto.Status.SHIPPED)
        .build();

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> orderService.update(Long.MAX_VALUE, orderUpdateDto));


  }


}
