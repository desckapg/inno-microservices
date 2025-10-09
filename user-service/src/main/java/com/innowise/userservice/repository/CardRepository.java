package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.Card;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@NullMarked
public interface CardRepository extends JpaRepository<Card, Long> {

  List<Card> findAll();

  @Query(value = "SELECT * FROM cards_info ci WHERE ci.id IN (:ids)", nativeQuery = true)
  List<Card> findAllByIdIn(List<Long> ids);

  boolean existsByNumber(String number);

  @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
  List<Card> findUserCards(Long userId);

}
