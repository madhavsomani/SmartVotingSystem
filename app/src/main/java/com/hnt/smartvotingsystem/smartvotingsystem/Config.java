package com.hnt.smartvotingsystem.smartvotingsystem;

/**
 * Created by madhav on 2/9/2017.
 */


public class Config {
    //URLs to register.php and confirm.php file
    public static final String REGISTER_URL = "http://hntdatabase.16mb.com/register.php";
    public static final String VOTING_URL = "http://hntdatabase.16mb.com/updatevote.php";


    //Keys to send username, password, phone and otp
    public static final String KEY_NAME= "name";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_AADHAR = "aadharcard";
    public static final String KEY_IMAGE = "image";
    //profile



    //JSON Tag from response from server
    public static final String TAG_RESPONSE= "ErrorMessage";
}