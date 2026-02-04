package com.olivadevelop.kore_android_tester;

import com.olivadevelop.kore.MainActivityViewModelStatic;
import com.olivadevelop.kore.annotation.RegularExpressionField;
import com.olivadevelop.kore.annotation.RegularExpressionOption;
import com.olivadevelop.kore.attributtes.KoreComponentViewAttribute;
import com.olivadevelop.kore.db.dto.VoidEntityDto;
import com.olivadevelop.kore.viewmodel.KoreViewModel;
import com.olivadevelop.kore_annotations.StaticProperties;

@StaticProperties
public class MainActivityViewModel extends KoreViewModel<VoidEntityDto> {
    private String testStaticName;
    private String testStaticName2;
    @RegularExpressionField(value = "^[a-zA-Z0-9]+$", options = {@RegularExpressionOption(attribute = KoreComponentViewAttribute.MAXIMAGES, value = "5")})
    private Integer testStaticNumeric;
    @Override
    public boolean isValid() { return "testStaticName".equals(MainActivityViewModelStatic.testStaticName); }
}
