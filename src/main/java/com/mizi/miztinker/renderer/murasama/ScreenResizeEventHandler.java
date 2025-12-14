package com.mizi.miztinker.renderer.murasama;


@FunctionalInterface
public interface ScreenResizeEventHandler {
    void consume(float w, float h);

}
