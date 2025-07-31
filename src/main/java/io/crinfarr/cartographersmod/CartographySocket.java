package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.NotActiveException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import static java.lang.Thread.sleep;

public class CartographySocket {
    private final AsynchronousSocketChannel socketChannel;
    private Thread serverThread;

    /**
     * @return the listening port
     * @throws IOException if getLocalAddress fails
     * @throws NotActiveException if socket is closed
     */
    public int getPort() throws IOException{
        if (!socketChannel.isOpen()) {
            throw new NotActiveException("Cartographer RPC server not active");
        } else return ((InetSocketAddress)socketChannel.getLocalAddress()).getPort();
    }

    public CartographySocket() {
        try {
            this.socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open socket: IOException: "+e.getLocalizedMessage(), e);
        }
    }
    public void bind(String address, @Nullable Integer port) {
        try {
            this.socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024*1024);
            this.socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024*1024);
            this.socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            this.socketChannel.bind(new InetSocketAddress(address, (port == null) ? 0 : port));
        } catch (ConnectionPendingException e) {
            throw new RuntimeException("Bind already pending on "+((port == null) ? 0 : port)+":"+e.getLocalizedMessage(), e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException("Port "+((port == null) ? 0 : port)+" already bound:"+e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Generic IOException whiel binding on "+((port == null) ? 0 : port)+":"+e.getLocalizedMessage(), e);
        }
    }
    public void bindClient(Minecraft inst) {
        this.serverThread = new Thread(() -> {
            while (true) {
                try {
                    this.socketChannel.read(ByteBuffer.allocate(1));
                    break;
                } catch (NotYetConnectedException e) {
                    LogUtils.getLogger().info("Waiting for socket to open");
                    try {
                        sleep(2000);
                    } catch (InterruptedException ex) {
                        LogUtils.getLogger().warn("Server thread interrupted!: {}", e.getLocalizedMessage(), e);
                        continue;
                    }
                }
            }
            LogUtils.getLogger().info("Remote connected");
            ByteBuffer inputBuffer = ByteBuffer.allocate(1024*1024);
            final Minecraft MC_INST = inst;
            while (true) {
                try {
                    switch (inputBuffer.get(0)) {
                        case 0x01: //Return list of crafting providers
                            inputBuffer.clear(); // No further data required
                            if (MC_INST.level == null) {
                                this.socketChannel.write(ByteBuffer.wrap((0xff+"Level Not Loaded\0").getBytes()));
                            }
                            break;
                        default:
                            break;
                    }
                    sleep(10);
                    this.socketChannel.read(inputBuffer);
                } catch (InterruptedException e) {
                    LogUtils.getLogger().warn("Server thread interrupted!: {}", e.getLocalizedMessage(), e);
                }
            }
        });
        this.serverThread.setDaemon(true);
        this.serverThread.start();
    }
}
