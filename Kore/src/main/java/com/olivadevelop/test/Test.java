package com.olivadevelop.test;

import com.olivadevelop.kore_annotations.GenerateDto;

import java.util.List;

@GenerateDto(dtoPackage = "com.olivadevelop.kore", suffix = "DTO")
public class Test {
    String texto;
    List<String> cadenas;
    List<Test2> test2List;
}
