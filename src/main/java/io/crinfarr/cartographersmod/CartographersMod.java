package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

@Mod(CartographersMod.MODID)
public class CartographersMod {
    public static final String MODID = "cartographersmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public CartographersMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::postCommonSetup);


        MinecraftForge.EVENT_BUS.register(modEventBus);
    }

    private void postCommonSetup(final FMLClientSetupEvent _event) {
        LOGGER.info("Entering postCommonSetup");
        @NotNull Set<Map.Entry<ResourceKey<Item>, Item>> items = ForgeRegistries.ITEMS.getEntries();
        final Stack<String> itemStack = new Stack<>();
        items.forEach(item -> itemStack.push(
                String.format(
                        "%c%s",
                        item
                                .getKey()
                                .location()
                                .toString()
                                .length(),
                        item
                                .getKey()
                                .location()
                )
        ));
        final String outString = String.join("", itemStack);
        try (FileWriter fileWriter = new FileWriter("cartographyMap.dump")) {
            fileWriter.write(outString);
            LOGGER.info("Dumped {} items", items.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
