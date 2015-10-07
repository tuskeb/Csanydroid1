package hu.csany_zeg.one.csanydroid1.core;

import android.bluetooth.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Player {

	public static final Player CURRENT = null;
	public final static byte
			ACTION_NO_OPERATION = 0,
			ACTION_READY_FOR_BATTLE = 1,
			ACTION_GIVE_UP = 2,
			ACTION_INVITE = 3;

	private final static ArrayList<Player> sPlayers = new ArrayList<Player>();

	public static void startListeningViaBluetooth(final BluetoothAdapter bluetoothAdapter) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				BluetoothServerSocket bluetoothServerSocket;
				try {
					bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Its me", UUID.randomUUID());
				} catch (IOException e) { return; }

				Log.v("bluetooth", "listening started");

				try {
					while(true) {
						BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();
						new Player(bluetoothSocket);
					}
				} catch (IOException e) { }

				try {
					bluetoothServerSocket.close();
				} catch (IOException e) { }

			}
		};
		thread.start();

	}


	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private final String mName;
	private final UUID mUUID;

	public Player(BluetoothSocket bluetoothSocket) throws IOException {
		this(bluetoothSocket.getRemoteDevice().getName(), bluetoothSocket.getOutputStream(), bluetoothSocket.getInputStream());
	}

	private Player(String name, OutputStream outputStream, InputStream inputStream) {
		Log.v("player", "player connected");

		mName = name;
		mUUID = UUID.randomUUID();
		mOutputStream = outputStream;
		mInputStream = inputStream;

		startListening();

		sPlayers.add(this);

	}

	private void startListening() {
		Thread thread = new Thread() {

			private final byte[] buffer = new byte[512];
			@Override
			public void run() {
				try {
					while(true) {
						final int length = mInputStream.read(buffer);
						if(length == -1) break;

						switch (buffer[0]) {
							case ACTION_GIVE_UP:

								break;
							default:
								Log.e("server", "unknown opcode");

						}

					}
				} catch (IOException e) {
					Player.this.dispose();
				}

				Log.v("server", "end listening");

			}

		};

		thread.start();

	}

	public static int getOpponentsCount() {
		return sPlayers.size();
	}

	public void dispose() {

		for(int i = Battle.sBattles.size();i > 0;) {
			Battle battle = Battle.sBattles.get(--i);
			if (battle.mOwner == this) {
				battle.dispose();
			}
		}

		sPlayers.remove(this);

	}

	public void send(byte actionCode, Object data) {
		try {
			mOutputStream.write(ACTION_INVITE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(mOutputStream);

			if(data != null) {
				objectOutputStream.writeObject(data);
			}

		} catch (IOException e) {

		}
	}
	public void invite(Battle battle) {
		if(battle.getState() == 0) {
			return; // TODO
		}
		send(ACTION_INVITE, null);
	}

}
