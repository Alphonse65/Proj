package fr.rise;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public final class WorldRender {
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("rise", "pipeline/filled_through_walls"))
                    .withDepthStencilState(Optional.empty())
                    .build());

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(() -> "Rise Buffer", RenderType.SMALL_BUFFER_SIZE);

    private static volatile List<Boxes.BoxState> boxes = List.of();

    public static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(WorldRender::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(WorldRender::draw);
    }

    // Phase d'extraction : on lit le monde et on construit l'etat immuable a dessiner
    private static void extract(LevelExtractionContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { boxes = List.of(); return; }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Boxes out = new Boxes();
        for (Module m : Modules.all()) if (m.isEnabled()) m.extract(out, partial);
        boxes = List.copyOf(out.list);
    }

    // Phase de dessin
    private static void draw(LevelRenderContext context) {
        List<Boxes.BoxState> list = boxes;
        if (list.isEmpty()) return;

        RenderPipeline renderPipeline = FILLED_THROUGH_WALLS;
        VertexFormat formatBinding = renderPipeline.getVertexFormatBinding(0);
        assert formatBinding != null;

        PrimitiveTopology primitive = renderPipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive,
                primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        final var builder = stagedBuffer.getVertexBuilder(draw);
        for (Boxes.BoxState b : list) {
            filledBox(matrices.last().pose(), builder, (float) b.x1(), (float) b.y1(), (float) b.z1(),
                    (float) b.x2(), (float) b.y2(), (float) b.z2(), b.r(), b.g(), b.b(), b.a());
        }
        matrices.popPose();

        stagedBuffer.upload();
        StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);
        if (info != null) execute(Minecraft.getInstance(), info, renderPipeline);
        stagedBuffer.endFrame();
    }

    private static void filledBox(Matrix4fc m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        v.addVertex(m, x1, y1, z2).setColor(r, g, b, a); v.addVertex(m, x2, y1, z2).setColor(r, g, b, a);
        v.addVertex(m, x2, y2, z2).setColor(r, g, b, a); v.addVertex(m, x1, y2, z2).setColor(r, g, b, a);

        v.addVertex(m, x2, y1, z1).setColor(r, g, b, a); v.addVertex(m, x1, y1, z1).setColor(r, g, b, a);
        v.addVertex(m, x1, y2, z1).setColor(r, g, b, a); v.addVertex(m, x2, y2, z1).setColor(r, g, b, a);

        v.addVertex(m, x1, y1, z1).setColor(r, g, b, a); v.addVertex(m, x1, y1, z2).setColor(r, g, b, a);
        v.addVertex(m, x1, y2, z2).setColor(r, g, b, a); v.addVertex(m, x1, y2, z1).setColor(r, g, b, a);

        v.addVertex(m, x2, y1, z2).setColor(r, g, b, a); v.addVertex(m, x2, y1, z1).setColor(r, g, b, a);
        v.addVertex(m, x2, y2, z1).setColor(r, g, b, a); v.addVertex(m, x2, y2, z2).setColor(r, g, b, a);

        v.addVertex(m, x1, y2, z2).setColor(r, g, b, a); v.addVertex(m, x2, y2, z2).setColor(r, g, b, a);
        v.addVertex(m, x2, y2, z1).setColor(r, g, b, a); v.addVertex(m, x1, y2, z1).setColor(r, g, b, a);

        v.addVertex(m, x1, y1, z1).setColor(r, g, b, a); v.addVertex(m, x2, y1, z1).setColor(r, g, b, a);
        v.addVertex(m, x2, y1, z2).setColor(r, g, b, a); v.addVertex(m, x1, y1, z2).setColor(r, g, b, a);
    }

    private static void execute(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();
        assert colorTexture != null;

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "rise render", colorTexture, Optional.empty(),
                        mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }
}
