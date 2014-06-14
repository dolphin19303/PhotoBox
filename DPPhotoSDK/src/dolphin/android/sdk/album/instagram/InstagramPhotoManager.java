package dolphin.android.sdk.album.instagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.model.InstagramPhoto;

//http://instagram.com/developer/endpoints/users/#get_users_media_recent
public class InstagramPhotoManager implements AppConstants {
	InstagramLogin mInstagramLogin;

	public InstagramPhotoManager() {
	}

	public InstagramPhotoManager(InstagramLogin mInstagramLogin) {
		this.mInstagramLogin = mInstagramLogin;
	}

	public void getListPhoto(final GetImageInstagramCallback mCallBack) {
		(new AsyncTask<Void, Integer, ArrayList<InstagramPhoto>>() {

			@Override
			protected ArrayList<InstagramPhoto> doInBackground(Void... params) {
				String url = "https://api.instagram.com/v1" + "/users/"
						+ mInstagramLogin.getSession().getId()
						+ "/media/recent/?access_token="
						+ mInstagramLogin.getSession().getAccessToken()
						+ "&count=10";

				ArrayList<InstagramPhoto> mList = new ArrayList<InstagramPhoto>();
				HttpClient httpclient = new DefaultHttpClient();

				// Prepare a request object
				HttpGet httpget = new HttpGet(url);

				// Execute the request
				HttpResponse response;
				try {
					response = httpclient.execute(httpget);
					// Examine the response status
					Log.i("Praeda", response.getStatusLine().toString());

					// Get hold of the response entity
					HttpEntity entity = response.getEntity();
					// If the response does not enclose an entity, there is no
					// need
					// to worry about connection release
					if (entity != null) {
						// A Simple JSON Response Read
						InputStream instream = entity.getContent();
						String result = convertStreamToString(instream);
						// Log.e("Praeda", result);

						// Define output arrray

						// A Simple JSONObject Creation
						JSONObject json = new JSONObject(result);
						JSONArray mData = json.getJSONArray("data");
						for (int i = 0; i < mData.length(); i++) {
							JSONObject mObject = (JSONObject) mData.get(i);
							JSONObject mImages = mObject
									.getJSONObject("images");
							String createdDate = mObject
									.getString("created_time");
							String id = mObject.getString("id");
							InstagramPhoto mNewPhoto = new InstagramPhoto(
									mImages);

							Date date = new java.util.Date(
									Long.parseLong(createdDate) * 1000);

							mNewPhoto.setDate(date).setId(id);

							mList.add(mNewPhoto);
						}

						// Closing the input stream will trigger connection
						// release
						instream.close();

					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return mList;
			}

			@Override
			protected void onPostExecute(ArrayList<InstagramPhoto> result) {
				super.onPostExecute(result);
				mCallBack.onReceivedPhoto(INSTAGRAM_GETPHOTO_SUCCESS, result);
			}
		}).execute();
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public interface GetImageInstagramCallback {
		public void onReceivedPhoto(short status,
				ArrayList<InstagramPhoto> result);
	}
}
