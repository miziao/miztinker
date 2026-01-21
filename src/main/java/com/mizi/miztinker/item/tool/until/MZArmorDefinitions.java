package com.mizi.miztinker.item.tool.until;

import com.mizi.miztinker.miztinker;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;

public class MZArmorDefinitions {
    public MZArmorDefinitions() {
    }

    public static final ModifiableArmorMaterial SOULIZATION;

    static {
        SOULIZATION = ModifiableArmorMaterial.create(miztinker.location("soulization"), Sounds.EQUIP_PLATE.getSound());
    }
}
