package com.innowise.userservice.cache;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheHelper {

  public static final String USER_CACHE = "users";

  private final CacheManager cacheManager;
  private final UserMapper userMapper;

  public void updateUserCaches(User user) {
    UserDto cached = getFromCache(USER_CACHE, user.getId());
    if (cached != null) {
      putInCache(
          USER_CACHE,
          user.getId(),
          new UserDto(
              user.getId(),
              user.getName(),
              user.getSurname(),
              user.getBirthDate(),
              user.getEmail(),
              cached.cards()
          )
      );
    }
  }

  public void addCardToCache(Long userId, CardDto newCard) {
    UserDto cached = getFromCache(USER_CACHE, userId);
    if (cached != null) {
      List<CardDto> cards = new ArrayList<>(Optional.ofNullable(cached.cards()).orElse(List.of()));
      cards.add(newCard);
      putInCache(USER_CACHE, userId, new UserDto(
          cached.id(),
          cached.name(),
          cached.surname(),
          cached.birthDate(),
          cached.email(),
          cards
      ));
    }
  }

  public List<CardDto> getCardsFromCache(Long userId) {
    UserDto cached = getFromCache(USER_CACHE, userId);
    if (cached != null && cached.cards() != null) {
      return cached.cards();
    }
    return List.of();
  }

  public void removeCardFromCache(Long userId, Long cardId) {
    UserDto cached = getFromCache(USER_CACHE, userId);
    if (cached != null && cached.cards() != null) {
      List<CardDto> cards = new ArrayList<>(cached.cards());
      cards.removeIf(card -> Objects.equals(card.id(), cardId));
      putInCache(USER_CACHE, userId, new UserDto(
          cached.id(),
          cached.name(),
          cached.surname(),
          cached.birthDate(),
          cached.email(),
          cards
      ));
    }
  }

  public boolean isUserCached(Long userId) {
    return getFromCache(USER_CACHE, userId) != null;
  }

  public void updateCardInCache(Long userId, CardDto updatedCard) {
    UserDto cached = getFromCache(USER_CACHE, userId);
    if (cached != null) {
      List<CardDto> cards = new ArrayList<>(Optional.ofNullable(cached.cards()).orElse(List.of()));
      boolean replaced = false;
      for (int i = 0; i < cards.size(); i++) {
        if (Objects.equals(cards.get(i).id(), updatedCard.id())) {
          cards.set(i, updatedCard);
          replaced = true;
          break;
        }
      }
      if (!replaced) {
        cards.add(updatedCard);
      }
      putInCache(USER_CACHE, userId, new UserDto(
          cached.id(),
          cached.name(),
          cached.surname(),
          cached.birthDate(),
          cached.email(),
          cards
      ));
    }
  }

  public void invalidate() {
    cacheManager.getCacheNames().forEach(name -> {
      Cache cache = cacheManager.getCache(name);
      if (cache != null) {
        cache.invalidate();
      }
    });
  }

  private void putInCache(String cacheName, Object key, Object value) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.put(key, value);
    }
  }

  private <T> T getFromCache(String cacheName, Long key) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      return cache.get(key, (Class<T>) UserDto.class);
    }
    return null;
  }
}
