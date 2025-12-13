package com.mizi.miztinker.renderer.other;


@FunctionalInterface
public interface ScreenResizeEventHandler {
    void consume(float w, float h);

}
