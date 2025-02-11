package com.codenamed.rodspawn.registry;

import com.codenamed.rodspawn.Rodspawn;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RodspawnModelTemplates {

    public static final ModelTemplate FLAT_HANDHELD_SPEAR_ITEM = createItem("handheld_spear", TextureSlot.LAYER0);

    private static ModelTemplate createItem(String itemModelLocation, TextureSlot... requiredSlots) {
        return new ModelTemplate(Optional.of(ResourceLocation.fromNamespaceAndPath(Rodspawn.MOD_ID,"item/" + itemModelLocation)), Optional.empty(), requiredSlots);
    }

    public  static  void init() {

    }
}
