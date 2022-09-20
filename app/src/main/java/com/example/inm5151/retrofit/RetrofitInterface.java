package com.example.inm5151.retrofit;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("/login")
    Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

    @POST("/signup")
    Call<SignUpResult> executeSignup (@Body HashMap<String, String> map);

    @POST("/group")
    Call<GroupResult> executeGroup (@Body HashMap<String, String> map);

    @POST("/create_group")
    Call<GroupResult> create_group (@Body HashMap<String, String> map);

    @GET("/all_users")
    Call<AllUsersResult> executeGetAllUsers();

    @POST("/invite_group")
    Call<InvitationResult> executeInviteGroup(@Body HashMap<String, String> map);

    @POST("/invitations")
    Call<InvitationResult> executeInvitationsList (@Body HashMap<String, String> map);

    @POST("/accept_invitation")
    Call<InvitationResult> executeAcceptInvitation (@Body HashMap<String, String> map);

    @POST("/decline_invitation")
    Call<InvitationResult> executeDeclineInvitation (@Body HashMap<String, String> map);

    @GET("/first_players")
    Call<PlayersResult> executeGetFirstPlayers();

    @POST("/get_players")
    Call<PlayersResult> executeGetPlayers(@Body HashMap<String, String> map);

    @POST("/add_player")
    Call<PlayersResult> executeAddPlayer(@Body HashMap<String, String> map);

    @POST("/participants")
    Call<GroupResult> executeGetParticipants(@Body HashMap<String, String> map);

    @POST("/participant_players")
    Call<PlayersResult> executeGetParticipantPlayers(@Body HashMap<String, String> map);

    @POST("/confirm_players")
    Call<PlayersResult> executeConfirmPlayers(@Body HashMap<String, String> map);

    @POST("/delete_player")
    Call<PlayersResult> executeDeletePlayer(@Body HashMap<String, String> map);

    @POST("/tradable_player")
    Call<PlayersResult> executeTradablePlayer(@Body HashMap<String, String> map);

    @POST("/all_tradable_players")
    Call<PlayersResult> executeGetUserTradablePlayers(@Body HashMap<String, String> map);

    @POST("/exchange_players")
    Call<PlayersResult> executeExchangePlayers(@Body HashMap<String, String> map);

    @POST("/exchange_invitations")
    Call<InvitationResult> executeExchangeInvitations(@Body HashMap<String, String> map);

    @POST("/username")
    Call<InvitationResult> executeGetUsername(@Body HashMap<String, String> map);

    @POST("/accept_exchange")
    Call<InvitationResult> executeAcceptExchange(@Body HashMap<String, String> map);

    @POST("/decline_exchange")
    Call<InvitationResult> executeDeclineExchange(@Body HashMap<String, String> map);

    @POST("/get_points")
    Call<GroupResult> executeGetPoints(@Body HashMap<String, String> map);

    @POST("/get_points_group")
    Call<GroupResult> executeGetPointsGroup(@Body HashMap<String, String> map);
}