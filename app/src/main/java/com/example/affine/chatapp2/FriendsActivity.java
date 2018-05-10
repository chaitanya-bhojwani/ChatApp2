package com.example.affine.chatapp2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    RecyclerView friendList;
    FriendsListAdapter adapter;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        friendList = findViewById(R.id.friendList);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsListAdapter();
        friendList.setAdapter(adapter);

        preferenceManager = new PreferenceManager(this);
        adapter.setOnItemClickListener(new FriendsListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(UserInfo userInfo) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", userInfo);
                ActivityChangeUtil.change(FriendsActivity.this, MainActivity.class, bundle);
            }
        });

        if (preferenceManager.getId() == -1) {
            showDialog();
        } else {
            int id = preferenceManager.getId();
            List<UserInfo> userInfos = Lists.newArrayList();

            for (int i = 1; i <= 5; i++) {
                if (i == id)
                    continue;
                UserInfo userInfo = new UserInfo();
                userInfo.setId(i);
                userInfo.setName("Name" + i);
                userInfos.add(userInfo);
            }
            adapter.setmDataset(userInfos);
        }

    }

    private void showDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userId = (EditText) promptsView
                .findViewById(R.id.id);

        final EditText userName = (EditText) promptsView
                .findViewById(R.id.name);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int idddd = Integer.valueOf(userId.getText().toString());
                                preferenceManager.setId(idddd);
                                preferenceManager.setName(userName.getText().toString());

                                List<UserInfo> userInfos = Lists.newArrayList();


                                for (int i = 1; i <= 5; i++) {
                                    if (i == idddd)
                                        continue;
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.setId(i);
                                    userInfo.setName("Name" + i);
                                    userInfos.add(userInfo);
                                }
                                adapter.setmDataset(userInfos);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
