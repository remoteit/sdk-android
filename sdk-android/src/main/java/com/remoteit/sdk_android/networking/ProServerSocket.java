package com.remoteit.sdk_android.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProServerSocket {
    private ServerSocket serverSocket;
    private final int port;
    private final NewClientHandler newClientHandler;
    private boolean canRun;

    public ProServerSocket(int port, NewClientHandler newClientHandler) {
        this.port = port;
        this.newClientHandler = newClientHandler;
    }

    public void Run(ExceptionHandler exceptionHandler) {
        this.canRun = true;

        new Thread(() -> {
            try {
                this.serverSocket = new ServerSocket();
                this.serverSocket.setReuseAddress(true);
                this.serverSocket.bind(new InetSocketAddress(this.port));

                while (this.canRun) {
                    Socket clientSocket = this.serverSocket.accept();
                    handleClient(clientSocket, exceptionHandler);
                }
            } catch (Exception e) {
                if (exceptionHandler != null) {
                    exceptionHandler.HandleException(e);
                }
            }
        }).start();
    }

    public void Stop() {
        this.canRun = false;

        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket socket, ExceptionHandler exceptionHandler) {
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                if (this.newClientHandler != null) {
                    this.newClientHandler.HandleNewClient(inputStream, outputStream);
                }
            } catch (Exception e) {
                if (exceptionHandler != null) {
                    exceptionHandler.HandleException(e);
                }
            }
        }).start();
    }
}
