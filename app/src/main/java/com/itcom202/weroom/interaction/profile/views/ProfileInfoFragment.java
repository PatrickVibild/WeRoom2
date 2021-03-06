package com.itcom202.weroom.interaction.profile.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.itcom202.weroom.MainActivity;
import com.itcom202.weroom.R;
import com.itcom202.weroom.account.models.Profile;
import com.itcom202.weroom.account.onboarding.controllers.tagDescription.TagView;
import com.itcom202.weroom.framework.ProfileSingleton;
import com.itcom202.weroom.framework.cameraandgallery.PictureConversion;
import com.itcom202.weroom.framework.queries.ImageController;
import com.itcom202.weroom.interaction.InteractionActivity;

import java.util.Locale;
import java.util.Objects;

/**
 * Fragment that allows the user to see their profile information and navigate to setting and edit
 * their own profile and tenant/landlord profile, and in case of being a landlord it should also be able
 * to edit their rooms.
 */
public class ProfileInfoFragment extends Fragment {
    private Button mLogoutButton;
    private Button mSettingButton;
    private Button mEditButton;
    private Button mEditSubProfile;
    private Button mModifyRoom;
    private TextView mShowName;
    private TextView mShowAge;
    private TextView mShowRole;
    private TextView mShowNation;
    private TextView mShowGender;
    private ImageView mProfilePicture;
    private TagView mTag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_info, container, false);
        mLogoutButton = v.findViewById(R.id.profile_logout_button);
        mShowName = v.findViewById(R.id.profile_username);
        mShowAge = v.findViewById(R.id.profileshow_age);
        mShowRole = v.findViewById(R.id.profile_role);
        mShowNation = v.findViewById(R.id.profiles_nationatility);
        mShowGender = v.findViewById(R.id.profile_gender);
        mProfilePicture = v.findViewById(R.id.profile_profilePhoto);
        mTag = v.findViewById(R.id.profile_tags);
        mEditButton = v.findViewById(R.id.profile_edit_profile);
        mSettingButton = v.findViewById(R.id.profile_account_setting);
        mEditSubProfile = v.findViewById(R.id.modify_sub_profile);
        mModifyRoom = v.findViewById(R.id.modify_rooms);

        final Profile p = ProfileSingleton.getInstance();

        if (p.getRole().equals("Landlord")) {
            mEditSubProfile.setText(R.string.edit_landlord);
            mModifyRoom.setText(R.string.edit_rooms);
        } else {
            mModifyRoom.setVisibility(View.GONE);
            mEditSubProfile.setText(R.string.edit_tenant);
        }
        for (String s : p.getTags())
            mTag.addTag(s, false);

        mShowName.setText(p.getName());
        mShowAge.setText(String.format(Locale.getDefault(), "%d", p.getAge()));
        mShowRole.setText(p.getRole());

        Locale obj = new Locale("", p.getCountry());

        mShowNation.setText(obj.getDisplayCountry(Locale.ENGLISH));
        mShowGender.setText(p.getGender());

        Task<byte[]> t = ImageController.getProfilePicture(p.getUserID());

        t.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(final byte[] bytes) {
                mProfilePicture.setImageBitmap(PictureConversion.byteArrayToBitmap(bytes));
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(MainActivity.newIntent(getActivity()));
            }
        });
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InteractionActivity) Objects.requireNonNull(getActivity())).changeToProfileEditFragment();
            }
        });
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InteractionActivity) Objects.requireNonNull(getActivity())).changeToSettingFragment();
            }
        });
        mEditSubProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p.getRole().equals("Landlord")) {
                    ((InteractionActivity) Objects.requireNonNull(getActivity())).changeToLandlordEditFragment();
                } else {
                    ((InteractionActivity) Objects.requireNonNull(getActivity())).changeToTenantEditFragment();
                }
            }
        });

        mModifyRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InteractionActivity) Objects.requireNonNull(getActivity())).changeToRoomEditing();
            }
        });

        return v;
    }
}
