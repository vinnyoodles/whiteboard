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
    EditText roomText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        roomText = (EditText) view.findViewById(R.id.room_name);
        roomText.setOnKeyListener(this);
    }

    public void setCallback(FragmentCallback cb) {
        this.cb = cb;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (roomText != null && view.getId() == roomText.getId() &&
                event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            String name = roomText.getText().toString();
            if (name == null || name.length() < 1)
                return false;
            cb.enterRoom(name);
            return true;
        }
        return false;
    }
}
