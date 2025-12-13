package com.mizi.miztinker.gui.hudhelder;

public class MurasamaDrawTime {
    public static float energy_quantity;
    public static float energy_point;

    public static void setEnergyQuantityPercentage(float f){
        energy_quantity =f;
    }
    public static float getEnergyQuantityPercentage(){
        return energy_quantity;
    }

    public static void setEnergyPointPercentage(float f){
        energy_point =f;
    }
    public static float getEnergyPointPercentage(){
        return energy_point;
    }
}
