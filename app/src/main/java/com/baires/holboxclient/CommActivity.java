package com.baires.holboxclient;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class CommActivity extends AppCompatActivity  {

  private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       Button answercall = (Button) findViewById(R.id.answercall);
      answercall.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if(mp!=null&&mp.isPlaying()) {
            mp.stop();
          }
          String establishmentId = getIntent().getStringExtra("establishmentId");
          Intent returnIntent = new Intent();
          returnIntent.putExtra("establishmentId",establishmentId);
          setResult(Activity.RESULT_OK,returnIntent);
          finish();
        }
      });

      Button rejectcall = (Button) findViewById(R.id.rejectcall);
      rejectcall.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if(mp!=null&&mp.isPlaying()) {
            mp.stop();
          }
          String establishmentId = getIntent().getStringExtra("establishmentId");
          Intent returnIntent = new Intent();
          returnIntent.putExtra("establishmentId",establishmentId);
          setResult(Activity.RESULT_CANCELED, returnIntent);
          finish();
        }
      });
    }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    //Sound ringtone
    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    mp = MediaPlayer.create(getApplicationContext(), notification);
    mp.start();
  }
}
