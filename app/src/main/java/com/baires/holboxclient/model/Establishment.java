package com.baires.holboxclient.model;

/**
 * Created by alpo25 on 2/6/2017.
 */

public class Establishment {

  private String address= " ";
  private String adminPhone= " ";
  private String notice= "ANY";
  private Integer id;
  private Boolean inCalling= false;

  public Boolean getInCalling() {
    return inCalling;
  }

  public void setInCalling(Boolean inCalling) {
    this.inCalling = inCalling;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAdminPhone() {
    return adminPhone;
  }

  public void setAdminPhone(String adminPhone) {
    this.adminPhone = adminPhone;
  }

  public String getNotice() {
    return notice;
  }

  public void setNotice(String notice) {
    this.notice = notice;
  }

  @Override
  public String toString() {
    return "Establishment{" +
      "address='" + address + '\'' +
      ", adminPhone='" + adminPhone + '\'' +
      ", notice='" + notice + '\'' +
      ", id=" + id +
      ", inCalling=" + inCalling +
      '}';
  }
}
