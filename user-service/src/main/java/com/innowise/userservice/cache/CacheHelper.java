package com.innowise.userservice.cache;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
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

  public void updateUserCaches(User user) {
    UserDto cached = getFromCache(user.getId());
    if (cached != null) {
      putInCache(
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
    UserDto cached = getFromCache(userId);
    if (cached != null) {
      List<CardDto> cards = new ArrayList<>(Optional.ofNullable(cached.cards()).orElse(List.of()));
      cards.add(newCard);
      putInCache(userId, new UserDto(
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
    UserDto cached = getFromCache(userId);
    if (cached != null && cached.cards() != null) {
      return cached.cards();
    }
    return List.of();
  }

  public void removeCardFromCache(Long userId, Long cardId) {
    UserDto cached = getFromCache(userId);
    if (cached != null && cached.cards() != null) {
      List<CardDto> cards = new ArrayList<>(cached.cards());
      cards.removeIf(card -> Objects.equals(card.id(), cardId));
      putInCache(userId, new UserDto(
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
    return getFromCache(userId) != null;
  }

  public void updateCardInCache(Long userId, CardDto updatedCard) {
    UserDto cached = getFromCache(userId);
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
      putInCache(userId, new UserDto(
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

  private void putInCache(Object key, Object value) {
    Cache cache = cacheManager.getCache(CacheHelper.USER_CACHE);
    if (cache != null) {
      cache.put(key, value);
    }
  }

  private UserDto getFromCache(Long key) {
    Cache cache = cacheManager.getCache(CacheHelper.USER_CACHE);
    if (cache != null) {
      return cache.get(key, UserDto.class);
    }
    return null;
  }
}
