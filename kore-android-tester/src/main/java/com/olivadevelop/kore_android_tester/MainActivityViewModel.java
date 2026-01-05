package com.olivadevelop.kore_android_tester;

import com.olivadevelop.kore.MainActivityViewModelStatic;
import com.olivadevelop.kore.db.dto.VoidEntityDto;
import com.olivadevelop.kore.viewmodel.KoreViewModel;
import com.olivadevelop.kore_annotations.StaticProperties;

@StaticProperties
public class MainActivityViewModel extends KoreViewModel<VoidEntityDto> {
    private String testStaticName;
    private String testStaticName2;
    private Integer testStaticNumeric;
    @Override
    public boolean isValid() { return "testStaticName".equals(MainActivityViewModelStatic.testStaticName); }
}
