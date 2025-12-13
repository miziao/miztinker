package com.mizi.miztinker.renderer.other;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Queue;

import static net.minecraft.client.Minecraft.ON_OSX;

public class PostEffectPipelines {
    public static final Queue<Pipeline> PostEffectQueue = Queues.newConcurrentLinkedQueue();

    @Getter
    private static boolean Active = false;

    public static void active(){
        Active = true;
    }

    public static void close(){
        Active = false;
    }

    public static RenderTarget getSource(){
        if(Minecraft.getInstance().levelRenderer.transparencyChain == null){
            return Minecraft.getInstance().getMainRenderTarget();
        }
        else {
            return Minecraft.getInstance().levelRenderer.getParticlesTarget();
        }
    }

    public static abstract class Pipeline implements Comparable<Pipeline>{
        protected boolean called = false;
        protected boolean started = false;
        protected RenderTarget bufferTarget;
        public final ResourceLocation name;

        public int priority = 0;

        @Override
        public int compareTo(Pipeline o) {
            if(priority > o.priority) return 1;
            else if(priority == o.priority) return 0;
            else return -1;
        }

        public Pipeline(ResourceLocation name){
            this.name = name;
        }

        public void start(){
            if(started){
                if(Active){
                    //ClientCommands.Debug();
                    bufferTarget.copyDepthFrom(getSource());
                    bufferTarget.bindWrite(false);
                }
            }
            else {
                if(bufferTarget == null){
                    bufferTarget = TargetManager.getTarget(name);
                    bufferTarget.clear(ON_OSX);
                }

                RenderTarget main = getSource();
                if(Active){
                    //System.out.println("push")
                    bufferTarget.copyDepthFrom(main);
                    PostEffectQueue.add(this);
                    bufferTarget.bindWrite(false);
                    started = true;
                }
                //System.out.println("push");
            }
        }

        public void call(){
            if(Active) {
                //ClientCommands.Debug();
                called = true;
            }
        }

        public void suspend(){
            if(Active){
                //System.out.println("aaaaa");
                bufferTarget.unbindWrite();
                bufferTarget.unbindRead();
                RenderTarget rt = getSource();
                rt.copyDepthFrom(bufferTarget);
                rt.bindWrite(false);
            }
            else {
                //bufferTarget.clear(Minecraft.ON_OSX);
                getSource().bindWrite(false);
            }
            //ClientCommands.Debug();
        }
        public abstract void PostEffectHandler();
        public void HandlePostEffect(){
            if(called) PostEffectHandler();
            bufferTarget = null;
            started = false;
            called = false;
        }
    }


}
