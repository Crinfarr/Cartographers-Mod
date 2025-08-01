package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

@Mod(CartographersMod.MODID)
public class CartographersMod{
    public static final String MODID = "cartographersmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    private CartographerRPC comms;
    public CartographersMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
    @SubscribeEvent
    public void lifecycle(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        comms = new CartographerRPC(31201);
    }
}
