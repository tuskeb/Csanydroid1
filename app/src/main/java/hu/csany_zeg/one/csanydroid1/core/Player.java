package hu.csany_zeg.one.csanydroid1.core;

import android.bluetooth.*;
import android.os.Parcel;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Player {

	public static final Player CURRENT = new Player();
	private static final byte SCOPE_PLAYER = (byte)0x80;
	public static final byte
			ACTION_NO_OPERATION = 0,
			ACTION_READY_FOR_BATTLE = 1,
			ACTION_GIVE_UP = 2,
			ACTION_GET_BATTLES = 3 | SCOPE_PLAYER,
			ACTION_GET_BATTLE = 4,
			ACTION_JOIN_TO_BATTLE = 5,
			ACTION_JOIN_TO_BATTLE_ACCEPTED = 6,
			ACTION_GET_HERO = 7,
			ACTION_INVITE = 5;
	private final static String TAG = "player";
	private final static ArrayList<Player> sPlayers = new ArrayList<Player>();
	private final OutputStream mOutputStream;
	private final InputStream mInputStream;
	private final UUID mUUID;
	//private ArrayList<OnDataReceivedListener> mListeners = new ArrayList<OnDataReceivedListener>();
	private String mName;
	private Parcel mParcel = Parcel.obtain();

	public Player(BluetoothSocket bluetoothSocket) throws IOException {
		this(bluetoothSocket.getRemoteDevice().getName(), bluetoothSocket.getOutputStream(), bluetoothSocket.getInputStream());
	}

	private Player() {
		mName = "Ez vok én";
		mUUID = UUID.randomUUID();
		mOutputStream = null;
		mInputStream = null;

	}

	private Player(String name, OutputStream outputStream, InputStream inputStream) {
		Log.v(TAG, "player connected");

		mName = name;
		mUUID = UUID.randomUUID();
		mOutputStream = outputStream;
		mInputStream = inputStream;

		startListening();

		sPlayers.add(this);

	}

	public static void startListeningViaBluetooth(final BluetoothAdapter bluetoothAdapter) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				BluetoothServerSocket bluetoothServerSocket;
				try {
					bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Its me", UUID.randomUUID());
				} catch (IOException e) {
					return;
				}

				Log.v(TAG, "listening started");

				try {
					while (true) {
						BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();
						new Player(bluetoothSocket);

					}
				} catch (IOException e) {
				}

				try {
					bluetoothServerSocket.close();
				} catch (IOException e) {
				}

			}
		};
		thread.start();

	}

	/*public void setDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		for (int i = mListeners.size(); i > 0; ) {
			final OnDataReceivedListener listener = mListeners.get(--i);
			if (listener.mAction == onDataReceivedListener.mAction) {
				mListeners.set(i, onDataReceivedListener);
				return;
			}
		}

		mListeners.add(onDataReceivedListener);

	}

	public void removeDataReceivedListener(byte actionCode) {
		for (int i = mListeners.size(); i > 0; ) {
			final OnDataReceivedListener listener = mListeners.get(--i);
			if (listener.mAction == actionCode) {
				mListeners.remove(i);
				return;
			}
		}
	}

	public void removeDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
		mListeners.remove(onDataReceivedListener);
	}*/


	public boolean setName(String name) throws IllegalAccessException {
		name = name.trim();

		mName = name;
		return true;
	}

	private void startListening() {
		Thread thread = new Thread() {

			private final byte[] buffer = new byte[512];

			@Override
			public void run() {
				try {
					while (true) {
						final int length = mInputStream.read(buffer);
						final Parcel parcel = Parcel.obtain();
						parcel.unmarshall(buffer, 0, buffer.length);
						Player.this.processMessage(parcel);

					}
				} catch (IOException e) {
					Player.this.dispose();
				}

			}

		};

		thread.start();

	}

	public void writeHeaderToParcel(Parcel parcel) {
		parcel.writeLong(mUUID.getMostSignificantBits());
		parcel.writeLong(mUUID.getLeastSignificantBits());
	}


	private void processMessage(Parcel parcel) {
		try {
			parcel.setDataPosition(0);

			final byte action = parcel.readByte();

			if ((action & SCOPE_PLAYER) != 0) {
				final Parcel resp = Parcel.obtain();
				this.writeHeaderToParcel(resp);

				switch (action) {
					case Player.ACTION_GET_BATTLES: {
						final ArrayList<Battle> ownBattles = Battle.getOwnBattles();
						resp.writeByte((byte) ownBattles.size());

						for (Battle battle : ownBattles) {
							if (battle.getOwner() == Player.CURRENT) {
								resp.writeString(battle.getName());
							}
						}
					}

					default:
						break;

				}

				// válasz küldése
				send(resp);

			} else {
				final String name = parcel.readString();
				final Battle battle = Battle.findBattle(name);
				if (battle == null) throw new NullPointerException();

				battle.processMessage(action, parcel, this);
			}


		} catch(Exception e) {
			Log.v(TAG, "misformatted message");
		}


	}

	public UUID getUUID() {
		return mUUID;
	}

	public void dispose() {

		for (int i = Battle.sBattles.size(); i > 0; ) {
			Battle battle = Battle.sBattles.get(--i);
			//if (battle.send() == this) {
			//	battle.dispose();
			//}
		}

		sPlayers.remove(this);

	}

	public void send(Message msg) {

		synchronized (mParcel) {
			mParcel.setDataPosition(0);
			mParcel.writeString(msg.mName);

			msg.obtain(mParcel);

			send(mParcel);

		}

	}

	public void send(Parcel parcel) {

		if (mOutputStream != null) {
			try {
				mOutputStream.write(parcel.marshall());
			} catch (IOException e) { }
		} else {
			// loopback
			// TODO? static player HANDLER
			processMessage(mParcel);
		}

	}

	public static abstract class Message {

		final String mName;

		public Message(Battle port) {
			mName = port.getName();
		}

		public Message(Hero port) { mName = port.getName(); }

		public abstract void obtain(Parcel parcel);

	}
/*
	public static abstract class OnDataReceivedListener {

		private final byte mAction;

		public OnDataReceivedListener(byte action) {
			mAction = action;
		}

		public abstract boolean OnDataReceived(Parcel data);

	}
*/
}
