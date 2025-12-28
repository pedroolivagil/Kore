package com.olivadevelop.kore.util;

import com.olivadevelop.kore.component.KoreComponentView;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoCalculateFormulaData {
    private String formula;
    private List<Pair<String, KoreComponentView<?>>> components;
    private List<Pair<String, Double>> parameters;
}
