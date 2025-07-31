package io.crinfarr.cartographersmod;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.codehaus.plexus.util.dag.Vertex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.Buffer;
import java.util.*;

@Mod(CartographersMod.MODID)
public class CartographersMod {
    public static final String MODID = "cartographersmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CartographersMod() {
//        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//        modEventBus.register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void renderItems(final net.minecraftforge.client.event.ClientChatEvent event) {
        LOGGER.info("ChatEvent, text={}", event.getMessage());
        if (!event.getMessage().equals("!--CartographersTableClientDump--!"))
            return;
        @NotNull Set<Map.Entry<ResourceKey<Item>, Item>> items = ForgeRegistries.ITEMS.getEntries();
        LOGGER.info("Trying to dump items");
        ItemRenderer r = Minecraft.getInstance().getItemRenderer();
        items.forEach(item -> {
            LOGGER.info("Dumping {}", item.getKey().location());
            if (item.getKey().location().toString().equals("minecraft:debug_stick"))
                return;
//            final OutputStream oStream;
//            try {
//                oStream = cartographersComms.getOutputStream();
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to get output stream on socket", e);
//            }
            BakedModel model = r.getModel(
                    item.getValue().getDefaultInstance(),
                    Minecraft.getInstance().level,
                    Minecraft.getInstance().player,
                    2);
            Block blockinst = Block.byItem(item.getValue());
            assert Minecraft.getInstance().player != null;
            List<BakedQuad> quads = model.getQuads(
                    blockinst.defaultBlockState(),
                    null,
                    Minecraft.getInstance().player.getRandom(),
                    ModelData.EMPTY,
                    null
            );
            LOGGER.info("Sprite: {}", quads);
            if (!quads.isEmpty()) {
                LOGGER.info("Detail: {}", quads.get(0).getSprite());
            }
        });
    }
}
