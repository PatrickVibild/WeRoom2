package com.itcom202.weroom.services;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.itcom202.weroom.R;
import com.itcom202.weroom.account.models.Match;
import com.itcom202.weroom.account.models.Profile;
import com.itcom202.weroom.account.models.RoomPosted;
import com.itcom202.weroom.framework.DataBasePath;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class NotificationService extends Service {
    public final static String KEY_MATCH = "match";
    public final static String KEY_IDS = "ids";
    private static final String ACTION_START_SERVICE = "match_service";
    public static boolean isServiceRunning = false;
    private final String TAG = "NotificationService";
    private ArrayList<Match> mMatches;
    private ArrayList<String> mIds;
    private List<ListenerRegistration> mRegistration = new ArrayList<>( );
    private NotificationManager mNotificationManager;

    public NotificationService( ) {
    }

    public static Intent getIntent( Activity activity, ArrayList<Match> matches, ArrayList<String> ids ) {
        Intent intent = new Intent( activity, NotificationService.class );
        intent.putExtra( KEY_MATCH, matches );
        intent.putExtra( KEY_IDS, ids );
        intent.setAction( ACTION_START_SERVICE );

        return intent;
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }


    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        if ( intent != null && intent.getAction( ).equals( ACTION_START_SERVICE ) ) {
            startServiceWithNotification( );
        } else stopMyService( );
        //getting systems default ringtone
        if ( intent != null ) {
            try{
                mMatches = ( ArrayList<Match> ) intent.getExtras( ).get( KEY_MATCH );
                mIds = ( ArrayList<String> ) intent.getExtras( ).get( KEY_IDS );
            }catch(ClassCastException e){
                Log.e(TAG,"Error on service intent : " +e);
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance( );
            //rooms case.

            try {
                mNotificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

                if ( mIds.get( 0 ).length( ) == 36 ) {
                    for ( int i = 0 ; i < mIds.size( ) ; i++ ) {
                        final int k = i;
                        DocumentReference docRef = db.collection( DataBasePath.ROOMS.getValue( ) )
                                .document( mIds.get( i ) );
                        mRegistration.add( docRef.addSnapshotListener( new EventListener<DocumentSnapshot>( ) {
                            @Override
                            public void onEvent( @Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e ) {
                                if ( documentSnapshot != null ) {
                                    RoomPosted r = documentSnapshot.toObject( RoomPosted.class );
                                    if ( r != null ) {
                                        Match newMatch = r.getMatch( );
                                        newMatch.getMatch( ).removeAll( mMatches.get( k ).getMatch( ) );
                                        for ( String s : newMatch.getMatch( ) ) {
                                            mMatches.get( k ).addLiked( s );
                                            mMatches.get( k ).addExternalLikes( s );
                                            createNotification( );
                                        }
                                    }
                                }

                            }
                        } ) );
                    }
                }//tenant case.
                else {
                    DocumentReference docRef = db.collection( DataBasePath.USERS.getValue( ) )
                            .document( mIds.get( 0 ) );
                    mRegistration.add( docRef.addSnapshotListener( new EventListener<DocumentSnapshot>( ) {
                        @Override
                        public void onEvent( @Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e ) {
                            if ( documentSnapshot != null ) {
                                Profile p = documentSnapshot.toObject( Profile.class );
                                Match newMatch;
                                if ( p != null ) {
                                    newMatch = p.getMatch( );
                                    newMatch.getMatch( ).removeAll( mMatches.get( 0 ).getMatch( ) );
                                    for ( String s : newMatch.getMatch( ) ) {
                                        mMatches.get( 0 ).addLiked( s );
                                        mMatches.get( 0 ).addExternalLikes( s );
                                        createNotification( );
                                    }
                                }
                            }
                        }
                    } ) );

                }
            } catch ( Exception e ) {
                Log.d( TAG, "Exception in NotificationService" + e.toString( ) );
            }
        }

        //we have some options for service
        //start sticky means service will be explicity started and stopped
        return START_STICKY;
    }

    @Override
    public void onDestroy( ) {
        Log.d( TAG, "NotificationService Destroyed" );
        super.onDestroy( );
        for ( ListenerRegistration lr : mRegistration )
            lr.remove( );
    }

    /**
     * Creates a notification in the phone announcing the user that a new match has been created.
     */
    private void createNotification( ) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( this, "Tortuga" );
        mBuilder.setSmallIcon( R.mipmap.ic_launcher );
        mBuilder.setContentTitle( "New match on WeRoom" )
                .setContentText( "You got a new match, you can now chat with someone more!!!" )
                .setAutoCancel( false )
                .setSound( Settings.System.DEFAULT_NOTIFICATION_URI );


        NotificationManager mNotificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
            String NOTIFICATION_CHANNEL_ID = "1001";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance );
            notificationChannel.enableLights( true );
            notificationChannel.setLightColor( Color.RED );
            notificationChannel.enableVibration( true );
            notificationChannel.setVibrationPattern( new long[]{ 100, 200, 300, 400, 500, 400, 300, 200, 400 } );
            assert mNotificationManager != null;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID );
            mNotificationManager.createNotificationChannel( notificationChannel );
        }
        assert mNotificationManager != null;
        mNotificationManager.notify( 0 /* Request Code */, mBuilder.build( ) );
    }

    //Source: https://stackoverflow.com/questions/42126979/cannot-keep-android-service-alive-after-app-is-closed

    /**
     * Starts the Service notification and display a new notification for debugging porpoise.
     */
    void startServiceWithNotification( ) {
        if ( isServiceRunning ) return;
        isServiceRunning = true;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( this, "Tortuga" );
        mBuilder.setSmallIcon( R.mipmap.ic_launcher );
        mBuilder.setContentTitle( "WeRoom debug" )
                .setContentText( "Notification service is running." )
                .setAutoCancel( false )
                .setSound( Settings.System.DEFAULT_NOTIFICATION_URI );


        NotificationManager mNotificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
            String NOTIFICATION_CHANNEL_ID = "1001";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance );
            notificationChannel.enableLights( true );
            notificationChannel.setLightColor( Color.RED );
            notificationChannel.enableVibration( true );
            notificationChannel.setVibrationPattern( new long[]{ 100, 200, 300, 400, 500, 400, 300, 200, 400 } );
            assert mNotificationManager != null;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID );
            mNotificationManager.createNotificationChannel( notificationChannel );
        }
        assert mNotificationManager != null;
        mNotificationManager.notify( 0 /* Request Code */, mBuilder.build( ) );
    }

    void stopMyService( ) {
        Log.d( TAG, "NotificationService been stop!!!" );
        stopForeground( true );
        stopSelf( );
        isServiceRunning = false;
    }
}
