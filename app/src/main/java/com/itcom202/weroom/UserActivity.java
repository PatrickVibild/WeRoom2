package com.itcom202.weroom;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AccountCreationFragment();
    }
}
