
package com.ennismore.cape.mixin;

import com.ennismore.cape.client.CapeSelection;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {

    @Shadow
    @Final
    private HumanoidModel<AvatarRenderState> model;

    @Shadow
    private boolean hasLayer(
            ItemStack itemStack,
            EquipmentClientInfo.LayerType layerType
    ) {
        throw new AssertionError();
    }

    @Inject(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cape$renderSelectedCape(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            AvatarRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || state.id != minecraft.player.getId()) {
            return;
        }

        String selected = CapeSelection.getSelectedCape();

        if (selected.equals("default")) {
            return;
        }

        ci.cancel();

        if (selected.equals("none") || state.isInvisible || !state.showCape) {
            return;
        }

        if (hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
            return;
        }

        Identifier texture = Identifier.fromNamespaceAndPath(
                "cape",
                "textures/capes/" + selected + ".png"
        );

        if (minecraft.getResourceManager().getResource(texture).isEmpty()) {
            return;
        }

        poseStack.pushPose();

        if (hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
            poseStack.translate(0.0F, -0.053125F, 0.06875F);
        }

        collector.submitModel(
                model,
                state,
                poseStack,
                RenderTypes.entitySolid(texture),
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );

        poseStack.popPose();
    }
}
