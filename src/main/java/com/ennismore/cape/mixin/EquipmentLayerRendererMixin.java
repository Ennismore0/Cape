package com.ennismore.cape.mixin;

import com.ennismore.cape.client.CapeSelection;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {

    @Redirect(
            method = "renderLayers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            ),
            require = 1
    )
    @SuppressWarnings("unchecked")
    private Object cape$replaceWingsTexture(
            Function<?, ?> lookup,
            Object key
    ) {
        Object result = ((Function<Object, Object>) lookup).apply(key);
        String selected = CapeSelection.getSelectedCape();
        String keyText = key.toString();

        if (keyText.startsWith("LayerTextureKey[")
                && keyText.contains("layerType=WINGS")
                && !selected.equals("default")
                && !selected.equals("none")) {
            return Identifier.fromNamespaceAndPath(
                    "cape",
                    "textures/capes/" + selected + ".png"
            );
        }

        return result;
    }
}
