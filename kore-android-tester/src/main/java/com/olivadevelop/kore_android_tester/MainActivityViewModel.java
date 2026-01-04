package com.olivadevelop.kore_android_tester;

import com.olivadevelop.kore.db.entity.VoidEntity;
import com.olivadevelop.kore.viewmodel.KoreViewModel;

public class MainActivityViewModel extends KoreViewModel<VoidEntity> {
    @Override
    public boolean isValid() {
        return false;
    }
    @Override
    public KoreViewModel<VoidEntity> buildEntityData() {
        return null;
    }
}
