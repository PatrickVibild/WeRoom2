package com.itcom202.weroom.interaction.swipe.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itcom202.weroom.R;
import com.itcom202.weroom.account.models.Profile;
import com.itcom202.weroom.account.models.RoomPosted;
import com.itcom202.weroom.account.onboarding.views.widget.OpenPictureFragment;
import com.itcom202.weroom.framework.DataBasePath;
import com.itcom202.weroom.framework.cameraandgallery.PictureConversion;
import com.itcom202.weroom.framework.queries.ImageController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment used to display a room and its corresponding landlord.
 */
public class CardInfoRoomFragment extends Fragment {
    private static final String KEY_ROOM = "KEY_ROOM";
    private ImageButton mButtonExit;
    // private ImageView mPhoto;
    private ImageView mPhotoLandlord;
    private TextView mLandlordName;
    private TextView mLandlordAge;
    private TextView mLandlordGender;
    private TextView mLandlordNation;
    private TextView mRoomDescription;
    private TextView mRoomLocation;
    private TextView mRent;
    private TextView mDeposit;
    private TextView mArea;
    private TextView mRentPeriod;
    private CheckBox mRoomInternet;
    private CheckBox mRoomLaundry;
    private CheckBox mRoomFurnished;
    private CheckBox mRoomCommonArea;
    private Profile mLandlord;
    private RoomPosted mRoomPosted;
    private List<Bitmap> mRoomPictures = new ArrayList<>( );
    private ImageView[] mPictures = new ImageView[ 10 ];


