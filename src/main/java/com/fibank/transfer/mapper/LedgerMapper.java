package com.fibank.transfer.mapper;

import com.fibank.transfer.dto.response.LedgerEntryResponse;
import com.fibank.transfer.entity.LedgerEntryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LedgerMapper {

    LedgerEntryResponse toResponse(LedgerEntryEntity entity);
}
