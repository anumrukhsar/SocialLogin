package com.application.socialloginhelper.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.application.socialloginhelper.R
import com.facebook.*
import com.facebook.GraphRequest.GraphJSONObjectCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.signin.*
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient


object SocialLogin {
    lateinit var callbackManager : CallbackManager
    lateinit var mListener : FbLoginInfoFetcher
    const val FB_REQ_CODE = 64206

    lateinit var mGoogleSignInClient : GoogleSignInClient
    lateinit var mGoogleLoginInfoFetcher: GoogleLoginInfoFetcher
    const val G_REQ_CODE = 103

    lateinit var authClient: TwitterAuthClient
    private var email: String?=null
    /**Replace with your own app CONSUMER_KEY & CONSUMER_SECRET**/
    const val CONSUMER_KEY = "Y4C1ynYlXMdgf7xjinEj41ecU"
    const val CONSUMER_SECRET = "cq4iBrgNTWbDNKd9ETwuJraRydqB8auGny6z8QlMVHRfFCklwI"
    lateinit var mTwitterlistener: TwitterLoginInfoFetcher
    const val TWITTER_REQ_CODE = TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE


    /***********************************************************Facebook**********************************************/
private fun configureFacebook(context: Context) {
//        FacebookSdk.sdkInitialize(context);
    callbackManager = CallbackManager.Factory.create()
    LoginManager.getInstance()
        .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                //  Log.e("===",loginResult.toString());
                val accessToken = loginResult.accessToken
                fetchUserInfo(accessToken)
                disconnectSocialNetworks()
            }

            override fun onCancel() {
                //   Log.e("onCancel", "onCancel");
            }

            override fun onError(e: FacebookException) {
                // Log.e("error", e.toString());
            }
        })

}

    fun loginWithFacebook(activity: Activity) {
        configureFacebook(activity)
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"))
    }

   fun setSocialInfoFetcher(fbLoginInfoFetcher: FbLoginInfoFetcher) {
        mListener = fbLoginInfoFetcher
    }

    private fun fetchUserInfo(accessToken: AccessToken?) {
        if (accessToken != null) {
            val request = GraphRequest.newMeRequest(
                accessToken
            ) { jsonObject, graphResponse -> // interface to pass json object
                Log.e("JSON OBJECT:", jsonObject.toString())
                try {
                    val id = jsonObject.getString("id")
                    val name =
                        jsonObject.getString("first_name") + " " + jsonObject.getString("last_name")
                    var email: String? = ""
                    val picture =
                        "https://graph.facebook.com/$id/picture?type=large"
                    if (jsonObject.has("email")) {
                        email = jsonObject.getString("email")
                    }
                    mListener.onFbInfoFetched(
                        id,
                        name,
                        accessToken.token,
                        email!!,
                        picture
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val parameters = Bundle()
            parameters.putString("fields", "id,first_name,last_name,email,hometown")
            request.parameters = parameters
            request.executeAsync()
        }
    }

    private fun logoutFromFacebook() {
        LoginManager.getInstance().logOut()
    }

    private fun disconnectSocialNetworks() {
        logoutFromFacebook()
    }

    fun getFaceBookonActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    interface FbLoginInfoFetcher{
        fun onFbInfoFetched(id:String, name:String , token:String , email:String, picture:String)
    }

    /***********************************************************Google**********************************************/
    private fun configureGoogle(context: Context) {
//        val SERVER_CLIENT_ID = context.getString(R.string.client_server_auth)
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
//            .requestServerAuthCode(SERVER_CLIENT_ID, false)
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    private fun signOut(activity: Activity) {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(activity, object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    Log.e("tag", "googleSignOut")
                }
            })
    }

    fun loginWithGoogle(activity: Activity) {
        configureGoogle(activity)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(activity, signInIntent, G_REQ_CODE,null)
    }

    fun getGoogleonActivityResult(activity: Activity,requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == G_REQ_CODE) {
            val result: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)!!
            Log.e("tag", "handleGoogleSignInResult:" + result.isSuccess())
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                val acct: GoogleSignInAccount = result.getSignInAccount()!!
                handleResult(acct,activity)
            }
        }
    }

    fun setGoogleInfoFetcher(googleLoginInfoFetcher: GoogleLoginInfoFetcher) {
        mGoogleLoginInfoFetcher = googleLoginInfoFetcher
    }

    interface GoogleLoginInfoFetcher {
        fun onGoogleInfoFetched(
            id: String?,
            name: String?,
            email: String?,
            pictureUrl: String?,
            accessToken: String?,
            profileUrl: String?
        )
    }

    fun handleResult(account: GoogleSignInAccount?,activity: Activity) {
        if (account != null) {
            val id: String = account.getId()!!
            var accesstoken = ""
            var pictureUrl = ""
            var email = ""
            val name: String = account.getDisplayName()!!
            if (account.getPhotoUrl() != null) {
                pictureUrl = account.getPhotoUrl().toString()
            }
            val profileUrl = "https://plus.google.com/profile_$id"
            if (account.getServerAuthCode() != null) {
                accesstoken = account.getServerAuthCode()!!
            }
            if (account.getEmail() != null) {
                email = account.getEmail()!!
            }
            mGoogleLoginInfoFetcher.onGoogleInfoFetched(
                id,
                name,
                email,
                pictureUrl,
                accesstoken,
                profileUrl
            )
            signOut(activity)
        }
    }

    /***********************************************************Twitter**********************************************/
    open fun configureTwitter(context: Context?) {
        val config = TwitterConfig.Builder(context)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    CONSUMER_KEY,
                    CONSUMER_SECRET
                )
            )
            .debug(true)
            .build()
        Twitter.initialize(config)
        authClient = TwitterAuthClient()
    }

    fun doTwitterLogin(activity: Activity?) {
        configureTwitter(activity)
        authClient.cancelAuthorize()
        authClient.authorize(
            activity,
            object : Callback<TwitterSession>() {
                override fun success(twitterSessionResult: Result<TwitterSession>) {
//                "success twitter"
                    Log.e("user_id", twitterSessionResult.data.userId.toString() + "")
                    Log.e("user_name", twitterSessionResult.data.userName + "")
                    authClient.requestEmail(
                        twitterSessionResult.data,
                        object : Callback<String>() {
                            override fun success(result: Result<String>) {
                                //Log.e("email", result.data + "");
                                email = result.data
                                val id = twitterSessionResult.data.userId.toString()
                                val name = twitterSessionResult.data.userName
                                val picture =
                                    "https://twitter.com/$name/profile_image?size=original"
                                mTwitterlistener.onTwitterInfoFetched(
                                    id,
                                    name,
                                    picture,
                                    twitterSessionResult.data.authToken.token,
                                    if (email!=null) email else ""
                                )
                            }

                            override fun failure(exception: TwitterException) {
                                Log.e("result", exception.message + "")
                            }
                        })
                }

                override fun failure(e: TwitterException) {
                    //Toast.makeText(getActivity(), "failure", Toast.LENGTH_SHORT).show();
                    println("Failure twitter")
                }
            })
    }


    fun getTwitteronActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       authClient.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }


    fun setTwitterLoginInfoFetcher(twitterLoginInfoFetcher: TwitterLoginInfoFetcher) {
        mTwitterlistener = twitterLoginInfoFetcher
    }

    interface TwitterLoginInfoFetcher {
        fun onTwitterInfoFetched(
            id: String?,
            name: String?,
            picture: String?,
            token: String?,
            email: String?
        )
    }


}