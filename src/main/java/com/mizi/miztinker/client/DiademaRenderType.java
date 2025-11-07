package com.mizi.miztinker.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class DiademaRenderType extends RenderType {

    // OniMiko蝴蝶结渲染类型
    public static final Function<ResourceLocation, RenderType> ONIMIKO_BOW_RENDER = Util.memoize((resourceLocation) -> {
        RenderType.CompositeState state = CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .createCompositeState(true);
        return create("tcondiadema_onimiko_bow_render", DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS, 256, true, false, state);
    });

    private DiademaRenderType(String name, VertexFormat format, VertexFormat.Mode drawMode, int bufferSize,
                              boolean useDelegate, boolean needsSorting, Runnable setupTask, Runnable clearTask) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, setupTask, clearTask);
    }
}