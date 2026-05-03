package com.mizi.miztinker.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class GlobalStructureHandler {

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide || !(level instanceof ServerLevel toLevel)) return;

        ResourceLocation toDimLocation = event.getTo().location();

        if (toDimLocation.getNamespace().equals("miztinker") &&
                toDimLocation.getPath().equals("diamond_continent")) {

            Player player = event.getEntity();
            GlobalPortalData data = GlobalPortalData.get(toLevel);

            if (!data.isHomeSpawned()) {
                BlockPos playerPos = player.blockPosition();
                BlockPos structurePos = playerPos.east(3).below(4);

                if (generateStructure(toLevel, structurePos)) {
                    data.setHomeSpawned(true);
                }
            }
        }
    }

    private static boolean generateStructure(ServerLevel level, BlockPos pos) {
        StructureTemplateManager manager = level.getStructureManager();

        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath(MODID, "small_home");

        Optional<StructureTemplate> templateOpt = manager.get(structureId);

        if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setIgnoreEntities(false)
                    .setKeepLiquids(false);

            if (level.isLoaded(pos)) {
                template.placeInWorld(level, pos, pos, settings, level.getRandom(), 3);
                return true;
            }
        }
        return false;
    }
}