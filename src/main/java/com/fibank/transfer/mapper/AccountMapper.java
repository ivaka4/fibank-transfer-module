package com.fibank.transfer.mapper;

import com.fibank.transfer.dto.response.AccountResponse;
import com.fibank.transfer.entity.AccountEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(AccountEntity entity);

    List<AccountResponse> toResponseList(List<AccountEntity> entities);
}
