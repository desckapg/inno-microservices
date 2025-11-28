package com.innowise.orderservice.service.impl;

import com.innowise.auth.model.AuthConstants;
import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.common.model.dto.payment.PaymentDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.orderservice.controller.kafka.producer.OrderProducer;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.dto.order.OrderSpecsDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.enums.OrderStatus;
import com.innowise.orderservice.model.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.client.UserServiceClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@NullMarked
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final OrderMapper orderMapper;
  private final UserServiceClient userServiceClient;
  private final AuthTokenProvider authTokenProvider;
  private final OrderProducer orderProducer;

  @Override
  @PreAuthorize("""
        hasAuthority(T(com.innowise.auth.model.Role).MANAGER) ||\s
          authentication.principal.id == @orderServiceImpl.findOrderUserId(#id)
      """)
  public OrderDto findById(Long id) {
    var order = orderRepository.findById(id)
        .orElseThrow(() -> generateNotFoundException(id));
    return orderMapper.toDto(order,
        userServiceClient.findById(order.getUserId(),
            AuthConstants.AUTH_SCHEME + authTokenProvider.get().getJwtToken())
    );
  }

  @Override
  @PreAuthorize("""
        hasAuthority(T(com.innowise.auth.model.Role).MANAGER) ||\s
        (#orderSpecsDto != null && #orderSpecsDto.userId() != null &&\s
          (authentication.principal.id == #orderSpecsDto.userId()\s
            || hasAuthority(T(com.innowise.auth.model.Role).MANAGER)
          )
        )
      \s""")
  public List<OrderDto> findAll(OrderSpecsDto orderSpecsDto) {
    Specification<Order> specification = orderSpecsDto.convertToSpecification();
    return orderRepository.findAll(specification).stream()
        .map(order -> orderMapper.toDto(order,
            userServiceClient.findById(order.getUserId(),
                AuthConstants.AUTH_SCHEME + authTokenProvider.get().getJwtToken())))
        .toList();
  }

  @Override
  @Transactional
  public void processPaymentCreation(PaymentDto payment) {
    var orderEntity = orderRepository.findById(payment.orderId())
        .orElseThrow(() -> generateNotFoundException(payment.orderId()));

    log.info("Received payment creation for Order (id={}), change status to PROCESSING",
        payment.orderId());
    orderEntity.setStatus(OrderStatus.PROCESSING);

    orderRepository.save(orderEntity);
  }

  @Override
  @Transactional
  public void processPaymentUpdate(Long orderId, PaymentStatus newStatus) {
    var orderEntity = orderRepository.findById(orderId)
        .orElseThrow(() -> generateNotFoundException(orderId));

    if (newStatus == PaymentStatus.SUCCEEDED) {
      log.info("Order (id={}) payment has succeeded", orderId);
      orderEntity.setStatus(OrderStatus.DELIVERING);
    } else if (newStatus == PaymentStatus.FAILED) {
      log.info("Order (id={}) payment has failed", orderId);
    }

    orderRepository.save(orderEntity);
  }

  @Override
  @Transactional
  public OrderDto create(OrderDto orderDto) {
    var orderEntity = orderMapper.toEntity(orderDto);

    orderEntity.getOrderItems().forEach(orderItem ->
        orderItem.setItem(itemRepository.getReferenceById(orderItem.getItem().getId())));

    var userId = authTokenProvider.get().getPrincipal().userId();
    var user = userServiceClient.findById(userId,
        AuthConstants.AUTH_SCHEME + authTokenProvider.get().getJwtToken());
    orderEntity.setStatus(OrderStatus.NEW);
    orderEntity.setUserId(userId);
    var savedOrderDto = orderMapper.toDto(orderRepository.save(orderEntity), user);
    orderProducer.sendOrderCreated(savedOrderDto);
    return savedOrderDto;
  }

  @Override
  @Transactional
  public OrderDto update(Long id, OrderDto orderDto) {
    var order = orderRepository.findById(id)
        .orElseThrow(() -> generateNotFoundException(id));
    order.setStatus(OrderStatus.valueOf(orderDto.status().name()));
    return orderMapper.toDto(
        orderRepository.save(order),
        userServiceClient.findById(order.getUserId(),
            AuthConstants.AUTH_SCHEME + authTokenProvider.get().getJwtToken()));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    orderRepository.deleteById(id);
  }

  @SuppressWarnings("unused")
  public Long findOrderUserId(Long id) {
    return orderRepository.findUserIdById(id)
        .orElseThrow(() -> generateNotFoundException(id));
  }

  private ResourceNotFoundException generateNotFoundException(Long id) {
    return ResourceNotFoundException.byId("Order", id);
  }

}
