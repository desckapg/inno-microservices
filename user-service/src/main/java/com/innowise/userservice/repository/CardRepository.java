package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<Card, Long> {

  @Query(value = "SELECT * FROM cards_info ci WHERE ci.id IN (:ids)", nativeQuery = true)
  List<Card> findAllByIdIn(List<Long> ids);

  boolean existsByNumber(String number);

  @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
  List<Card> findUserCards(Long userId);

}
