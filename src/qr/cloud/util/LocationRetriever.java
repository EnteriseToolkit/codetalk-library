/*
 * Copyright (c) 2014 Simon Robinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package qr.cloud.util;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

// manual location retriever, for when Google Play Services isn't available - slow and buggy, but better than nothing
// see: http://stackoverflow.com/a/3145655/1993220 for potential improvements
public class LocationRetriever {

	// wait for a maximum of LOCATION_WAIT_TIME, returning earlier if accuracy is better than LOCATION_ACCURACY_REQUIRED
	public static final int LOCATION_WAIT_TIME = 5000; // milliseconds
	public static final int LOCATION_ACCURACY_REQUIRED = 30; // metres

	Timer mTimer;
	LocationManager mLocationManager;
	LocationResult mLocationResult;

	boolean mGPSEnabled = false;
	boolean mNetworkEnabled = false;

	public static abstract class LocationResult {
		public abstract void gotLocation(Location location);
	}

	public boolean getLocation(Context context, LocationResult result) {
		// use LocationResult callback class to pass location value to callback
		mLocationResult = result;

		if (mLocationManager == null) {
			mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}

		// exceptions will be thrown if provider is not permitted.
		try {
			mGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			mNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!mGPSEnabled && !mNetworkEnabled) {
			return false;
		}

		// set up listeners for available location sources
		if (mGPSEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGPSLocationListener);
		}
		if (mNetworkEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkLocationListener);
		}

		// schedule timing out after wait period
		mTimer = new Timer();
		mTimer.schedule(new GetLastLocation(), LOCATION_WAIT_TIME);
		return true;
	}

	public void cancelGetLocation() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mLocationManager != null) {
			if (mGPSLocationListener != null) {
				mLocationManager.removeUpdates(mGPSLocationListener);
			}
			if (mNetworkLocationListener != null) {
				mLocationManager.removeUpdates(mNetworkLocationListener);
			}
		}
	}

	LocationListener mGPSLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// if this location is accurate enough, return it
			if (location != null && location.getAccuracy() < LOCATION_ACCURACY_REQUIRED) {
				mTimer.cancel();
				mLocationManager.removeUpdates(mGPSLocationListener);
				mLocationManager.removeUpdates(mNetworkLocationListener);
				mLocationResult.gotLocation(location);
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	LocationListener mNetworkLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// if this location is accurate enough, return it
			if (location != null && location.getAccuracy() < LOCATION_ACCURACY_REQUIRED) {
				mTimer.cancel();
				mLocationManager.removeUpdates(mGPSLocationListener);
				mLocationManager.removeUpdates(mNetworkLocationListener);
				mLocationResult.gotLocation(location);
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			mLocationManager.removeUpdates(mGPSLocationListener);
			mLocationManager.removeUpdates(mNetworkLocationListener);

			Location networkLocation = null;
			Location gpsLocation = null;
			if (mGPSEnabled) {
				gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (mNetworkEnabled) {
				networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			// if there are both values use the most accurate one
			if (gpsLocation != null && networkLocation != null) {
				if (gpsLocation.getAccuracy() < networkLocation.getAccuracy()) {
					mLocationResult.gotLocation(gpsLocation);
				} else {
					mLocationResult.gotLocation(networkLocation);
				}
				return;
			}

			// otherwise just return whichever provided a result
			if (gpsLocation != null) {
				mLocationResult.gotLocation(gpsLocation);
				return;
			}
			if (networkLocation != null) {
				mLocationResult.gotLocation(networkLocation);
				return;
			}

			mLocationResult.gotLocation(null);
		}
	}
}
