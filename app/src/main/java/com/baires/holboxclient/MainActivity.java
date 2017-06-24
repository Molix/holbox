package com.baires.holboxclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baires.holboxclient.common.AppData;
import com.baires.holboxclient.common.Constants;
import com.baires.holboxclient.model.Establishment;
import com.baires.holboxclient.videoconference.ConnectActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.baires.holboxclient.R.string.NombreSistema;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton alarmButton = (ImageButton) findViewById(R.id.alarm_button);
        ImageButton commButton = (ImageButton) findViewById(R.id.comm_button);
        ImageButton camsButton = (ImageButton) findViewById(R.id.cams_button);
        alarmButton.setOnClickListener(this);
        commButton.setOnClickListener(this);
        camsButton.setOnClickListener(this);

      // load the id locally
      SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
      final Integer appId = sharedPref.getInt(getString(R.string.app_id), 0);
      Log.d("LOCAL_ID:", appId.toString());

      // first time execution
      if(appId==0) {
        installSettings();
      }else{ // not first time execution (appId!=0)
        setupEstablishment(appId);
      }

    }

  private void installSettings() {
    FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
    DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.FREE_ID);
    dbFreeIdValue.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Integer id = dataSnapshot.getValue(Integer.class); // remote Id
        id++;
        Log.d("REMOTE_ID:", id.toString());
        AppData.getMyInstance().setMaxId(id);

        // save the id locally
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.app_id), id);
        editor.commit();

        // save remote used id
        FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
        DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.FREE_ID);
        dbFreeIdValue.setValue(id);

        // add new establishment
        Establishment establishment = new Establishment();
        establishment.setId(id);
        dbHolbox.getReference().child("establishment" + id).setValue(establishment);

        setupEstablishment(id);

      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());

      }
    });
  }

  private void setupEstablishment(Integer appId) {
    TextView tv = (TextView) findViewById(R.id.sample_text);
    tv.setText(NombreSistema);
    tv.setText(tv.getText().toString()+appId);

    // save in singleton the id
    AppData.getMyInstance().setEstablishmentId(appId.toString());

    Log.d("setupEstablishment", "appId:"+appId.toString());
    // get data of specific own establishment
    FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
    loadOwnEstablishment(appId, dbHolbox);

    listenEstablishmentCalls(dbHolbox);

    // Listen this establishment to get some change and the response
    listenAnswerCall(appId, dbHolbox);

  }

  private void listenAnswerCall(Integer appId, FirebaseDatabase dbHolbox) {
    DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT+appId);
    Log.d("setupEstablishment", Constants.ESTABLISHMENT+appId);
    ValueEventListener onChangeMe = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Establishment establishment = dataSnapshot.getValue(Establishment.class);
        //Update this establishment in memory
        AppData.getMyInstance().setEstablishment(establishment);
        String notice = establishment.getNotice();
        Log.d("setupEstablishment", establishment.toString());
        if (Constants.RESPONDING.equals(notice)) {

          ImageView img = (ImageView) findViewById(R.id.comm_button);
          img.setImageResource(R.drawable.operator);

          FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
          DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT+establishment.getId());

          // The next 2 lines change on firebase ref and fire this event?
          establishment.setNotice(Constants.ANY);
          Log.d("setupEstablishment",Constants.ESTABLISHMENT+establishment.getId()+ "<-"+establishment);
          dbNoticeValue.setValue(establishment); // change the state of bd

          Log.d("setupEstablishment",Constants.CALLING+ " videoChat:"+establishment.getId());
          videoChat(establishment.getId().toString());
        }else if (Constants.ANY.equals(notice)) { // cancel call
          if(mp!=null&&mp.isPlaying()){
            mp.stop();
          }
          ImageView img = (ImageView) findViewById(R.id.comm_button);
          img.setImageResource(R.drawable.operator);

        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());

      }
    };
    dbNoticeValue.addValueEventListener(onChangeMe);
  }

  private void listenEstablishmentCalls(FirebaseDatabase dbHolbox) {
    DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.MAX_ID);
    dbFreeIdValue.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Integer maxId = dataSnapshot.getValue(Integer.class);
        if(maxId<AppData.getMyInstance().getMaxId()) {
          // update max id
          maxId=AppData.getMyInstance().getMaxId();
          Log.d("setupEstablishment", "Update max id:"+maxId.toString());
          FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
          DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.MAX_ID);
          dbFreeIdValue.setValue(maxId);
        }else{
          AppData.getMyInstance().setMaxId(maxId);
        }
        Log.d("setupEstablishment", "MAX_ID:"+maxId.toString());

        // Activate the listeners for Answer call (control center)
        for(int i=1;i<=maxId;i++) {

          if(AppData.getMyInstance().getEstablishmentId()!=String.valueOf(i)) {
            listenEstablishmentCallFrom(i);
          }

        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());
      }
    });
  }

  private void listenEstablishmentCallFrom(int i) {
    FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
    Log.d("setupEstablishment", Constants.ESTABLISHMENT + String.valueOf(i));
    DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT + String.valueOf(i));
    dbNoticeValue.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Establishment establishment = dataSnapshot.getValue(Establishment.class);
        String notice = establishment.getNotice();
        String id =String.valueOf(establishment.getId());
        Log.d("setupEstablishment", "id:"+id+" notice:"+notice);
        if (Constants.CALLING.equals(notice)) {

          videoChatCallingFrom(establishment);

        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());
      }
    });
  }



  private void loadOwnEstablishment(Integer appId, FirebaseDatabase dbHolbox) {
    DatabaseReference dbFreeAddrValue = dbHolbox.getReference(Constants.ESTABLISHMENT+appId);
    dbFreeAddrValue.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Establishment establishment = dataSnapshot.getValue(Establishment.class);
        Log.d("setupEstablishment", establishment.toString());
        //Initializing start values
        establishment.setInCalling(false);
        establishment.setNotice(Constants.ANY);
        AppData.getMyInstance().setEstablishment(establishment);
        FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
        DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT+establishment.getId());
        Log.d("setupEstablishment",Constants.ESTABLISHMENT+establishment.getId()+"<-"+establishment);
        // update remote initial calling state
        dbNoticeValue.setValue(establishment);
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());
      }
    });
  }


  public void videoChatCallingFrom(Establishment establishment){

    Intent intent1 = new Intent(this, CommActivity.class);
    intent1.putExtra("establishmentId", establishment.getId().toString());
    startActivityForResult(intent1, 1);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 1) {
      if(resultCode == Activity.RESULT_OK){
        if(data!=null) {
          String establishmentId = data.getStringExtra("establishmentId");

          AppData.getMyInstance().setCallId(establishmentId);

          FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
          DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT + establishmentId+"/"+ Constants.NOTICE);
          Log.d("setupEstablishment",Constants.ESTABLISHMENT+ establishmentId);
          dbNoticeValue.setValue(Constants.RESPONDING);
          Log.d("setupEstablishment", Constants.RESPONDING+" videoChat:"+establishmentId);

          final AppCompatActivity context = this;

          // ask if it isn't busy
          DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.ESTABLISHMENT+establishmentId);
          dbFreeIdValue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              Establishment establishmentFb = dataSnapshot.getValue(Establishment.class);

              if(!establishmentFb.getInCalling()&&!AppData.getMyInstance().getEstablishment().getInCalling()) {

                // save in singleton the state inCalling
                AppData.getMyInstance().getEstablishment().setInCalling(true);
                // change remote inCalling state
                establishmentFb.setInCalling(true);
                // update remote calling state
                FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
                DatabaseReference dbNoticeValue = dbHolbox.getReference( Constants.ESTABLISHMENT+establishmentFb.getId());
                Log.d("videoChatCallingFrom",Constants.ESTABLISHMENT+establishmentFb.getId()+ "<-"+establishmentFb);
                dbNoticeValue.setValue(establishmentFb);

                Log.d("onActivityResult", "call:" + establishmentFb.getId());
                ConnectActivity conn = new ConnectActivity();
                conn.call(context, establishmentFb.getId().toString());
                //startActivity(intent1);
              }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
              Log.e(Constants.ERROR, databaseError.getMessage());

            }
          });

        }
      }
      if (resultCode == Activity.RESULT_CANCELED) {
        if(data!=null) {
          String establishmentId = data.getStringExtra("establishmentId");
          Log.d("onActivityResult", "cancel call:" + establishmentId);
          FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
          DatabaseReference dbNoticeValue = dbHolbox.getReference(Constants.ESTABLISHMENT + establishmentId+"/"+ Constants.NOTICE);
          Log.d("setupEstablishment",Constants.ESTABLISHMENT+ establishmentId);
          dbNoticeValue.setValue(Constants.ANY);
        }
      }
    }


  }

  public void videoChat(String establishmentId){
    AppData.getMyInstance().setCallId(establishmentId);

    final AppCompatActivity context = this;

    // ask if it isn't busy
    FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
    DatabaseReference dbFreeIdValue = dbHolbox.getReference(Constants.ESTABLISHMENT+establishmentId);
    dbFreeIdValue.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Establishment establishmentFb = dataSnapshot.getValue(Establishment.class);

        if(!establishmentFb.getInCalling()&&!AppData.getMyInstance().getEstablishment().getInCalling()) {

          // save in singleton the state inCalling
          AppData.getMyInstance().getEstablishment().setInCalling(true);
          // change remote inCalling state
          establishmentFb.setInCalling(true);
          // update remote calling state
          FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
          DatabaseReference dbNoticeValue = dbHolbox.getReference( Constants.ESTABLISHMENT+establishmentFb.getId());
          Log.d("videoChat",Constants.ESTABLISHMENT+establishmentFb.getId()+ "<-"+establishmentFb);
          dbNoticeValue.setValue(establishmentFb);

          Log.d("videoChat", "call:"+establishmentFb.getId());
          ConnectActivity conn = new ConnectActivity();
          conn.call(context, establishmentFb.getId().toString());
        }

      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e(Constants.ERROR, databaseError.getMessage());

      }
    });
  }

  private String getEstablishmentName(){
    return AppData.getMyInstance().getEstablishmentName();
  }
  @Override
  public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_button:
                Intent intent1 = new Intent(this, AlarmActivity.class);
                startActivity(intent1);
                break;
            case R.id.comm_button:

              if(Constants.CALLING.equals(AppData.getMyInstance().getEstablishment().getNotice())){
                if(mp!=null&&mp.isPlaying()) {
                  mp.stop();
                }
                ImageView img= (ImageView) findViewById(R.id.comm_button);
                img.setImageResource(R.drawable.operator);
                FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
                DatabaseReference dbNoticeValue = dbHolbox.getReference(getEstablishmentName());
                Establishment establishment= AppData.getMyInstance().getEstablishment();
                Log.d("onClick",getEstablishmentName()+ "<-"+establishment);
                establishment.setNotice(Constants.ANY);
                dbNoticeValue.setValue(establishment);

              }else {
                FirebaseDatabase dbHolbox = FirebaseDatabase.getInstance();
                DatabaseReference dbNoticeValue = dbHolbox.getReference(getEstablishmentName());
                Establishment establishment= AppData.getMyInstance().getEstablishment();
                establishment.setNotice(Constants.CALLING);
                Log.d("onClick",getEstablishmentName()+ "<-"+establishment);
                dbNoticeValue.setValue(establishment);

                //paint button red and play sound ring...
                ImageView img= (ImageView) findViewById(R.id.comm_button);
                img.setImageResource(R.drawable.operator_calling);
                //Sound ringtone
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                mp = MediaPlayer.create(getApplicationContext(), notification);
                mp.start();

              }

                break;
            case R.id.cams_button:
                Intent intent3 = new Intent(this, CamsActivity.class);
                startActivity(intent3);
                break;
            default:
                break;
        }

    }
  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public native String stringFromJNI();

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }
}
