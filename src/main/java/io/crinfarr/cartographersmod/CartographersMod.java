package io.crinfarr.cartographersmod;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.targets.FMLServerLaunchHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

@Mod(CartographersMod.MODID)
public class CartographersMod {
    public static final String MODID = "cartographersmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CartographersMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//        modEventBus.addListener(this::postCommonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void postCommonSetup(final net.minecraftforge.event.server.ServerStartedEvent _event) {
        LOGGER.info("Entering postCommonSetup");
        @NotNull Set<Map.Entry<ResourceKey<Item>, Item>> items = ForgeRegistries.ITEMS.getEntries();
        final Stack<String> itemStack = new Stack<>();
        items.forEach(item -> {
            final Set<ITag<Item>> tags = ForgeRegistries.ITEMS.tags().stream().filter(itag -> {
                return itag.contains(item.getValue());
            }).collect(Collectors.toSet());
            itemStack.push(
                    String.format(
                            "%c%s%c%s",
                            item
                                    .getKey()
                                    .location()
                                    .toString()
                                    .length(),
                            item
                                    .getKey()
                                    .location(),
                            tags.size(),
                            tags.stream().map(itag -> {
                                return String.format("%c%s",
                                        itag
                                                .getKey()
                                                .location()
                                                .toString()
                                                .length(),
                                        itag
                                                .getKey()
                                                .location()
                                );
                            }).collect(Collectors.joining())
                    )
            );
        });
        final String outString = String.join("", itemStack);
        try (FileWriter fileWriter = new FileWriter("items.dump")) {
            fileWriter.write(outString);
            LOGGER.info("Dumped {} items", items.size());
            FileWriter doneFile = new FileWriter(".done");
            doneFile.write(0x00);
            doneFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
