package com.itcom202.weroom.account.authentification.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.itcom202.weroom.R;

/**
 * View that allow users to recover their password if they forgot it.
 */
public class ForgotPasswordFragment extends Fragment {
    private static final String TAG = "ForgotPasswordFragment";
    private EditText mEmailForgotPass;
    private Button mSendNewPassButton;

    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View v = inflater.inflate( R.layout.forgot_password_fragment, container, false );

        mEmailForgotPass = v.findViewById( R.id.emailForgotPass );
        mSendNewPassButton = v.findViewById( R.id.sendNewPassButton );
        mSendNewPassButton.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick( View v ) {
                FirebaseAuth auth = FirebaseAuth.getInstance( );
                if ( ! mEmailForgotPass.getText( ).toString( ).equals( "" ) ) {
                    auth.sendPasswordResetEmail( mEmailForgotPass.getText( ).toString( ) )
                            .addOnCompleteListener( new OnCompleteListener<Void>( ) {
                                @Override
                                public void onComplete( @NonNull Task<Void> task ) {
                                    if ( task.isSuccessful( ) ) {
                                        Log.d( TAG, "Email sent." );
                                        Toast.makeText( getActivity( ), getString( R.string.email_sent ), Toast.LENGTH_SHORT ).show( );
                                    } else {
                                        Toast.makeText( getActivity( ), getString( R.string.not_succ ), Toast.LENGTH_SHORT ).show( );

                                    }

                                }
                            } );
                } else
                    Toast.makeText( getActivity( ), getString( R.string.invalid_email ), Toast.LENGTH_SHORT ).show( );

            }
        } );

        return v;
    }
}
