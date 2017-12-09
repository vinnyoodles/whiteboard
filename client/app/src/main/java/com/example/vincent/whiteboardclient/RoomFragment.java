package com.example.vincent.whiteboardclient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by vincent on 11/25/17.
 */

public class RoomFragment extends Fragment implements View.OnKeyListener {
    FragmentCallback cb;
    EditText userText;
    EditText roomText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        roomText = (EditText) view.findViewById(R.id.room_name);
        userText = (EditText) view.findViewById(R.id.user_name);

        roomText.setOnKeyListener(this);
        roomText.setOnKeyListener(this);
    }

    public void setCallback(FragmentCallback cb) {
        this.cb = cb;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN || keyCode != KeyEvent.KEYCODE_ENTER) {
            return false;
        }
        String user = "", room = "";
        if (roomText != null && view.getId() == roomText.getId()) {
            room = roomText.getText().toString();
            if (room == null || room.length() < 1)
                return false;

        }

        if (userText != null && view.getId() == userText.getId()) {
            user = roomText.getText().toString();
            if (user == null || user.length() < 1)
                return false;

        }
        cb.enterRoom(user, room);
        return true;
    }
}
