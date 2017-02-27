package com.android.notebook.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.notebook.NoteBookApp;
import com.android.notebook.preferences.UserPreference;

public class BaseFragment extends Fragment {

    public NoteBookApp getApp() {
        return (NoteBookApp) getActivity().getApplication();
    }

    public UserPreference getUserPreference() {
        if (getActivity() instanceof BaseActivity) {
            return ((BaseActivity) getActivity()).getUserPreference();
        }

        return new UserPreference(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void finish() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).finish();
        }
    }
}
