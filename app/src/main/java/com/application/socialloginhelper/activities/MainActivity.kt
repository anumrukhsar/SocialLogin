package com.application.socialloginhelper.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import com.application.socialloginhelper.R
import com.application.socialloginhelper.databinding.ActivityMainBinding
import com.application.socialloginhelper.util.SocialLogin
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() ,View.OnClickListener,SocialLogin.FbLoginInfoFetcher, SocialLogin.GoogleLoginInfoFetcher,SocialLogin.TwitterLoginInfoFetcher{
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setListeners()
    }

    private fun setListeners() {
        binding.btnFb.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnTwitter.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnFb-> fbLogin()
            R.id.btnGoogle-> googleLogin()
            R.id.btnTwitter-> twitterLogin()
        }
    }

    private fun fbLogin() {
        SocialLogin.setSocialInfoFetcher(this)
        SocialLogin.loginWithFacebook(this@MainActivity)
    }

    private fun googleLogin() {
        SocialLogin.setGoogleInfoFetcher(this)
        SocialLogin.loginWithGoogle(this@MainActivity)
    }

    private fun twitterLogin() {
        SocialLogin.setTwitterLoginInfoFetcher(this)
        SocialLogin.doTwitterLogin(this@MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    when(requestCode){
        SocialLogin.FB_REQ_CODE->
            if (resultCode == RESULT_OK && data != null) {
                SocialLogin
                    .getFaceBookonActivityResult(requestCode, resultCode, data)
            }

            SocialLogin.G_REQ_CODE->
                if (resultCode == RESULT_OK && data != null) {
                    SocialLogin
                        .getGoogleonActivityResult(this,requestCode, resultCode, data)
                }

          SocialLogin.TWITTER_REQ_CODE->
                    if (resultCode == Activity.RESULT_OK && data != null) {
                       SocialLogin.getTwitteronActivityResult(requestCode, resultCode, data);
                    }


        }
    }

    override fun onGoogleInfoFetched(
        id: String?,
        name: String?,
        email: String?,
        pictureUrl: String?,
        accessToken: String?,
        profileUrl: String?
    ) {
        Log.e("Google",name!!)

    }


    override fun onFbInfoFetched(
        id: String,
        name: String,
        token: String,
        email: String,
        picture: String
    ) {
        Log.e("Fb",name)
    }

    override fun onTwitterInfoFetched(
        id: String?,
        name: String?,
        picture: String?,
        token: String?,
        email: String?
    ) {
        Log.e("Twitter",name!!)
    }

}