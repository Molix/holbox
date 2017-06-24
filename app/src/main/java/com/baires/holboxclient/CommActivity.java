package com.baires.holboxclient;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baires.holboxclient.common.AppData;
import com.baires.holboxclient.common.Constants;
import com.baires.holboxclient.model.Establishment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommActivity extends AppCompatActivity  {

  private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm);

      FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
      String establishmentId = getIntent().getStringExtra("establishmentId");
      DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT+establishmentId);
      Log.d("onPostCreate", Constants.ESTABLISHMENT+establishmentId);
      ValueEventListener onChangeMe = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          Establishment establishment = dataSnapshot.getValue(Establishment.class);
          //Update this establishment in memory
          AppData.getMyInstance().setEstablishment(establishment);
          String notice = establishment.getNotice();
          Log.d("onPostCreate", establishment.toString());
          if (Constants.ANY.equals(notice)) { //Cancel call
            cancelCall();
          }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
          Log.e(Constants.ERROR, databaseError.getMessage());
        }
      };
      dbNoticeValue.addValueEventListener(onChangeMe);

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
          cancelCall();
        }
      });
    }

  private void cancelCall() {
    if (mp != null && mp.isPlaying()) {
      mp.stop();
    }
    String establishmentId = getIntent().getStringExtra("establishmentId");
    Intent returnIntent = new Intent();
    returnIntent.putExtra("establishmentId", establishmentId);
    setResult(Activity.RESULT_CANCELED, returnIntent);
    finish();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

      //Sound ringtone
    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    mp = MediaPlayer.create(getApplicationContext(), notification);
    mp.start();

    // timer count down to cancel calling
    CountDownTimer timerCountDown = new CountDownTimer(30000, 30000) {
      @Override
      public void onTick(long millisUntilFinished) {
      }
      @Override
      public void onFinish() {
        cancelCall();
      }
    };
    timerCountDown.start();
  }
}
