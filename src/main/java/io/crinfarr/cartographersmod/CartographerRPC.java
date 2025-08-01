package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class CartographerRPC {
    private final ServerSocket srv;
    private Thread RPCThread;
    public CartographerRPC(int port) {
        try {
            this.srv = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LogUtils.getLogger().info("[Cartographer-RPC] Server opened on port {}", this.srv.getLocalPort());
        RPCThread = new Thread(() -> this.threadEventLoop(Minecraft.getInstance()));
        RPCThread.setDaemon(true);
    }
    protected void threadEventLoop(Minecraft mcInst) {
        LogUtils.getLogger().info("[Cartographer-RPC {}]: Waiting for connection", Thread.currentThread().toString());
        Socket channel;
        InputStream IncomingStream = null;
        OutputStream OutgoingStream = null;
        try {
            channel = this.srv.accept();
            IncomingStream = channel.getInputStream();
            OutgoingStream = channel.getOutputStream();
        } catch (SecurityException e) {
            LogUtils.getLogger().error("[Cartographer-RPC {}]: SecurityException {}",Thread.currentThread(), e.getLocalizedMessage());
        } catch (IOException e) {
            throw new RuntimeException("Uncaught IOException: "+e.getLocalizedMessage(), e);
        }
        assert (IncomingStream != null);
        assert (OutgoingStream != null);
        while(true) {
            try {
                final int COMMAND = IncomingStream.read();
                switch (COMMAND) {
                    //TODO MORE HERE
                    default:
                        LogUtils.getLogger().info("[Cartographer-RPC {}]: Unknown command byte {}", Thread.currentThread(), COMMAND);
                        OutgoingStream.write(new byte[]{0x00});
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
