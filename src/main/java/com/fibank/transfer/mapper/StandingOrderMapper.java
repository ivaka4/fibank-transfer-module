package com.fibank.transfer.mapper;

import com.fibank.transfer.dto.response.StandingOrderResponse;
import com.fibank.transfer.entity.StandingOrderEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StandingOrderMapper {

    StandingOrderResponse toResponse(StandingOrderEntity entity);

    List<StandingOrderResponse> toResponseList(List<StandingOrderEntity> entities);
}
