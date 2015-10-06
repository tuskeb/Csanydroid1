package hu.csany_zeg.one.csanydroid1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;
import hu.csany_zeg.one.csanydroid1.core.LocalHero;
import hu.csany_zeg.one.csanydroid1.core.Opponent;

public class MainActivity extends AppCompatActivity {
	private final static int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter bluetoothAdapter;
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case BluetoothDevice.ACTION_FOUND: {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Log.v("bluetooth", "device found: " + device.getName());
				}
				break;
				case BluetoothAdapter.ACTION_STATE_CHANGED: {
					switch (bluetoothAdapter.getState()) {
						case BluetoothAdapter.STATE_ON:
							for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
								Log.v("bluetooth", "device found: " + device.getName() + " (" + device.toString() + ")" + " [paired]");
							}

							bluetoothAdapter.startDiscovery();
							break;
					}
					Log.v("bluetooth", "state change");
				}
				break;
			}

		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);

		outState.putString("malac", "laci");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getSupportActionBar().hide();

/*
		try {
			FileInputStream fis = openFileInput("hero_repository");

			byte[] bytes = new byte[fis.available()];

			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(bytes, 0, bytes.length);

			parcel.readTypedList(LocalHero.sHeros, LocalHero.CREATOR);

			parcel.recycle();
			Log.v("mama", "end" + LocalHero.sHeros.size());

		} catch (java.io.IOException e) {
e.printStackTrace();
		}
*/

		if (LocalHero.sHeros.size() == 0) {
			new LocalHero().setName("A");
			new LocalHero().setName("B");
			new LocalHero().setName("C");
			new LocalHero().setName("D");
		} else {
			Log.v("mama", "!!!!!!!!!!!!!!!!!");
		}

		//Display display = getWindowManager().getDefaultDisplay();
		//Point size = new Point();
		//display.getSize(size);
		//setRequestedOrientation(size.x > size.y ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		{
			Battle battle = new Battle("Waterló-i csata", null);
			for (Hero h : LocalHero.sHeros) {
				h.addToBattle(battle);
			}

			battle.beginMassacre();
		}

		{
			Battle battle = new Battle("Trója", null);
			for (Hero h : LocalHero.sHeros) {
				h.addToBattle(battle);
			}
			battle.beginMassacre();
		}
		// http://developer.android.com/reference/android/bluetooth/BluetoothServerSocket.html
// http://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection


		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister during onDestroy
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)); // Don't forget to unregister during onDestroy


		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean sBluetoothAvailable;

		Opponent.watchOnBluetooth(bluetoothAdapter);
		Opponent.setListener(new Opponent.OpponentListener() {

			@Override
			public void onOpponentAdded(Opponent o) {
				return;
			}

			@Override
			public void onOpponentRemoved(Opponent o) {
				return;
			}
		});
		if ((sBluetoothAvailable = (bluetoothAdapter != null))) {

			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}

			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
			startActivity(discoverableIntent);

			// Register the BroadcastReceiver
		} else {
			Log.v("bluetooth", "not supported");
		}

		setContentView(R.layout.activity_main);

		((Button) findViewById(R.id.hero_repository_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, HeroListActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		((Button) findViewById(R.id.new_battle_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent myIntent = new Intent(MainActivity.this, BattleActivity.class);
				//MainActivity.this.startActivity(myIntent);
			}
		});

		((Button) findViewById(R.id.view_battles_button)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, BattleActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});


	}

	@Override
	protected void onPause() {
		super.onPause();
		/*
		try {
			FileOutputStream fos = openFileOutput("hero_repository", MODE_PRIVATE);

			Parcel parcel = Parcel.obtain();

			LocalHero[] heroList = new LocalHero[LocalHero.sHeros.size()];
			parcel.writeTypedArray(LocalHero.sHeros.toArray(heroList), 0);

			fos.write(parcel.marshall());

			parcel.recycle();

		} catch (java.io.IOException e) {

		}
		*/

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

		/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/
}
