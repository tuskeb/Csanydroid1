package hu.csany_zeg.one.csanydroid1.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

public class Player {

	public static final Player CURRENT = new Player();
	private static final byte SCOPE_PLAYER = (byte) 0x80;
	public static final byte
			ACTION_NO_OPERATION = 0,
			ACTION_READY_FOR_BATTLE = 1,
			ACTION_GIVE_UP = 2,
			ACTION_GET_NAME = 4 | SCOPE_PLAYER,
			ACTION_GET_BATTLES = 5 | SCOPE_PLAYER,
			ACTION_GET_BATTLE = 6,
			ACTION_JOIN_TO_BATTLE = 7,
			ACTION_JOIN_TO_BATTLE_ACCEPTED = 8,
			ACTION_GET_HERO = 9 | SCOPE_PLAYER,
			ACTION_UPDATE_HERO = 10 | SCOPE_PLAYER,
			ACTION_UPDATE_HERO_STAT = 11 | SCOPE_PLAYER,
			ACTION_ADD_HERO_TO_BATTLE = 12,
			ACTION_INVITE = 13;
	private final static String TAG = "player";
	private final static ArrayList<Player> sPlayers = new ArrayList<Player>();
	private final OutputStream mOutputStream;
	private final InputStream mInputStream;
	// private final UUID mUUID;
	//private ArrayList<OnDataReceivedListener> mListeners = new ArrayList<OnDataReceivedListener>();
	private String mName;
	private final Parcel mParcel = Parcel.obtain();

	public Player(BluetoothSocket bluetoothSocket) throws IOException {
		this(bluetoothSocket.getRemoteDevice().getName(), bluetoothSocket.getOutputStream(), bluetoothSocket.getInputStream());
	}

	private Player() {
		mName = "Player #" + (int) (Math.random() * 10);

		//mUUID = UUID.randomUUID();
		mOutputStream = null;
		mInputStream = null;

	}

	private Player(String name, OutputStream outputStream, InputStream inputStream) {
		Log.v(TAG, "player connected");

		mName = name;
		//mUUID = UUID.randomUUID();
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
					while (mInputStream.read(buffer) > -1) {
						final Parcel m = Parcel.obtain();
						m.unmarshall(buffer, 0, buffer.length);
						Player.this.processMessage(m);

					}
				} catch (IOException ignored) { }

				Player.this.dispose();

			}

		};

		thread.start();

	}

	//public void writeHeaderToParcel(Parcel parcel) {
	//	parcel.writeLong(mUUID.getMostSignificantBits());
	//	parcel.writeLong(mUUID.getLeastSignificantBits());
	//}


	private void processMessage(Parcel m) {
		try {
			m.setDataPosition(0);

			final byte action = m.readByte();

			// Log.v(TAG, "message received");

			if ((action & SCOPE_PLAYER) != 0) {
				final Parcel resp = Parcel.obtain();
				//this.writeHeaderToParcel(resp);

				switch (action) {
					case Player.ACTION_GET_BATTLES: {
						ArrayList<Battle> ownBattles = Battle.getOwnBattles();
						resp.writeByte((byte) ownBattles.size());

						for (Battle battle : ownBattles) {
							if (battle.getOwner() == Player.CURRENT) {
								resp.writeString(battle.getName());
							}
						}
					}
					break;
					case Player.ACTION_GET_NAME: {
						resp.writeString(mName);
					}
					break;
					case Player.ACTION_GET_HERO: {
						final Hero hero = Hero.findHero(m.readString());
						if (hero == null) break;

						resp.writeString(hero.getName());
						resp.writeFloat(hero.getHealthPoint());
						resp.writeFloat(hero.getCharm());
						resp.writeFloat(hero.getBaseOffensivePoint());
						resp.writeFloat(hero.getBaseDefensivePoint());
						resp.writeInt(hero.getPicture());
					}
					break;
					case Player.ACTION_UPDATE_HERO: {
						final Hero hero = Hero.findHero(m.readString());
						if (hero == null) break;


					}
					break;
					case Player.ACTION_UPDATE_HERO_STAT: {

						final Hero hero = Hero.findHero(m.readString());
						final String name = m.readString();
						final Number number = (Number)m.readValue(null);

						for (final Field field : hero.getClass().getDeclaredFields()) {
							final HeroStatistics annotation = field.getAnnotation(HeroStatistics.class);
							if (annotation == null) continue;
							if (annotation.value().compareTo(name) != 0) continue;

							try {
								field.setAccessible(true);
								if (number instanceof Integer) {
									field.setInt(hero, (int) (field.getInt(hero) + number.intValue()));
								} else if (number instanceof Float) {
									field.setFloat(hero, (float) (field.getFloat(hero) + number.floatValue()));
								} else {
									Log.e(TAG, "unknown type");
								}

							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}

							break;

						}
					}
					break;
					default:
						break;

				}

				if (resp.dataSize() > 0) send(resp);
				resp.recycle();
			} else {
				final String name = m.readString();
				// Log.v(TAG, "process by battle " + name + "(" + action + ")");
				final Battle battle = Battle.findBattle(name);
				if (battle == null) throw new NullPointerException();

				battle.processMessage(action, m, this);
			}


		} catch (Exception e) {
			Log.v(TAG, "malformed message");
		}


	}

	// public UUID getUUID() { return mUUID; }

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

		//Log.v(TAG, "begin send message");

		synchronized (mParcel) {
			mParcel.setDataPosition(0);

			mParcel.writeByte(msg.mAction);
			if (msg.mName != null) mParcel.writeString(msg.mName);
			msg.extra(mParcel);

			send(mParcel);

		}

	}

	public void send(Parcel parcel) {

		if (mOutputStream != null) {
			try {
				mOutputStream.write(parcel.marshall());
			} catch (IOException ignored) { }
			;

			//Log.v(TAG, "message sent");
		} else {
			//Log.v(TAG, "loopback message");
			// loopback
			// TODO? static player HANDLER
			processMessage(mParcel);
		}

	}

	public static abstract class Message {

		final String mName;
		final byte mAction;

		public Message(byte action, Hero port) {
			mAction = action;
			mName = port.getName();
		}

		public Message(byte action, Battle port) {
			mAction = action;
			mName = port.getName();
		}

		public Message(byte action) {
			mAction = action;
			mName = null;
		}


		public abstract void extra(Parcel m);

	}

}
