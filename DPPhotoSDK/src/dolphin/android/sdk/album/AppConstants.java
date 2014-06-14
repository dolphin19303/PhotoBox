package dolphin.android.sdk.album;

public interface AppConstants {
	public static final boolean releaseMode = false;

	// facebook constant
	public static final short FACEBOOK = 0;
	public static final short FACEBOOK_GETALBUM_SUCCESS = 1;
	public static final short FACEBOOK_GETALBUM_FAILED = 0;
	public static final short FACEBOOK_GETALBUM_NOT_AUTHORIZED = 3;
	public static final short FACEBOOK_GETPHOTO_SUCCESS = 1;
	public static final short FACEBOOK_GETPHOTO_FAILED = 0;
	public static final short FACEBOOK_GETPHOTO_NOT_AUTHORIZED = 3;
	public static final short FACEBOOK_LOGIN_SUCCESS = 1;
	public static final short FACEBOOK_LOGIN_FAILED = 2;
	public static final short FACEBOOK_LOGIN_RESTORETOKEN_SUCCESS = 0;
	public static final short FACEBOOK_LOGIN_RESTORETOKEN_FAILED = 1;
	public static final String FACEBOOK_LOGIN_PERMISSION_PHOTO = "user_photos";

	// Twitter constant
	public static final short TWITTER = 1;
	public static final String CONSUMER_KEY = "cuMSs4GAzGsDQt3Vk2KLdrdhx";
	public static final String CONSUMER_SECRET = "2WSJwQJGGDClc8sxczlulVVPZRPi5HmF976PVjjDSgyo4S4QDS";
	public static final String PREFERENCE_NAME = "twitter_oauth";
	public static final String PREF_KEY_SECRET = "oauth_token_secret";
	public static final String PREF_KEY_TOKEN = "oauth_token";
	public static final String CALLBACK_URL = "oauth://t4jsample";
	public static final String IEXTRA_AUTH_URL = "auth_url";
	public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
	public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";
	public static final short TWITTER_GETPHOTO_SUCCESS = 1;
	public static final short TWITTER_GETPHOTO_FAILED = 0;
	public static final short TWITTER_LOGIN_SUCCESS = 1;
	public static final short TWITTER_LOGIN_FAILED = 2;

	// Instagram constant
	public static final short INSTAGRAM = 2;
	public static final String INSTAGRAM_CLIENT_ID = "5b2e1975dc9645a8b69321f6238c20c5";
	public static final String INSTAGRAM_CLIENT_SECRET = "1c9b44e815844daabebcc200abca3c49";
	public static final String INSTAGRAM_CALLBACK_URL = "instagram://connect";
	public static final short INSTAGRAM_LOGIN_SUCCESS = 1;
	public static final short INSTAGRAM_LOGIN_FAILED = 2;
	public static final short INSTAGRAM_LOGIN_RESTORETOKEN_SUCCESS = 0;
	public static final short INSTAGRAM_LOGIN_RESTORETOKEN_FAILED = 1;
	public static final short INSTAGRAM_GETPHOTO_SUCCESS = 1;
	public static final short INSTAGRAM_GETPHOTO_FAILED = 0;
}