    /**
     * Method that creates a intent to start CardInfoRoomFragment with its corresponding bundle.
     *
     * @param room Postedroom used to create the bundle for the intent.
     * @return Intent to start CardInfoRoomFragment with its corresponding Bundle.
     */
    public static CardInfoRoomFragment newInstance( RoomPosted room ) {
        CardInfoRoomFragment fragment = new CardInfoRoomFragment( );
        Bundle bundle = new Bundle( );
        bundle.putParcelable( KEY_ROOM, room );
        fragment.setArguments( bundle );
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View v = inflater.inflate( R.layout.fragment_card_info_room, container, false );


        mLandlordName = v.findViewById( R.id.card_lord_name );
        mLandlordAge = v.findViewById( R.id.card_lord_age );
        mLandlordGender = v.findViewById( R.id.card_lord_gender );
        mLandlordNation = v.findViewById( R.id.card_lord_nation );
        mRoomDescription = v.findViewById( R.id.card_lord_description );
        mRoomLocation = v.findViewById( R.id.card_lord_address );
        mRoomInternet = v.findViewById( R.id.card_land_internet );
        mRoomLaundry = v.findViewById( R.id.card_land_laundry );
        mRoomFurnished = v.findViewById( R.id.card_land_furnished );
        mRoomCommonArea = v.findViewById( R.id.card_land_common );
        mArea = v.findViewById( R.id.area_room_cardinfo );
        mDeposit = v.findViewById( R.id.deposit_room_cardinfo );
        mRent = v.findViewById( R.id.rent_room_cardinfo );
        mRentPeriod = v.findViewById( R.id.period_room_cardinfo );
        mPhotoLandlord = v.findViewById( R.id.landlord_pb_cardinfo );


        if ( getArguments( ) != null ) {
            mRoomPosted = getArguments( ).getParcelable( KEY_ROOM );
            initializePictures( );
        }

        for ( int i = 0 ; i < 10 ; i++ ) {
            String btnID = "pictureroom" + ( i + 1 );
            int resID = getResources( ).getIdentifier( btnID, "id", Objects.requireNonNull( getActivity( ) ).getPackageName( ) );
            mPictures[ i ] = v.findViewById( resID );
            try {
                mPictures[i].setVisibility(View.GONE);
            }catch(Exception e){
                Log.d("blabla", "not 1");
            }
        }


        mButtonExit = v.findViewById( R.id.button_exit_info_page_lord );
        mButtonExit.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick( View v ) {
                AppCompatActivity activity = ( AppCompatActivity ) mButtonExit.getContext( );
                activity.getSupportFragmentManager( )
                        .popBackStackImmediate( );
            }
        } );

        updateUI( );

        return v;
    }

    /**
     * Updates the fields of the fragment with the values found on mRoomPosted and its
     * corresponding landlord.
     */
    private void updateUI( ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance( );
        Task t = db.collection( DataBasePath.USERS.getValue( ) )
                .document( mRoomPosted.getLandlordID( ) )
                .get( ).addOnSuccessListener( new OnSuccessListener<DocumentSnapshot>( ) {
                    @Override
                    public void onSuccess( DocumentSnapshot documentSnapshot ) {
                        mLandlord = documentSnapshot.toObject( Profile.class );
                    }
                } );


        t.addOnCompleteListener( new OnCompleteListener( ) {
            @Override
            public void onComplete( @NonNull Task task ) {
                mLandlordName.setText( mLandlord.getName( ) );
                mLandlordAge.setText( String.format( Locale.getDefault( ), "%d", mLandlord.getAge( ) ) );
                mLandlordGender.setText( mLandlord.getGender( ) );
                Locale l = new Locale( "", mLandlord.getCountry( ) );
                mLandlordNation.setText( l.getDisplayCountry( ) );
                mArea.setText( String.format( Locale.getDefault( ), "%d", mRoomPosted.getSize( ) ) );
                mRent.setText( String.format( Locale.getDefault( ), "%d", mRoomPosted.getRent( ) ) );
                mRentPeriod.setText( String.format( Locale.getDefault( ), "%d", mRoomPosted.getPeriodOfRenting( ) ) );
                mDeposit.setText( String.format( Locale.getDefault( ), "%d", mRoomPosted.getDeposit( ) ) );


            }
        } );

        mRoomDescription.setText( mRoomPosted.getDescription( ) );
        mRoomLocation.setText( mRoomPosted.getCompleteAddress( ) );
        mRoomInternet.setChecked( mRoomPosted.isLaundry( ) );
        mRoomLaundry.setChecked( mRoomPosted.isLaundry( ) );
        mRoomFurnished.setChecked( mRoomPosted.isFurnished( ) );
        mRoomCommonArea.setChecked( mRoomPosted.isComonAreas( ) );


        Task<byte[]> t1 = ImageController.getProfilePicture( mRoomPosted.getLandlordID( ) );
        t1.addOnSuccessListener( new OnSuccessListener<byte[]>( ) {
            @Override
            public void onSuccess( byte[] bytes ) {
                Bitmap bmp = PictureConversion.byteArrayToBitmap( bytes );
                mPhotoLandlord.setImageBitmap( bmp );
            }
        } );
    }

    /**
     * display pictures in the fragment.
     *
     * @param bmp bitmap to be shown in the fragment.
     */

    private void showPicture( Bitmap bmp ) {
        int picturePosition = mRoomPictures.size( );
        if ( picturePosition < 10 ) {
           try {
               mPictures[picturePosition].setVisibility(View.VISIBLE);
               mPictures[picturePosition].setScaleType(ImageView.ScaleType.CENTER_CROP);
               mPictures[picturePosition].setImageBitmap(bmp);
               mRoomPictures.add(bmp);
           }catch(Exception e){Log.d("blabla", "not 2");}

        }
    }

    /**
     * Method that creates 10 task to read all possible pictures from the database.
     * Each task will upload a picture into the corresponding ImageView.
     */
    private void initializePictures( ) {
        Task[] t = ImageController.getAllRoomPicture( mRoomPosted.getRoomID( ) );
        for ( int i = 0 ; i < 9 ; i++ ) {
            synchronized ( this ) {
                final int k = i;
                t[ i ].addOnSuccessListener( new OnSuccessListener<byte[]>( ) {
                    @Override
                    public void onSuccess( final byte[] bytes ) {
                        Bitmap picture;
                        picture = PictureConversion.byteArrayToBitmap( bytes );
                       try{ mPictures[ k ].setImageBitmap( picture );
                        mPictures[ k ].setVisibility( View.VISIBLE );
                        showPicture( picture );

                        mPictures[ k ].setOnClickListener( new View.OnClickListener( ) {
                            @Override
                            public void onClick( View v ) {
                                FragmentTransaction ft = Objects.requireNonNull( getActivity( ) )
                                        .getSupportFragmentManager( ).beginTransaction( );
                                ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
                                OpenPictureFragment openPic = new OpenPictureFragment( );

                                Bundle bundle = new Bundle( );
                                bundle.putByteArray( OpenPictureFragment.KEY_PICTURE, bytes );
                                openPic.setArguments( bundle );
                                ft.replace( android.R.id.content, openPic );
                                ft.addToBackStack( null );
                                ft.commit( );
                            }
                        } );
                        }catch(Exception e){Log.d("blabla", "not 3");}
                    }
                } );
            }
        }

    }
}
