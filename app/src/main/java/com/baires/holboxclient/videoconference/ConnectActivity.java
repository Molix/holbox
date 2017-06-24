/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.baires.holboxclient.videoconference;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.baires.holboxclient.R;
import com.baires.holboxclient.common.Constants;

import java.util.ArrayList;
import java.util.Random;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class  ConnectActivity {
  private static final String TAG = "ConnectActivity";
  private static final int CONNECTION_REQUEST = 1;
  private static final int REMOVE_FAVORITE_INDEX = 0;
  private static boolean commandLineRun = false;

  private ImageButton connectButton;
  private ImageButton addFavoriteButton;
  private EditText roomEditText;
  private ListView roomListView;
  private SharedPreferences sharedPref;
  private String keyprefVideoCallEnabled;
  private String keyprefScreencapture;
  private String keyprefCamera2;
  private String keyprefResolution;
  private String keyprefFps;
  private String keyprefCaptureQualitySlider;
  private String keyprefVideoBitrateType;
  private String keyprefVideoBitrateValue;
  private String keyprefVideoCodec;
  private String keyprefAudioBitrateType;
  private String keyprefAudioBitrateValue;
  private String keyprefAudioCodec;
  private String keyprefHwCodecAcceleration;
  private String keyprefCaptureToTexture;
  private String keyprefNoAudioProcessingPipeline;
  private String keyprefAecDump;
  private String keyprefOpenSLES;
  private String keyprefDisableBuiltInAec;
  private String keyprefDisableBuiltInAgc;
  private String keyprefDisableBuiltInNs;
  private String keyprefEnableLevelControl;
  private String keyprefDisplayHud;
  private String keyprefTracing;
  private String keyprefRoomServerUrl;
  private String keyprefRoom;
  private String keyprefRoomList;
  private ArrayList<String> roomList;
  private ArrayAdapter<String> adapter;
  private String keyprefEnableDataChannel;
  private String keyprefOrdered;
  private String keyprefMaxRetransmitTimeMs;
  private String keyprefMaxRetransmits;
  private String keyprefDataProtocol;
  private String keyprefNegotiated;
  private String keyprefDataId;
  private AppCompatActivity context;




  /**
   * Get a value from the shared preference or from the intent, if it does not
   * exist the default is used.
   */
  private String sharedPrefGetString(
      int attributeId, String intentName, int defaultId, boolean useFromIntent) {
    String defaultValue = context.getString(defaultId);
    if (useFromIntent) {
      String value =  context.getIntent().getStringExtra(intentName);
      if (value != null) {
        return value;
      }
      return defaultValue;
    } else {
      String attributeName = context.getString(attributeId);
      return sharedPref.getString(attributeName, defaultValue);
    }
  }

  /**
   * Get a value from the shared preference or from the intent, if it does not
   * exist the default is used.
   */
  private boolean sharedPrefGetBoolean(
      int attributeId, String intentName, int defaultId, boolean useFromIntent) {
    boolean defaultValue = Boolean.valueOf (context.getString(defaultId));
    if (useFromIntent) {
      return  context.getIntent().getBooleanExtra(intentName, defaultValue);
    } else {
      String attributeName = context.getString(attributeId);
      return sharedPref.getBoolean(attributeName, defaultValue);
    }
  }

  /**
   * Get a value from the shared preference or from the intent, if it does not
   * exist the default is used.
   */
  private int sharedPrefGetInteger(
      int attributeId, String intentName, int defaultId, boolean useFromIntent) {
    String defaultString = context.getString(defaultId);
    int defaultValue = Integer.parseInt(defaultString);
    if (useFromIntent) {
      return  context.getIntent().getIntExtra(intentName, defaultValue);
    } else {
      String attributeName = context.getString(attributeId);
      String value = sharedPref.getString(attributeName, defaultString);
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
        return defaultValue;
      }
    }
  }

  private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
      boolean useValuesFromIntent, int runTimeMs) {
    this.commandLineRun = commandLineRun;

    // roomId is random for loopback.
    if (loopback) {
      roomId = Integer.toString((new Random()).nextInt(100000000));
    }

    String roomUrl = sharedPref.getString(
        keyprefRoomServerUrl, context.getString(R.string.pref_room_server_url_default));

    // Video call enabled flag.
    boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
        CallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

    // Use screencapture option.
    boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
        CallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

    // Use Camera2 option.
    boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, CallActivity.EXTRA_CAMERA2,
        R.string.pref_camera2_default, useValuesFromIntent);

    // Get default codecs.
    String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
        CallActivity.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
    String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
        CallActivity.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

    // Check HW codec flag.
    boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
        CallActivity.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

    // Check Capture to texture.
    boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
        CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
        useValuesFromIntent);

    // Check Disable Audio Processing flag.
    boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
        CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
        useValuesFromIntent);

    // Check Disable Audio Processing flag.
    boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
        CallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

    // Check OpenSL ES enabled flag.
    boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
        CallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

    // Check Disable built-in AEC flag.
    boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
        CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
        useValuesFromIntent);

    // Check Disable built-in AGC flag.
    boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
        CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
        useValuesFromIntent);

    // Check Disable built-in NS flag.
    boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
        CallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
        useValuesFromIntent);

    // Check Enable level control.
    boolean enableLevelControl = sharedPrefGetBoolean(R.string.pref_enable_level_control_key,
        CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key,
        useValuesFromIntent);

    // Get video resolution from settings.
    int videoWidth = 0;
    int videoHeight = 0;
    if (useValuesFromIntent) {
      videoWidth =  context.getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
      videoHeight =  context.getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
    }
    if (videoWidth == 0 && videoHeight == 0) {
      String resolution =
          sharedPref.getString(keyprefResolution, context.getString(R.string.pref_resolution_default));
      String[] dimensions = resolution.split("[ x]+");
      if (dimensions.length == 2) {
        try {
          videoWidth = Integer.parseInt(dimensions[0]);
          videoHeight = Integer.parseInt(dimensions[1]);
        } catch (NumberFormatException e) {
          videoWidth = 0;
          videoHeight = 0;
          Log.e(TAG, "Wrong video resolution setting: " + resolution);
        }
      }
    }

    // Get camera fps from settings.
    int cameraFps = 0;
    if (useValuesFromIntent) {
      cameraFps =  context.getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
    }
    if (cameraFps == 0) {
      String fps = sharedPref.getString(keyprefFps, context.getString(R.string.pref_fps_default));
      String[] fpsValues = fps.split("[ x]+");
      if (fpsValues.length == 2) {
        try {
          cameraFps = Integer.parseInt(fpsValues[0]);
        } catch (NumberFormatException e) {
          cameraFps = 0;
          Log.e(TAG, "Wrong camera fps setting: " + fps);
        }
      }
    }

    // Check capture quality slider flag.
    boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
        CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
        R.string.pref_capturequalityslider_default, useValuesFromIntent);

    // Get video and audio start bitrate.
    int videoStartBitrate = 0;
    if (useValuesFromIntent) {
      videoStartBitrate =  context.getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
    }
    if (videoStartBitrate == 0) {
      String bitrateTypeDefault = context.getString(R.string.pref_maxvideobitrate_default);
      String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
      if (!bitrateType.equals(bitrateTypeDefault)) {
        String bitrateValue = sharedPref.getString(
            keyprefVideoBitrateValue, context.getString(R.string.pref_maxvideobitratevalue_default));
        videoStartBitrate = Integer.parseInt(bitrateValue);
      }
    }

    int audioStartBitrate = 0;
    if (useValuesFromIntent) {
      audioStartBitrate =  context.getIntent().getIntExtra(CallActivity.EXTRA_AUDIO_BITRATE, 0);
    }
    if (audioStartBitrate == 0) {
      String bitrateTypeDefault = context.getString(R.string.pref_startaudiobitrate_default);
      String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
      if (!bitrateType.equals(bitrateTypeDefault)) {
        String bitrateValue = sharedPref.getString(
            keyprefAudioBitrateValue, context.getString(R.string.pref_startaudiobitratevalue_default));
        audioStartBitrate = Integer.parseInt(bitrateValue);
      }
    }

    // Check statistics display option.
    boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
        CallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

    boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, CallActivity.EXTRA_TRACING,
        R.string.pref_tracing_default, useValuesFromIntent);

    // Get datachannel options
    boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
        CallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
        useValuesFromIntent);
    boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, CallActivity.EXTRA_ORDERED,
        R.string.pref_ordered_default, useValuesFromIntent);
    boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
        CallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
    int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
        CallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
        useValuesFromIntent);
    int maxRetr =
        sharedPrefGetInteger(R.string.pref_max_retransmits_key, CallActivity.EXTRA_MAX_RETRANSMITS,
            R.string.pref_max_retransmits_default, useValuesFromIntent);
    int id = sharedPrefGetInteger(R.string.pref_data_id_key, CallActivity.EXTRA_ID,
        R.string.pref_data_id_default, useValuesFromIntent);
    String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
        CallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

    // Start AppRTCMobile activity.
    Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
    if (validateUrl(roomUrl)) {
      Uri uri = Uri.parse(roomUrl);
      Intent intent = new Intent(this.context, CallActivity.class);
      intent.setData(uri);
      intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
      intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
      intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
      intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
      intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
      intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
      intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
      intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
      intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
      intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
      intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
      intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
      intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
      intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
      intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
      intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
      intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
      intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
      intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
      intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
      intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
      intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
      intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
      intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
      intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
      intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

      intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

      if (dataChannelEnabled) {
        intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
        intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
        intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
        intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
        intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
        intent.putExtra(CallActivity.EXTRA_ID, id);
      }

      if (useValuesFromIntent) {
        if ( context.getIntent().hasExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA)) {
          String videoFileAsCamera =
               context.getIntent().getStringExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA);
          intent.putExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
        }

        if ( context.getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
          String saveRemoteVideoToFile =
               context.getIntent().getStringExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
          intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
        }

        if ( context.getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
          int videoOutWidth =
               context.getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
          intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
        }

        if ( context.getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
          int videoOutHeight =
               context.getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
          intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
        }
      }

      context.startActivityForResult(intent, CONNECTION_REQUEST);
    }
  }

  private boolean validateUrl(String url) {
    if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
      return true;
    }

    new AlertDialog.Builder(this.context)
        .setTitle(context.getText(R.string.invalid_url_title))
        .setMessage (context.getString(R.string.invalid_url_text, url))
        .setCancelable(false)
        .setNeutralButton(R.string.ok,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            })
        .create()
        .show();
    return false;
  }

  private final AdapterView.OnItemClickListener roomListClickListener =
      new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          String roomId = ((TextView) view).getText().toString();
          connectToRoom(roomId, false, false, false, 0);
        }
      };

  private final OnClickListener addFavoriteListener = new OnClickListener() {
    @Override
    public void onClick(View view) {
      String newRoom = roomEditText.getText().toString();
      if (newRoom.length() > 0 && !roomList.contains(newRoom)) {
        adapter.add(newRoom);
        adapter.notifyDataSetChanged();
      }
    }
  };

  private final OnClickListener connectListener = new OnClickListener() {
    @Override
    public void onClick(View view) {
      connectToRoom(roomEditText.getText().toString(), false, false, false, 0);
    }
  };

  public void call(AppCompatActivity baseContext, String establishmentId) {

    this.context =baseContext;

    // Get setting keys.
    PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    keyprefVideoCallEnabled = context.getString(R.string.pref_videocall_key);
    keyprefScreencapture = context.getString(R.string.pref_screencapture_key);
    keyprefCamera2 = context.getString(R.string.pref_camera2_key);
    keyprefResolution = context.getString(R.string.pref_resolution_key);
    keyprefFps = context.getString(R.string.pref_fps_key);
    keyprefCaptureQualitySlider = context.getString(R.string.pref_capturequalityslider_key);
    keyprefVideoBitrateType = context.getString(R.string.pref_maxvideobitrate_key);
    keyprefVideoBitrateValue = context.getString(R.string.pref_maxvideobitratevalue_key);
    keyprefVideoCodec = context.getString(R.string.pref_videocodec_key);
    keyprefHwCodecAcceleration = context.getString(R.string.pref_hwcodec_key);
    keyprefCaptureToTexture = context.getString(R.string.pref_capturetotexture_key);
    keyprefAudioBitrateType = context.getString(R.string.pref_startaudiobitrate_key);
    keyprefAudioBitrateValue = context.getString(R.string.pref_startaudiobitratevalue_key);
    keyprefAudioCodec = context.getString(R.string.pref_audiocodec_key);
    keyprefNoAudioProcessingPipeline = context.getString(R.string.pref_noaudioprocessing_key);
    keyprefAecDump = context.getString(R.string.pref_aecdump_key);
    keyprefOpenSLES = context.getString(R.string.pref_opensles_key);
    keyprefDisableBuiltInAec = context.getString(R.string.pref_disable_built_in_aec_key);
    keyprefDisableBuiltInAgc = context.getString(R.string.pref_disable_built_in_agc_key);
    keyprefDisableBuiltInNs = context.getString(R.string.pref_disable_built_in_ns_key);
    keyprefEnableLevelControl = context.getString(R.string.pref_enable_level_control_key);
    keyprefDisplayHud = context.getString(R.string.pref_displayhud_key);
    keyprefTracing = context.getString(R.string.pref_tracing_key);
    keyprefRoomServerUrl = context.getString(R.string.pref_room_server_url_key);
    keyprefRoom = context.getString(R.string.pref_room_key);
    keyprefRoomList = context.getString(R.string.pref_room_list_key);
    keyprefEnableDataChannel = context.getString(R.string.pref_enable_datachannel_key);
    keyprefOrdered = context.getString(R.string.pref_ordered_key);
    keyprefMaxRetransmitTimeMs = context.getString(R.string.pref_max_retransmit_time_ms_key);
    keyprefMaxRetransmits = context.getString(R.string.pref_max_retransmits_key);
    keyprefDataProtocol = context.getString(R.string.pref_data_protocol_key);
    keyprefNegotiated = context.getString(R.string.pref_negotiated_key);
    keyprefDataId = context.getString(R.string.pref_data_id_key);

    connectToRoom(Constants.ESTABLISHMENT+ establishmentId, false, false, false, 0);


  }
}
