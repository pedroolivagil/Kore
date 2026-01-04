package com.olivadevelop.kore.db.entity;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore_annotations.GenerateDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
@Getter
@GenerateDto(dtoPackage = Constants.Defaults.PACKAGE_DTO)
public class VoidEntity implements KoreEntity { }
