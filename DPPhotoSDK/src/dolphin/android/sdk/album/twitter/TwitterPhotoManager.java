package dolphin.android.sdk.album.twitter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.model.TwitterPhoto;
import dolphin.android.sdk.album.utils.L;

//https://dev.twitter.com/docs/entities
public class TwitterPhotoManager implements AppConstants {
	private TwitterLogin mLogin;
	private static final int TWITTER_PAGE_NUM_ITEM = 20;

	public TwitterPhotoManager(TwitterLogin mLogin) {
		this.mLogin = mLogin;
	}

	public void getListPhoto(final GetImageTwitterCallback mCallBack) {
		if (mCallBack != null) {
			new AsyncTask<Void, Void, List<twitter4j.Status>>() {

				@Override
				protected List<twitter4j.Status> doInBackground(Void... params) {
					try {
						return mLogin.getTwitter().getUserTimeline(
								mLogin.getTwitter().getScreenName(),
								new Paging(1, TWITTER_PAGE_NUM_ITEM));
						// page.setPage(2);
					} catch (TwitterException e) {
						L.e("Twitter query failed" + e);
					} catch (NullPointerException e) {
						L.e("Null at twitter manager");
					} catch (Exception e) {
						L.e("get Twitter image error " + e.toString());
					}
					return null;
				}

				protected void onPostExecute(List<twitter4j.Status> result) {
					if (result != null && result.size() > 0) {
						ArrayList<TwitterPhoto> mPhotos = new ArrayList<TwitterPhoto>();
						for (twitter4j.Status mStt : result) {
							String date = mStt.getCreatedAt().toString();

							for (MediaEntity mediaEntity : mStt
									.getMediaEntities()) {
								TwitterPhoto mNewPhoto = new TwitterPhoto(
										String.valueOf(mediaEntity.getId()),
										mediaEntity.getMediaURL());
								mNewPhoto.setDate(mStt.getCreatedAt());

								mPhotos.add(mNewPhoto);
							}
						}
						mCallBack.onReceivedPhoto(TWITTER_GETPHOTO_SUCCESS,
								mPhotos);
					}
				}
			}.execute();
		}
	}

	public interface GetImageTwitterCallback {
		public void onReceivedPhoto(short status, ArrayList<TwitterPhoto> result);
	}
}
