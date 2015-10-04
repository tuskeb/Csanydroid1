package hu.csany_zeg.one.csanydroid1.core;

import android.bluetooth.*;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Opponent {

	private static ArrayList<Opponent> sOpponents = new ArrayList<Opponent>();
	private OutputStream mOutputStream;
	private InputStream mInputStream;

	public static void setListener(OpponentListener listener) {
		mListener = listener;
	}

	private static OpponentListener mListener = null;

	public Opponent(BluetoothSocket bluetoohSocket) throws IOException {
		this();

		mOutputStream = bluetoohSocket.getOutputStream();
		mInputStream = bluetoohSocket.getInputStream();
	}

	private Opponent() {
		startWatching();

		if(mListener != null) {
			mListener.onOpponentAdded(this);
		}
	}

	public static Opponent getOpponent(int i) {
		return sOpponents.get(i);
	}

	public static int getOpponentsCount() {
		return sOpponents.size();
	}

	public static void watchOnBluetooth(final BluetoothAdapter bluetoothAdapter) {
		new Thread() {
			@Override
			public void run() {
				BluetoothServerSocket bluetoothServerSocket;
				try {
					bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Its me", UUID.randomUUID());
				} catch (IOException e) {
					return;
				}


				try {
					BluetoothSocket bluetoothSocket;
					for (; ; ) {
						bluetoothSocket = bluetoothServerSocket.accept();
						new Opponent(bluetoothSocket);
					}
				} catch (IOException e) {

				}

				try {
					bluetoothServerSocket.close();
				} catch (IOException e) { }

			}
		}.start();

	}

	public void startWatching() {
		new Thread() {
			byte[] buffer = new byte[128];
			@Override
			public void run() {
				try {
					for (; ; ) {
						int length = mInputStream.read(buffer);
						processMessage();
					}
				} catch (IOException e) {
					Opponent.this.dispose();
				}

			}

			public void processMessage() {


			}

		}.start();

	}

	public void dispose() {

		int i = Battle.sBattles.size();
		do {
			Battle battle = Battle.sBattles.get(--i);
			if (battle.mOpponent == this) {
				battle.dispose();
			}
		} while (i > 0);


		if(mListener != null) {
			mListener.onOpponentRemoved(this);
		}

		sOpponents.remove(this);

	}

	public void invite(Battle battle) {
		try {
			mOutputStream.write(10);
		} catch (IOException e) {

		}
	}

	public static abstract class OpponentListener {
		public abstract void onOpponentAdded(Opponent o);
		public abstract void onOpponentRemoved(Opponent o);
	}

}
