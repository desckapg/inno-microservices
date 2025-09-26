package com.innowise.userservice.cache;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.dto.user.UserWithCardsDto;
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

  public static final String USER_BASIC_CACHE = "users:basic";
  public static final String USER_WITH_CARDS_CACHE = "users:with-cards";

  private final CacheManager cacheManager;
  private final UserMapper userMapper;

  public void updateUserCaches(User user) {
    Long userId = user.getId();

    putInCache(USER_BASIC_CACHE, userId, userMapper.toDto(user));

    UserWithCardsDto cached = getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class);
    if (cached != null) {
      putInCache(
          USER_WITH_CARDS_CACHE,
          userId,
          new UserWithCardsDto(
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

  public void evictUserCaches(Long userId) {
    evictIfPresent(USER_BASIC_CACHE, userId);
    evictIfPresent(USER_WITH_CARDS_CACHE, userId);
  }

  public void addCardToCache(Long userId, CardDto newCard) {
    UserWithCardsDto cached = getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class);
    if (cached != null) {
      List<CardDto> cards = new ArrayList<>(Optional.ofNullable(cached.cards()).orElse(List.of()));
      cards.add(newCard);
      putInCache(USER_WITH_CARDS_CACHE, userId, new UserWithCardsDto(
          cached.id(),
          cached.name(),
          cached.surname(),
          cached.birthDate(),
          cached.email(),
          cards
      ));
    }
  }

  public boolean isUserCardsCached(Long userId) {
    return getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class) != null;
  }

  public List<CardDto> getCardsFromCache(Long userId) {
    UserWithCardsDto cached = getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class);
    if (cached != null && cached.cards() != null) {
      return cached.cards();
    }
    return List.of();
  }

  public void removeCardFromCache(Long userId, Long cardId) {
    UserWithCardsDto cached = getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class);
    if (cached != null && cached.cards() != null) {
      List<CardDto> cards = new ArrayList<>(cached.cards());
      cards.removeIf(card -> Objects.equals(card.id(), cardId));
      putInCache(USER_WITH_CARDS_CACHE, userId, new UserWithCardsDto(
          cached.id(),
          cached.name(),
          cached.surname(),
          cached.birthDate(),
          cached.email(),
          cards
      ));
    }
  }

  public void updateCardInCache(Long userId, CardDto updatedCard) {
    UserWithCardsDto cached = getFromCache(USER_WITH_CARDS_CACHE, userId, UserWithCardsDto.class);
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
      putInCache(USER_WITH_CARDS_CACHE, userId, new UserWithCardsDto(
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

  private void evictIfPresent(String cacheName, Object key) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.evictIfPresent(key);
    }
  }

  private <T> T getFromCache(String cacheName, Long key, Class<T> type) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      return cache.get(key, type);
    }
    return null;
  }
}
