package hu.csany_zeg.one.csanydroid1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
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
import java.io.IOException;
import java.util.ArrayList;

import hu.csany_zeg.one.csanydroid1.core.Battle;
import hu.csany_zeg.one.csanydroid1.core.Hero;
import hu.csany_zeg.one.csanydroid1.core.Player;

public class MainActivity extends AppCompatActivity {
/*
	private final static int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter bluetoothAdapter;
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
*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getSupportActionBar().hide();

		setContentView(R.layout.activity_main);

		if(Hero.sHeroRepository == null) {
			Hero.sHeroRepository = new ArrayList<>();

			FileInputStream fis = null;
			try {
				// if(Math.random() < 2f) throw new Exception();

				fis = openFileInput("hero_repository");
				final byte bytes[] = new byte[fis.available()];
				fis.read(bytes);

				final Parcel parcel = Parcel.obtain();
				parcel.unmarshall(bytes, 0, bytes.length);
				parcel.setDataPosition(0);

				int size = parcel.readInt();
				Log.v("hero", size + " heroes to load");

				while(--size >= 0) {
					new Hero(parcel);
				}

			} catch (Exception e) {
                final String heroNames[] = {"Szilárd", "Tibor", "Dávid", "Richárd", "Szabolcs", "András", "Roland", "Zoltán", "László", "Sári", "Kati", "Józsi", "Béla", "Karcsi", "Peti", "Janka", "Sándor", "Bence"};
				for (int i = 0; i < heroNames.length; i++) {
					new Hero(Hero.getNextName(heroNames[i]));
				}
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException ignored) { }
				}
			}

		}

		{
            Battle battle = new Battle(null);
            battle.addPlayer(Player.CURRENT, true);
            for(Hero hero : Hero.sHeroRepository) {
                battle.addHero(hero);
            }
            battle.setPlayerReady(Player.CURRENT);
		}


		//Display display = getWindowManager().getDefaultDisplay();
		//Point size = new Point();
		//display.getSize(size);
		//setRequestedOrientation(size.x > size.y ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);


		// http://developer.android.com/reference/android/bluetooth/BluetoothServerSocket.html
// http://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection

/*
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister during onDestroy
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)); // Don't forget to unregister during onDestroy


		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean sBluetoothAvailable;

		Player.startListeningViaBluetooth(bluetoothAdapter);

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
*/

		findViewById(R.id.hero_repository_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, HeroListActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		findViewById(R.id.new_battle_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, HeroSelectorActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		findViewById(R.id.view_battles_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, BattleActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		findViewById(R.id.view_help_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, HelpActivity.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

	}

	@Override
	protected void onDestroy() {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("hero_repository", MODE_PRIVATE);

			final Parcel parcel = Parcel.obtain();
			parcel.setDataPosition(0);

			parcel.writeInt(Hero.sHeroRepository.size());
			for(Hero hero : Hero.sHeroRepository) {
				hero.obtainProperties(parcel);
			}

			fos.write(parcel.marshall());

			parcel.recycle();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ignored) { }
			}
		}

		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
