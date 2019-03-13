package com.itcom202.weroom.authentification;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.itcom202.weroom.SingleFragmentActivity;

public class SignActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new SignFragment();
    }

    public static Intent newIntent(Context myContext){
        Intent i = new Intent(myContext, SignActivity.class);
        return i;
    }
}
