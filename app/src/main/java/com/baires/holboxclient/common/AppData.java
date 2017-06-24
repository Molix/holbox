package com.baires.holboxclient.common;

import android.util.Log;

import com.baires.holboxclient.model.Establishment;

/**
 * Created by alpo2 on 25/5/2017.
 */

public class AppData {

  private String establishmentId="0";
  private Integer maxId=0;
  private String callId="0";
  private Establishment establishment =null;

  private static AppData myInstance=null;
  private AppData(){}

  public static synchronized AppData getMyInstance() {
    if(myInstance == null){
      myInstance = new AppData();
    }
    return myInstance;
  }

  public String getCallId() {
    return callId;
  }

  public void setCallId(String callId) {
    this.callId = callId;
  }

  public Integer getMaxId() {
    return maxId;
  }

  public void setMaxId(Integer maxId) {
    this.maxId = maxId;
  }

  public String getEstablishmentId() {
    return establishmentId;
  }

  public void setEstablishmentId(String establishmentId) {
    this.establishmentId = establishmentId;
  }

  public Establishment getEstablishment() {
    return establishment;
  }

  public void setEstablishment(Establishment establishment) {
    this.establishment = establishment;
  }

  public String getEstablishmentName() {
    Log.d("DEBUG", Constants.ESTABLISHMENT+getEstablishmentId());
    return Constants.ESTABLISHMENT+getEstablishmentId();
  }
}
