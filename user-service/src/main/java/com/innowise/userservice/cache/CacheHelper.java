package com.innowise.userservice.cache;

import com.innowise.userservice.model.dto.card.CardResponseDto;
import com.innowise.userservice.model.dto.user.UserResponseDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

    UserResponseDto basicDto = userMapper.toDto(user);
    putInCache(USER_BASIC_CACHE, userId, basicDto);

    if (isUserCardsCached(user)) {
      UserResponseDto fullDto = userMapper.toWithCardsDto(user);
      putInCache(USER_WITH_CARDS_CACHE, userId, fullDto);
    } else {
      evictIfPresent(USER_WITH_CARDS_CACHE, userId);
    }
  }

  public void evictUserCaches(Long userId) {
    evictIfPresent(USER_BASIC_CACHE, userId);
    evictIfPresent(USER_WITH_CARDS_CACHE, userId);
  }

  public void evictUserCardsCaches(Long userId) {
    evictIfPresent(USER_BASIC_CACHE, userId);
  }

  private void putInCache(String cacheName, Object key, Object value) {
    var cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.put(key, value);
    }
  }

  private void evictIfPresent(String cacheName, Object key) {
    var cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.evictIfPresent(key);
    }
  }

  private boolean isUserCardsCached(User user) {
    return user.getCards() != null;
  }

  public void addCardToCache(Long userId, CardResponseDto newCard) {
    UserResponseDto cachedUser = getCachedUser(USER_WITH_CARDS_CACHE, userId);
    if (cachedUser != null) {
      List<CardResponseDto> cards = new LinkedList<>(
          cachedUser.cards() != null ? cachedUser.cards() : new LinkedList<>());
      cards.add(newCard);

      putInCache(USER_WITH_CARDS_CACHE, userId, new UserResponseDto(
          cachedUser.id(),
          cachedUser.name(),
          cachedUser.surname(),
          cachedUser.birthDate(),
          cachedUser.email(),
          cards
      ));
    }
  }

  public boolean isUserCardsCached(Long userId) {
    UserResponseDto cachedUser = getCachedUser(USER_WITH_CARDS_CACHE, userId);
    return cachedUser != null;
  }

  public List<CardResponseDto> getCardsFromCache(Long userId) {
    UserResponseDto cachedUser = getCachedUser(USER_WITH_CARDS_CACHE, userId);
    if (cachedUser != null && cachedUser.cards() != null) {
      return cachedUser.cards();
    }
    return List.of();
  }

  public void removeCardFromCache(Long userId, Long cardId) {
    UserResponseDto cachedUser = getCachedUser(USER_WITH_CARDS_CACHE, userId);
    if (cachedUser != null && cachedUser.cards() != null) {
      cachedUser.cards().removeIf(card -> card.userId().equals(cardId));
      putInCache(USER_WITH_CARDS_CACHE, userId, cachedUser);
    }
  }

  public void updateCardInCache(Long userId, CardResponseDto updatedCard) {
    UserResponseDto cachedUser = getCachedUser(USER_WITH_CARDS_CACHE, userId);
    if (cachedUser != null && cachedUser.cards() != null) {
      Optional<CardResponseDto> existingCard = cachedUser.cards().stream()
          .filter(card -> card.id().equals(updatedCard.id()))
          .findFirst();

      if (existingCard.isPresent()) {
        cachedUser.cards().removeIf(card -> card.id().equals(updatedCard.id()));
        cachedUser.cards().add(updatedCard);
        putInCache(USER_WITH_CARDS_CACHE, userId, cachedUser);
      }
    }
  }

  private UserResponseDto getCachedUser(String cacheName, Long userId) {
    var cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      return cache.get(userId, UserResponseDto.class);
    }
    return null;
  }

}
