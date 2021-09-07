package me.earth.phobos.mixin.mixins;

import javax.annotation.Nullable;

import me.earth.phobos.features.modules.render.Wireframe;
import me.earth.phobos.util.ColorUtil;
import org.spongepowered.asm.mixin.Overwrite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.entity.RenderDragon;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.model.ModelBase;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.client.renderer.entity.Render;

@Mixin({ RenderEnderCrystal.class })
public class MixinRenderEnderCrystal extends Render<EntityEnderCrystal>
{
    @Shadow
    private static final ResourceLocation ENDER_CRYSTAL_TEXTURES;
    @Shadow
    private final ModelBase modelEnderCrystal;
    @Shadow
    private final ModelBase modelEnderCrystalNoBase;

    protected MixinRenderEnderCrystal(final RenderManager renderManager) {
        super(renderManager);
        this.modelEnderCrystal = (ModelBase)new ModelEnderCrystal(0.0f, true);
        this.modelEnderCrystalNoBase = (ModelBase)new ModelEnderCrystal(0.0f, false);
    }

    @Overwrite
    public void doRender(final EntityEnderCrystal entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        final float f = entity.innerRotation + partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        this.bindTexture(MixinRenderEnderCrystal.ENDER_CRYSTAL_TEXTURES);
        float f2 = MathHelper.sin(f * 0.2f) / 2.0f + 0.5f;
        f2 += f2 * f2;
        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor((EntityEnderCrystal) entity));
        }
        if (Wireframe.getINSTANCE().isOn() && Wireframe.getINSTANCE().crystals.getValue()) {
            final float red = Wireframe.getInstance().red.getValue() / 255.0f;
            final float green = Wireframe.getInstance().green.getValue() / 255.0f;
            final float blue = Wireframe.getInstance().blue.getValue() / 255.0f;
            if (Wireframe.getINSTANCE().cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME) && Wireframe.getINSTANCE().crystalModel.getValue()) {
                this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f * 3.0f, f2 * 0.2f, 0.0f, 0.0f, 0.0625f);
            }
            GlStateManager.pushMatrix();
            GL11.glPushAttrib(1048575);
            if (Wireframe.getINSTANCE().cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME)) {
                GL11.glPolygonMode(1032, 6913);
            }
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            if (Wireframe.getINSTANCE().cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME)) {
                GL11.glEnable(2848);
            }
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glColor4f(((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getRed() / 255.0f) : red, ((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getGreen() / 255.0f) : green, ((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getBlue() / 255.0f) : blue, Wireframe.getINSTANCE().cAlpha.getValue() / 255.0f);
            if (Wireframe.getINSTANCE().cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME)) {
                GL11.glLineWidth((float)Wireframe.getINSTANCE().crystalLineWidth.getValue());
            }
            this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f * 3.0f, f2 * 0.2f, 0.0f, 0.0f, 0.0625f);
            GL11.glDisable(2896);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glColor4f(((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getRed() / 255.0f) : red, ((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getGreen() / 255.0f) : green, ((boolean)Wireframe.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(Wireframe.getInstance().rainbowHue.getValue()).getBlue() / 255.0f) : blue, Wireframe.getINSTANCE().cAlpha.getValue() / 255.0f);
            if (Wireframe.getINSTANCE().cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME)) {
                GL11.glLineWidth((float)Wireframe.getINSTANCE().crystalLineWidth.getValue());
            }
            this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f * 3.0f, f2 * 0.2f, 0.0f, 0.0f, 0.0625f);
            GlStateManager.enableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
        else {
            this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f * 3.0f, f2 * 0.2f, 0.0f, 0.0f, 0.0625f);
        }
        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        final BlockPos blockpos = entity.getBeamTarget();
        if (blockpos != null) {
            this.bindTexture(RenderDragon.ENDERCRYSTAL_BEAM_TEXTURES);
            final float f3 = blockpos.getX() + 0.5f;
            final float f4 = blockpos.getY() + 0.5f;
            final float f5 = blockpos.getZ() + 0.5f;
            final double d0 = f3 - entity.posX;
            final double d2 = f4 - entity.posY;
            final double d3 = f5 - entity.posZ;
            RenderDragon.renderCrystalBeams(x + d0, y - 0.3 + f2 * 0.4f + d2, z + d3, partialTicks, (double)f3, (double)f4, (double)f5, entity.innerRotation, entity.posX, entity.posY, entity.posZ);
        }
        super.doRender((EntityEnderCrystal) entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(final EntityEnderCrystal entityEnderCrystal) {
        return null;
    }

    static {
        ENDER_CRYSTAL_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
    }
}