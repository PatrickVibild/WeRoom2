package com.itcom202.weroom.interaction.swipe.views;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itcom202.weroom.framework.ProfileSingleton;
import com.itcom202.weroom.R;
import com.itcom202.weroom.framework.DataBasePath;
import com.itcom202.weroom.account.models.Profile;
import com.itcom202.weroom.account.models.RoomPosted;
import com.itcom202.weroom.interaction.swipe.controllers.adapters.ListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import swipeable.com.layoutmanager.OnItemSwiped;
import swipeable.com.layoutmanager.SwipeableLayoutManager;
import swipeable.com.layoutmanager.SwipeableTouchHelperCallback;
import swipeable.com.layoutmanager.touchelper.ItemTouchHelper;

public class SwipeFragment extends Fragment {
    public static final String KEY_TENANT_LIST = "tenant_list";
    public static final String KEY_ROOM_LIST_LANDLORD = "room_list_landlord";
    public static final String KEY_ROOM_LIST_ALL = "room_list_all";

    private static final String TAG = "Swipe";
    private ListAdapter mCurrentAdapter;
    public static Fragment thisFragment;
    private ArrayList<Profile> mTenantProfiles;
    private ArrayList<RoomPosted> mLandlordsRooms;
    private ArrayList<RoomPosted> mAllRooms;
    private RoomPosted mCurrentSelectedRoom;
    private Profile mThisProfile;
    private ImageButton mRightButton;
    private ImageButton mLeftButton;
    private List<ListAdapter> mLandlordsAdapter = new ArrayList<>();
    private RecyclerView mRecyclerView;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_swipe, container, false);

        mThisProfile = ProfileSingleton.getInstance();

        TabLayout tabLayout = v.findViewById(R.id.tab_layout_swipe);
        mRightButton = v.findViewById(R.id.likeButton);
        mLeftButton = v.findViewById(R.id.dislikeButton);

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRightAction();
            }
        });
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLeftAction();
            }
        });
        if (ProfileSingleton.getInstance().getRole().equals("Landlord" ) && getArguments() != null){
            mTenantProfiles = getArguments().getParcelableArrayList(KEY_TENANT_LIST);
            mLandlordsRooms = getArguments().getParcelableArrayList(KEY_ROOM_LIST_LANDLORD);
            List<String> rooms = getRoomsStrings();
            mCurrentSelectedRoom = mLandlordsRooms.get(0);

            for (String s : rooms){
                tabLayout.addTab(tabLayout.newTab().setText(s));
                mLandlordsAdapter.add( new ListAdapter(mTenantProfiles, mLandlordsRooms,mAllRooms, mThisProfile));
            }
            mCurrentAdapter = mLandlordsAdapter.get(0);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    mCurrentSelectedRoom = mLandlordsRooms.get(tab.getPosition());
                    //mCurrentAdapter = mLandlordsAdapter.get(tab.getPosition());
                    mRecyclerView.swapAdapter(mLandlordsAdapter.get(tab.getPosition()), false);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        if (ProfileSingleton.getInstance().getRole().equals("Tenant") && getArguments() != null){
            mAllRooms = getArguments().getParcelableArrayList(KEY_ROOM_LIST_ALL);
            tabLayout.setVisibility(View.GONE);
            mCurrentAdapter = new ListAdapter(mTenantProfiles, mLandlordsRooms,mAllRooms, mThisProfile);
        }

        thisFragment = this;

        mRecyclerView = v.findViewById(R.id.recycler_view);
        SwipeableTouchHelperCallback swipeableTouchHelperCallback =
                new SwipeableTouchHelperCallback(new OnItemSwiped() {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    @Override
                    public void onItemSwiped() {

                    }
                    @Override
                    public void onItemSwipedLeft() {
                        swipeLeftAction();
                    }

                    @Override
                    public void onItemSwipedRight() {
                        swipeRightAction();
                    }

                    @Override
                    public void onItemSwipedUp() {
                        Log.d(TAG, "UP");

                    }

                    @Override
                    public void onItemSwipedDown() {
                        Log.d(TAG, "DOWN");
                    }
                }) {
                    @Override
                    public int getAllowedSwipeDirectionsMovementFlags(RecyclerView.ViewHolder viewHolder) {
                        return ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT ;
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeableTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setLayoutManager(new SwipeableLayoutManager().setAngle(10)
                .setAnimationDuratuion(450)
                .setMaxShowCount(3)
                .setScaleGap(0.1f)
                .setTransYGap(0));
        mRecyclerView.setAdapter(mCurrentAdapter);

        return v;
    }



    public void goToInformationFragment(){
        final CardInfoTenantFragment cardInfoTenantFragment = new CardInfoTenantFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, cardInfoTenantFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //update whatever your list
        mCurrentAdapter.notifyDataSetChanged();
    }

    private List<String> getRoomsStrings(){
        List<String> roomsName = new ArrayList<>();
        for (RoomPosted p : mLandlordsRooms)
            roomsName.add(p.getCompleteAddress());

        return roomsName;
    }
    private void swipeRightAction(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "RIGHT");
        //this is when we swipe a room.
        if (mCurrentAdapter.returnTopItemID().length() == 36 ){
            mThisProfile.getMatch().addLiked(mCurrentAdapter.returnTopItemID());
            ProfileSingleton.update(mThisProfile);

            RoomPosted room = mCurrentAdapter.returnTopRoom();

            room.getMatch().addExternalLikes(mThisProfile.getUserID());

            db.collection(DataBasePath.ROOMS.getValue())
                    .document(room.getRoomID())
                    .set(room);
        }//this when we swipe a tenant.
        else{

            mCurrentSelectedRoom.getMatch().addLiked(mCurrentAdapter.returnTopItemID());
            db.collection(DataBasePath.ROOMS.getValue())
                    .document(mCurrentSelectedRoom.getRoomID())
                    .set(mCurrentSelectedRoom);
            Profile p = mCurrentAdapter.returnTopTenant();
            p.getMatch().addExternalLikes(mCurrentSelectedRoom.getRoomID());
            db.collection(DataBasePath.USERS.getValue())
                    .document(p.getUserID())
                    .set(p);
        }
        mCurrentAdapter.removeTopItem();
    }
    private void swipeLeftAction(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "LEFT");
        //this when we swipe a room.
        if (mCurrentAdapter.returnTopItemID().length() == 36 ){
            mThisProfile.getMatch().addDislike(mCurrentAdapter.returnTopItemID());
            ProfileSingleton.update(mThisProfile);
        }//this when we swipe a tenant.
        else{
            mCurrentSelectedRoom.getMatch().addDislike(mCurrentAdapter.returnTopItemID());
            db.collection(DataBasePath.ROOMS.getValue())
                    .document(mCurrentSelectedRoom.getRoomID())
                    .set(mCurrentSelectedRoom);
            Profile p = mCurrentAdapter.returnTopTenant();
            p.getMatch().addExternalLikes(mCurrentSelectedRoom.getRoomID());
            db.collection(DataBasePath.USERS.getValue())
                    .document(p.getUserID())
                    .set(p);
        }
        mCurrentAdapter.removeTopItem();
    }
}