package com.fibank.transfer.mapper;

import com.fibank.transfer.dto.response.TransferResponse;
import com.fibank.transfer.service.model.TransferResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    TransferResponse toResponse(TransferResult result);
}
