package hu.csany_zeg.one.csanydroid1.core;

import android.bluetooth.*;
import android.os.Parcel;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Player {

    public static final Player CURRENT = new Player();

    public final static byte
            ACTION_NO_OPERATION = 0,
            ACTION_READY_FOR_BATTLE = 1,
            ACTION_GIVE_UP = 2,
            ACTION_GET_BATTLES = 3,
            ACTION_GET_BATTLE = 4,
            ACTION_INVITE = 5;

    private final static ArrayList<Player> sPlayers = new ArrayList<Player>();

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

                Log.v("bluetooth", "listening started");

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


    private final OutputStream mOutputStream;
    private final InputStream mInputStream;
    private String mName;
    private final UUID mUUID;

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
        Log.v("player", "player connected");

        mName = name;
        mUUID = UUID.randomUUID();
        mOutputStream = outputStream;
        mInputStream = inputStream;

        startListening();

        sPlayers.add(this);

    }


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

                        // üzenet érkezett tőle
                        long mostSigBits = parcel.readLong();
                        long leastSigBits = parcel.readLong();

                        Battle battle = Battle.findBattleByUUID(new UUID(mostSigBits, leastSigBits));
                        if (battle != null) {
                            battle.processMessage(parcel, Player.this);
                        } else {
                            this.processMessage(parcel);
                        }

                    }
                } catch (IOException e) {
                    Player.this.dispose();
                }

                Log.v("server", "end listening");

            }

            private void processMessage(Parcel parcel) {
                Parcel response = Parcel.obtain();

                switch (parcel.readByte()) {
                    case ACTION_GET_BATTLES:
                        ArrayList<Battle> ownBattles = Battle.getOwnBattles();
                        response.writeByte((byte)ownBattles.size());

                        for(Battle battle : ownBattles) {
                            if(battle.getServer() == Player.CURRENT) {
                                battle.writeHeaderToParcel(response);
                            }
                        }
                        break;
                }

                Player.this.send(response);

            }

        };

        thread.start();

    }

    public static int getOpponentsCount() {
        return sPlayers.size();
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

    public void send(Parcel data) {

        try {
            mOutputStream.write(data.marshall());
        } catch (IOException e) { }

    }

}
