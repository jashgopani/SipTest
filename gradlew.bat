package com.allincall.cobrowsingsdk.cobrowsing.network.rest;


import com.allincall.cobrowsingsdk.cobrowsing.model.EncryptedRequestPacket;
import com.allincall.cobrowsingsdk.cobrowsing.model.authentication.request.EncryptedClientAuthenticationRequest;
import com.allincall.cobrowsingsdk.cobrowsing.model.authentication.response.ClientAuthenticationResponse;
import com.allincall.cobrowsingsdk.cobrowsing.model.sessionid.response.EncryptedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Interface used by Retrofit to manage api calls
 * generates code based on the functions in the interface
 */

public interface CoBrowsingApi {

//    String BASE_URL = "https://easyassist.allincall.in";
    String BASE_URL = "https://cobrowse.bajajallianzlife.com";


    @POST("/easy-assist/app/client-authentication/")
    Call<ClientAuthenticationResponse> clientAuthentication(@Body EncryptedClientAuthenticationRequest authenticationRequest);

    @POST("/easy-assist/app/initialize/")
    Call<EncryptedResponse> getSessionId(@Header("Authorization") String authorization,
                                         @Header("X-AccessToken") String accessToken,
                                         @Body EncryptedRequestPacket encryptedRequestPacket);

    @POST("/easy-assist/app/close-session/")
    Call<EncryptedResponse> closeSession(@Header("Authorization") String authorization,
                                         @Header("X-AccessToken") String accessToken,
                                         @Body EncryptedRequestPacket encryptedRequestPacket);

    @POST("/easy-assist/app/agent-update-cobrowse-io/")
    Call<EncryptedResponse> syncAgent(@Header("Authorization") String authorization,
                                      @Header("X-AccessToken") String accessToken,
                                      @Body EncryptedRequestPacket encryptedRequestPacket);

    @POST("/easy-assist/app/save-document/")
    Call<EncryptedResponse> sendAttachment(@Header("Authorization") String authorization,
                                           @Body EncryptedRequestPacket encryptedRequestPacket);

    @POST("/easy-assis