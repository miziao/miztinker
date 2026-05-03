package com.mizi.miztinker.util;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;


@Getter
public class GlobalPortalData extends SavedData {
    private boolean homeSpawned = false;

    public GlobalPortalData() {}

    public static GlobalPortalData load(CompoundTag tag) {
        GlobalPortalData data = new GlobalPortalData();
        data.homeSpawned = tag.getBoolean("homeSpawned");
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("homeSpawned", homeSpawned);
        return tag;
    }

    public void setHomeSpawned(boolean value) {
        this.homeSpawned = value;
        this.setDirty();
    }

    @NotNull
    public static GlobalPortalData get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerLevel overworld = server.overworld();

        return overworld.getDataStorage()
                .computeIfAbsent(GlobalPortalData::load, GlobalPortalData::new, "miztinker_global_config");
    }
}