package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import static java.lang.Thread.sleep;

public class CartographerRPC {
    private final ServerSocket srv;
    private Thread RPCThread;
    private final Thread CoordinatorThread;
    public CartographerRPC(int port) {
        try {
            this.srv = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LogUtils.getLogger().info("[Cartographer-RPC] Server opened on port {}", this.srv.getLocalPort());
        RPCThread = new Thread(() -> this.threadEventLoop(Minecraft.getInstance()));
        CoordinatorThread = new Thread(() -> {
            LogUtils.getLogger().info("[Cartographer-RPC {}] Coordinator thread started", Thread.currentThread());
            while (true) {
                while (RPCThread.isAlive()) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                RPCThread = new Thread(() -> threadEventLoop(Minecraft.getInstance()));
                RPCThread.setDaemon(true);
                RPCThread.start();
            }
        });
        RPCThread.setDaemon(true);
        CoordinatorThread.setDaemon(true);
        CoordinatorThread.start();
        RPCThread.start();
    }
    protected void threadEventLoop(Minecraft mcInst) {
        LogUtils.getLogger().info("[Cartographer-RPC {}]: Waiting for connection", Thread.currentThread());
        Socket channel = null;
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
        assert (channel != null);
        assert (IncomingStream != null);
        assert (OutgoingStream != null);
        while(true) {
            try {
                final int COMMAND = IncomingStream.read();
                OutputStream os = OutgoingStream;
                switch (COMMAND) {
                    case 0x01://All Crafting Types
                        ForgeRegistries.RECIPE_SERIALIZERS.getKeys().forEach(resourceLocation -> {
                            try {
                                os.write(resourceLocation.toString().getBytes());
                                os.write('\n');
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case 0x02://All Items
                        ForgeRegistries.ITEMS.getKeys().forEach(resourceLocation -> {
                            try {
                                os.write(resourceLocation.toString().getBytes());
                                os.write('\n');
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case 0x03://All Fluids
                        ForgeRegistries.FLUIDS.getKeys().forEach(resourceLocation -> {
                            try {
                                os.write(resourceLocation.toString().getBytes());
                                os.write('\n');
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case 0x04: //Item render
                        String name = new String(IncomingStream.readNBytes(IncomingStream.read()));
                        //TODO RENDER CODE HERE
                        break;
                    default:
                        LogUtils.getLogger().info("[Cartographer-RPC {}]: Unknown command byte {}", Thread.currentThread(), COMMAND);
                        IncomingStream.readNBytes(IncomingStream.available());
                        OutgoingStream.write("UNKNOWN COMMAND BYTE".getBytes());
                }
                //Drain socket
                IncomingStream.readNBytes(IncomingStream.available());
            } catch (SocketException se) {
                LogUtils.getLogger().warn("[Cartographer-RPC Thread {}]: Socket did not exit correctly!", Thread.currentThread());
                LogUtils.getLogger().warn("[Cartographer-RPC Thread {}]: SocketException: {}", Thread.currentThread(), se.getLocalizedMessage());
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close socket", e);
                }
                break;
            } catch (IOException e) {
                throw new RuntimeException("IOException in RPC server thread: "+e.getLocalizedMessage(), e);
            }
        }
    }
}
