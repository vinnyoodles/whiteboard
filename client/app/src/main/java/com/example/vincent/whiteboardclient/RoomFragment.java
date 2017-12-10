package com.example.vincent.whiteboardclient;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by vincent on 11/25/17.
 */

public class RoomFragment extends Fragment {
    FragmentCallback cb;
    EditText userText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        userText = (EditText) view.findViewById(R.id.user_name);

        userText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (isEnter(s)) {
                    userText.setText(userText.getText().toString().replace("\n", ""));
                    String user = userText.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    cb.enterRoom(user);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    public boolean isEnter(CharSequence s) {
        return s.length()>0 && s.subSequence(s.length()-1, s.length()).toString().equalsIgnoreCase("\n");
    }

    public void setCallback(FragmentCallback cb) {
        this.cb = cb;
    }
}
