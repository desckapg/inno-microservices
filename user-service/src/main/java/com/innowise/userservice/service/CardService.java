package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.card.CardDto;
import java.util.List;

public interface CardService {

  CardDto create(CardDto dto) ;

  CardDto update(Long id, CardDto dto);

  void delete(Long id);

  CardDto findById(Long id);

  List<CardDto> findAllByIdIn(List<Long> ids) ;


  List<CardDto> findAll();

  List<CardDto> findUserCards(Long userId);

}
