package dolphin.android.sdk.album.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.utils.L;
import dolphin.android.sdk.album.utils.T;

public class TwitterLogin implements AppConstants {
	private Activity mActivity;
	private Uri uri;

	private static Twitter twitter;
	private static RequestToken requestToken;
	private static SharedPreferences mSharedPreferences;

	public TwitterLogin(Activity mActivity) {
		if (mActivity != null) {
			this.mActivity = mActivity;
			uri = mActivity.getIntent().getData();
			mSharedPreferences = mActivity.getSharedPreferences(
					PREFERENCE_NAME, Activity.MODE_PRIVATE);
		}
	}

	public void logIn(final TwitterLoginCallback callback) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mSharedPreferences = mActivity.getSharedPreferences(
						PREFERENCE_NAME, Activity.MODE_PRIVATE);
				uri = mActivity.getIntent().getData();
				String error = null;
				if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
					String verifier = uri
							.getQueryParameter(IEXTRA_OAUTH_VERIFIER);
					try {
						AccessToken accessToken = twitter.getOAuthAccessToken(
								requestToken, verifier);
						Editor e = mSharedPreferences.edit();
						e.putString(PREF_KEY_TOKEN, accessToken.getToken());
						e.putString(PREF_KEY_SECRET,
								accessToken.getTokenSecret());
						e.commit();

					} catch (TwitterException e) {
						error = e.toString();
						if (e != null && e.getMessage() != null) {
							L.e(e.getMessage());
							T.show(e.getMessage());
						} else {
							error = "Unknow Error";
							T.show("Unknow Error");
						}
					}
				}
				if (callback != null) {
					callback.onLogin(error == null ? TWITTER_LOGIN_SUCCESS
							: TWITTER_LOGIN_FAILED, error);
				}
				return null;
			}

		}.execute();

	}

	public Twitter getTwitter() {
		if (twitter == null && isLoggedIn()) {
			String oauthAccessToken = mSharedPreferences.getString(
					PREF_KEY_TOKEN, "");
			String oAuthAccessTokenSecret = mSharedPreferences.getString(
					PREF_KEY_SECRET, "");
			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			Configuration conf = confbuilder.setOAuthConsumerKey(CONSUMER_KEY)
					.setOAuthConsumerSecret(CONSUMER_SECRET)
					.setOAuthAccessToken(oauthAccessToken)
					.setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();
			twitter = new TwitterFactory(conf).getInstance();
		}
		return twitter;
	}

	public void askOAuth() {
		(new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
				configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);
				configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);
				Configuration configuration = configurationBuilder.build();
				twitter = new TwitterFactory(configuration).getInstance();

				try {
					requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
					// T.show("Please authorize this app!");
					mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(requestToken.getAuthenticationURL())));
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
		}).execute();
	}

	public boolean isLoggedIn() {
		mSharedPreferences = mActivity.getSharedPreferences(PREFERENCE_NAME,
				Activity.MODE_PRIVATE);
		return mSharedPreferences.getString(PREF_KEY_TOKEN, null) != null;
	}

	public interface TwitterLoginCallback {
		public void onLogin(int result, String mess);
	}

	public void logOut() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.remove(PREF_KEY_TOKEN);
		editor.remove(PREF_KEY_SECRET);
		editor.commit();
	}
}
