package remoteit.remoteit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import remoteit.helpers.Helpers;
import remoteit.transport.TransportConnectionStatus;
import remoteit.transport.TransportConnectionStatusHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ConnectD {
    private final static String LogTag = ConnectD.class.toString();

    private Process process;

    @SuppressLint("LogNotTimber")
    public void Run(Context context, final String args, TransportConnectionStatusHandler connectionStatusHandler) {

        try {
            String connectdPath = context.getApplicationInfo().nativeLibraryDir + "/libconnectd.so";
            process = Runtime.getRuntime().exec(connectdPath + " " + args);

            new Thread(() -> {
                try {
                    Log.d(LogTag, "starting");

                    if (connectionStatusHandler != null) {
                        connectionStatusHandler.HandleTransportConnectionStatus(TransportConnectionStatus.Connecting);
                    }

                    BufferedReader dataReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = dataReader.readLine()) != null) {
                        Log.d(LogTag, String.format("STDOUT: %s", line));

                        if (line.contains("server connection changed to state 5")) {
                            if (connectionStatusHandler != null) {
                                Log.d(LogTag, "STDOUT: CONNECTED");
                                connectionStatusHandler.HandleTransportConnectionStatus(TransportConnectionStatus.Connected);
                            }
                        } else if (line.contains("!!exit")) {
                            Log.d(LogTag, "STDOUT: DISCONNECTED");
                            if (connectionStatusHandler != null) {
                                connectionStatusHandler.HandleTransportConnectionStatus(TransportConnectionStatus.Disconnected);
                            }
                        }
                    }

//					BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//					while ((line = errReader.readLine()) != null) {
//						Log.d(LogTag, String.format("STDERR: ", line));
//					}

                    process.waitFor();

                    Log.d(LogTag, "finished");
                } catch (IOException e) {
                    Helpers.ReportException(LogTag, e);
                } catch (Exception e) {
                    Helpers.ReportException(LogTag, e);
                    Thread currentThread = Thread.currentThread();
                    Objects.requireNonNull(currentThread.getUncaughtExceptionHandler()).uncaughtException(currentThread, e);
                }
            }).start();

        } catch (IOException e) {
            Helpers.ReportException(LogTag, e);
        }
    }

    public void Stop() {
        if (this.process != null) {
            this.process.destroy();
        }
    }
}
