package fun.sodium.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.sodium.api.events.EventLink;
import fun.sodium.api.events.implement.Event3DRender;
import fun.sodium.api.events.implement.EventAttackEntity;
import fun.sodium.api.events.implement.EventRender;
import fun.sodium.api.utils.color.ColorUtils;
import fun.sodium.api.utils.render.RenderUtils;
import fun.sodium.client.modules.Module;
import fun.sodium.client.modules.settings.implement.BooleanSetting;
import fun.sodium.client.modules.settings.implement.FloatSetting;
import fun.sodium.client.modules.settings.implement.ModeSetting;
import fun.sodium.client.modules.settings.implement.TextSetting;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KillEffect extends Module {

    public static KillEffect INSTANCE = new KillEffect();

    /**
     * Р   Р  Р’ВµР  Р’В¶Р  РЎвЂР  РЎВР РЋРІР‚в„– Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р  Р’В°. statue = Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В° Р  Р вЂ¦Р  Р’Вµ Р  РЎвЂ”Р  Р’В°Р  РўвЂР  Р’В°Р  Р’ВµР РЋРІР‚С™, Р  Р’В° Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚ВР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  Р’В° Р  РЎВР  Р’ВµР РЋР С“Р РЋРІР‚С™Р  Р’Вµ
     * Р  РЎвЂ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р вЂ¦Р  РЎвЂўР  Р вЂ Р  РЎвЂР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р РЋРІР‚РЋР  Р’В°Р РЋР С“Р РЋРІР‚С™Р РЋР Р‰Р РЋР вЂ№ Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р  Р’В° (Р  Р’В»Р РЋРІР‚ВР  РўвЂ, Р  РЎВР  Р’В°Р  РЎвЂ“Р  РЎВР  Р’В°, Р РЋР С“Р  РЎвЂ”Р  Р’В°Р  РЎвЂ“Р  Р’ВµР РЋРІР‚С™Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚С›Р  РЎвЂР  РЎвЂќР  Р’В°Р РЋРІР‚ Р  РЎвЂР РЋР РЏ, Р  Р’В±Р  Р’В°Р  Р’В±Р  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  РЎвЂ).
     */
    public enum EffectMode {
        RIFT("Р   Р  Р’В°Р  Р’В·Р  Р’В»Р  РЎвЂўР  РЎВ", 2600f, false),
        BLACK_HOLE("Р  Р’В§Р РЋРІР‚ВР РЋР вЂљР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР  Р’В°", 4400f, true),
        MAGMA("Р  РЎС™Р  Р’В°Р  РЎвЂ“Р  РЎВР  Р’В°", 4600f, true),
        CRYSTAL("Р  РЎв„ўР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»", 3900f, true),
        AURORA("Р  Р Р‹Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР  Р’Вµ", 4200f, true),
        VAN("Р‘СѓСЃРёРє", 7500f, true),
        SOUL("Р   Р  РЎвЂР РЋРІР‚С™Р РЋРЎвЂњР  Р’В°Р  Р’В» Р  РўвЂР РЋРЎвЂњР РЋРІвЂљВ¬", 3500f, false);

        public final String title;
        public final float duration;
        public final boolean statue;

        EffectMode(String title, float duration, boolean statue) {
            this.title = title;
            this.duration = duration;
            this.statue = statue;
        }
    }

    // ===== Р  РЎСљР  Р’В°Р РЋР С“Р РЋРІР‚С™Р РЋР вЂљР  РЎвЂўР  РІвЂћвЂ“Р  РЎвЂќР  РЎвЂ Р  Р вЂ  GUI =====
    private final ModeSetting modeSetting = new ModeSetting("Р   Р  Р’ВµР  Р’В¶Р  РЎвЂР  РЎВ", "Р  РЎСџР  РЎвЂў Р  РЎвЂўР РЋРІР‚РЋР  Р’ВµР РЋР вЂљР  Р’ВµР  РўвЂР  РЎвЂ",
            "Р  РЎСџР  РЎвЂў Р  РЎвЂўР РЋРІР‚РЋР  Р’ВµР РЋР вЂљР  Р’ВµР  РўвЂР  РЎвЂ", "Р  Р Р‹Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  Р’В°Р  РІвЂћвЂ“Р  Р вЂ¦Р  РЎвЂў", "Р   Р  Р’В°Р  Р’В·Р  Р’В»Р  РЎвЂўР  РЎВ", "Р  Р’В§Р РЋРІР‚ВР РЋР вЂљР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР  Р’В°", "Р  РЎС™Р  Р’В°Р  РЎвЂ“Р  РЎВР  Р’В°", "Р  РЎв„ўР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»", "Р  Р Р‹Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР  Р’Вµ", "Р   Р  РЎвЂР РЋРІР‚С™Р РЋРЎвЂњР  Р’В°Р  Р’В» Р  РўвЂР РЋРЎвЂњР РЋРІвЂљВ¬", "Р‘СѓСЃРёРє");
    private final FloatSetting sizeSetting = new FloatSetting("Р   Р  Р’В°Р  Р’В·Р  РЎВР  Р’ВµР РЋР вЂљ", 1.0f, 0.5f, 2.5f, 0.05f);
    private final FloatSetting speedSetting = new FloatSetting("Р  Р Р‹Р  РЎвЂќР  РЎвЂўР РЋР вЂљР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР Р‰", 1.0f, 0.4f, 2.5f, 0.05f);
    private final FloatSetting brightnessSetting = new FloatSetting("Р  Р вЂЎР РЋР вЂљР  РЎвЂќР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР Р‰", 1.0f, 0.3f, 2.0f, 0.05f);
    private final ModeSetting colorSetting = new ModeSetting("Р  Р’В¦Р  Р вЂ Р  Р’ВµР РЋРІР‚С™", "Р  РЎС›Р  Р’ВµР  РЎВР  Р’В°", "Р  РЎС›Р  Р’ВµР  РЎВР  Р’В°", "Р  Р Р‹Р  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“");
    private final TextSetting customHex = new TextSetting("Р  Р Р‹Р  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“ Р РЋРІР‚ Р  Р вЂ Р  Р’ВµР РЋРІР‚С™ HEX", "#9B5CFF", 9)
            .visible(() -> colorSetting.is("Р  Р Р‹Р  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“"));
    private final BooleanSetting screenFlash = new BooleanSetting("Р­РєСЂР°РЅРЅР°СЏ РІСЃРїС‹С€РєР°", true);
    private final FloatSetting flashPower = new FloatSetting("РЎРёР»Р° РІСЃРїС‹С€РєРё", 1.0f, 0.2f, 2.0f, 0.05f);
    private final BooleanSetting onlyPlayers = new BooleanSetting("Р  РЎС›Р  РЎвЂўР  Р’В»Р РЋР Р‰Р  РЎвЂќР  РЎвЂў Р  РЎвЂР  РЎвЂ“Р РЋР вЂљР  РЎвЂўР  РЎвЂќР  РЎвЂ", false);

    private int cycleIndex = 0;

    private EffectMode pickMode() {
        EffectMode[] all = EffectMode.values();
        if (modeSetting.is("Р  Р Р‹Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  Р’В°Р  РІвЂћвЂ“Р  Р вЂ¦Р  РЎвЂў")) {
            return all[modeRandom.nextInt(all.length)];
        }
        if (modeSetting.is("Р  РЎСџР  РЎвЂў Р  РЎвЂўР РЋРІР‚РЋР  Р’ВµР РЋР вЂљР  Р’ВµР  РўвЂР  РЎвЂ")) {
            return all[1 + (cycleIndex++ % (all.length - 1))];
        }
        for (EffectMode m : all) {
            if (modeSetting.is(m.title)) {
                return m;
            }
        }
        return EffectMode.BLACK_HOLE;
    }

    private int pickColor() {
        if (colorSetting.is("Р  Р Р‹Р  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“")) {
            try {
                String s = customHex.get().trim().replace("#", "");
                return 0xFF000000 | (Integer.parseUnsignedInt(s.length() > 6 ? s.substring(s.length() - 6) : s, 16) & 0xFFFFFF);
            } catch (Exception ignored) {
                return 0xFF9B5CFF;
            }
        }
        return ColorUtils.getThemeColor();
    }

    private static final Identifier GLOW_TEX =
            Identifier.of("sodium", "textures/particle/bloom.png");

    private static final float HEIGHT = 4.8f;
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final float PI = (float) Math.PI;
    private static final float HALF_PI = PI * 0.5f;

    private static final int MAX_EFFECTS = 6;
    private static final int PARTICLE_COUNT = 56;
    private static final long TRACK_TIMEOUT_MS = 10_000L;
    /** Р”Р»РёС‚РµР»СЊРЅРѕСЃС‚СЊ СЌРєСЂР°РЅРЅРѕРіРѕ РёРјРїР°РєС‚Р° (РІСЃРїС‹С€РєР° + РєРѕР»СЊС†Рѕ). */
    private static final long IMPACT_MS = 850L;

    /** Р  РЎв„ўР РЋРЎвЂњР  РўвЂР  Р’В° Р  РЎвЂ”Р РЋР вЂљР РЋР РЏР РЋРІР‚РЋР  Р’ВµР  РЎВ Р  Р вЂ Р  Р’В°Р  Р вЂ¦Р  РЎвЂР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋРІР‚С™Р РЋР вЂљР РЋРЎвЂњР  РЎвЂ”, Р  РЎвЂ”Р  РЎвЂўР  РЎвЂќР  Р’В° Р РЋР С“Р  Р’ВµР РЋР вЂљР  Р вЂ Р  Р’ВµР РЋР вЂљ Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  Р вЂ¦Р  Р’Вµ Р РЋРЎвЂњР  РўвЂР  Р’В°Р  Р’В»Р  РЎвЂР  Р’В» (Р РЋРІР‚РЋР РЋРІР‚С™Р  РЎвЂўР  Р’В±Р РЋРІР‚в„– Р  РЎвЂўР  Р вЂ¦ Р  Р вЂ¦Р  Р’Вµ Р  РЎвЂ”Р  Р’В°Р  РўвЂР  Р’В°Р  Р’В»). */
    private static final double HIDE_DEPTH = 2048.0;

    /** Р  РЎСљР  Р’В°Р  РЎвЂ”Р РЋР вЂљР  Р’В°Р  Р вЂ Р  Р’В»Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ Р вЂ™Р’В«Р РЋР С“Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р РЋРІР‚ Р  Р’В°Р вЂ™Р’В» Р  РўвЂР  Р’В»Р РЋР РЏ 3D-Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р  Р’ВµР  Р вЂ¦Р  Р’ВµР  Р вЂ¦Р  РЎвЂР РЋР РЏ Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р  Р’ВµР  РІвЂћвЂ“. */
    private static final float LX = 0.36f, LY = 0.84f, LZ = 0.40f;

    private final Map<Entity, TrackedTarget> trackedEntities =
            new IdentityHashMap<>();

    private final List<ActiveEffect> effects = new ArrayList<>();
    private final List<Impact> impacts = new ArrayList<>();

    // РїСЂРѕРµРєС†РёСЏ РїРѕСЃР»РµРґРЅРµРіРѕ 3D-РєР°РґСЂР° вЂ” РґР»СЏ СЌРєСЂР°РЅРЅРѕРіРѕ РёРјРїР°РєС‚Р°
    private final Matrix4f lastProjection = new Matrix4f();
    private final Quaternionf lastCamRot = new Quaternionf();
    private Vec3d lastCamPos = Vec3d.ZERO;
    private boolean hasProjection;
    private final Random modeRandom = new Random();

    private ClientWorld lastWorld;

    public KillEffect() {
        super(
                "KillEffect",
                "Р  РЎв„ўР РЋР вЂљР  Р’В°Р РЋР С“Р  РЎвЂР  Р вЂ Р РЋРІР‚в„–Р  Р’Вµ 3D Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р РЋРІР‚в„– Р  РЎвЂ”Р РЋР вЂљР  РЎвЂ Р РЋРЎвЂњР  Р’В±Р  РЎвЂР  РІвЂћвЂ“Р РЋР С“Р РЋРІР‚С™Р  Р вЂ Р  Р’Вµ Р РЋРІР‚ Р  Р’ВµР  Р’В»Р  РЎвЂ",
                ModuleCategory.RENDER
        );
        addSettings(modeSetting, sizeSetting, speedSetting, brightnessSetting,
                colorSetting, customHex, onlyPlayers, screenFlash, flashPower);
    }

    @Override
    public void onEnable() {
        clear();
        lastWorld = mc.world;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        clear();
        lastWorld = null;
        super.onDisable();
    }

    private void clear() {
        for (ActiveEffect fx : effects) {
            releaseBody(fx);
        }
        trackedEntities.clear();
        effects.clear();
        impacts.clear();
    }

    private void checkWorld() {
        if (lastWorld != mc.world) {
            trackedEntities.clear();
            effects.clear();
            impacts.clear();
            lastWorld = mc.world;
        }
    }

    @EventLink
    public void onAttack(EventAttackEntity event) {
        checkWorld();

        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity target = event.getTarget();

        if (target instanceof LivingEntity && target != mc.player
                && (!onlyPlayers.isState() || target instanceof PlayerEntity)) {
            trackedEntities.put(target, new TrackedTarget(target, nowMs()));
        }
    }

    @EventLink
    public void onRender3D(Event3DRender event) {
        checkWorld();

        hasProjection = true;
        lastProjection.set(event.getProjectionMatrix());
        lastCamRot.set(event.getCamera().getRotation());
        lastCamPos = event.getCamera().getPos();

        if (mc.player == null || mc.world == null) {
            clear();
            return;
        }

        long now = nowMs();
        updateTargets(now);

        Iterator<ActiveEffect> finished = effects.iterator();
        while (finished.hasNext()) {
            ActiveEffect fx = finished.next();
            if (now - fx.startTime >= fx.duration) {
                releaseBody(fx);
                finished.remove();
            }
        }

        if (effects.isEmpty()) {
            return;
        }

        // Р  Р вЂ Р  Р’В°Р  Р вЂ¦Р  РЎвЂР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋРІР‚С™Р РЋР вЂљР РЋРЎвЂњР  РЎвЂ” Р  Р вЂ¦Р  Р’Вµ Р  РўвЂР  РЎвЂўР  Р’В»Р  Р’В¶Р  Р’ВµР  Р вЂ¦ Р  РЎвЂ”Р  Р’В°Р  РўвЂР  Р’В°Р РЋРІР‚С™Р РЋР Р‰: Р  РўвЂР  Р’ВµР РЋР вЂљР  Р’В¶Р  РЎвЂР  РЎВ Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  Р вЂ Р  Р вЂ¦Р  Р’Вµ Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р РЋР РЏ Р  Р’В·Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  РЎвЂР РЋР РЏ
        for (ActiveEffect fx : effects) {
            hideVanilla(fx);
        }

        MatrixStack matrices = event.getMatrices();
        Vec3d cameraPos = event.getCamera().getPos();

        RenderSystem.enableBlend();
        additive();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, brightnessSetting.get());

        try {
            for (ActiveEffect effect : effects) {
                float progress = clamp((now - effect.startTime) / effect.duration);
                float time = (now - effect.startTime) / 1000f * (effect.mode.duration / effect.duration);

                matrices.push();

                try {
                    matrices.translate(
                            effect.position.x - cameraPos.x,
                            effect.position.y - cameraPos.y + 0.025,
                            effect.position.z - cameraPos.z
                    );

                    Ctx c = new Ctx(
                            matrices, event, effect, progress, time,
                            (float) (cameraPos.x - effect.position.x),
                            (float) (cameraPos.y - effect.position.y - 0.025),
                            (float) (cameraPos.z - effect.position.z)
                    );

                    renderImpactPulse(c);

                    switch (effect.mode) {
                        case RIFT -> renderRift(c);
                        case BLACK_HOLE -> renderBlackHole(c);
                        case MAGMA -> { renderMagma(c); magmaExtra(c); }
                        case CRYSTAL -> { renderCrystal(c); crystalExtra(c); }
                        case AURORA -> renderAurora(c);
                        case SOUL -> { renderSoul(c); soulExtra(c); }
                        case VAN -> renderVan(c);
                    }
                } finally {
                    matrices.pop();
                    RenderSystem.enableBlend();
                    additive();
                    RenderSystem.depthMask(false);
                }
            }
        } finally {
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    // =====================================================================
    //  IMPACT: СѓРЅРёРІРµСЂСЃР°Р»СЊРЅС‹Р№ 3D-РёРјРїСѓР»СЊСЃ + СЌРєСЂР°РЅРЅС‹Р№ СЃР»РѕР№ (РІСЃРїС‹С€РєР°, РєРѕР»СЊС†Рѕ, С…СЂРѕРјР°С‚РёРєР°)
    // =====================================================================

    /** РЇСЂРєРѕРµ СЏРґСЂРѕ-РІСЃРїС‹С€РєР°, СЃС‚РѕР»Р± СЃРІРµС‚Р° Рё СѓРґР°СЂРЅС‹Рµ РєРѕР»СЊС†Р° РІ РїРµСЂРІС‹Рµ С‚РёРєРё Р›Р®Р‘РћР“Рћ СЂРµР¶РёРјР°. */
    private void renderImpactPulse(Ctx c) {
        float k = clamp(c.p / 0.20f);
        float a = (1f - smoothstep(k));
        if (a <= 0.003f) {
            return;
        }
        float s = c.fx.scale;
        float e = easeOutCubic(k);
        float cyc = c.fx.height * 0.5f;

        BufferBuilder g = beginGlow();
        // СЏРґСЂРѕ-РІСЃРїС‹С€РєР° РІ С†РµРЅС‚СЂРµ С‚РµР»Р°
        glow(g, c, 0f, cyc, 0f, (0.7f + e * 2.6f) * s, (0.7f + e * 2.6f) * s,
                mixColor(c.fx.accent, 0xFFFFFF, 0.5f), a);
        // СЃС‚РѕР»Р± СЃРІРµС‚Р°: СѓР·РєРѕРµ Р±РµР»РѕРµ СЏРґСЂРѕ + С€РёСЂРѕРєР°СЏ С†РІРµС‚РЅР°СЏ Р°СѓСЂР°
        glow(g, c, 0f, cyc * 1.1f, 0f, 0.55f * s, c.fx.height * 2.0f, c.fx.color, a * 0.6f);
        glow(g, c, 0f, cyc * 1.1f, 0f, 0.18f * s, c.fx.height * 1.8f, 0xFFFFFF, a * 0.85f);
        // РіРѕСЂРёР·РѕРЅС‚Р°Р»СЊРЅС‹Р№ Р±Р»РёРє + Р·РІС‘Р·РґРЅС‹Р№ С„Р»СЌСЂ
        glow(g, c, 0f, cyc, 0f, (3.6f + e * 6.5f) * s, 0.11f * s, 0xFFFFFF, a * 0.75f);
        starFlare(g, c, 0f, cyc, 0f, (1.4f + e * 5.5f) * s, c.fx.accent, a * 0.9f, 6, c.t * 0.5f);
        drawGlow(g);

        BufferBuilder b = beginColor();
        // РґРІРѕР№РЅРѕРµ СѓРґР°СЂРЅРѕРµ РєРѕР»СЊС†Рѕ РїРѕ Р·РµРјР»Рµ
        ring(b, c.mat(), (0.2f + e * 3.0f) * s, 0.06f * (1f - k) + 0.01f, 0.03f,
                0f, TAU, c.fx.accent, a * 0.9f, 112);
        ring(b, c.mat(), (0.14f + e * 2.1f) * s, 0.03f * (1f - k) + 0.008f, 0.04f,
                0f, TAU, 0xFFFFFF, a * 0.6f, 96);
        drawColor(b);
    }

    @EventLink
    public void onRender2D(EventRender.Default event) {
        if (!screenFlash.isState() || !hasProjection || impacts.isEmpty()
                || mc.player == null || mc.world == null) {
            return;
        }

        long now = nowMs();
        DrawContext context = event.getContext();
        if (context == null) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        float sw = mc.getWindow().getScaledWidth();
        float sh = mc.getWindow().getScaledHeight();
        float pw = flashPower.get();
        float diag = (float) Math.sqrt(sw * sw + sh * sh);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        try {
            Iterator<Impact> it = impacts.iterator();
            while (it.hasNext()) {
                Impact im = it.next();
                float t = clamp((now - im.start) / (float) IMPACT_MS);
                if (t >= 1f) {
                    it.remove();
                    continue;
                }

                Vec3d sp = worldToScreen(im.position.add(0, im.height * 0.55, 0));
                if (sp == null) {
                    continue;
                }
                float sx = (float) sp.x;
                float sy = (float) sp.y;

                // РІСЃРїС‹С€РєР°: СЂР°РґРёР°Р»СЊРЅС‹Р№ РіСЂР°РґРёРµРЅС‚ РѕС‚ С‚РѕС‡РєРё СѓР±РёР№СЃС‚РІР° + РїР»С‘РЅРѕС‡РЅР°СЏ Р·Р°Р»РёРІРєР°
                float flash = 1f - smoothstep(t / 0.34f);
                if (flash > 0.004f) {
                    int col = im.color;
                    float ga = flash * pw;
                    drawRadialGlow(matrices, sx, sy, diag * 0.85f, col, 0.30f * ga);
                    drawRadialGlow(matrices, sx, sy, diag * 0.38f, mixColor(col, 0xFFFFFF, 0.6f), 0.38f * ga);
                    drawRadialGlow(matrices, sx, sy, diag * 0.16f, 0xFFFFFF, 0.45f * ga);
                    RenderUtils.drawRoundedRect(matrices, 0, 0, sw, sh, 0,
                            withAlpha(col, 0.07f * flash * pw));
                }

                // СЂР°СЃС€РёСЂСЏСЋС‰РµРµСЃСЏ РєРѕР»СЊС†Рѕ РѕС‚ С‚РѕС‡РєРё СѓР±РёР№СЃС‚РІР°
                float ringA = 1f - smoothstep((t - 0.35f) / 0.65f);
                if (ringA > 0.004f) {
                    float r = (0.06f + easeOutCubic(t) * 0.30f) * diag;
                    float ringW = 1.8f + 3.2f * (1f - t);

                    // С€РёСЂРѕРєР°СЏ РјСЏРіРєР°СЏ Р°СѓСЂР° СЃРЅР°СЂСѓР¶Рё
                    int oc = withAlpha(im.color, 0.16f * ringA);
                    float ro = r * 1.12f;
                    RenderUtils.drawRoundedRectOutline(matrices, sx - ro, sy - ro, ro * 2f, ro * 2f, ro,
                            ringW * 2.4f, oc, oc, oc, oc);

                    int tc = withAlpha(im.color, 0.55f * ringA);
                    RenderUtils.drawRoundedRectOutline(matrices, sx - r, sy - r, r * 2f, r * 2f, r,
                            ringW, tc, tc, tc, tc);

                    float rw = r * 0.86f;
                    int wc = withAlpha(0xFFFFFF, 0.9f * ringA);
                    float cw = Math.max(1.0f, ringW * 0.4f);
                    RenderUtils.drawRoundedRectOutline(matrices, sx - rw, sy - rw, rw * 2f, rw * 2f, rw,
                            cw, wc, wc, wc, wc);

                    // С…СЂРѕРјР°С‚РёС‡РµСЃРєР°СЏ РґРёСЃРїРµСЂСЃРёСЏ: РєСЂР°СЃРЅС‹Р№ Рё РіРѕР»СѓР±РѕР№ РєРѕРЅС‚СѓСЂС‹ СЃРѕ СЃРґРІРёРіРѕРј
                    if (t < 0.5f) {
                        float ca = 0.15f * ringA * (1f - t * 2f);
                        float cr = r * 1.05f;
                        int red = withAlpha(0xFF5040, ca);
                        int cyan = withAlpha(0x40E8FF, ca);
                        RenderUtils.drawRoundedRectOutline(matrices, sx - cr + 2.2f, sy - cr,
                                cr * 2f, cr * 2f, cr, 1.2f, red, red, red, red);
                        RenderUtils.drawRoundedRectOutline(matrices, sx - cr - 2.2f, sy - cr,
                                cr * 2f, cr * 2f, cr, 1.2f, cyan, cyan, cyan, cyan);
                    }
                }
            }
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    /** РџРѕР»РЅРѕСЌРєСЂР°РЅРЅС‹Р№ СЂР°РґРёР°Р»СЊРЅС‹Р№ РіСЂР°РґРёРµРЅС‚ (РІРµРµСЂ РёР· С‚СЂРµСѓРіРѕР»СЊРЅРёРєРѕРІ): С†РµРЅС‚СЂ СЏСЂРєРёР№, РєСЂР°Р№ вЂ” РІ РЅРѕР»СЊ. */
    private static void drawRadialGlow(MatrixStack matrices, float cx, float cy, float radius,
                                       int color, float alpha) {
        if (alpha <= 0.003f || radius <= 1.0f) {
            return;
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f m = matrices.peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        int center = withAlpha(color, alpha);
        int rim = withAlpha(color, 0f);
        int seg = 36;
        float px = cx + radius;
        float py = cy;
        for (int i = 1; i <= seg; i++) {
            float ang = (float) (Math.PI * 2.0 * i / seg);
            float nx = cx + (float) Math.cos(ang) * radius;
            float ny = cy + (float) Math.sin(ang) * radius;
            b.vertex(m, cx, cy, 0f).color(center);
            b.vertex(m, px, py, 0f).color(rim);
            b.vertex(m, nx, ny, 0f).color(rim);
            px = nx;
            py = ny;
        }
        drawBuilt(b.endNullable());
    }

    /** РњРёСЂ -> СЌРєСЂР°РЅ РїРѕ РїСЂРѕРµРєС†РёРё РїРѕСЃР»РµРґРЅРµРіРѕ 3D-РєР°РґСЂР°. */
    private Vec3d worldToScreen(Vec3d worldPos) {
        if (mc == null || mc.getWindow() == null) {
            return null;
        }
        Vector3f relative = new Vector3f(
                (float) (worldPos.x - lastCamPos.x),
                (float) (worldPos.y - lastCamPos.y),
                (float) (worldPos.z - lastCamPos.z)
        );
        Quaternionf inv = new Quaternionf(lastCamRot).conjugate();
        relative.rotate(inv);

        Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0f);
        lastProjection.transform(clip);

        float w = clip.w;
        if (w <= 0.00001f) {
            return null;
        }
        float ndcX = clip.x / w;
        float ndcY = clip.y / w;
        float screenX = (ndcX * 0.5f + 0.5f) * mc.getWindow().getScaledWidth();
        float screenY = (1.0f - (ndcY * 0.5f + 0.5f)) * mc.getWindow().getScaledHeight();

        if (Float.isNaN(screenX) || Float.isNaN(screenY)
                || Float.isInfinite(screenX) || Float.isInfinite(screenY)) {
            return null;
        }
        if (screenX < -600 || screenY < -600
                || screenX > mc.getWindow().getScaledWidth() + 600
                || screenY > mc.getWindow().getScaledHeight() + 600) {
            return null;
        }
        return new Vec3d(screenX, screenY, 0);
    }

    private static final class Impact {
        final Vec3d position;
        final float height;
        final long start;
        final int color;

        Impact(Vec3d position, float height, long start, int color) {
            this.position = position;
            this.height = height;
            this.start = start;
            this.color = color;
        }
    }

    private void updateTargets(long now) {
        Iterator<Map.Entry<Entity, TrackedTarget>> iterator =
                trackedEntities.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Entity, TrackedTarget> entry = iterator.next();
            Entity entity = entry.getKey();
            TrackedTarget tracked = entry.getValue();

            if (now - tracked.lastAttack > TRACK_TIMEOUT_MS) {
                iterator.remove();
                continue;
            }

            // Р  Р Р‹Р  РЎВР  Р’ВµР РЋР вЂљР РЋРІР‚С™Р РЋР Р‰ Р  Р’ВР  РІР‚С”Р  Р’В Р РЋРЎвЂњР  РўвЂР  Р’В°Р  Р’В»Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ Р РЋРІР‚ Р  Р’ВµР  Р’В»Р  РЎвЂ.
            if (entity.isRemoved() || !entity.isAlive()) {
                Vec3d position = entity.isRemoved() ? tracked.position : entity.getPos();

                EffectMode chosen = pickMode();
                LivingEntity body = chosen.statue && entity instanceof LivingEntity living
                        ? living : null;

                int killColor = pickColor();
                effects.add(new ActiveEffect(
                        position, now, killColor, chosen, tracked.width, tracked.height,
                        sizeSetting.get(), speedSetting.get(),
                        body, tracked.bodyYaw, tracked.headYaw, tracked.pitch
                ));

                if (screenFlash.isState()) {
                    impacts.add(new Impact(position, tracked.height, now, killColor));
                    while (impacts.size() > 4) impacts.remove(0);
                }

                while (effects.size() > MAX_EFFECTS) {
                    releaseBody(effects.remove(0));
                }

                iterator.remove();
            } else {
                tracked.update(entity);
            }
        }
    }

    // =====================================================================
    //  Р  РЎС›Р  РІР‚СћР  РІР‚С”Р  РЎвЂє-Р  Р Р‹Р  РЎС›Р  РЎвЂ™Р  РЎС›Р  Р в‚¬Р  Р вЂЎ: Р  Р вЂ¦Р  Р’В°Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР РЋР РЏР РЋРІР‚В°Р  Р’В°Р РЋР РЏ Р  РЎВР  РЎвЂўР  РўвЂР  Р’ВµР  Р’В»Р РЋР Р‰ Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В°, Р  Р’В·Р  Р’В°Р  РЎВР  РЎвЂўР РЋР вЂљР  РЎвЂўР  Р’В¶Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ  Р  РЎвЂ”Р  РЎвЂўР  Р’В·Р  Р’Вµ Р РЋР С“Р  РЎВР  Р’ВµР РЋР вЂљР РЋРІР‚С™Р  РЎвЂ
    // =====================================================================

    /** Р  РЎСџР  РЎвЂўР  РЎвЂќР  Р’В° Р РЋР С“Р  Р’ВµР РЋР вЂљР  Р вЂ Р  Р’ВµР РЋР вЂљ Р  Р вЂ¦Р  Р’Вµ Р РЋРЎвЂњР  РўвЂР  Р’В°Р  Р’В»Р  РЎвЂР  Р’В» Р РЋРІР‚С™Р РЋР вЂљР РЋРЎвЂњР  РЎвЂ”, Р РЋРЎвЂњР  Р вЂ¦Р  РЎвЂўР РЋР С“Р  РЎвЂР  РЎВ Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  РЎвЂ”Р  РЎвЂўР  РўвЂ Р  РЎВР  РЎвЂР РЋР вЂљ: Р  Р вЂ Р  Р’В°Р  Р вЂ¦Р  РЎвЂР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р  РЎвЂ”Р  Р’В°Р  РўвЂР  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ Р  Р вЂ¦Р  Р’Вµ Р  Р вЂ Р  РЎвЂР  РўвЂР  Р вЂ¦Р  РЎвЂў. */
    private void hideVanilla(ActiveEffect fx) {
        LivingEntity e = fx.body;
        if (e == null || e.isRemoved()) {
            return;
        }
        double x = fx.position.x;
        double y = fx.position.y - HIDE_DEPTH;
        double z = fx.position.z;
        e.setPos(x, y, z);
        e.prevX = x;
        e.prevY = y;
        e.prevZ = z;
        e.setVelocity(Vec3d.ZERO);
        e.deathTime = 0;
        e.hurtTime = 0;
        fx.hidden = true;
    }

    /** Р  РІР‚в„ўР  РЎвЂўР  Р’В·Р  Р вЂ Р РЋР вЂљР  Р’В°Р РЋРІР‚В°Р  Р’В°Р  Р’ВµР  РЎВ Р РЋР С“Р РЋРЎвЂњР РЋРІР‚В°Р  Р вЂ¦Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР Р‰ Р  Р вЂ¦Р  Р’В° Р  РЎВР  Р’ВµР РЋР С“Р РЋРІР‚С™Р  РЎвЂў, Р  Р’ВµР РЋР С“Р  Р’В»Р  РЎвЂ Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™ Р  Р’В·Р  Р’В°Р  РЎвЂќР  РЎвЂўР  Р вЂ¦Р РЋРІР‚РЋР  РЎвЂР  Р’В»Р РЋР С“Р РЋР РЏ Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋР Р‰Р РЋРІвЂљВ¬Р  Р’Вµ, Р РЋРІР‚РЋР  Р’ВµР  РЎВ Р  Р’ВµР РЋРІР‚В Р РЋРЎвЂњР  РўвЂР  Р’В°Р  Р’В»Р  РЎвЂР  Р’В»Р  РЎвЂ. */
    private void releaseBody(ActiveEffect fx) {
        LivingEntity e = fx.body;
        if (e == null || !fx.hidden || e.isRemoved()) {
            return;
        }
        e.setPos(fx.position.x, fx.position.y, fx.position.z);
        e.prevX = fx.position.x;
        e.prevY = fx.position.y;
        e.prevZ = fx.position.z;
        fx.hidden = false;
    }

    /**
     * Р   Р  РЎвЂР РЋР С“Р РЋРЎвЂњР  Р’ВµР РЋРІР‚С™ Р  РЎВР  РЎвЂўР  РўвЂР  Р’ВµР  Р’В»Р РЋР Р‰ Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В° Р  Р вЂ  Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР РЋРЎвЂњР РЋРІР‚В°Р  Р’ВµР  РІвЂћвЂ“ Р  РЎВР  Р’В°Р РЋРІР‚С™Р РЋР вЂљР  РЎвЂР РЋРІР‚ Р  Р’Вµ (Р  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  РЎвЂ Р  Р вЂ  Р  Р вЂ¦Р  Р’В°Р РЋРІР‚РЋР  Р’В°Р  Р’В»Р  Р’Вµ Р  РЎвЂќР  РЎвЂўР  РЎвЂўР РЋР вЂљР  РўвЂР  РЎвЂР  Р вЂ¦Р  Р’В°Р РЋРІР‚С™).
     * r/g/b Р РЋРЎвЂњР  РЎВР  Р вЂ¦Р  РЎвЂўР  Р’В¶Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р РЋРІР‚ Р  Р вЂ Р  Р’ВµР РЋРІР‚С™ Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР РЋР С“Р РЋРІР‚С™Р РЋРЎвЂњР РЋР вЂљР РЋРІР‚в„–; Р  Р’В·Р  Р вЂ¦Р  Р’В°Р РЋРІР‚РЋР  Р’ВµР  Р вЂ¦Р  РЎвЂР РЋР РЏ Р  Р’В±Р  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІвЂљВ¬Р  Р’Вµ 1 Р  Р’В·Р  Р’В°Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р вЂ Р  Р’В»Р РЋР РЏР РЋР вЂ№Р РЋРІР‚С™ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂР РЋРІР‚С™Р РЋР Р‰Р РЋР С“Р РЋР РЏ.
     */
    private void drawBody(Ctx c, float r, float g, float b) {
        LivingEntity e = c.fx.body;
        if (e == null) {
            return;
        }
        ActiveEffect fx = c.fx;

        // Р  Р’В·Р  Р’В°Р  РЎВР  РЎвЂўР РЋР вЂљР  Р’В°Р  Р’В¶Р  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР  РЎВ Р  РЎвЂ”Р  РЎвЂўР  Р’В·Р РЋРЎвЂњ: Р  Р вЂ¦Р  РЎвЂР  РЎвЂќР  Р’В°Р  РЎвЂќР  РЎвЂўР  РЎвЂ“Р  РЎвЂў Р  Р вЂ¦Р  Р’В°Р  РЎвЂќР  Р’В»Р  РЎвЂўР  Р вЂ¦Р  Р’В° Р РЋР С“Р  РЎВР  Р’ВµР РЋР вЂљР РЋРІР‚С™Р  РЎвЂ Р  РЎвЂ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР С“Р  Р вЂ¦Р  РЎвЂўР  РІвЂћвЂ“ Р  Р вЂ Р РЋР С“Р  РЎвЂ”Р РЋРІР‚в„–Р РЋРІвЂљВ¬Р  РЎвЂќР  РЎвЂ Р РЋРЎвЂњР РЋР вЂљР  РЎвЂўР  Р вЂ¦Р  Р’В°
        e.deathTime = 0;
        e.hurtTime = 0;
        e.bodyYaw = fx.bodyYaw;
        e.prevBodyYaw = fx.bodyYaw;
        e.headYaw = fx.headYaw;
        e.prevHeadYaw = fx.headYaw;
        e.setYaw(fx.bodyYaw);
        e.prevYaw = fx.bodyYaw;
        e.setPitch(fx.pitch);
        e.prevPitch = fx.pitch;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(Math.max(0f, r), Math.max(0f, g), Math.max(0f, b), 1f);
        dispatcher.setRenderShadows(false);
        try {
            dispatcher.render(e, 0.0, 0.0, 0.0, 1f, c.m, immediate,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
        } catch (Throwable ignored) {
            // Р РЋР РЉР  РЎвЂќР  Р’В·Р  РЎвЂўР РЋРІР‚С™Р  РЎвЂР РЋРІР‚РЋР  Р’ВµР РЋР С“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  РўвЂР  Р’ВµР РЋР вЂљР  Р’ВµР РЋР вЂљ Р  Р вЂ¦Р  Р’Вµ Р  РўвЂР  РЎвЂўР  Р’В»Р  Р’В¶Р  Р’ВµР  Р вЂ¦ Р РЋР вЂљР  РЎвЂўР  Р вЂ¦Р РЋР РЏР РЋРІР‚С™Р РЋР Р‰ Р  Р вЂ Р  Р’ВµР РЋР С“Р РЋР Р‰ Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™
        } finally {
            dispatcher.setRenderShadows(true);
            RenderSystem.setShaderColor(1f, 1f, 1f, brightnessSetting.get());
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            additive();
        }
    }

    /** Р  РЎСџР РЋР вЂљР  РЎвЂўР РЋРІР‚С›Р  РЎвЂР  Р’В»Р РЋР Р‰ Р РЋРІвЂљВ¬Р  РЎвЂР РЋР вЂљР  РЎвЂР  Р вЂ¦Р РЋРІР‚в„– Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° Р  РЎвЂ”Р  РЎвЂў Р  Р вЂ Р РЋРІР‚в„–Р РЋР С“Р  РЎвЂўР РЋРІР‚С™Р  Р’Вµ: Р  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  РЎвЂ Р РЋРЎвЂњР  Р’В¶Р  Р’Вµ, Р РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР РЋР С“ Р РЋРІвЂљВ¬Р  РЎвЂР РЋР вЂљР  Р’Вµ, Р  РЎвЂ“Р  РЎвЂўР  Р’В»Р  РЎвЂўР  Р вЂ Р  Р’В° Р РЋРІР‚РЋР РЋРЎвЂњР РЋРІР‚С™Р РЋР Р‰ Р РЋРЎвЂњР  Р’В¶Р  Р’Вµ. */
    /**
     * РђРґРґРёС‚РёРІРЅР°СЏ РєРѕРїРёСЏ СЃС‚Р°С‚СѓРё: С‚РµР»Рѕ РЎР’Р•РўРРўРЎРЇ РР—РќРЈРўР Р С†РІРµС‚РѕРј СЂРµР¶РёРјР° (emissive),
     * С„РѕСЂРјР° РЅРµ РјРµРЅСЏРµС‚СЃСЏ. Р›С‘РіРєРёР№ scale 1.02 вЂ” РјСЏРіРєРѕРµ РґРІРѕР№РЅРѕРµ РёР·РѕР±СЂР°Р¶РµРЅРёРµ Р±РµР· СЂРІР°РЅС‹С… РєСЂР°С‘РІ.
     */
    private void drawBodyGlow(Ctx c, float r, float g, float b, float intensity) {
        LivingEntity e = c.fx.body;
        if (e == null || intensity <= 0.02f) {
            return;
        }
        ActiveEffect fx = c.fx;
        e.deathTime = 0;
        e.hurtTime = 0;
        e.bodyYaw = fx.bodyYaw;
        e.prevBodyYaw = fx.bodyYaw;
        e.headYaw = fx.headYaw;
        e.prevHeadYaw = fx.headYaw;
        e.setYaw(fx.bodyYaw);
        e.prevYaw = fx.bodyYaw;
        e.setPitch(fx.pitch);
        e.prevPitch = fx.pitch;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        try {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            additive();
            RenderSystem.setShaderColor(
                    Math.max(0f, r * intensity),
                    Math.max(0f, g * intensity),
                    Math.max(0f, b * intensity), 1f);
            dispatcher.setRenderShadows(false);
            c.m.push();
            c.m.scale(1.02f, 1.015f, 1.02f);
            dispatcher.render(e, 0.0, 0.0, 0.0, 1f, c.m, immediate,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);
            c.m.pop();
            immediate.draw();
        } catch (Throwable ignored) {
            // СЌРєР·РѕС‚РёС‡РµСЃРєРёР№ СЂРµРЅРґРµСЂРµСЂ РЅРµ РґРѕР»Р¶РµРЅ СЂРѕРЅСЏС‚СЊ СЌС„С„РµРєС‚
        } finally {
            dispatcher.setRenderShadows(true);
            RenderSystem.setShaderColor(1f, 1f, 1f, brightnessSetting.get());
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            additive();
        }
    }

    private static float prof(float rel) {
        return 0.74f + 0.26f * smoothstep((rel - 0.28f) / 0.25f)
                - 0.18f * smoothstep((rel - 0.8f) / 0.12f);
    }

    private static float lerp(float a, float b, float k) {
        return a + (b - a) * k;
    }

    // =====================================================================
    //  RIFT (Р  РЎвЂўР РЋР вЂљР  РЎвЂР  РЎвЂ“Р  РЎвЂР  Р вЂ¦Р  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋР вЂљР  Р’ВµР  Р’В¶Р  РЎвЂР  РЎВ)
    // =====================================================================

    private void renderRift(Ctx c) {
        float alpha = envelope(c.p);
        if (alpha <= 0.002f) {
            return;
        }
        renderRings(c.m, c.fx, c.p, c.t, alpha);
        renderLight(c.m, c.e, c.fx, c.p, c.t, alpha);
    }

    private void renderRings(
            MatrixStack matrices,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha
    ) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.TRIANGLES,
                VertexFormats.POSITION_COLOR
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Р  РЎвЂєР РЋР С“Р  Р вЂ¦Р  РЎвЂўР  Р вЂ Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋРЎвЂњР  РўвЂР  Р’В°Р РЋР вЂљР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р  Р’В°.
        float wave = clamp(progress / 0.65f);

        ring(
                buffer, matrix,
                0.12f + easeOutCubic(wave) * 2.7f,
                0.045f * (1f - wave) + 0.008f,
                0.005f,
                0f, TAU,
                effect.color,
                alpha * (1f - smoothstep(wave)),
                96
        );

        // Р  РІР‚в„ўР РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР  Р’В°Р РЋР РЏ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р  Р’В° Р РЋР С“ Р  Р вЂ¦Р  Р’ВµР  Р’В±Р  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІвЂљВ¬Р  РЎвЂўР  РІвЂћвЂ“ Р  Р’В·Р  Р’В°Р  РўвЂР  Р’ВµР РЋР вЂљР  Р’В¶Р  РЎвЂќР  РЎвЂўР  РІвЂћвЂ“.
        float second = clamp((progress - 0.09f) / 0.72f);

        ring(
                buffer, matrix,
                0.08f + easeOutCubic(second) * 2.1f,
                0.025f,
                0.013f,
                0f, TAU,
                effect.accent,
                alpha * smoothstep(second / 0.08f)
                        * (1f - smoothstep(second)) * 0.65f,
                80
        );

        // Р  РЎС›Р РЋР вЂљР  Р’ВµР РЋРІР‚С™Р РЋР Р‰Р РЋР РЏ Р  РЎВР  Р’ВµР  РўвЂР  Р’В»Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р  Р’В° Р  РўвЂР  РЎвЂўР  РЎвЂ“Р  РЎвЂўР  Р вЂ¦Р РЋР РЏР  Р’ВµР РЋРІР‚С™ Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ.
        float third = clamp((progress - 0.2f) / 0.8f);

        ring(
                buffer, matrix,
                0.06f + easeOutCubic(third) * 3.2f,
                0.018f,
                0.009f,
                0f, TAU,
                effect.color,
                alpha * smoothstep(third / 0.12f)
                        * (1f - smoothstep(third)) * 0.45f,
                72
        );

        float baseRadius = 0.85f * easeOutCubic(progress / 0.18f);

        for (int i = 0; i < 3; i++) {
            ring(
                    buffer, matrix,
                    baseRadius,
                    0.018f,
                    0.035f,
                    time * 1.8f + i * TAU / 3f,
                    1.25f,
                    effect.accent,
                    alpha * 0.75f,
                    24
            );

            // Р  Р Р‹Р  Р’ВµР  РЎвЂ“Р  РЎВР  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р РЋРІР‚в„– Р  РЎВР  Р’ВµР  Р вЂ¦Р РЋР Р‰Р РЋРІвЂљВ¬Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  РЎвЂќР  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІР‚ Р  Р’В° Р  Р вЂ Р РЋР вЂљР  Р’В°Р РЋРІР‚В°Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  Р’В°Р  Р вЂ Р РЋР С“Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚РЋР РЋРЎвЂњ.
            ring(
                    buffer, matrix,
                    baseRadius * 0.65f,
                    0.012f,
                    0.045f,
                    -time * 2.4f + i * TAU / 3f,
                    0.85f,
                    effect.color,
                    alpha * 0.6f,
                    18
            );
        }

        drawBuilt(buffer.endNullable());
    }

    private void renderLight(
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha
    ) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, GLOW_TEX);

        BufferBuilder buffer = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR
        );

        float growth = easeOutCubic(progress / 0.19f);
        float height = HEIGHT * growth;

        float collapse = 1f - smoothstep(
                (progress - 0.48f) / 0.52f
        );

        float pulse = 0.94f + 0.06f * (float) Math.sin(time * 13f);
        float beamWidth = (0.13f + collapse * 0.32f) * pulse;

        // Р  Р РѓР  РЎвЂР РЋР вЂљР  РЎвЂўР  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РЎвЂўР РЋР вЂљР  Р’ВµР  РЎвЂўР  Р’В» Р  РЎвЂ Р РЋРЎвЂњР  Р’В·Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋР РЏР РЋР вЂљР  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋР С“Р РЋРІР‚С™Р  Р’ВµР РЋР вЂљР  Р’В¶Р  Р’ВµР  Р вЂ¦Р РЋР Р‰.
        glow(
                buffer, matrices, event,
                0f, height * 0.5f, 0f,
                beamWidth * 4.5f, height * 1.25f,
                effect.color, alpha * 0.2f
        );

        glow(
                buffer, matrices, event,
                0f, height * 0.5f, 0f,
                beamWidth, height * 1.08f,
                effect.accent, alpha * 0.88f
        );

        // Р  РЎвЂєР  Р’В±Р РЋР вЂ°Р РЋРІР‚ВР  РЎВ Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В±Р  Р’В° Р  РЎвЂР  Р’В· Р  РЎвЂ”Р  Р’ВµР РЋР вЂљР  Р’ВµР  РЎвЂќР РЋР вЂљР РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦Р РЋР С“Р РЋР РЏ Р  РЎВР РЋР РЏР  РЎвЂ“Р  РЎвЂќР  РЎвЂР РЋРІР‚В¦ Р  Р’В±Р  Р’В»Р  РЎвЂР  РЎвЂќР  РЎвЂўР  Р вЂ .
        for (int i = 0; i < 28; i++) {
            float t = (i + 0.5f) / 28f;

            float shape = (float) Math.sin(t * Math.PI);
            float width = (0.16f + shape * 0.72f)
                    * (0.3f + collapse * 0.7f);

            glow(
                    buffer, matrices, event,
                    0f, t * height, 0f,
                    width * 1.9f, width * 1.5f,
                    effect.color,
                    alpha * (0.07f + shape * 0.07f)
            );
        }

        // Р  РІР‚СњР  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋР С“Р  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  Р’В»Р РЋР Р‰ Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В±Р  Р’В°.
        for (int strand = 0; strand < 2; strand++) {
            for (int i = 0; i < 44; i++) {
                float t = i / 43f;
                float taper = (float) Math.sin(t * Math.PI);

                float angle = t * TAU * 1.65f
                        - time * 4f
                        + strand * (float) Math.PI;

                float orbit = (0.18f + taper * 0.65f)
                        * growth
                        * (0.4f + collapse * 0.6f);

                float x = (float) Math.cos(angle) * orbit;
                float z = (float) Math.sin(angle) * orbit;
                float y = t * height;

                float size = 0.075f + taper * 0.07f;

                int color = strand == 0
                        ? effect.color
                        : effect.accent;

                glow(
                        buffer, matrices, event,
                        x, y, z,
                        size * 2.5f, size * 2.5f,
                        color, alpha * taper * 0.18f
                );

                glow(
                        buffer, matrices, event,
                        x, y, z,
                        size * 0.7f, size * 0.7f,
                        effect.accent, alpha * taper * 0.85f
                );
            }
        }

        // Р  РЎСљР  Р’В°Р РЋРІР‚РЋР  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ Р РЋР С“Р  РЎвЂ”Р РЋРІР‚в„–Р РЋРІвЂљВ¬Р  РЎвЂќР  Р’В° Р  Р’В±Р РЋРІР‚в„–Р РЋР С“Р РЋРІР‚С™Р РЋР вЂљР  РЎвЂў Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋРІР‚В¦Р  Р’В°Р  Р’ВµР РЋРІР‚С™.
        float flash = 1f - smoothstep(progress / 0.24f);
        float flashSize = 0.8f
                + easeOutCubic(progress / 0.24f) * 2.8f;

        glow(
                buffer, matrices, event,
                0f, 0.3f, 0f,
                flashSize, flashSize,
                effect.accent, alpha * flash * 0.9f
        );

        // Р  РІР‚СљР  РЎвЂўР РЋР вЂљР  РЎвЂР  Р’В·Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂўР  Р вЂ Р  Р’В°Р РЋР РЏ Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р  РЎвЂўР РЋР С“Р  Р’В°.
        glow(
                buffer, matrices, event,
                0f, 0.18f, 0f,
                4.8f * growth, 0.18f,
                effect.accent, alpha * flash * 0.75f
        );

        // Р  Р Р‹Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  РЎвЂР  РІвЂћвЂ“Р РЋР С“Р РЋР РЏ Р РЋРЎвЂњР  Р’В·Р  Р’ВµР  Р’В» Р  Р вЂ¦Р  Р’В° Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІвЂљВ¬Р  РЎвЂР  Р вЂ¦Р  Р’Вµ Р РЋР вЂљР  Р’В°Р  Р’В·Р  Р’В»Р  РЎвЂўР  РЎВР  Р’В°.
        glow(
                buffer, matrices, event,
                0f, height, 0f,
                0.5f * collapse + 0.1f,
                0.5f * collapse + 0.1f,
                effect.accent, alpha * 0.65f
        );

        renderCurtain(buffer, matrices, event, effect, progress, time, alpha, growth, collapse);
        renderGroundHalo(matrices, event, effect, progress, alpha, growth);
        renderOrbiters(buffer, matrices, event, effect, progress, time, alpha, growth, collapse);
        renderShards(buffer, matrices, event, effect, progress, time, alpha, growth);

        renderSparks(
                buffer, matrices, event,
                effect, progress, time, alpha
        );

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    /** Р  РІР‚в„ўР  Р’ВµР РЋР вЂљР РЋРІР‚С™Р  РЎвЂР  РЎвЂќР  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р РЋР вЂљР  Р вЂ Р  Р’В°Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  Р’В»Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р РЋРІР‚в„–: Р  РўвЂР  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р РЋРІР‚С™Р  РЎвЂўР РЋРІР‚С™ Р РЋР С“Р  Р’В°Р  РЎВР РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋР вЂљР  Р’В°Р  Р’В·Р  Р’В»Р  РЎвЂўР  РЎВ, Р  Р’В° Р  Р вЂ¦Р  Р’Вµ Р  РЎвЂ”Р  Р’В»Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ bloom-Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В±. */
    private void renderCurtain(
            BufferBuilder buffer,
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha,
            float growth,
            float collapse
    ) {
        float fade = alpha * (1f - smoothstep((progress - 0.66f) / 0.34f));
        if (fade <= 0.002f) return;

        for (int i = 0; i < 12; i++) {
            float seed = i * TAU / 12f;
            float sway = (float) Math.sin(time * (2.4f + i * 0.08f) + seed) * 0.12f;
            float angle = seed + time * (i % 2 == 0 ? 0.55f : -0.42f) + sway;
            float radius = (0.26f + (i % 3) * 0.08f) * growth;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float ribbonHeight = HEIGHT * growth * (0.72f + (i % 4) * 0.08f);
            float y = ribbonHeight * (0.43f + (i % 5) * 0.075f);
            float width = 0.035f + (i % 3) * 0.018f;

            glow(buffer, matrices, event, x, y, z,
                    width * 4.2f, ribbonHeight * 0.34f,
                    effect.color, fade * 0.11f);
            glow(buffer, matrices, event, x, y, z,
                    width, ribbonHeight * 0.18f,
                    effect.accent, fade * 0.46f);
        }
    }

    /** Р  РЎСљР  Р’ВµР РЋР С“Р  РЎвЂќР  РЎвЂўР  Р’В»Р РЋР Р‰Р  РЎвЂќР  РЎвЂў Р  Р вЂ¦Р  Р’ВµР  Р’В·Р  Р’В°Р  Р вЂ Р  РЎвЂР РЋР С“Р  РЎвЂР  РЎВР РЋРІР‚в„–Р РЋРІР‚В¦ Р  РЎвЂќР  РЎвЂўР  Р’В»Р  Р’ВµР РЋРІР‚  Р  Р вЂ¦Р  Р’В° Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  Р’Вµ Р РЋР С“Р  РЎвЂўР  Р’В·Р  РўвЂР  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р РЋРЎвЂњР  РўвЂР  Р’В°Р РЋР вЂљР  Р вЂ¦Р РЋРЎвЂњР РЋР вЂ№ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р РЋРЎвЂњ Р  РЎвЂ Р  РЎвЂ“Р  Р’В»Р РЋРЎвЂњР  Р’В±Р  РЎвЂР  Р вЂ¦Р РЋРЎвЂњ. */
    private void renderGroundHalo(
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float alpha,
            float growth
    ) {
        float p = clamp(progress / 0.58f);
        float fade = alpha * (1f - smoothstep((progress - 0.52f) / 0.48f));
        float radius = 0.25f + easeOutCubic(p) * 2.9f;

        if (fade > 0.002f) {
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            BufferBuilder rings = Tessellator.getInstance().begin(
                    VertexFormat.DrawMode.TRIANGLES,
                    VertexFormats.POSITION_COLOR
            );

            ring(rings, matrix, radius,
                    0.035f + (1f - p) * 0.025f, 0.018f,
                    0f, TAU, effect.accent, fade * 0.72f, 112);

            ring(rings, matrix, radius * 0.68f,
                    0.018f, 0.025f,
                    (float) Math.PI / 4f, TAU * 0.72f,
                    effect.color, fade * 0.52f, 80);

            ring(rings, matrix, radius * 1.18f,
                    0.012f, 0.012f,
                    -(float) Math.PI / 3f, TAU * 0.56f,
                    effect.accent, fade * 0.38f, 64);

            drawBuilt(rings.endNullable());
        }

        float flash = 1f - smoothstep(progress / 0.16f);
        if (flash <= 0.002f) {
            return;
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, GLOW_TEX);

        BufferBuilder flashQuad = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR
        );

        glow(flashQuad, matrices, event, 0f, 0.045f, 0f,
                0.9f + radius * 0.35f, 0.9f + radius * 0.35f,
                effect.accent, alpha * flash * 0.55f);

        BuiltBuffer built = flashQuad.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    /** Р  РЎвЂєР РЋР вЂљР  Р’В±Р  РЎвЂР РЋРІР‚С™Р  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂўР  Р’В»Р  РЎвЂќР  РЎвЂ, Р  РЎвЂќР  РЎвЂўР РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂўР  Р’В±Р  Р’В»Р  Р’ВµР РЋРІР‚С™Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В± Р  РЎвЂ Р  РЎвЂР РЋР С“Р РЋРІР‚РЋР  Р’ВµР  Р’В·Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р  РЎвЂќ Р  РЎвЂќР  РЎвЂўР  Р вЂ¦Р РЋРІР‚ Р РЋРЎвЂњ Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р  Р’В°. */
    private void renderOrbiters(
            BufferBuilder buffer,
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha,
            float growth,
            float collapse
    ) {
        float fade = alpha * (1f - smoothstep((progress - 0.72f) / 0.28f));
        if (fade <= 0.002f) return;

        for (int i = 0; i < 8; i++) {
            float phase = i * TAU / 8f;
            float angle = phase + time * (1.7f + i * 0.06f);
            float y = (0.32f + ((i * 37) % 100) / 100f * 0.64f) * HEIGHT * growth;
            float orbit = (0.42f + 0.18f * (float) Math.sin(time * 2.1f + phase))
                    * (0.5f + collapse * 0.5f);
            float x = (float) Math.cos(angle) * orbit;
            float z = (float) Math.sin(angle) * orbit;
            float size = 0.075f + 0.025f * (float) Math.sin(time * 8f + phase);

            glow(buffer, matrices, event, x, y, z,
                    size * 4.5f, size * 4.5f,
                    effect.color, fade * 0.28f);
            glow(buffer, matrices, event, x, y, z,
                    size, size * 2.1f,
                    effect.accent, fade * 0.9f);
        }
    }

    /** Р  РЎв„ўР РЋР вЂљР РЋРЎвЂњР  РЎвЂ”Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  Р вЂ Р РЋРІР‚в„–Р РЋРІР‚С™Р РЋР РЏР  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂўР  Р’В»Р  РЎвЂќР  РЎвЂ: Р  РЎвЂќР  РЎвЂўР РЋР вЂљР  РЎвЂўР РЋРІР‚С™Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋР вЂљР РЋРІР‚С™Р  РЎвЂўР  Р вЂ Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  Р вЂ Р РЋРІР‚в„–Р  Р’В±Р РЋР вЂљР  РЎвЂўР РЋР С“ Р  РЎвЂР  Р’В· Р РЋР РЏР  РўвЂР РЋР вЂљР  Р’В° Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р  Р’В°. */
    private void renderShards(
            BufferBuilder buffer,
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha,
            float growth
    ) {
        float start = clamp(progress / 0.16f);
        float fade = alpha * (1f - smoothstep((progress - 0.58f) / 0.42f));
        if (fade <= 0.002f) return;

        for (int i = 0; i < 14; i++) {
            float seed = i * TAU / 14f;
            float angle = seed + (float) Math.sin(i * 12.73f) * 0.3f;
            float travel = easeOutCubic(clamp((progress - (i % 4) * 0.018f) / 0.62f));
            float radius = (0.15f + (0.55f + (i % 5) * 0.11f) * travel) * growth;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float y = 0.18f + (0.55f + (i % 6) * 0.18f) * travel
                    - travel * travel * (0.25f + (i % 3) * 0.08f);

            float length = (0.12f + (i % 4) * 0.035f) * (1f - travel * 0.25f);
            float width = 0.022f + (i % 3) * 0.012f;
            float flicker = 0.82f + 0.18f * (float) Math.sin(time * 18f + seed);
            int color = i % 3 == 0 ? effect.accent : effect.color;

            glow(buffer, matrices, event, x, y, z,
                    width * 5.5f, length * 4.5f,
                    color, fade * flicker * 0.24f);
            glow(buffer, matrices, event, x, y, z,
                    width, length,
                    effect.accent, fade * flicker * 0.95f * start);

            for (int j = 1; j <= 3; j++) {
                float k = j / 3f;
                glow(buffer, matrices, event,
                        x * (1f - k * 0.18f),
                        y - k * 0.035f,
                        z * (1f - k * 0.18f),
                        width * (1f - k * 0.2f),
                        length * (1f - k * 0.2f),
                        color, fade * flicker * (0.18f / j));
            }
        }
    }

    private void renderSparks(
            BufferBuilder buffer,
            MatrixStack matrices,
            Event3DRender event,
            ActiveEffect effect,
            float progress,
            float time,
            float alpha
    ) {
        for (Spark spark : effect.sparks) {
            float age = clamp(
                    (progress - spark.delay) / (1f - spark.delay)
            );

            if (age <= 0f) {
                continue;
            }

            float travel = easeOutCubic(age);
            float angle = spark.angle + time * spark.spin;

            float distance = 0.12f + spark.distance * travel;
            float x = (float) Math.cos(angle) * distance;
            float z = (float) Math.sin(angle) * distance;

            float y = 0.15f
                    + spark.height * travel
                    - age * age * 0.7f;

            float flicker = 0.75f + 0.25f * (float) Math.sin(
                    time * 16f + spark.angle * 3f
            );

            float sparkAlpha = alpha
                    * smoothstep(age / 0.07f)
                    * (1f - smoothstep(age))
                    * flicker;

            float size = spark.size * (1f - age * 0.45f);
            int color = mixColor(effect.color, effect.accent, spark.tint);

            glow(
                    buffer, matrices, event,
                    x, y, z,
                    size * 4f, size * 4f,
                    color, sparkAlpha * 0.24f
            );

            glow(
                    buffer, matrices, event,
                    x, y, z,
                    size * 0.7f, size * 1.8f,
                    effect.accent, sparkAlpha
            );

            // Р  РЎв„ўР  РЎвЂўР РЋР вЂљР  РЎвЂўР РЋРІР‚С™Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋР С“Р  Р’В»Р  Р’ВµР  РўвЂ Р  РЎвЂР  Р’В· Р  РўвЂР  Р вЂ Р РЋРЎвЂњР РЋРІР‚В¦ Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋРІР‚В¦Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦ Р  Р’В±Р  Р’В»Р  РЎвЂР  РЎвЂќР  РЎвЂўР  Р вЂ .
            for (int j = 1; j <= 2; j++) {
                float trail = j * 0.035f;

                glow(
                        buffer, matrices, event,
                        x * (1f - trail),
                        y - trail * spark.height,
                        z * (1f - trail),
                        size * 0.65f,
                        size * 1.4f,
                        color,
                        sparkAlpha * (0.32f / j)
                );
            }
        }
    }

    private void glow(
            BufferBuilder buffer,
            MatrixStack matrices,
            Event3DRender event,
            float x,
            float y,
            float z,
            float width,
            float height,
            int color,
            float alpha
    ) {
        if (alpha <= 0.002f || width <= 0f || height <= 0f) {
            return;
        }

        matrices.push();

        matrices.translate(x, y, z);
        matrices.multiply(event.getCamera().getRotation());

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        int rgba = withAlpha(color, alpha);

        buffer.vertex(matrix, -halfW, -halfH, 0f)
                .texture(0f, 1f).color(rgba);
        buffer.vertex(matrix, -halfW, halfH, 0f)
                .texture(0f, 0f).color(rgba);
        buffer.vertex(matrix, halfW, halfH, 0f)
                .texture(1f, 0f).color(rgba);
        buffer.vertex(matrix, halfW, -halfH, 0f)
                .texture(1f, 1f).color(rgba);

        matrices.pop();
    }

    private void ring(
            BufferBuilder buffer,
            Matrix4f matrix,
            float radius,
            float width,
            float y,
            float start,
            float length,
            int color,
            float alpha,
            int segments
    ) {
        // Р  РЎСџР РЋР вЂљР  РЎвЂўР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР РЏ, Р РЋРІР‚ Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  Р вЂ¦Р  РЎвЂўР  РІвЂћвЂ“ Р  РЎвЂўР РЋР вЂљР  Р’ВµР  РЎвЂўР  Р’В» Р  РЎвЂ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  Р’В»Р  РЎвЂўР  Р’Вµ Р РЋР РЏР  РўвЂР РЋР вЂљР  РЎвЂў.
        float[] radii = {
                Math.max(0f, radius - width * 4f),
                Math.max(0f, radius - width),
                radius,
                radius + width,
                radius + width * 4f
        };

        int[] colors = {
                withAlpha(color, 0f),
                withAlpha(color, alpha * 0.3f),
                withAlpha(mixColor(color, 0xFFFFFF, 0.65f), alpha),
                withAlpha(color, alpha * 0.3f),
                withAlpha(color, 0f)
        };

        for (int i = 0; i < segments; i++) {
            float a = start + length * i / segments;
            float b = start + length * (i + 1) / segments;

            float ax = (float) Math.cos(a);
            float az = (float) Math.sin(a);
            float bx = (float) Math.cos(b);
            float bz = (float) Math.sin(b);

            for (int band = 0; band < radii.length - 1; band++) {
                float inner = radii[band];
                float outer = radii[band + 1];

                int innerColor = colors[band];
                int outerColor = colors[band + 1];

                vertex(buffer, matrix, ax * inner, y, az * inner, innerColor);
                vertex(buffer, matrix, ax * outer, y, az * outer, outerColor);
                vertex(buffer, matrix, bx * outer, y, bz * outer, outerColor);

                vertex(buffer, matrix, ax * inner, y, az * inner, innerColor);
                vertex(buffer, matrix, bx * outer, y, bz * outer, outerColor);
                vertex(buffer, matrix, bx * inner, y, bz * inner, innerColor);
            }
        }
    }

    private static void vertex(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            int color
    ) {
        buffer.vertex(matrix, x, y, z).color(color);
    }

    private static float envelope(float progress) {
        float appear = smoothstep(progress / 0.035f);
        float disappear = 1f - smoothstep(
                (progress - 0.38f) / 0.62f
        );

        return appear * disappear;
    }

    private static float clamp(float value) {
        return MathHelper.clamp(value, 0f, 1f);
    }

    private static float smoothstep(float value) {
        float t = clamp(value);
        return t * t * (3f - 2f * t);
    }

    private static float easeOutCubic(float value) {
        float t = 1f - clamp(value);
        return 1f - t * t * t;
    }

    private static int withAlpha(int color, float alpha) {
        return ((int) (clamp(alpha) * 255f) << 24)
                | (color & 0x00FFFFFF);
    }

    private static int mixColor(int first, int second, float factor) {
        float t = clamp(factor);

        int r = (int) (
                ((first >> 16) & 255) * (1f - t)
                        + ((second >> 16) & 255) * t
        );

        int g = (int) (
                ((first >> 8) & 255) * (1f - t)
                        + ((second >> 8) & 255) * t
        );

        int b = (int) (
                (first & 255) * (1f - t)
                        + (second & 255) * t
        );

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static long nowMs() {
        return System.nanoTime() / 1_000_000L;
    }

    // =====================================================================
    //  1. Р  Р’В§Р  Р С“Р   Р  РЎСљР  РЎвЂ™Р  Р вЂЎ Р  РІР‚СњР  Р’В«Р   Р  РЎвЂ™: Р  Р вЂ¦Р  Р’В°Р  РўвЂ Р  РЎВР  РЎвЂўР  Р’В±Р  РЎвЂўР  РЎВ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  РЎвЂќР  Р’В°Р  Р’В»Р РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РЎвЂ“Р  РЎвЂўР РЋР вЂљР  РЎвЂР  Р’В·Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™ Р РЋР С“Р  РЎвЂўР  Р’В±Р РЋРІР‚в„–Р РЋРІР‚С™Р  РЎвЂР  РІвЂћвЂ“. Р  РЎС™Р  РЎвЂўР  Р’В±
    //     Р  Р’В·Р  Р’В°Р  РЎВР  РЎвЂР РЋР вЂљР  Р’В°Р  Р’ВµР РЋРІР‚С™, Р  Р вЂ Р РЋР С“Р  РЎвЂ”Р  Р’В»Р РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™, Р  Р’В° Р  РЎвЂ”Р  РЎвЂўР РЋРІР‚С™Р  РЎвЂўР  РЎВ Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  Р вЂ Р РЋРІР‚в„–Р РЋРІР‚С™Р РЋР РЏР  РЎвЂ“Р  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  Р вЂ  Р  РЎВР  Р’В°Р  РЎвЂќР  Р’В°Р РЋР вЂљР  РЎвЂўР  Р вЂ¦Р  РЎвЂР  Р вЂ¦Р РЋРЎвЂњ
    //     (Р РЋР С“Р  РЎвЂ”Р  Р’В°Р  РЎвЂ“Р  Р’ВµР РЋРІР‚С™Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚С›Р  РЎвЂР  РЎвЂќР  Р’В°Р РЋРІР‚ Р  РЎвЂР РЋР РЏ) Р  РЎвЂ Р  РЎвЂ”Р  РЎвЂў Р РЋР С“Р  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  Р’В»Р  РЎвЂ Р  Р’В·Р  Р’В°Р РЋР С“Р  Р’В°Р РЋР С“Р РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  Р’В·Р  Р’В° Р  РЎвЂ“Р  РЎвЂўР РЋР вЂљР  РЎвЂР  Р’В·Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™.
    //     Р  Р’В¤Р  РЎвЂР  Р вЂ¦Р  Р’В°Р  Р’В»: Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР  Р’В° Р РЋР С“Р РЋРІР‚В¦Р  Р’В»Р  РЎвЂўР  РЎвЂ”Р РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ Р  РЎвЂў Р  Р вЂ Р РЋР С“Р  РЎвЂ”Р РЋРІР‚в„–Р РЋРІвЂљВ¬Р  РЎвЂќР РЋРЎвЂњ.
    // =====================================================================

    private void renderBlackHole(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p;
        float t = c.t;
        float s = fx.scale;
        float h = fx.height;

        float hx = 0f;
        float hy = h + 1.45f * s;
        float hz = 0f;

        float open = easeOutCubic(p / 0.14f);
        float collapse = smoothstep((p - 0.8f) / 0.08f);
        float size = open * (1f - collapse);
        float breathe = 1f + 0.22f * smoothstep((p - 0.58f) / 0.18f) * (1f - collapse);
        float radius = 0.42f * s * size * breathe;
        float boom = clamp((p - 0.86f) / 0.14f);

        // С„РёРЅР°Р»СЊРЅС‹Р№ РІР·СЂС‹РІ РґС‹СЂС‹ Р±СЊС‘С‚ РїРѕ СЌРєСЂР°РЅСѓ (РѕРґРёРЅ СЂР°Р·)
        if (boom > 0f && !fx.finishFlash) {
            fx.finishFlash = true;
            if (screenFlash.isState()) {
                impacts.add(new Impact(fx.position.add(0, fx.height + 1.45f * fx.scale, 0), 0.4f,
                        nowMs(), mixColor(fx.accent, 0xFFF3E0, 0.5f)));
            }
        }

        int hot = mixColor(fx.accent, 0xFFF3E0, 0.6f);
        int warm = mixColor(fx.color, 0xFF7A2E, 0.35f);
        int deep = mixColor(fx.color, 0x3A0CA8, 0.45f);

        // ---------- Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў: Р  Р’В»Р  Р’ВµР  Р вЂ Р  РЎвЂР РЋРІР‚С™Р  Р’В°Р РЋРІР‚ Р  РЎвЂР РЋР РЏ Р  РЎвЂ Р РЋР С“Р  РЎвЂ”Р  Р’В°Р  РЎвЂ“Р  Р’ВµР РЋРІР‚С™Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚С›Р  РЎвЂР  РЎвЂќР  Р’В°Р РЋРІР‚ Р  РЎвЂР РЋР РЏ ----------
        float lift = smoothstep((p - 0.06f) / 0.3f);
        float topPull = smoothstep((p - 0.3f) / 0.3f);
        float botRaw = clamp((p - 0.3f) / 0.46f);
        float botPull = botRaw * botRaw * botRaw;

        float spiral = (1f - botPull) * (0.25f + botPull) * 0.9f * s * topPull;
        float sa = t * (3f + botPull * 9f);

        float fx0 = cos(sa) * spiral;
        float fz0 = sin(sa) * spiral;
        float fy0 = lift * 0.5f * s;
        float bx = fx0 + (hx - fx0) * botPull;
        float by = fy0 + (hy - fy0) * botPull;
        float bz = fz0 + (hz - fz0) * botPull;

        float headY = fy0 + h;
        float tx = hx * topPull * 0.96f;
        float ty = headY + (hy - headY) * topPull * 0.96f;
        float tz = hz * topPull * 0.96f;

        float ax = tx - bx;
        float ay = ty - by;
        float az = tz - bz;
        float len = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        float tidal = topPull;

        if (botPull < 0.985f && len > 0.01f) {
            float thin = (1f - 0.8f * tidal) * Math.min(1f, len / (h * 0.25f));
            float shiver = lift * (1f - tidal) * 0.018f * s;

            c.m.push();
            c.m.translate(bx + sin(t * 77f) * shiver, by, bz + cos(t * 63f) * shiver);
            c.m.multiply(new Quaternionf().rotationTo(0f, 1f, 0f, ax / len, ay / len, az / len));
            c.m.multiply(RotationAxis.POSITIVE_Y.rotation(tidal * tidal * 10f + lift * 0.6f));
            c.m.scale(Math.max(0.02f, thin), len / h, Math.max(0.02f, thin));

            // Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р РЋРІР‚С™Р  Р’ВµР  РЎВР  Р вЂ¦Р  Р’ВµР  Р’ВµР РЋРІР‚С™ Р  РЎвЂ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР С“Р  Р вЂ¦Р  Р’ВµР  Р’ВµР РЋРІР‚С™ (Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ Р  РЎвЂР РЋРІР‚С™Р  Р’В°Р РЋРІР‚ Р  РЎвЂР  РЎвЂўР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР С“Р  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р РЋР С“Р  РЎВР  Р’ВµР РЋРІР‚В°Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ)
            float red = tidal;
            drawBody(c,
                    lerp(0.82f, 1.02f, red) * (1f - botPull * 0.6f),
                    lerp(0.78f, 0.42f, red) * (1f - botPull * 0.6f),
                    lerp(1.0f, 0.62f, red) * (1f - botPull * 0.6f));
            // РіСЂР°РІРёС‚Р°С†РёРѕРЅРЅРѕРµ РєСЂР°СЃРЅРѕРµ СЃРјРµС‰РµРЅРёРµ: СЃРґРµСЂР¶Р°РЅРЅРѕРµ РіРѕСЂСЏС‡РµРµ СЃРІРµС‡РµРЅРёРµ РїРѕ РєРѕРЅС‚СѓСЂСѓ
            drawBodyGlow(c, 1.4f, 0.5f, 0.7f,
                    (0.22f + 0.45f * tidal) * (1f - botPull * 0.85f));
            c.m.pop();

            // Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В»Р  РЎвЂР  Р вЂ Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂ”Р  РЎвЂўР РЋРІР‚С™Р  РЎвЂўР  РЎвЂќР  РЎвЂ: Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  РЎвЂР  Р’ВµР РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  РЎвЂР РЋРІР‚С™Р  РЎвЂ Р  РЎвЂўР РЋРІР‚С™ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° Р  РЎвЂќ Р  РЎвЂ“Р  РЎвЂўР РЋР вЂљР  РЎвЂР  Р’В·Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™Р РЋРЎвЂњ
            additive();
            BufferBuilder b = beginColor();
            float strandA = tidal * (1f - botPull) * size;
            for (int k = 0; k < 4; k++) {
                float px = bx;
                float py = by;
                float pz = bz;
                for (int j = 1; j <= 14; j++) {
                    float f = j / 14f;
                    float tw = f * 7f + t * 6f + k * HALF_PI;
                    float rr = (1f - f) * fx.width * 0.35f * (1f - tidal * 0.7f);
                    float nx = bx + (hx - bx) * f + cos(tw) * rr;
                    float ny = by + (hy - by) * f;
                    float nz = bz + (hz - bz) * f + sin(tw) * rr;
                    int col = mixColor(fx.accent, hot, f);
                    beam(b, c, px, py, pz, nx, ny, nz, 0.05f * s, col, strandA * (0.25f + 0.6f * f));
                    px = nx;
                    py = ny;
                    pz = nz;
                }
            }
            drawColor(b);
        }

        // ---------- Р РЋР С“Р  Р’В°Р  РЎВР  Р’В° Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР  Р’В° (Р РЋР С“Р  РЎВР  Р’ВµР РЋРІР‚В°Р РЋРІР‚ВР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋР С“Р  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’ВµР  РЎВР  Р’В° Р  РЎвЂќР  РЎвЂўР  РЎвЂўР РЋР вЂљР  РўвЂР  РЎвЂР  Р вЂ¦Р  Р’В°Р РЋРІР‚С™) ----------
        c.m.push();
        c.m.translate(hx, hy, hz);
        Ctx hc = c.shift(hx, hy, hz);
        float camA = (float) Math.atan2(hc.cz, hc.cx);

        if (size > 0.01f) {
            additive();
            BufferBuilder b = beginColor();
            // Р  РЎвЂР РЋР С“Р  РЎвЂќР  Р’В°Р  Р’В¶Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋР С“Р РЋРІР‚С™Р  Р вЂ Р  Р’В°: Р  РЎвЂќР  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІР‚ Р  Р’В°, Р  Р вЂ Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  Р’Вµ Р  Р вЂ  Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР РЋРЎвЂњ
            for (int k = 0; k < 8; k++) {
                float f = frac(t * 0.55f + k / 8f);
                float r = radius * (1.3f + (1f - f) * (1f - f) * 7.5f);
                float a = sin(f * PI) * 0.22f * size;
                billboardRing(b, hc, 0f, 0f, 0f, r, (0.012f + 0.03f * (1f - f)) * s,
                        0f, TAU, k % 2 == 0 ? fx.color : deep, a, 72);
            }
            // Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ Р  РЎвЂР РЋРІР‚С™Р  Р’В°Р РЋРІР‚ Р  РЎвЂР  РЎвЂўР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р’В»Р  РЎвЂР  Р вЂ¦Р  Р’В·Р  Р’В°
            billboardRing(b, hc, 0f, 0f, 0f, radius * 1.55f, 0.05f * s,
                    t * 0.8f, TAU, hot, 0.75f * size, 96);
            billboardRing(b, hc, 0f, 0f, 0f, radius * 1.9f, 0.08f * s,
                    -t * 0.5f, TAU, fx.accent, 0.35f * size, 96);
            billboardRing(b, hc, 0f, 0f, 0f, radius * 2.6f, 0.12f * s,
                    t * 0.3f, TAU, fx.color, 0.16f * size, 96);

            accretionDisk(b, hc, 0f, radius, camA, camA + HALF_PI, hot, warm, size);

            // Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р РЋР РЏР РЋР вЂљР  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РўвЂР  Р’В¶Р  Р’ВµР РЋРІР‚С™Р РЋРІР‚в„–
            float jet = smoothstep((p - 0.12f) / 0.12f) * (1f - collapse);
            float jl = (2.2f + 0.3f * sin(t * 9f)) * s * jet;
            for (int d = -1; d <= 1; d += 2) {
                beam(b, hc, 0f, d * radius * 0.6f, 0f, 0f, d * jl, 0f,
                        0.5f * s, fx.color, 0.25f * jet);
                beam(b, hc, 0f, d * radius * 0.6f, 0f, 0f, d * jl * 0.9f, 0f,
                        0.12f * s, hot, 0.8f * jet);
            }
            drawColor(b);

            BufferBuilder halo = beginGlow();
            glow(halo, hc, 0f, 0f, 0f, radius * 9f, radius * 9f, deep, 0.2f * size);
            glow(halo, hc, 0f, 0f, 0f, radius * 5f, radius * 5f, fx.color, 0.3f * size);
            glow(halo, hc, 0f, 0f, 0f, radius * 3.2f, radius * 3.2f, hot, 0.35f * size);
            glow(halo, hc, 0f, 0f, 0f, radius * 11f, radius * 0.5f, hot, 0.35f * size);
            for (int i = 0; i < 40; i++) {
                float ph = frac(t * 1.4f + i / 40f);
                float d = (i % 2 == 0) ? 1f : -1f;
                float ang = i * 2.4f + t * 6f;
                float rr = (0.05f + ph * 0.25f) * s;
                float y = d * (radius * 0.6f + ph * jl);
                glow(halo, hc, cos(ang) * rr, y, sin(ang) * rr,
                        0.12f * s, 0.2f * s, hot, (1f - ph) * jet * 0.8f);
            }
            drawGlow(halo);

            // Р  РЎвЂ“Р  РЎвЂўР РЋР вЂљР  РЎвЂР  Р’В·Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™ Р РЋР С“Р  РЎвЂўР  Р’В±Р РЋРІР‚в„–Р РЋРІР‚С™Р  РЎвЂР  РІвЂћвЂ“: Р  Р’В°Р  Р’В±Р РЋР С“Р  РЎвЂўР  Р’В»Р РЋР вЂ№Р РЋРІР‚С™Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋРІР‚РЋР  Р’ВµР РЋР вЂљР  Р вЂ¦Р  РЎвЂўР РЋРІР‚С™Р  Р’В°
            translucent();
            b = beginColor();
            softDisc(b, hc, 0f, 0f, 0f, radius, radius * 0.35f, 0x030206, 1f, 64);
            drawColor(b);

            additive();
            b = beginColor();
            accretionDisk(b, hc, 0f, radius, camA, camA - HALF_PI, hot, warm, size);
            billboardRing(b, hc, 0f, 0f, 0f, radius * 1.03f, 0.012f * s,
                    0f, TAU, 0xFFFFFF, 0.95f * size, 96);
            billboardRing(b, hc, 0f, 0f, 0f, radius * 1.1f, 0.03f * s,
                    0f, TAU, hot, 0.5f * size, 96);
            drawColor(b);
        }

        additive();
        BufferBuilder g = beginGlow();

        float pierce = 1f - smoothstep(p / 0.14f);
        glow(g, hc, 0f, 0f, 0f, 3f * s, 3f * s, fx.accent, pierce * 0.8f);
        glow(g, hc, 0f, 0f, 0f, 6f * s, 0.12f * s, 0xFFFFFF, pierce * 0.9f);
        glow(g, hc, 0f, 0f, 0f, 0.12f * s, 4f * s, 0xFFFFFF, pierce * 0.6f);

        // Р РЋРІР‚РЋР  Р’В°Р РЋР С“Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚ Р РЋРІР‚в„–, Р РЋР С“Р  РЎвЂўР РЋР вЂљР  Р вЂ Р  Р’В°Р  Р вЂ¦Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р РЋР С“ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° (Р  РЎвЂќР  РЎвЂўР  РЎвЂўР РЋР вЂљР  РўвЂР  РЎвЂР  Р вЂ¦Р  Р’В°Р РЋРІР‚С™Р РЋРІР‚в„– Р  РЎвЂўР РЋРІР‚С™Р  Р вЂ¦Р  РЎвЂўР РЋР С“Р  РЎвЂР РЋРІР‚С™Р  Р’ВµР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  РЎвЂў Р  РўвЂР РЋРІР‚в„–Р РЋР вЂљР РЋРІР‚в„–)
        for (Mote mo : fx.motes) {
            float st = 0.3f + mo.delay() * 0.4f;
            float life = clamp((p - st) / 0.28f);
            if (life <= 0f) continue;
            float pull = life * life * life;
            float rot = pull * 5f;
            float cs = cos(rot);
            float sn = sin(rot);
            float rx = mo.x() * cs - mo.z() * sn;
            float rz = mo.x() * sn + mo.z() * cs;

            float sx = bx + rx - hx;
            float sy = by + mo.y() * (1f - tidal * 0.5f) - hy;
            float sz0 = bz + rz - hz;
            float x = sx * (1f - pull);
            float y = sy * (1f - pull);
            float z = sz0 * (1f - pull);

            float a = smoothstep(life / 0.1f)
                    * (1f - smoothstep((pull - 0.7f) / 0.3f))
                    * (1f - collapse);
            float sz = mo.size() * (1f - pull * 0.5f);
            int col = mixColor(fx.accent, hot, mo.tint());

            glow(g, hc, x, y, z, sz * 4f, sz * 4f, fx.color, a * 0.25f);
            glow(g, hc, x, y, z, sz, sz * (1f + pull * 6f), col, a);
            if (pull > 0.15f) {
                float k = 1f - pull * 0.8f;
                glow(g, hc, sx * k, sy * k, sz0 * k, sz * 0.6f, sz * 3f, col, a * 0.45f);
            }
        }

        // Р  Р вЂ Р  Р’ВµР РЋРІР‚В°Р  Р’ВµР РЋР С“Р РЋРІР‚С™Р  Р вЂ Р  РЎвЂў Р РЋР С“Р  РЎвЂў Р  Р вЂ Р РЋР С“Р  Р’ВµР  РІвЂћвЂ“ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“Р  РЎвЂ Р  Р’В·Р  Р’В°Р  РЎвЂќР РЋР вЂљР РЋРЎвЂњР РЋРІР‚РЋР  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ Р  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋР вЂљР РЋР Р‰
        for (Mote d : fx.dust) {
            float ph = frac(t * d.speed() + d.phase());
            float inward = ph * ph;
            float r = radius * 1.2f + d.x() * (1f - inward);
            float ang = d.dy() + ph * 7f;
            float y = d.y() * (1f - inward) * 0.5f;
            float a = sin(ph * PI) * size;
            int col = mixColor(fx.accent, hot, d.tint());

            glow(g, hc, cos(ang) * r, y, sin(ang) * r,
                    d.size() * 3.5f, d.size() * 3.5f, fx.color, a * 0.25f);
            glow(g, hc, cos(ang) * r, y, sin(ang) * r,
                    d.size(), d.size() * (1f + inward * 3f), col, a);
        }

        if (boom > 0f) {
            float fb = 1f - smoothstep(boom);
            float e = easeOutCubic(boom);
            glow(g, hc, 0f, 0f, 0f, (0.8f + e * 7f) * s, (0.8f + e * 7f) * s, hot, fb * 0.95f);
            glow(g, hc, 0f, 0f, 0f, (0.4f + e * 3f) * s, (0.4f + e * 3f) * s, 0xFFFFFF, fb);
            starFlare(g, hc, 0f, 0f, 0f, (2f + e * 9f) * s, hot, fb, 8, t * 0.4f);
            c.m.push();
            c.m.translate(0f, -0.15f, 0f);
            renderSparks(g, c.m, c.e, fx, boom, t, fb);
            c.m.pop();
        }
        drawGlow(g);

        if (boom > 0f) {
            float fb = 1f - smoothstep(boom);
            float e = easeOutCubic(boom);
            BufferBuilder b = beginColor();
            billboardRing(b, hc, 0f, 0f, 0f, (0.2f + e * 3.4f) * s, 0.05f * s,
                    0f, TAU, hot, fb * 0.85f, 96);
            billboardRing(b, hc, 0f, 0f, 0f, (0.1f + e * 2f) * s, 0.03f * s,
                    0f, TAU, 0xFFFFFF, fb * 0.7f, 72);
            drawColor(b);
        }
        c.m.pop();

        // ---------- Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р РЋР РЏ: Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚В°Р  РЎвЂР  Р вЂ¦Р РЋРІР‚в„– Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ Р  РЎвЂР РЋРІР‚С™Р  Р’В°Р РЋРІР‚ Р  РЎвЂР  РЎвЂ Р  РЎвЂ Р  РЎвЂ”Р РЋРІР‚в„–Р  Р’В»Р РЋР Р‰, Р  Р вЂ Р РЋРІР‚С™Р РЋР РЏР  РЎвЂ“Р  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР  РЎВР  Р’В°Р РЋР РЏ Р  Р вЂ Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚В¦ ----------
        additive();
        BufferBuilder b = beginColor();
        float groundA = open * (1f - collapse);
        for (int k = 0; k < 5; k++) {
            float f = frac(t * 0.4f + k / 5f);
            ring(b, c.mat(), (0.3f + (1f - f) * 2.8f) * s, 0.02f * s, 0.03f,
                    0f, TAU, k % 2 == 0 ? fx.color : deep, sin(f * PI) * 0.45f * groundA, 72);
        }
        if (boom > 0f) {
            float fb = 1f - smoothstep(boom);
            float e = easeOutCubic(boom);
            ring(b, c.mat(), (0.3f + e * 4.5f) * s, 0.06f * (1f - boom) + 0.012f, 0.03f,
                    0f, TAU, fx.accent, fb * 0.9f, 128);
        }
        drawColor(b);

        BufferBuilder up = beginGlow();
        for (int i = 0; i < 36; i++) {
            float hs = hash(i * 3.17f + fx.seedF);
            float ph = frac(t * (0.5f + hs * 0.4f) + hs);
            float ang = hs * TAU * 3f + ph * 3f;
            float r = (0.4f + hash(i * 1.3f) * 2.4f) * s * (1f - ph * ph);
            float y = ph * ph * hy;
            glow(up, c, cos(ang) * r, y, sin(ang) * r, 0.06f * s, 0.06f * s * (1f + ph * 4f),
                    mixColor(fx.accent, hot, hs), sin(ph * PI) * 0.7f * groundA);
        }
        drawGlow(up);
    }

    /** Р  РЎСџР  Р’В»Р  РЎвЂўР РЋРІР‚С™Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  Р’В°Р  РЎвЂќР  РЎвЂќР РЋР вЂљР  Р’ВµР РЋРІР‚ Р  РЎвЂР  РЎвЂўР  Р вЂ¦Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  РўвЂР  РЎвЂР РЋР С“Р  РЎвЂќ: Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  РўвЂР  РЎвЂР  Р’ВµР  Р вЂ¦Р РЋРІР‚С™ Р  РЎвЂ”Р  РЎвЂў Р РЋР вЂљР  Р’В°Р  РўвЂР  РЎвЂР РЋРЎвЂњР РЋР С“Р РЋРЎвЂњ, Р  РЎвЂќР  Р’ВµР  РЎвЂ”Р  Р’В»Р  Р’ВµР РЋР вЂљР  РЎвЂўР  Р вЂ Р  РЎвЂў Р  Р вЂ Р РЋР вЂљР  Р’В°Р РЋРІР‚В°Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ, Р  РўвЂР  РЎвЂўР  РЎвЂ”Р  Р’В»Р  Р’ВµР РЋР вЂљ-Р РЋР РЏР РЋР вЂљР  РЎвЂќР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР Р‰. */
    private void accretionDisk(
            BufferBuilder b, Ctx c, float cy, float radius,
            float camA, float start, int hot, int warm, float size
    ) {
        c.m.push();
        c.m.translate(0f, cy, 0f);
        c.m.multiply(new Quaternionf().rotationAxis(0.26f, -sin(camA), 0f, cos(camA)));
        c.m.multiply(new Quaternionf().rotationAxis(0.36f, cos(camA), 0f, sin(camA)));
        Matrix4f m = c.mat();

        final int bands = 14;
        final int seg = 48;
        float rIn = radius * 1.25f;
        float rOut = radius * 4.2f;

        for (int k = 0; k < bands; k++) {
            float f0 = k / (float) bands;
            float f1 = (k + 1) / (float) bands;
            float r0 = rIn + (rOut - rIn) * f0 * f0;
            float r1 = rIn + (rOut - rIn) * f1 * f1;
            float fm = (f0 + f1) * 0.5f;
            int col = fm < 0.35f
                    ? mixColor(0xFFFFFF, hot, fm / 0.35f)
                    : fm < 0.7f
                      ? mixColor(hot, c.fx.accent, (fm - 0.35f) / 0.35f)
                      : mixColor(c.fx.accent, warm, (fm - 0.7f) / 0.3f);
            float spin = c.t * 3.2f / (float) Math.pow(0.35f + fm, 1.5);
            float baseA = size * (0.75f * (1f - fm) + 0.08f);

            for (int j = 0; j < seg; j++) {
                float a0 = start + PI * j / seg;
                float a1 = start + PI * (j + 1) / seg;
                float am = (a0 + a1) * 0.5f;
                // Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  Р’В° Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР  РЎвЂўР  Р вЂ¦Р  Р’В° Р РЋР РЏР РЋР вЂљР РЋРІР‚РЋР  Р’Вµ: Р  Р вЂ Р  Р’ВµР РЋРІР‚В°Р  Р’ВµР РЋР С“Р РЋРІР‚С™Р  Р вЂ Р  РЎвЂў Р  Р’В»Р  Р’ВµР РЋРІР‚С™Р  РЎвЂР РЋРІР‚С™ Р  Р вЂ¦Р  Р’В° Р  Р вЂ¦Р  Р’В°Р РЋР С“
                float doppler = 0.55f + 0.45f * cos(am - camA + HALF_PI);
                float streak = 0.55f + 0.45f * sin(am * 7f - spin + k * 1.3f)
                        * sin(am * 3f + spin * 0.6f + k);
                float a = clamp(baseA * doppler * (0.6f + 0.6f * streak));
                float edge = (f0 == 0f) ? 1f : 1f;
                int ci = withAlpha(col, a * edge * (k == 0 ? 0.3f : 1f));
                int co = withAlpha(col, a * (k == bands - 1 ? 0f : 1f));
                quad(b, m,
                        cos(a0) * r0, 0f, sin(a0) * r0, ci,
                        cos(a0) * r1, 0f, sin(a0) * r1, co,
                        cos(a1) * r1, 0f, sin(a1) * r1, co,
                        cos(a1) * r0, 0f, sin(a1) * r0, ci);
            }
        }
        // Р РЋР РЏР РЋР вЂљР  РЎвЂќР  Р’В°Р РЋР РЏ Р  Р вЂ Р  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р РЋР РЏР РЋР РЏ Р  РЎвЂќР РЋР вЂљР  РЎвЂўР  РЎВР  РЎвЂќР  Р’В°
        ring(b, m, rIn, 0.02f * c.fx.scale, 0f, start, PI, 0xFFFFFF, 0.9f * size, 48);
        c.m.pop();
    }

    /** Р  РІР‚вЂќР  Р вЂ Р РЋРІР‚ВР  Р’В·Р  РўвЂР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ Р РЋР С“Р  РЎвЂ”Р РЋРІР‚в„–Р РЋРІвЂљВ¬Р  РЎвЂќР  Р’В° Р  РЎвЂР  Р’В· Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  Р’ВµР  РІвЂћвЂ“. */
    private void starFlare(BufferBuilder g, Ctx c, float x, float y, float z,
                           float len, int color, float alpha, int rays, float rot) {
        if (alpha <= 0.002f) return;
        for (int i = 0; i < rays; i++) {
            float a = rot + PI * i / rays;
            float l = len * (i % 2 == 0 ? 1f : 0.55f);
            c.m.push();
            c.m.translate(x, y, z);
            c.m.multiply(c.e.getCamera().getRotation());
            c.m.multiply(RotationAxis.POSITIVE_Z.rotation(a));
            Matrix4f m = c.mat();
            int rgba = withAlpha(color, alpha * (i % 2 == 0 ? 0.8f : 0.5f));
            float hw = l * 0.5f;
            float hh = len * 0.018f;
            g.vertex(m, -hw, -hh, 0f).texture(0f, 1f).color(rgba);
            g.vertex(m, -hw, hh, 0f).texture(0f, 0f).color(rgba);
            g.vertex(m, hw, hh, 0f).texture(1f, 0f).color(rgba);
            g.vertex(m, hw, -hh, 0f).texture(1f, 1f).color(rgba);
            c.m.pop();
        }
    }

    // =====================================================================
    //  2. Р  РЎС™Р  РЎвЂ™Р  РІР‚СљР  РЎС™Р  РЎвЂ™: Р  Р’В»Р  Р’В°Р  Р вЂ Р  Р’В° Р  РЎвЂ”Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  РЎвЂР  РЎВР  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РЎвЂР  Р’В· Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  РЎвЂ Р  РЎвЂ Р  Р’В·Р  Р’В°Р  Р’В»Р  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В° Р  РЎвЂ”Р  РЎвЂў Р  Р вЂ Р РЋР С“Р  Р’ВµР  РЎВР РЋРЎвЂњ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р РЋРЎвЂњ
    //     (Р  РЎвЂ”Р  РЎвЂў Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р РЋР вЂљР  Р’В°Р  Р’В·Р  РЎВР  Р’ВµР РЋР вЂљР РЋРЎвЂњ), Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р РЋР вЂљР  Р’В°Р РЋР С“Р  РЎвЂќР  Р’В°Р  Р’В»Р РЋР РЏР  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РўвЂР  РЎвЂўР  Р’В±Р  Р’ВµР  Р’В»Р  Р’В°, Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р  Р’ВµР  РЎВ Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  Р вЂ
    //     Р  РЎвЂўР  Р’В±Р РЋР С“Р  РЎвЂР  РўвЂР  РЎвЂР  Р’В°Р  Р вЂ¦Р  РЎвЂўР  Р вЂ Р РЋРЎвЂњР РЋР вЂ№ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР вЂ№, Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋР С“Р  РЎвЂќР  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РЎвЂ Р РЋР вЂљР  Р’В°Р РЋР С“Р РЋР С“Р РЋРІР‚в„–Р  РЎвЂ”Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ  Р  РЎвЂ”Р  Р’ВµР  РЎвЂќР  Р’В»Р  РЎвЂў.
    // =====================================================================

    private static final int ROCK = 0x2B1B14;
    private static final int LAVA_DARK = 0x8A1E05;
    private static final int LAVA = 0xFF6A00;
    private static final int LAVA_HOT = 0xFFE2A0;
    private static final int LAVA_CORE = 0xFFFBEA;

    private static final float MAGMA_CRUMBLE = 0.7f;

    private void renderMagma(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p;
        float t = c.t;
        float s = fx.scale;
        float h = fx.height;
        float bodyR = fx.width * 0.5f;

        float level = smoothstep((p - 0.03f) / 0.26f) * (h + 0.1f);
        float cover = clamp(level / h);
        float cool = smoothstep((p - 0.36f) / 0.24f);
        float molten = smoothstep(p / 0.05f) * (1f - cool);
        float crackP = clamp((p - 0.55f) / 0.15f);
        float crack = easeOutCubic(clamp(p / 0.25f));
        float pitOpen = easeOutCubic(clamp((p - 0.5f) / 0.2f));
        float pitClose = smoothstep((p - 0.9f) / 0.1f);
        float pitLive = pitOpen * (1f - pitClose);
        float pitR = (0.45f + fx.width * 0.85f) * s * pitLive;
        float end = 1f - smoothstep((p - 0.88f) / 0.12f);

        // ---------- Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р вЂ Р  Р’В°Р  Р’В» ----------
        if (pitR > 0.01f) {
            translucent();
            BufferBuilder b = beginColor();
            flatDisc(b, c.mat(), pitR * 1.02f, 0.02f,
                    0x0A0200, 0.94f * pitLive, 0x2A0800, 0.75f * pitLive, 48);
            drawColor(b);
        }

        // ---------- Р  Р’В»Р  Р’В°Р  Р вЂ Р  РЎвЂўР  Р вЂ Р  Р’В°Р РЋР РЏ Р  Р’В»Р РЋРЎвЂњР  Р’В¶Р  Р’В° Р  РЎвЂ”Р  РЎвЂўР  РўвЂ Р  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  Р’В°Р  РЎВР  РЎвЂ + Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚В°Р  РЎвЂР  Р вЂ¦Р РЋРІР‚в„– ----------
        additive();
        BufferBuilder b = beginColor();
        Matrix4f m = c.mat();

        float pool = smoothstep(p / 0.08f) * end;
        float poolR = (bodyR * 1.6f + 0.35f * s) * easeOutCubic(p / 0.12f);
        flatDisc(b, m, poolR, 0.022f, LAVA_HOT, 0.55f * pool * (1f - pitLive), LAVA, 0f, 40);
        for (int k = 0; k < 4; k++) {
            float f = frac(t * 0.8f + k / 4f);
            ring(b, m, poolR * (0.3f + f * 0.9f), 0.03f * s, 0.026f, 0f, TAU,
                    mixColor(LAVA_HOT, LAVA, f), sin(f * PI) * 0.6f * pool, 48);
        }

        if (crack > 0f && end > 0f) {
            for (int i = 0; i < 16; i++) {
                float base = i * TAU / 16f + hash(i * 3.1f + fx.seedF) * 0.5f;
                float len = (0.9f + hash(i * 7.7f + fx.seedF) * 1.1f) * s * crack;
                float pulse = 0.7f + 0.3f * sin(t * 6f + i * 1.3f);
                float px = 0f;
                float pz = 0f;

                for (int j = 1; j <= 5; j++) {
                    float f = j / 5f;
                    float ang = base + (hash(i * 13.3f + j * 5.1f + fx.seedF) - 0.5f) * 0.55f;
                    float nx = cos(ang) * len * f;
                    float nz = sin(ang) * len * f;
                    float a = end * pulse * (1f - f * 0.55f);
                    float w = 1.1f - f * 0.6f;

                    flatLine(b, m, px, pz, nx, nz, 0.02f, 0.2f * w, 0xFF3A00, a * 0.3f);
                    flatLine(b, m, px, pz, nx, nz, 0.022f, 0.05f * w, 0xFFB050, a * 0.85f);
                    flatLine(b, m, px, pz, nx, nz, 0.024f, 0.018f * w, LAVA_CORE, a * 0.8f);
                    px = nx;
                    pz = nz;
                }
            }
        }

        if (pitR > 0.01f) {
            for (int k = 0; k < 16; k++) {
                float f = frac(t * 0.5f + k / 16f);
                float r = pitR * (1f - f) * 0.96f;
                int col = mixColor(0xFF4A00, LAVA_HOT, f);
                float a = sin(f * PI) * 0.85f * pitLive;
                ring(b, m, r, (0.02f + 0.06f * (1f - f)) * s, 0.024f, 0f, TAU, col, a, 48);
            }
            ring(b, m, pitR, 0.06f * s, 0.03f, 0f, TAU, 0xFFB44A, 1f * pitLive, 72);
            ring(b, m, pitR * 1.12f, 0.025f * s, 0.028f, t, TAU * 0.7f, 0xFF5A10, 0.7f * pitLive, 56);
            ring(b, m, pitR * 0.35f, 0.03f * s, 0.026f, -t * 1.4f, TAU, LAVA_CORE, 0.8f * pitLive, 40);
        }
        drawColor(b);

        // ---------- Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў ----------
        if (p < MAGMA_CRUMBLE) {
            float pulse = 0.5f + 0.5f * sin(t * 7f);
            float shake = crackP * crackP * 0.04f * s;

            // Р РЋР вЂљР  Р’В°Р РЋР С“Р  РЎвЂќР  Р’В°Р  Р’В»Р РЋРІР‚ВР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў (Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂР РЋРІР‚С™Р РЋР С“Р РЋР РЏ) -> Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРІР‚в„–Р  Р вЂ Р РЋРІвЂљВ¬Р  РЎвЂР  РІвЂћвЂ“ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР  Р вЂ¦Р РЋР Р‰ Р РЋР С“ Р РЋРІР‚С™Р  Р’В»Р  Р’ВµР РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  РЎВР  РЎвЂ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В¶Р  РЎвЂР  Р’В»Р  РЎвЂќР  Р’В°Р  РЎВР  РЎвЂ
            float k = cover * molten;
            float mr = lerp(1f, 1.35f + 0.15f * pulse, k);
            float mg = lerp(1f, 0.55f, k);
            float mb = lerp(1f, 0.22f, k);
            float ember = crackP * (0.4f + 0.6f * pulse);
            float rr = lerp(mr, 0.3f + 0.9f * ember, cool);
            float gg = lerp(mg, 0.2f + 0.3f * ember, cool);
            float bb = lerp(mb, 0.19f + 0.05f * ember, cool);

            c.m.push();
            c.m.translate(sin(t * 61f) * shake, 0f, cos(t * 53f) * shake);
            drawBody(c, rr, gg, bb);
            // Р¶Р°СЂ: СЃРґРµСЂР¶Р°РЅРЅРѕРµ СЂР°СЃРєР°Р»С‘РЅРЅРѕРµ СЃРІРµС‡РµРЅРёРµ, РЅР° РѕСЃС‚С‹РІС€РµР№ вЂ” С‚Р»РµСЋС‰РёРµ РїСЂРѕР¶РёР»РєРё
            drawBodyGlow(c, 1.8f, 0.75f, 0.22f,
                    0.14f + 0.5f * Math.max(molten * cover, ember * cool * 1.3f));
            c.m.pop();

            // Р  Р’В»Р  Р’В°Р  Р вЂ Р  РЎвЂўР  Р вЂ Р  Р’В°Р РЋР РЏ Р  РЎвЂўР  Р’В±Р  РЎвЂўР  Р’В»Р  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  Р’В° Р РЋРІР‚С™Р  РЎвЂўР РЋРІР‚РЋР  Р вЂ¦Р  РЎвЂў Р  РЎвЂ”Р  РЎвЂў Р РЋР вЂљР  Р’В°Р  Р’В·Р  РЎВР  Р’ВµР РЋР вЂљР РЋРЎвЂњ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В°
            lavaSkin(c, level, bodyR, h, 0.4f * molten, 0.9f * molten + 0.25f * cool * (1f - crackP));

            additive();
            b = beginColor();
            m = c.mat();
            // Р  РЎвЂќР РЋР вЂљР  РЎвЂўР  РЎВР  РЎвЂќР  Р’В° Р  РЎвЂ”Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  РЎвЂР  РЎВР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  Р’ВµР  РІвЂћвЂ“Р РЋР С“Р РЋР РЏ Р  Р’В»Р  Р’В°Р  Р вЂ Р РЋРІР‚в„–
            float rim = molten * (1f - smoothstep((p - 0.3f) / 0.08f));
            float rimR = bodyR * prof(cover) * 1.12f + 0.04f;
            ring(b, m, rimR, 0.04f * s, level, 0f, TAU, LAVA_HOT, rim, 48);
            ring(b, m, rimR * 1.1f, 0.08f * s, level, 0f, TAU, LAVA, rim * 0.45f, 48);

            // Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  РЎвЂР  Р’ВµР РЋР С“Р РЋР РЏ Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚В°Р  РЎвЂР  Р вЂ¦Р РЋРІР‚в„– Р  Р вЂ¦Р  Р’В° Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРІР‚в„–Р  Р вЂ Р РЋРІвЂљВ¬Р  Р’ВµР  РІвЂћвЂ“ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР  Р’Вµ
            if (crackP > 0f) {
                for (int kk = 0; kk < 11; kk++) {
                    float ang = hash(kk * 4.3f + fx.seedF) * TAU;
                    float y = hash(kk * 9.1f + fx.seedF) * h * 0.85f;
                    float drawn = crackP * 6f;
                    float r0 = bodyR * prof(y / h) * 1.06f + 0.02f;
                    float px = cos(ang) * r0;
                    float pz = sin(ang) * r0;
                    float py = y;
                    for (int j = 1; j <= 6 && j - 1 < drawn; j++) {
                        float part = clamp(drawn - (j - 1));
                        float na = ang + (hash(kk * 17f + j * 3.3f) - 0.5f) * 0.8f;
                        float ny = Math.max(0.02f, Math.min(h, y + (hash(kk * 11f + j * 7.7f) - 0.5f) * 0.35f * j));
                        float nr = bodyR * prof(ny / h) * 1.06f + 0.02f;
                        float nx = cos(na) * nr;
                        float nz = sin(na) * nr;
                        float tx = px + (nx - px) * part;
                        float ty = py + (ny - py) * part;
                        float tz = pz + (nz - pz) * part;
                        float pul = 0.7f + 0.3f * sin(t * 12f + kk);
                        beam(b, c, px, py, pz, tx, ty, tz, 0.14f * s, LAVA, 0.35f * pul);
                        beam(b, c, px, py, pz, tx, ty, tz, 0.035f * s, LAVA_HOT, 0.95f * pul);
                        px = nx; py = ny; pz = nz; ang = na; y = ny;
                    }
                }
            }
            drawColor(b);

            // Р  РЎвЂќР  Р’В°Р  РЎвЂ”Р  Р’В»Р  РЎвЂ Р  Р’В»Р  Р’В°Р  Р вЂ Р РЋРІР‚в„–, Р РЋР С“Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  Р’Вµ Р  РЎвЂ”Р  РЎвЂў Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р РЋРЎвЂњ
            BufferBuilder g = beginGlow();
            for (int i = 0; i < 22; i++) {
                float hs = hash(i * 2.71f + fx.seedF);
                float ph = frac(t * (0.45f + hs * 0.4f) + hs);
                float y = level * (1f - ph);
                float ang = hs * TAU * 3f;
                float r = bodyR * prof(y / h) * 1.1f + 0.04f;
                float a = sin(ph * PI) * (molten + cool * (1f - crackP) * 0.3f);
                glow(g, c, cos(ang) * r, y, sin(ang) * r, 0.07f * s, 0.2f * s, LAVA_HOT, a);
                glow(g, c, cos(ang) * r, y, sin(ang) * r, 0.3f * s, 0.4f * s, LAVA, a * 0.25f);
            }
            // Р  РЎвЂ”Р РЋРЎвЂњР  Р’В·Р РЋРІР‚в„–Р РЋР вЂљР  РЎвЂ Р  Р вЂ¦Р  Р’В° Р  РЎвЂ”Р  РЎвЂўР  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚В¦Р  Р вЂ¦Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р  РЎвЂ Р  Р’В»Р  Р’В°Р  Р вЂ Р РЋРІР‚в„–
            for (int i = 0; i < 10; i++) {
                float hs = hash(i * 5.9f + fx.seedF);
                float ph = frac(t * 1.3f + hs);
                float ang = hs * TAU * 2f + t * 0.3f;
                float r = bodyR * prof(cover) * 1.05f;
                float bs = (0.06f + ph * 0.12f) * s;
                glow(g, c, cos(ang) * r, level + ph * 0.06f, sin(ang) * r, bs, bs, LAVA_HOT,
                        rim * (1f - ph) * 0.9f);
            }
            // Р  Р’В¶Р  Р’В°Р РЋР вЂљ Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В°
            glow(g, c, 0f, h * 0.5f, 0f, (fx.width + 1.2f) * s * 1.2f, h * 1.4f, 0xFF5A10,
                    0.28f * molten * cover + 0.12f * ember * cool);
            float flash = 1f - smoothstep(p / 0.12f);
            glow(g, c, 0f, 0.3f, 0f, 3f * s, 1.2f * s, LAVA_HOT, flash * 0.7f);
            drawGlow(g);
        }

        // ---------- Р РЋР вЂљР  Р’В°Р  Р’В·Р РЋР вЂљР РЋРЎвЂњР РЋРІвЂљВ¬Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’Вµ: Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР РЏ Р РЋР вЂљР  Р’В°Р РЋР С“Р РЋР С“Р РЋРІР‚в„–Р  РЎвЂ”Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  Р’В° Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р  Р вЂ  Р  РЎвЂ”Р  Р’ВµР  РЎвЂќР  Р’В»Р  РЎвЂў ----------
        if (p >= MAGMA_CRUMBLE) {
            // СЂР°СЃРєР°Р»С‹РІР°РЅРёРµ СЃС‚Р°С‚СѓРё Р±СЊС‘С‚ РїРѕ СЌРєСЂР°РЅСѓ (РѕРґРёРЅ СЂР°Р·)
            if (!fx.finishFlash) {
                fx.finishFlash = true;
                if (screenFlash.isState()) {
                    impacts.add(new Impact(fx.position, fx.height * 0.5f, nowMs(), LAVA_HOT));
                }
            }
            translucent();
            RenderSystem.depthMask(true);
            b = beginColor();
            float[][] st = new float[fx.chunks.length][];
            for (int i = 0; i < fx.chunks.length; i++) {
                Chunk ch = fx.chunks[i];
                st[i] = chunkState(fx, ch, p);
                float[] cs = st[i];
                if (cs[3] <= 0.01f) continue;
                float ft = cs[5];
                Quaternionf q = new Quaternionf()
                        .rotationY(ch.ry() + ch.spin() * ft * 3f)
                        .rotateX(ch.rx() + ch.spin() * ft * 4f);
                float hs = ch.size() * cs[3];
                gem(b, c, cs[0], cs[1], cs[2], q, hs * 0.95f, hs * 1.7f, 6, i * 2.3f + fx.seedF,
                        heatColor(cs[4]), LAVA_HOT, 1f, 0.35f, false);
            }
            drawColor(b);
            RenderSystem.depthMask(false);

            additive();
            BufferBuilder g = beginGlow();
            for (int i = 0; i < fx.chunks.length; i++) {
                float[] cs = st[i];
                Chunk ch = fx.chunks[i];
                if (cs[3] > 0.01f) {
                    float gs = ch.size() * 5f * cs[3];
                    glow(g, c, cs[0], cs[1], cs[2], gs, gs, 0xFF5A00, cs[4] * 0.3f * cs[3]);
                }
                if (cs[3] < 0.95f && cs[3] > 0f) {
                    glow(g, c, cs[0], 0.08f, cs[2], 0.6f * s, 0.35f * s, 0xFFB040, (1f - cs[3]) * 0.6f);
                }
            }
            float burst = 1f - smoothstep((p - MAGMA_CRUMBLE) / 0.08f);
            glow(g, c, 0f, h * 0.5f, 0f, 3.4f * s, 3.4f * s, LAVA, burst * 0.6f);
            glow(g, c, 0f, h * 0.5f, 0f, 1.6f * s, 1.6f * s, LAVA_CORE, burst * 0.8f);
            starFlare(g, c, 0f, h * 0.5f, 0f, 6f * s, LAVA_HOT, burst, 10, 0.42f);
            starFlare(g, c, 0f, h * 0.5f, 0f, 3.2f * s, LAVA_CORE, burst, 6, 0.25f);
            drawGlow(g);
        }
    }

    /** Р  РЎСџР  РЎвЂўР  Р’В»Р РЋРЎвЂњР  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р’В»Р  Р’В°Р  Р вЂ Р  РЎвЂўР  Р вЂ Р  Р’В°Р РЋР РЏ Р  РЎвЂќР  РЎвЂўР  Р’В¶Р  Р’В° Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° Р  РўвЂР  РЎвЂў Р РЋРЎвЂњР РЋР вЂљР  РЎвЂўР  Р вЂ Р  Р вЂ¦Р РЋР РЏ level + Р РЋР РЏР РЋР вЂљР  РЎвЂќР  РЎвЂР  Р’Вµ Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР РЋРЎвЂњР РЋРІР‚В°Р  РЎвЂР  Р’Вµ Р  Р вЂ Р  Р вЂ¦Р  РЎвЂР  Р’В· Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В¶Р  РЎвЂР  Р’В»Р  РЎвЂќР  РЎвЂ. */
    private void lavaSkin(Ctx c, float level, float bodyR, float h, float skinA, float veinA) {
        if (level <= 0.01f || (skinA <= 0.002f && veinA <= 0.002f)) return;
        final int rows = 12;
        final int seg = 20;
        float t = c.t;

        for (int pass = 0; pass < 2; pass++) {
            float passA = pass == 0 ? skinA : veinA;
            if (passA <= 0.002f) continue;
            if (pass == 0) translucent(); else additive();
            BufferBuilder b = beginColor();
            Matrix4f m = c.mat();
            for (int j = 0; j < rows; j++) {
                float y0 = level * j / rows;
                float y1 = level * (j + 1) / rows;
                for (int i = 0; i < seg; i++) {
                    float a0 = TAU * i / seg;
                    float a1 = TAU * (i + 1) / seg;
                    float r00 = skinR(a0, y0, h, bodyR, t);
                    float r10 = skinR(a1, y0, h, bodyR, t);
                    float r01 = skinR(a0, y1, h, bodyR, t);
                    float r11 = skinR(a1, y1, h, bodyR, t);
                    int c00 = skinColor(pass, lavaNoise(a0, y0, t), passA);
                    int c10 = skinColor(pass, lavaNoise(a1, y0, t), passA);
                    int c01 = skinColor(pass, lavaNoise(a0, y1, t), passA);
                    int c11 = skinColor(pass, lavaNoise(a1, y1, t), passA);
                    quad(b, m,
                            cos(a0) * r00, y0, sin(a0) * r00, c00,
                            cos(a1) * r10, y0, sin(a1) * r10, c10,
                            cos(a1) * r11, y1, sin(a1) * r11, c11,
                            cos(a0) * r01, y1, sin(a0) * r01, c01);
                }
            }
            drawColor(b);
        }
    }

    private static float skinR(float a, float y, float h, float bodyR, float t) {
        return bodyR * prof(y / h) * 1.1f + 0.035f
                + 0.03f * sin(a * 5f + y * 9f - t * 3f) * sin(a * 3f - t * 1.7f);
    }

    /** Р  Р в‚¬Р  Р’В·Р  РЎвЂўР РЋР вЂљ Р РЋРІР‚С™Р  Р’ВµР РЋРІР‚РЋР РЋРІР‚ВР РЋРІР‚С™ Р  Р вЂ Р  Р вЂ¦Р  РЎвЂР  Р’В· Р РЋР С“Р  РЎвЂў Р  Р вЂ Р РЋР вЂљР  Р’ВµР  РЎВР  Р’ВµР  Р вЂ¦Р  Р’ВµР  РЎВ. */
    private static float lavaNoise(float a, float y, float t) {
        return 0.5f + 0.5f * sin(a * 3f + y * 6f + t * 3.5f) * cos(a * 2f - y * 4f + t * 2.2f + sin(y * 3f + a));
    }

    private static int skinColor(int pass, float n, float alpha) {
        if (pass == 0) {
            return withAlpha(heatColor(0.42f + 0.45f * n), alpha * (0.6f + 0.4f * n));
        }
        float vein = clamp((n - 0.55f) * 2.4f);
        return withAlpha(mixColor(LAVA, LAVA_HOT, vein), alpha * vein);
    }

    /** x, y, z, scale, heat, fallTimeSec */
    private float[] chunkState(ActiveEffect fx, Chunk ch, float p) {
        float rel = ch.y() / fx.height;
        float fallAt = MAGMA_CRUMBLE + (1f - rel) * 0.03f + ch.delay() * 0.05f;
        float ft = Math.max(0f, p - fallAt) * fx.duration / 1000f;

        float spread = 1f + ft * ch.drift() * 0.9f;
        float x = ch.x() * spread;
        float z = ch.z() * spread;
        float y = ch.y() + ch.size() * 0.5f + ch.drift() * 0.8f * ft - 5f * ft * ft;

        float melt = clamp((ch.size() * 0.2f - y) / (ch.size() * 2.4f));
        float scale = 1f - melt;
        float heat = Math.min(1f, 0.28f + ft * 1.5f + melt);

        return new float[]{x, y, z, scale, heat, ft};
    }

    private static int heatColor(float h) {
        h = clamp(h);
        if (h < 0.28f) return mixColor(ROCK, LAVA_DARK, h / 0.28f);
        if (h < 0.58f) return mixColor(LAVA_DARK, LAVA, (h - 0.28f) / 0.3f);
        if (h < 0.85f) return mixColor(LAVA, LAVA_HOT, (h - 0.58f) / 0.27f);
        return mixColor(LAVA_HOT, LAVA_CORE, (h - 0.85f) / 0.15f);
    }

    // =====================================================================
    //  3. Р  РЎв„ўР   Р  Р’ВР  Р Р‹Р  РЎС›Р  РЎвЂ™Р  РІР‚С”Р  РІР‚С”: Р  РЎВР  РЎвЂўР  Р’В± Р  РЎВР  РЎвЂ“Р  Р вЂ¦Р  РЎвЂўР  Р вЂ Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂў Р  Р’В·Р  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР  Р’В·Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  Р вЂ Р  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋР вЂљР  РЎвЂ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  РЎвЂў Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р  Р’В°
    //     (Р  Р вЂ Р  РЎвЂР  РўвЂР  Р вЂ¦Р  РЎвЂў Р РЋР С“Р  Р’В°Р  РЎВР  РЎвЂў Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р  Р вЂ Р  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋР вЂљР  РЎвЂ), Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р  Р вЂ Р РЋРІР‚в„–Р РЋР вЂљР  Р’В°Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р РЋРІР‚в„–, Р  РЎвЂ”Р  РЎвЂў Р  Р’В»Р РЋР Р‰Р  РўвЂР РЋРЎвЂњ
    //     Р  Р’В±Р  Р’ВµР  РЎвЂ“Р РЋРЎвЂњР РЋРІР‚С™ Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚В°Р  РЎвЂР  Р вЂ¦Р РЋРІР‚в„–, Р  Р вЂ Р РЋР С“Р РЋРІР‚В Р  РўвЂР РЋР вЂљР  РЎвЂўР  Р’В¶Р  РЎвЂР РЋРІР‚С™ Р  РЎвЂ Р  Р вЂ Р  Р’В·Р РЋР вЂљР РЋРІР‚в„–Р  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  Р’В° Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂўР  Р’В»Р  РЎвЂќР  РЎвЂ Р  Р вЂ Р  РЎВР  Р’ВµР РЋР С“Р РЋРІР‚С™Р  Р’Вµ Р РЋР С“ Р  РЎВР  РЎвЂўР  Р’В±Р  РЎвЂўР  РЎВ.
    // =====================================================================

    private static final float CRYSTAL_SHATTER = 0.64f;

    private void renderCrystal(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p;
        float t = c.t;
        float s = fx.scale;
        float h = fx.height;

        float freeze = easeOutCubic(clamp(p / 0.17f));
        float crackP = clamp((p - 0.48f) / 0.16f);

        // РїР°Р»РёС‚СЂР° Р»СЊРґР°: С‚С‘РјРЅРѕРµ СЃС‚РµРєР»Рѕ -> СЃСЂРµРґРЅРёР№ Р»С‘Рґ -> Р±РµР»С‹Р№ РўРћР›Р¬РљРћ РІ СЏРґСЂР°С… Рё Р±Р»РёРєР°С…
        int iceDeep = mixColor(fx.color, 0x16327E, 0.78f);
        int ice = mixColor(fx.color, 0x58B8FF, 0.55f);
        int iceLight = mixColor(ice, 0xEAF8FF, 0.7f);

        float bigR = fx.width * 0.62f + 0.2f * s;
        float bigH = h + 0.4f * s;
        float shake = crackP * crackP * 0.045f * s;
        float jx = sin(t * 83f) * shake;
        float jz = cos(t * 71f) * shake;

        if (p < CRYSTAL_SHATTER) {
            // --- Р  Р’В·Р  Р’В°Р  РЎВР  РЎвЂўР РЋР вЂљР  РЎвЂўР  Р’В¶Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў ---
            float flash = 1f - smoothstep(p / 0.09f);
            float inner = crackP * (0.5f + 0.5f * sin(t * 30f));
            c.m.push();
            c.m.translate(jx, 0f, jz);
            // С‚РµР»Рѕ: С…РѕР»РѕРґРЅС‹Р№ СЃРёРЅРёР№ СЃРёР»СѓСЌС‚ Р‘Р•Р— РїРµСЂРµСЃРІРµС‚Р° вЂ” СЃРІРµС‚ РЅРµСЃС‘С‚ СЃС‚РµРєР»Рѕ Рё С‚СЂРµС‰РёРЅС‹
            drawBody(c,
                    lerp(1f, 0.55f, freeze) + flash * 0.35f + inner * 0.25f,
                    lerp(1f, 0.80f, freeze) + flash * 0.35f + inner * 0.3f,
                    lerp(1f, 1.18f, freeze) + flash * 0.4f + inner * 0.45f);
            // С‚РѕРЅРєРѕРµ Р»РµРґСЏРЅРѕРµ СЃРІРµС‡РµРЅРёРµ РёР·РЅСѓС‚СЂРё
            drawBodyGlow(c, 0.45f, 0.8f, 1.35f,
                    0.14f + 0.28f * freeze + 0.22f * inner);
            c.m.pop();

            // --- Р РЋР С“Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР  Р’В»Р РЋР РЏР  Р вЂ¦Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  РЎвЂќР  РЎвЂўР  РЎвЂќР  РЎвЂўР  Р вЂ¦ Р  РЎвЂ Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р РЋРІР‚в„– Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ ---
            float coH = bigH * freeze + 0.12f;
            translucent();
            BufferBuilder b = beginColor();
            prism(b, c, jx, -0.12f, jz, null, bigR, coH, 8, 0.3f, iceDeep, ice, 0.36f, 1f, true);

            int ci = 0;
            for (Crystal cr : fx.crystals) {
                float g = easeOutCubic(clamp((p - cr.delay()) / 0.14f));
                ci++;
                if (g <= 0f) continue;
                Quaternionf q = new Quaternionf().rotationY(-cr.angle()).rotateZ(-cr.tilt());
                // Р  Р’В»Р РЋРІР‚ВР  РЎвЂ“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В·Р  РЎВР  Р’В°Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚РЋР  Р’ВµР РЋР С“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РЎвЂўР РЋРІР‚С™Р  Р’В±Р  Р’В»Р  Р’ВµР РЋР С“Р  РЎвЂќ: Р  РЎвЂќР  Р’В°Р  Р’В¶Р  РўвЂР РЋРІР‚в„–Р  РІвЂћвЂ“ Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В» Р РЋРІР‚РЋР РЋРЎвЂњР РЋРІР‚С™Р РЋР Р‰ Р РЋР С“Р  Р вЂ Р  РЎвЂўР  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  РЎвЂўР РЋРІР‚С™Р РЋРІР‚С™Р  Р’ВµР  Р вЂ¦Р  РЎвЂќР  Р’В°
                int prismTint = mixColor(iceLight, mixColor(0xB08CFF, 0x8CFFE0, hash(ci * 3.7f)), 0.12f);
                int col = mixColor(mixColor(iceDeep, ice, 0.25f + hash(cr.twist()) * 0.55f), prismTint, 0.15f);
                int sides = 5 + (ci % 3);
                prism(b, c, cr.x() + jx, -0.1f, cr.z() + jz, q, cr.radius() * (0.4f + 0.6f * g),
                        cr.height() * g, sides, cr.twist(), col, ice, 0.5f, 1f, true);
                if (cr.height() > 0.8f * fx.scale) {
                    // РІРЅСѓС‚СЂРµРЅРЅРµРµ СЏРґСЂРѕ: СЃРІРµС‚, Р·Р°РїРµСЂС‚С‹Р№ РІ СЃС‚РµРєР»Рµ
                    prism(b, c, cr.x() + jx, -0.1f, cr.z() + jz, q,
                            cr.radius() * 0.55f * (0.4f + 0.6f * g), cr.height() * 0.62f * g,
                            sides, cr.twist(), mixColor(ice, iceLight, 0.5f), iceLight, 0.4f, 0.6f, false);
                }
            }

            flatDisc(b, c.mat(), (bigR + 1.8f * s) * freeze, 0.018f,
                    iceDeep, 0.34f, ice, 0.08f, 48);
            drawColor(b);

            // --- Р РЋР вЂљР РЋРІР‚ВР  Р’В±Р РЋР вЂљР  Р’В°, Р РЋРІР‚С›Р РЋР вЂљР  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™ Р  Р’В·Р  Р’В°Р  РЎВР  РЎвЂўР РЋР вЂљР  РЎвЂўР  Р’В·Р  РЎвЂќР  РЎвЂ, Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂўР  Р вЂ Р  Р’В°Р РЋР РЏ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р  Р’В°, Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚В°Р  РЎвЂР  Р вЂ¦Р РЋРІР‚в„– ---
            additive();
            b = beginColor();
            float tipB = Math.min(bigR * 0.8f, coH * 0.25f);
            float tipT = Math.min(bigR * 1.6f, coH * 0.3f);
            float yLo = -0.12f + tipB;
            float yHi = -0.12f + coH - tipT;
            float yTop = -0.12f + coH;
            for (int i = 0; i < 8; i++) {
                float a = 0.3f + TAU * i / 8f;
                float ex = cos(a) * bigR + jx;
                float ez = sin(a) * bigR + jz;
                float ux = cos(a) * bigR * 0.9f + jx;
                float uz = sin(a) * bigR * 0.9f + jz;
                float shine = 0.55f + 0.45f * sin(t * 4f + i * 1.3f);
                beam(b, c, ex, yLo, ez, ux, yHi, uz, 0.028f * s, ice, shine * 0.35f);
                beam(b, c, ux, yHi, uz, jx, yTop, jz, 0.022f * s, ice, shine * 0.3f);
            }

            // СЃРІРµС‚Р»С‹Рµ СЂС‘Р±СЂР° Р±РѕР»СЊС€РёС… С€РёРїРѕРІ вЂ” РёРіСЂР° РіСЂР°РЅРµР№
            int ei = 0;
            for (Crystal cr : fx.crystals) {
                ei++;
                if (cr.height() <= 0.8f * fx.scale) continue;
                float g = easeOutCubic(clamp((p - cr.delay()) / 0.14f));
                if (g <= 0.25f) continue;
                Quaternionf q = new Quaternionf().rotationY(-cr.angle()).rotateZ(-cr.tilt());
                for (int e = 0; e < 2; e++) {
                    float a0 = cr.twist() + e * (float) Math.PI;
                    Vector3f base = pt(q, cos(a0) * cr.radius(), 0f, sin(a0) * cr.radius(),
                            cr.x() + jx, -0.1f, cr.z() + jz);
                    Vector3f mid = pt(q, cos(a0) * cr.radius() * 0.8f, cr.height() * g * 0.45f,
                            sin(a0) * cr.radius() * 0.8f, cr.x() + jx, -0.1f, cr.z() + jz);
                    Vector3f tip = pt(q, 0f, cr.height() * g, 0f, cr.x() + jx, -0.1f, cr.z() + jz);
                    beam(b, c, base.x, base.y, base.z, mid.x, mid.y, mid.z, 0.008f * s, iceLight, 0.38f * g);
                    beam(b, c, mid.x, mid.y, mid.z, tip.x, tip.y, tip.z, 0.006f * s, iceLight, 0.28f * g);
                }
            }

            float front = 1f - smoothstep((p - 0.13f) / 0.06f);
            ring(b, c.mat(), bigR * 1.04f, 0.05f * s, yTop - 0.05f, 0f, TAU, ice, front * 0.7f, 64);
            ring(b, c.mat(), bigR * 1.15f, 0.12f * s, yTop - 0.05f, 0f, TAU, iceDeep, front * 0.4f, 64);

            // РјРѕСЂРѕР·РЅС‹Рµ С‚СЂРµС‰РёРЅС‹ РЅР° Р·РµРјР»Рµ РѕС‚ Р·РѕРЅС‹
            if (freeze > 0.05f) {
                for (int i = 0; i < 10; i++) {
                    float ang = hash(i * 3.7f + fx.seedF) * TAU;
                    float r0 = bigR + 0.1f;
                    float r1 = r0 + (0.5f + hash(i * 5.9f) * 0.7f) * s * freeze;
                    float fa = 0.35f * freeze;
                    flatLine(b, c.mat(), cos(ang) * r0, sin(ang) * r0, cos(ang) * r1, sin(ang) * r1,
                            0.02f, 0.012f * s, ice, fa);
                    flatLine(b, c.mat(), cos(ang) * r0, sin(ang) * r0, cos(ang) * r1, sin(ang) * r1,
                            0.022f, 0.005f * s, iceLight, fa * 0.8f);
                }
            }

            float sw = frac(t * 0.75f);
            ring(b, c.mat(), bigR * 0.96f, 0.06f * s, -0.1f + sw * coH, 0f, TAU, iceLight,
                    sin(sw * PI) * 0.45f * freeze, 64);

            // паутина трещин на стекле: появляется рано и растёт до разлома
            float webP = clamp((p - 0.22f) / 0.35f);
            if (webP > 0f) {
                for (float[][] line : fx.webCracks) {
                    float drawn = webP * (line.length - 1);
                    for (int w2 = 0; w2 < line.length - 1 && w2 < drawn; w2++) {
                        float part = clamp(drawn - w2);
                        float a0 = line[w2][0], y0 = line[w2][1];
                        float a1 = line[w2 + 1][0], y1 = line[w2 + 1][1];
                        float x0 = cos(a0) * bigR * 1.01f + jx;
                        float z0 = sin(a0) * bigR * 1.01f + jz;
                        float x1 = x0 + (cos(a1) * bigR * 1.01f - x0) * part;
                        float z1 = z0 + (sin(a1) * bigR * 1.01f - z0) * part;
                        float yy0 = -0.12f + y0 * coH;
                        float yy1 = yy0 + (y1 - y0) * coH * part;
                        beam(b, c, x0, yy0, z0, x1, yy1, z1, 0.014f * s, ice, 0.55f * webP);
                        beam(b, c, x0, yy0, z0, x1, yy1, z1, 0.005f * s, 0xFFFFFF, 0.85f * webP);
                    }
                }
            }

            ring(b, c.mat(), (bigR + 0.9f * s) * freeze, 0.03f * s, 0.03f,
                    t * 0.3f, TAU, ice, 0.3f, 96);
            ring(b, c.mat(), (bigR + 0.6f * s) * freeze, 0.015f * s, 0.032f,
                    -t * 0.5f, TAU * 0.8f, iceLight, 0.28f, 80);
            drawColor(b);

            // --- Р  Р’В±Р  Р’В»Р  РЎвЂР  РЎвЂќР  РЎвЂ-Р  Р’В·Р  Р вЂ Р РЋРІР‚ВР  Р’В·Р  РўвЂР  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  РЎвЂ Р  РЎвЂ Р  Р вЂ¦Р  Р’В°Р РЋР вЂљР  Р’В°Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  РІвЂћвЂ“ Р  Р вЂ Р  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂР  РІвЂћвЂ“ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™ ---
            BufferBuilder g = beginGlow();
            glow(g, c, jx, h * 0.55f, jz, 1.5f * s, 1.9f * s, mixColor(iceDeep, ice, 0.45f),
                    (0.22f + crackP * 0.45f) * freeze);
            for (int i = 0; i < 26; i++) {
                float ph = frac(t * 1.6f + hash(i * 2.9f + fx.seedF));
                float spark = (float) Math.pow(sin(ph * PI), 8.0) * freeze;
                float ang = hash(i * 5.3f) * TAU;
                float y = hash(i * 8.1f) * coH * 0.9f;
                float x = cos(ang) * bigR + jx;
                float z = sin(ang) * bigR + jz;
                int tint = (i % 3 == 0) ? mixColor(iceLight, 0xB08CFF, 0.4f)
                        : (i % 3 == 1) ? mixColor(iceLight, 0x8CFFE0, 0.4f) : 0xFFFFFF;
                glow(g, c, x, y, z, 0.55f * s, 0.055f * s, tint, spark);
                glow(g, c, x, y, z, 0.055f * s, 0.55f * s, tint, spark);
                glow(g, c, x, y, z, 0.27f * s, 0.27f * s, ice, spark * 0.6f);
            }
            float pre = 1f - smoothstep(Math.abs(p - CRYSTAL_SHATTER) / 0.05f);
            glow(g, c, 0f, h * 0.5f, 0f, 2.6f * s, 2.6f * s, iceLight, pre * 0.85f);
            drawGlow(g);
        }

        // --- Р  Р вЂ Р  Р’В·Р РЋР вЂљР РЋРІР‚в„–Р  Р вЂ  Р  Р вЂ¦Р  Р’В° Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂўР  Р’В»Р  РЎвЂќР  РЎвЂ: Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° + Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р  РЎвЂќР  РЎвЂўР  РЎвЂќР  РЎвЂўР  Р вЂ¦Р  Р’В° ---
        if (p >= CRYSTAL_SHATTER) {
            float sec = (p - CRYSTAL_SHATTER) * fx.duration / 1000f;
            float sp = clamp((p - CRYSTAL_SHATTER) / (1f - CRYSTAL_SHATTER));
            float fade = 1f - smoothstep((sp - 0.55f) / 0.45f);
            float gravity = 7.5f;

            translucent();
            BufferBuilder b = beginColor();
            float[][] pos = new float[fx.shards.length][];
            for (int i = 0; i < fx.shards.length; i++) {
                Shard sh = fx.shards[i];
                float st = Math.max(0f, sec - hash(i * 1.37f + fx.seedF) * 0.05f);
                float vx = sh.dx() * sh.speed();
                float vy = sh.dy() * sh.speed();
                float vz = sh.dz() * sh.speed();
                float ground = sh.size() * 0.35f;
                float y0 = Math.max(ground, sh.y());
                float tl = (vy + (float) Math.sqrt(vy * vy + 2f * gravity * (y0 - ground))) / gravity;

                float x, y, z, spinT;
                if (st < tl) {
                    x = sh.x() + vx * st;
                    y = y0 + vy * st - 0.5f * gravity * st * st;
                    z = sh.z() + vz * st;
                    spinT = st;
                } else {
                    float after = st - tl;
                    float slide = (1f - (float) Math.exp(-after * 5f)) / 5f;
                    float impact = gravity * tl - vy;
                    x = sh.x() + vx * tl + vx * 0.4f * slide;
                    z = sh.z() + vz * tl + vz * 0.4f * slide;
                    y = ground + Math.abs(sin(after * 11f)) * impact * 0.045f * (float) Math.exp(-after * 5f);
                    spinT = tl + slide * 0.6f;
                }
                pos[i] = new float[]{x, y, z};

                Quaternionf q = new Quaternionf().rotationAxis(sh.spin() + spinT * (4f + sh.spin()),
                        cos(sh.axis()) * 0.8f, 0.6f, sin(sh.axis()) * 0.8f);
                float sz = sh.size() * (1f - smoothstep((sp - 0.55f) / 0.45f) * 0.75f);
                boolean bodyPiece = i < fx.bodyShards;
                int base = bodyPiece
                        ? mixColor(ice, 0xDFF4FF, 0.2f + sh.tint() * 0.3f)
                        : mixColor(iceDeep, ice, 0.25f + sh.tint() * 0.6f);
                gem(b, c, x, y, z, q, sz * 0.55f, sz * 1.6f, 5, i * 3.7f + fx.seedF,
                        base, 0xFFFFFF, (bodyPiece ? 0.95f : 0.6f) * fade, 1f, true);
            }
            drawColor(b);

            additive();
            BufferBuilder g = beginGlow();
            for (int i = 0; i < fx.shards.length; i++) {
                Shard sh = fx.shards[i];
                float stL = Math.max(0f, sec - hash(i * 1.37f + fx.seedF) * 0.05f);
                float vy0 = sh.dy() * sh.speed();
                float y0l = Math.max(sh.size() * 0.35f, sh.y());
                float tlL = (vy0 + (float) Math.sqrt(vy0 * vy0 + 2f * 7.5f * (y0l - sh.size() * 0.35f))) / 7.5f;
                float tw = (float) Math.pow(0.5f + 0.5f * sin(t * 14f + i * 2.1f), 6.0);
                // осколок рассыпается в пыль после приземления
                if (stL > tlL) {
                    float la = clamp((stL - tlL) / 0.7f);
                    glow(g, c, pos[i][0], Math.max(0.05f, pos[i][1] * 0.4f), pos[i][2],
                            sh.size() * 4.5f, sh.size() * 1.8f, iceLight, (1f - la) * 0.28f * fade);
                }
                glow(g, c, pos[i][0], pos[i][1], pos[i][2],
                        sh.size() * 2.4f, sh.size() * 2.4f, ice, fade * (0.15f + 0.3f * tw));
                glow(g, c, pos[i][0], pos[i][1], pos[i][2],
                        fx.shards[i].size() * 2.5f, fx.shards[i].size() * 0.25f, 0xFFFFFF, fade * tw * 0.8f);
            }
            for (Mote d : fx.dust) {
                float age = clamp((sp - d.delay() * 0.1f) / 0.9f);
                if (age <= 0f) continue;
                float go = easeOutCubic(age) * d.speed();
                float x = d.x() + d.dx() * go;
                float y = d.y() + d.dy() * go + age * 0.5f;
                float z = d.z() + d.dz() * go;
                float tw = (float) Math.pow(0.5f + 0.5f * sin(t * 18f + d.phase() * 40f), 3.0);
                float a = (1f - smoothstep(age)) * (0.35f + 0.65f * tw);
                glow(g, c, x, y, z, d.size(), d.size(), mixColor(iceLight, 0xFFFFFF, d.tint()), a);
            }
            float burst = 1f - smoothstep(sp / 0.2f);
            glow(g, c, 0f, h * 0.5f, 0f, 1.5f * s, 1.5f * s, ice, burst * 0.28f);
            drawGlow(g);

            b = beginColor();
            float e = easeOutCubic(sp / 0.6f);
            ring(b, c.mat(), (0.4f + e * 2.0f) * s, 0.035f * s, 0.03f,
                    0f, TAU, ice, (1f - smoothstep(sp / 0.6f)) * 0.35f, 80);
            drawColor(b);
        }
    }

    // =====================================================================
    //  4. Р  Р Р‹Р  Р’ВР  Р вЂЎР  РЎСљР  Р’ВР  РІР‚Сћ (Р  Р вЂ Р  РЎВР  Р’ВµР РЋР С“Р РЋРІР‚С™Р  РЎвЂў Р  РЎВР  РЎвЂўР  Р’В»Р  Р вЂ¦Р  РЎвЂР  РІвЂћвЂ“): Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В° Р РЋР вЂљР  Р’В°Р  Р’В·Р  Р вЂ Р  РЎвЂўР РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  РЎвЂР  Р вЂ Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р РЋР С“Р  Р’ВµР  Р вЂ Р  Р’ВµР РЋР вЂљР  Р вЂ¦Р  РЎвЂўР  Р’Вµ
    //     Р РЋР С“Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР  Р’Вµ, Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў Р  Р вЂ¦Р  Р’В°Р РЋРІР‚РЋР  РЎвЂР  Р вЂ¦Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂР РЋРІР‚С™Р РЋР Р‰Р РЋР С“Р РЋР РЏ Р  РЎвЂ Р  РЎВР  Р’ВµР  РўвЂР  Р’В»Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  РЎвЂў Р  РЎвЂ”Р  РЎвЂўР  РЎвЂ“Р РЋР вЂљР РЋРЎвЂњР  Р’В¶Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ
    //     Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  РЎвЂўР  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“ Р  РЎвЂ”Р  РЎвЂўР РЋР вЂљР РЋРІР‚С™Р  Р’В°Р  Р’В», Р РЋР вЂљР  Р’В°Р РЋР С“Р РЋР С“Р РЋРІР‚в„–Р  РЎвЂ”Р  Р’В°Р РЋР РЏР РЋР С“Р РЋР Р‰ Р  Р вЂ¦Р  Р’В° Р РЋР С“Р  РЎвЂўР РЋРІР‚С™Р  Р вЂ¦Р  РЎвЂ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦Р РЋР С“Р РЋР РЏ Р  Р’В±Р  Р’В°Р  Р’В±Р  РЎвЂўР РЋРІР‚РЋР  Р’ВµР  РЎвЂќ,
    //     Р  РЎвЂќР  РЎвЂўР РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂ”Р  РЎвЂў Р РЋР С“Р  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  Р’В»Р  РЎвЂ Р РЋРЎвЂњР  Р’В»Р  Р’ВµР РЋРІР‚С™Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™ Р  Р вЂ  Р  Р вЂ¦Р  Р’ВµР  Р’В±Р  РЎвЂў.
    // =====================================================================

    private void renderAurora(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p;
        float t = c.t;
        float s = fx.scale;
        float h = fx.height;
        float bodyR = fx.width * 0.5f;

        int teal = mixColor(fx.color, 0x5CFFC8, 0.5f);
        int violet = mixColor(fx.color, 0xB06CFF, 0.5f);
        int pink = mixColor(fx.color, 0xFF8AD8, 0.45f);
        int pale = mixColor(teal, 0xFFFFFF, 0.6f);

        float appear = easeOutCubic(p / 0.16f);
        float fadeAll = 1f - smoothstep((p - 0.8f) / 0.2f);
        float sinkK = clamp((p - 0.24f) / 0.46f);
        float sink = sinkK * (h + 0.06f);
        float glowK = smoothstep(p / 0.2f);
        float portal = smoothstep((p - 0.16f) / 0.1f) * (1f - smoothstep((p - 0.72f) / 0.1f));

        // --- Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  Р’ВµР  Р’ВµР РЋР С“Р РЋР РЏ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў, Р  РЎвЂ”Р  РЎвЂўР  РЎвЂ“Р РЋР вЂљР РЋРЎвЂњР  Р’В¶Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  Р’ВµР  Р’ВµР РЋР С“Р РЋР РЏ Р  Р вЂ  Р  РЎвЂ”Р  РЎвЂўР РЋР вЂљР РЋРІР‚С™Р  Р’В°Р  Р’В» ---
        if (sinkK < 1f) {
            float gain = 1f + 0.85f * glowK + 0.18f * sin(t * 6f) * glowK;
            float tr = lerp(1f, ((pale >> 16) & 255) / 255f, glowK) * gain;
            float tg = lerp(1f, ((pale >> 8) & 255) / 255f, glowK) * gain;
            float tb = lerp(1f, (pale & 255) / 255f, glowK) * gain;
            c.m.push();
            c.m.translate(0f, -sink, 0f);
            drawBody(c, tr, tg, tb);
            c.m.pop();
        }

        // --- Р РЋР С“Р  Р’ВµР  Р вЂ Р  Р’ВµР РЋР вЂљР  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р РЋР С“Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР  Р’Вµ ---
        additive();
        BufferBuilder b = beginColor();
        float ca = appear * fadeAll;
        auroraCurtain(b, c, 1.3f * s, 2.8f * s * appear, 0.0f, 0.35f, 0f, teal, violet, 0.55f * ca);
        auroraCurtain(b, c, 1.85f * s, 3.5f * s * appear, 0.1f, -0.22f, 2f, violet, pink, 0.42f * ca);
        auroraCurtain(b, c, 2.45f * s, 2.3f * s * appear, 0.05f, 0.15f, 4f, teal, pale, 0.3f * ca);

        // --- Р  РЎвЂ”Р  РЎвЂўР РЋР вЂљР РЋРІР‚С™Р  Р’В°Р  Р’В» Р  Р вЂ¦Р  Р’В° Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  Р’Вµ ---
        Matrix4f m = c.mat();
        float pr = bodyR * 1.35f + 0.12f * s;
        flatDisc(b, m, pr * 1.3f, 0.025f, pale, 0.6f * portal, teal, 0f, 48);
        ring(b, m, pr, 0.05f * s, 0.03f, 0f, TAU, pale, 0.95f * portal, 72);
        ring(b, m, pr * 1.2f, 0.1f * s, 0.028f, 0f, TAU, teal, 0.4f * portal, 72);
        for (int k = 0; k < 4; k++) {
            ring(b, m, pr + 0.55f * s, 0.018f * s, 0.032f, t * 0.9f + k * HALF_PI, TAU * 0.16f,
                    k % 2 == 0 ? violet : teal, 0.8f * portal, 20);
            ring(b, m, pr + 0.95f * s, 0.012f * s, 0.032f, -t * 0.6f + k * HALF_PI + 0.4f, TAU * 0.1f,
                    pink, 0.6f * portal, 14);
        }
        for (int k = 0; k < 3; k++) {
            float f = frac(t * 0.7f + k / 3f);
            ring(b, m, pr * (1f + f * 2.2f), 0.02f * s, 0.03f, 0f, TAU, pale,
                    sin(f * PI) * 0.5f * portal, 64);
        }
        // Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  РЎвЂ Р  РЎвЂР  Р’В· Р  РЎвЂ”Р  РЎвЂўР РЋР вЂљР РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В°
        beam(b, c, 0f, 0f, 0f, 0f, 4.5f * s, 0f, pr * 2.2f, teal, 0.12f * portal);
        beam(b, c, 0f, 0f, 0f, 0f, 3.2f * s, 0f, pr * 0.8f, pale, 0.25f * portal);
        drawColor(b);

        BufferBuilder g = beginGlow();

        // --- Р  Р’В±Р  Р’В°Р  Р’В±Р  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  РЎвЂ ---
        for (Fly f : fx.flies) {
            float ts = 0.24f + 0.46f * f.y0();
            float age = (p - ts) * fx.duration / 1000f;
            if (age <= 0f) continue;
            float life = 2.1f + f.phase() * 0.9f;
            float a = smoothstep(age / 0.12f) * (1f - smoothstep((age - life * 0.55f) / (life * 0.45f))) * fadeAll;
            if (a <= 0.002f) continue;

            float[] now = flyPos(f, age, s);
            float[] prev = flyPos(f, age - 0.05f, s);
            float dx = now[0] - prev[0];
            float dy = now[1] - prev[1];
            float dz = now[2] - prev[2];
            float yaw = (float) Math.atan2(dx, dz);
            float pitch = -(float) Math.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz) + 1e-4f) * 0.6f;
            float flap = 0.2f + 1.1f * (0.5f + 0.5f * sin(age * (19f + f.phase() * 9f) + f.phase() * 20f));
            int col = f.tint() < 0.4f ? teal : f.tint() < 0.75f ? violet : pink;

            for (int j = 1; j <= 4; j++) {
                float[] tr = flyPos(f, age - j * 0.07f, s);
                glow(g, c, tr[0], tr[1], tr[2], f.size() * 0.5f, f.size() * 0.5f, col, a * 0.35f / j);
            }
            butterfly(g, c, now[0], now[1], now[2], yaw, pitch, flap, f.size(), col, a);
        }

        // --- Р  РЎвЂР РЋР С“Р  РЎвЂќР РЋР вЂљР РЋРІР‚в„–, Р  РЎвЂ”Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  РЎвЂР  РЎВР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  Р’ВµР РЋР С“Р РЋР РЏ Р  РЎвЂР  Р’В· Р  РЎвЂ”Р  РЎвЂўР РЋР вЂљР РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В° ---
        for (Mote d : fx.dust) {
            float ph = frac(t * d.speed() + d.phase());
            float ang = d.dy() + ph * 3f;
            float r = pr * (0.3f + d.x() * 0.9f);
            float y = ph * (2.5f + d.y() * 2f) * s;
            float tw = 0.5f + 0.5f * sin(t * 12f + d.phase() * 30f);
            glow(g, c, cos(ang) * r, y, sin(ang) * r, d.size(), d.size() * 2f,
                    mixColor(pale, pink, d.tint()), sin(ph * PI) * portal * tw);
        }

        glow(g, c, 0f, 0.08f, 0f, (pr * 3f + 0.8f * s), 0.4f * s, pale, portal * 0.8f);
        glow(g, c, 0f, h * 0.5f - sink, 0f, (fx.width + 1f) * s * 1.2f, h * 1.3f, teal,
                0.3f * glowK * (1f - sinkK));
        glow(g, c, 0f, h + 1.6f * s, 0f, 5.5f * s, 3f * s, violet, 0.14f * ca);

        float fin = 1f - smoothstep(Math.abs(p - 0.71f) / 0.05f);
        starFlare(g, c, 0f, 0.3f, 0f, 7f * s, pale, fin * 0.9f, 6, t * 0.2f);
        glow(g, c, 0f, 0.4f, 0f, 3.2f * s, 3.2f * s, teal, fin * 0.6f);
        float start = 1f - smoothstep(p / 0.12f);
        glow(g, c, 0f, h * 0.5f, 0f, 2.6f * s, 3.4f * s, pale, start * 0.6f);
        drawGlow(g);
    }

    private static float[] flyPos(Fly f, float age, float s) {
        age = Math.max(0f, age);
        float r = f.r0() + age * f.orbit() * s;
        float ang = f.angle() + age * f.spin();
        float y = 0.06f + age * f.rise() * s + sin(age * 2.5f + f.phase() * 6f) * 0.12f * s * Math.min(1f, age * 2f);
        return new float[]{cos(ang) * r, y, sin(ang) * r};
    }

    /** Р  РІР‚ВР  Р’В°Р  Р’В±Р  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  Р’В° Р  РЎвЂР  Р’В· Р РЋРІР‚РЋР  Р’ВµР РЋРІР‚С™Р РЋРІР‚в„–Р РЋР вЂљР РЋРІР‚ВР РЋРІР‚В¦ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦Р РЋР С“Р РЋР РЏ Р  РЎвЂќР РЋР вЂљР РЋРІР‚в„–Р  Р’В»Р РЋР Р‰Р  Р’ВµР  Р вЂ , Р  РЎВР  Р’В°Р РЋРІвЂљВ¬Р РЋРЎвЂњР РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦ Р  Р вЂ  3D. */
    private void butterfly(BufferBuilder g, Ctx c, float x, float y, float z,
                           float yaw, float pitch, float flap, float size, int color, float alpha) {
        int inner = mixColor(color, 0xFFFFFF, 0.55f);
        for (int side = -1; side <= 1; side += 2) {
            c.m.push();
            c.m.translate(x, y, z);
            c.m.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
            c.m.multiply(RotationAxis.POSITIVE_X.rotation(pitch));
            c.m.multiply(RotationAxis.POSITIVE_Z.rotation(side * flap));
            Matrix4f m = c.mat();
            wingQuad(g, m, side * size * 0.55f, size * 0.25f, size * 1.1f, size * 1.0f, color, alpha * 0.55f);
            wingQuad(g, m, side * size * 0.42f, -size * 0.32f, size * 0.85f, size * 0.75f, color, alpha * 0.45f);
            wingQuad(g, m, side * size * 0.45f, size * 0.2f, size * 0.5f, size * 0.5f, inner, alpha * 0.8f);
            wingQuad(g, m, side * size * 0.35f, -size * 0.28f, size * 0.38f, size * 0.36f, inner, alpha * 0.6f);
            c.m.pop();
        }
        glow(g, c, x, y, z, size * 0.28f, size * 0.28f, 0xFFFFFF, alpha);
        glow(g, c, x, y, z, size * 2.2f, size * 2.2f, color, alpha * 0.18f);
    }

    private static void wingQuad(BufferBuilder g, Matrix4f m, float cx, float cz,
                                 float w, float l, int color, float alpha) {
        if (alpha <= 0.002f) return;
        int rgba = withAlpha(color, alpha);
        float hw = w * 0.5f;
        float hl = l * 0.5f;
        g.vertex(m, cx - hw, 0f, cz - hl).texture(0f, 1f).color(rgba);
        g.vertex(m, cx - hw, 0f, cz + hl).texture(0f, 0f).color(rgba);
        g.vertex(m, cx + hw, 0f, cz + hl).texture(1f, 0f).color(rgba);
        g.vertex(m, cx + hw, 0f, cz - hl).texture(1f, 1f).color(rgba);
    }

    /** Р  РІР‚в„ўР  РЎвЂўР  Р’В»Р  Р вЂ¦Р  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋР РЏ Р  Р’В»Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р  Р’В° Р РЋР С“Р  Р’ВµР  Р вЂ Р  Р’ВµР РЋР вЂљР  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  РЎвЂў Р РЋР С“Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР РЋР РЏ Р  РЎвЂ”Р  РЎвЂў Р  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“Р РЋРЎвЂњ: Р РЋР РЏР РЋР вЂљР  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  Р вЂ¦Р  РЎвЂР  Р’В·, Р РЋРІР‚С™Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  РІвЂћвЂ“ Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚В¦. */
    private void auroraCurtain(BufferBuilder b, Ctx c, float radius, float height, float y0,
                               float speed, float phase, int low, int high, float alpha) {
        if (alpha <= 0.002f || height <= 0.01f) return;
        Matrix4f m = c.mat();
        float t = c.t;
        final int seg = 72;
        int mid = mixColor(low, high, 0.5f);
        for (int i = 0; i < seg; i++) {
            float a0 = phase + t * speed + TAU * i / seg;
            float a1 = phase + t * speed + TAU * (i + 1) / seg;
            float r0 = radius * (1f + 0.13f * sin(a0 * 3f + t * 2f) + 0.06f * sin(a0 * 7f - t * 3f));
            float r1 = radius * (1f + 0.13f * sin(a1 * 3f + t * 2f) + 0.06f * sin(a1 * 7f - t * 3f));
            float h0 = height * (0.6f + 0.4f * (0.5f + 0.5f * sin(a0 * 4f + t * 1.5f)));
            float h1 = height * (0.6f + 0.4f * (0.5f + 0.5f * sin(a1 * 4f + t * 1.5f)));
            float k0 = 0.5f + 0.5f * sin(a0 * 5f - t * 2.5f);
            float k1 = 0.5f + 0.5f * sin(a1 * 5f - t * 2.5f);
            k0 = 0.2f + 0.8f * k0 * k0;
            k1 = 0.2f + 0.8f * k1 * k1;
            float x0 = cos(a0) * r0, z0 = sin(a0) * r0;
            float x1 = cos(a1) * r1, z1 = sin(a1) * r1;
            float m0 = y0 + h0 * 0.3f, m1 = y0 + h1 * 0.3f;
            int bl0 = withAlpha(low, alpha * k0 * 0.4f);
            int bl1 = withAlpha(low, alpha * k1 * 0.4f);
            int md0 = withAlpha(mid, alpha * k0);
            int md1 = withAlpha(mid, alpha * k1);
            int tp0 = withAlpha(high, 0f);
            int tp1 = withAlpha(high, 0f);
            quad(b, m, x0, y0, z0, bl0, x1, y0, z1, bl1, x1, m1, z1, md1, x0, m0, z0, md0);
            quad(b, m, x0, m0, z0, md0, x1, m1, z1, md1, x1, y0 + h1, z1, tp1, x0, y0 + h0, z0, tp0);
        }
    }

    // =====================================================================
    //  5. Р   Р  Р’ВР  РЎС›Р  Р в‚¬Р  РЎвЂ™Р  РІР‚С” Р  РІР‚СњР  Р в‚¬Р  Р Рѓ: Р  РЎвЂ”Р  РЎвЂўР  РўвЂ Р РЋРІР‚ Р  Р’ВµР  Р’В»Р РЋР Р‰Р РЋР вЂ№ Р РЋР вЂљР  РЎвЂР РЋР С“Р РЋРЎвЂњР  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РЎВР  Р’В°Р  РЎвЂ“Р  РЎвЂР РЋРІР‚РЋР  Р’ВµР РЋР С“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р РЋР С“ Р  РЎвЂ”Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р  Р’В°Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  РЎВР  РЎВР  РЎвЂўР  РІвЂћвЂ“ Р  РЎвЂ
    //     Р РЋР вЂљР РЋРЎвЂњР  Р вЂ¦Р  Р’В°Р  РЎВР  РЎвЂ, Р  РЎвЂР  Р’В· Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° Р  Р вЂ Р РЋРІР‚в„–Р РЋРІР‚В¦Р  РЎвЂўР  РўвЂР  РЎвЂР РЋРІР‚С™ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  РўвЂР РЋРЎвЂњР РЋРІвЂљВ¬Р  Р’В° Р  РЎвЂ Р  РЎвЂ”Р  РЎвЂў Р РЋР С“Р  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  Р’В»Р  РЎвЂ Р  РЎвЂР  Р’В·
    //     Р  Р’В±Р  Р’В»Р РЋРЎвЂњР  Р’В¶Р  РўвЂР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР РЋРІР‚В¦ Р  РЎвЂўР  РЎвЂ“Р  РЎвЂўР  Р вЂ¦Р РЋР Р‰Р  РЎвЂќР  РЎвЂўР  Р вЂ  Р РЋРЎвЂњР  Р’В»Р  Р’ВµР РЋРІР‚С™Р  Р’В°Р  Р’ВµР РЋРІР‚С™ Р  Р вЂ  Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В± Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  Р’В°.
    // =====================================================================

    private static final float[][] SOUL_SHAPE = {
            // x, y, size (Р  РўвЂР  Р’В»Р РЋР РЏ Р РЋР вЂљР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р  Р’В° 1.8)
            {0f, 1.62f, 0.34f}, {0f, 1.42f, 0.2f},
            {0f, 1.25f, 0.42f}, {0f, 1.05f, 0.4f}, {0f, 0.85f, 0.36f},
            {0.32f, 1.28f, 0.2f}, {-0.32f, 1.28f, 0.2f},
            {0.4f, 1.05f, 0.18f}, {-0.4f, 1.05f, 0.18f},
            {0.44f, 0.85f, 0.16f}, {-0.44f, 0.85f, 0.16f},
            {0.12f, 0.6f, 0.22f}, {-0.12f, 0.6f, 0.22f},
            {0.13f, 0.35f, 0.2f}, {-0.13f, 0.35f, 0.2f},
            {0.14f, 0.12f, 0.18f}, {-0.14f, 0.12f, 0.18f}
    };

    private void renderSoul(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p;
        float t = c.t;
        float s = fx.scale;
        float h = fx.height;
        float hs = h / 1.8f;

        float draw = easeOutCubic(clamp(p / 0.22f));
        float circle = 1f - smoothstep((p - 0.78f) / 0.22f);
        float rot = t * 0.35f;
        float rc = 1.6f * s;

        int spirit = mixColor(fx.color, 0x8CFFE0, 0.45f);
        int spiritLight = mixColor(spirit, 0xFFFFFF, 0.6f);

        float rise = smoothstep((p - 0.18f) / 0.62f);
        float soulY = rise * 4.2f * s;
        float soulA = smoothstep((p - 0.1f) / 0.1f) * (1f - smoothstep((p - 0.78f) / 0.1f));
        float pillar = smoothstep((p - 0.15f) / 0.1f) * (1f - smoothstep((p - 0.75f) / 0.2f));

        additive();
        BufferBuilder b = beginColor();
        Matrix4f m = c.mat();
        float a0 = circle * (0.8f + 0.2f * sin(t * 5f));

        ring(b, m, rc, 0.025f * s, 0.02f, rot, TAU * draw, spirit, 0.9f * a0, 96);
        ring(b, m, rc * 0.82f, 0.015f * s, 0.022f, -rot, TAU * draw, spiritLight, 0.8f * a0, 80);
        ring(b, m, rc * 0.36f, 0.015f * s, 0.024f, rot * 2f, TAU * draw, spirit, 0.7f * a0, 48);

        // Р  РЎвЂ”Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р  Р’В°Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  РЎВР  РЎВР  Р’В° Р РЋР вЂљР  РЎвЂР РЋР С“Р РЋРЎвЂњР  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р’В»Р  РЎвЂР  Р вЂ¦Р  РЎвЂР РЋР РЏ Р  Р’В·Р  Р’В° Р  Р’В»Р  РЎвЂР  Р вЂ¦Р  РЎвЂР  Р’ВµР  РІвЂћвЂ“
        float pr = rc * 0.8f;
        for (int k = 0; k < 5; k++) {
            float part = clamp(draw * 5f - k);
            if (part <= 0f) continue;
            float aA = rot + k * 2f * TAU / 5f - HALF_PI;
            float aB = rot + (k + 1) * 2f * TAU / 5f - HALF_PI;
            float ax = cos(aA) * pr, az = sin(aA) * pr;
            float bx = cos(aB) * pr, bz = sin(aB) * pr;
            flatLine(b, m, ax, az, ax + (bx - ax) * part, az + (bz - az) * part,
                    0.026f, 0.05f * s, spiritLight, 0.85f * a0);
            flatLine(b, m, ax, az, ax + (bx - ax) * part, az + (bz - az) * part,
                    0.025f, 0.18f * s, spirit, 0.25f * a0);
        }

        // Р РЋР вЂљР РЋРЎвЂњР  Р вЂ¦Р РЋРІР‚в„– Р  РЎВР  Р’ВµР  Р’В¶Р  РўвЂР РЋРЎвЂњ Р  РЎвЂќР  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІР‚ Р  Р’В°Р  РЎВР  РЎвЂ
        for (int i = 0; i < 12; i++) {
            if (draw < i / 12f) continue;
            float base = rot + i * TAU / 12f + 0.13f;
            float rMid = rc * 0.91f;
            float tx = -sin(base), tz = cos(base);
            float nx = cos(base), nz = sin(base);
            float cx0 = nx * rMid, cz0 = nz * rMid;
            for (int k = 0; k < 3; k++) {
                float h1 = hash(i * 7.1f + k * 3.3f) - 0.5f;
                float h2 = hash(i * 5.9f + k * 9.7f) - 0.5f;
                float h3 = hash(i * 2.3f + k * 4.1f) - 0.5f;
                float h4 = hash(i * 8.8f + k * 1.9f) - 0.5f;
                float g = 0.12f * s;
                flatLine(b, m,
                        cx0 + (tx * h1 + nx * h2) * g, cz0 + (tz * h1 + nz * h2) * g,
                        cx0 + (tx * h3 + nx * h4) * g, cz0 + (tz * h3 + nz * h4) * g,
                        0.027f, 0.03f * s, spiritLight, 0.8f * a0);
            }
        }

        // Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В± Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  Р’В°
        beam(b, c, 0f, 0f, 0f, 0f, 7f * s, 0f, 0.9f * s, spirit, 0.12f * pillar);
        beam(b, c, 0f, 0f, 0f, 0f, 7f * s, 0f, 0.22f * s, spiritLight, 0.4f * pillar);

        float burst = clamp((p - 0.8f) / 0.2f);
        if (burst > 0f) {
            float e = easeOutCubic(burst);
            billboardRing(b, c, 0f, soulY + h * 0.6f, 0f, (0.3f + e * 2.5f) * s, 0.04f * s,
                    0f, TAU, spiritLight, (1f - smoothstep(burst)) * 0.8f, 72);
        }
        drawColor(b);

        BufferBuilder g = beginGlow();

        float hl = (float) Math.sqrt(c.cx * c.cx + c.cz * c.cz);
        float rx = hl > 1e-4f ? -c.cz / hl : 1f;
        float rz = hl > 1e-4f ? c.cx / hl : 0f;

        // Р  РўвЂР РЋРЎвЂњР РЋРІвЂљВ¬Р  Р’В°: Р РЋР С“Р  РЎвЂР  Р’В»Р РЋРЎвЂњР РЋР РЉР РЋРІР‚С™ Р  РЎвЂР  Р’В· Р  РЎВР РЋР РЏР  РЎвЂ“Р  РЎвЂќР  РЎвЂР РЋРІР‚В¦ Р  РЎвЂўР  РЎвЂ“Р  Р вЂ¦Р  Р’ВµР  РІвЂћвЂ“, Р  Р вЂ¦Р  РЎвЂўР  РЎвЂ“Р  РЎвЂ Р РЋР С“Р РЋРІР‚С™Р РЋР РЏР  РЎвЂ“Р  РЎвЂР  Р вЂ Р  Р’В°Р РЋР вЂ№Р РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ  Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋРІР‚В¦Р  Р вЂ Р  РЎвЂўР РЋР С“Р РЋРІР‚С™
        if (soulA > 0.002f) {
            for (int i = 0; i < SOUL_SHAPE.length; i++) {
                float[] sh = SOUL_SHAPE[i];
                float ox = sh[0] * s;
                float oy = sh[1] * hs;
                if (sh[1] < 0.7f) {
                    float tail = rise;
                    ox *= (1f - tail);
                    oy = oy + (0.8f * hs - oy) * tail * 0.3f - tail * (0.7f - sh[1]) * 1.2f * hs;
                }
                float wob = sin(t * 3f + i * 0.7f) * 0.05f * s;
                float x = rx * (ox + wob);
                float z = rz * (ox + wob);
                float y = oy + soulY + sin(t * 2f) * 0.06f;
                float flick = 0.8f + 0.2f * sin(t * 11f + i * 1.9f);
                float sz = sh[2] * s;
                glow(g, c, x, y, z, sz * 2.4f, sz * 2.4f, spirit, soulA * 0.3f * flick);
                glow(g, c, x, y, z, sz * 0.8f, sz * 0.8f, spiritLight, soulA * 0.65f * flick);
            }
            // Р  РЎвЂ“Р  Р’В»Р  Р’В°Р  Р’В·Р  Р’В°
            float ey = 1.64f * hs + soulY + sin(t * 2f) * 0.06f;
            for (int e = -1; e <= 1; e += 2) {
                glow(g, c, rx * e * 0.07f * s, ey, rz * e * 0.07f * s,
                        0.07f * s, 0.05f * s, 0xFFFFFF, soulA);
            }
        }

        // Р  Р’В±Р  Р’В»Р РЋРЎвЂњР  Р’В¶Р  РўвЂР  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  Р’Вµ Р  РЎвЂўР  РЎвЂ“Р  РЎвЂўР  Р вЂ¦Р РЋР Р‰Р  РЎвЂќР  РЎвЂ Р  РЎвЂ”Р  РЎвЂў Р РЋР С“Р  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  Р’В»Р  РЎвЂ Р  Р вЂ Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚В¦
        float activity = smoothstep(p / 0.15f) * (1f - smoothstep((p - 0.82f) / 0.18f));
        for (Mote d : fx.dust) {
            float ph = frac(t * d.speed() * 0.6f + d.phase());
            float ang = d.dy() + ph * TAU * 1.5f + t;
            float r = (0.55f + 0.35f * sin(ph * PI)) * s * (0.6f + 0.4f * d.x());
            float y = ph * (soulY + h * 1.2f);
            float a = sin(ph * PI) * activity;
            int col = mixColor(spirit, spiritLight, d.tint());
            glow(g, c, cos(ang) * r, y, sin(ang) * r, d.size() * 3f, d.size() * 3f, spirit, a * 0.25f);
            glow(g, c, cos(ang) * r, y, sin(ang) * r, d.size(), d.size() * 1.5f, col, a);
        }

        // Р  РЎвЂ”Р  Р’ВµР  РЎвЂ”Р  Р’ВµР  Р’В» Р РЋР вЂљР РЋРЎвЂњР  Р вЂ¦ Р  РЎвЂ”Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  РЎвЂР  РЎВР  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р РЋР С“ Р  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“Р  Р’В°
        float ash = clamp((p - 0.7f) / 0.3f);
        if (ash > 0f) {
            for (Mote d : fx.motes) {
                float life = clamp((ash - d.delay() * 0.3f) / 0.7f);
                if (life <= 0f) continue;
                float ang = d.x() + rot;
                float r = rc * (0.8f + d.z() * 0.2f);
                float y = 0.05f + life * (0.6f + d.y() * 1.2f) * s;
                float a = sin(life * PI) * 0.9f;
                glow(g, c, cos(ang) * r, y, sin(ang) * r, d.size(), d.size(), spiritLight, a);
            }
        }

        float intro = 1f - smoothstep(p / 0.25f);
        glow(g, c, 0f, h * 0.5f, 0f, h * 0.9f, h * 1.3f, spirit, intro * 0.4f);

        if (burst > 0f) {
            float fb = 1f - smoothstep(burst);
            glow(g, c, 0f, soulY + h * 0.6f, 0f, (0.6f + burst * 3f) * s, (0.6f + burst * 3f) * s,
                    spiritLight, fb * 0.9f);
        }
        drawGlow(g);
    }


    // =====================================================================
    //  Р  РІР‚СњР  РЎвЂўР  РЎвЂ”. Р РЋР С“Р  Р’В»Р  РЎвЂўР  РЎвЂ Р вЂ™Р’В«Р  Р вЂ Р  Р’В°Р РЋРЎвЂњР вЂ™Р’В»
    // =====================================================================

    private void magmaExtra(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p, t = c.t, s = fx.scale, h = fx.height;
        float pitOpen = easeOutCubic(clamp((p - 0.5f) / 0.2f));
        float pitLive = pitOpen * (1f - smoothstep((p - 0.9f) / 0.1f));
        float pitR = (0.5f + fx.width * 0.85f) * s * pitLive;

        // Р  РўвЂР РЋРІР‚в„–Р  РЎВ Р  РЎвЂ”Р  РЎвЂўР  РўвЂР  Р вЂ¦Р  РЎвЂР  РЎВР  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  РЎвЂР  Р’В· Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р вЂ Р  Р’В°Р  Р’В»Р  Р’В°
        translucent();
        BufferBuilder b = beginColor();
        for (int i = 0; i < 16; i++) {
            float hs = hash(i * 4.7f + fx.seedF);
            float ph = frac(t * (0.35f + hs * 0.2f) + hs);
            float ang = hs * TAU + t * 0.3f;
            float r = pitR * 0.6f * hs + ph * 0.4f * s;
            float y = 0.3f + ph * 3.2f * s;
            float sz = (0.25f + ph * 0.7f) * s;
            float a = sin(ph * PI) * 0.35f * Math.max(pitLive, 0f);
            softDisc(b, c, cos(ang) * r, y, sin(ang) * r, sz * 0.6f, sz * 0.5f,
                    0x1A120E, a, 20);
        }
        drawColor(b);

        additive();
        b = beginColor();
        // Р  РЎвЂўР  РЎвЂ“Р  Р вЂ¦Р  Р’ВµР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋРЎвЂњР  РўвЂР  Р’В°Р РЋР вЂљР  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  Р вЂ Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р  Р’В° Р  РЎвЂ”Р РЋР вЂљР  РЎвЂ Р  РЎвЂўР РЋРІР‚С™Р  РЎвЂќР РЋР вЂљР РЋРІР‚в„–Р РЋРІР‚С™Р  РЎвЂР  РЎвЂ Р  РЎвЂ”Р  Р’ВµР  РЎвЂќР  Р’В»Р  Р’В°
        float wave = clamp((p - 0.5f) / 0.3f);
        if (wave > 0f && wave < 1f) {
            float e = easeOutCubic(wave);
            float a = 1f - smoothstep(wave);
            ring(b, c.mat(), (0.3f + e * 4f) * s, 0.07f * s * (1f - wave) + 0.015f, 0.04f,
                    0f, TAU, 0xFF7A20, a * 0.9f, 128);
            ring(b, c.mat(), (0.2f + e * 2.8f) * s, 0.03f * s, 0.045f,
                    0f, TAU, LAVA_HOT, a * 0.7f, 96);
        }
        // Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В± Р  Р’В¶Р  Р’В°Р РЋР вЂљР  Р’В°
        beam(b, c, 0f, 0f, 0f, 0f, 3.5f * s, 0f, pitR * 1.6f, 0xFF4A00, 0.1f * pitLive);
        beam(b, c, 0f, 0f, 0f, 0f, 2.2f * s, 0f, pitR * 0.5f, 0xFFB050, 0.22f * pitLive);
        drawColor(b);

        BufferBuilder g = beginGlow();
        // Р  Р’В»Р  Р’В°Р  Р вЂ Р  РЎвЂўР  Р вЂ Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋРІР‚С›Р  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™Р  Р’В°Р  Р вЂ¦
        for (int i = 0; i < 90; i++) {
            float hs = hash(i * 2.13f + fx.seedF);
            float hs2 = hash(i * 7.91f + fx.seedF);
            float cyc = 0.85f + hs * 0.65f;
            float ph = frac(t / cyc + hs2);
            float ang = hs * TAU;
            float vr = (0.7f + hs2 * 1.7f) * s;
            float vy = (3.6f + hs * 3.1f) * s;
            float tt = ph * cyc;
            float x = cos(ang) * (pitR * 0.3f + vr * tt);
            float z = sin(ang) * (pitR * 0.3f + vr * tt);
            float y = vy * tt - 6.2f * tt * tt;
            if (y < -0.1f) continue;
            float a = pitLive * (1f - ph * 0.6f);
            int col = mixColor(LAVA_HOT, 0xFF3A00, ph);
            float sz = (0.1f + hs2 * 0.08f) * s;
            glow(g, c, x, y, z, sz * 4.5f, sz * 4.5f, 0xFF4A00, a * 0.32f);
            glow(g, c, x, y, z, sz * 1.4f, sz * 2.4f, col, a);
            glow(g, c, x, y, z, sz * 0.45f, sz * 0.9f, LAVA_CORE, a * (1f - ph) * 0.8f);
            // Р  Р’С‘Р РЋР С“Р  РЎвЂќР РЋР вЂљР  Р’В° Р РЋРІР‚РЋР РЋРЎвЂњР РЋРІР‚С™Р  РЎвЂ Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋР Р‰Р РЋРІвЂљВ¬Р  Р’Вµ Р  Р вЂ Р  РЎвЂў Р  Р вЂ Р РЋР вЂљР  Р’ВµР  РЎВР  Р’ВµР  Р вЂ¦Р  РЎвЂ Р  РўвЂР  Р’В»Р РЋР РЏ Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™Р  Р’В° Р РЋРІвЂљВ¬Р  Р’В»Р  Р’ВµР  РЎвЂР РЋРІР‚С›Р  Р’В°
            float ptt = Math.max(0f, tt - 0.05f);
            float px2 = cos(ang) * (pitR * 0.3f + vr * ptt);
            float pz2 = sin(ang) * (pitR * 0.3f + vr * ptt);
            float py2 = vy * ptt - 6.2f * ptt * ptt;
            if (py2 >= -0.1f) glow(g, c, px2, py2, pz2, sz * 0.5f, sz * 1f, 0xFF6A10, a * 0.4f);
        }
        // Р  РЎвЂ”Р  Р’ВµР  РЎвЂ”Р  Р’ВµР  Р’В»-Р РЋРЎвЂњР  РЎвЂ“Р  РЎвЂўР  Р’В»Р РЋР Р‰Р  РЎвЂќР  РЎвЂ Р  РўвЂР  РЎвЂўР  Р’В¶Р  РўвЂР РЋРІР‚ВР  РЎВ
        for (int i = 0; i < 55; i++) {
            float hs = hash(i * 9.3f + fx.seedF);
            float ph = frac(t * 0.4f + hs);
            float ang = hs * TAU * 3f;
            float r = (0.8f + hs * 2.6f) * s;
            float y = (1f - ph) * 3.8f * s;
            float a = sin(ph * PI) * 0.85f * clamp(p / 0.3f) * (1f - smoothstep((p - 0.88f) / 0.12f));
            float fl = 0.5f + 0.5f * sin(t * 17f + i);
            glow(g, c, cos(ang) * r, y, sin(ang) * r, 0.055f * s, 0.055f * s, 0xFFA040, a * fl);
        }
        glow(g, c, 0f, 1.3f * s, 0f, pitR * 3.4f, 3.4f * s, 0xFF5A10, 0.28f * pitLive);
        glow(g, c, 0f, 0.4f, 0f, pitR * 1.4f, 1.2f * s, LAVA_CORE, 0.35f * pitLive);
        float op = 1f - smoothstep(Math.abs(p - 0.52f) / 0.06f);
        starFlare(g, c, 0f, 0.3f, 0f, 7.5f * s, 0xFFB050, op * 0.9f, 8, 0.32f);
        starFlare(g, c, 0f, 0.3f, 0f, 4f * s, LAVA_CORE, op, 6, 0.2f);
        glow(g, c, 0f, h * 0.5f, 0f, 3.4f * s, 3.4f * s, 0xFF7A20, op * 0.55f);
        drawGlow(g);
    }


    private void crystalExtra(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p, t = c.t, s = fx.scale, h = fx.height;
        int iceDeep = mixColor(fx.color, 0x16327E, 0.78f);
        int ice = mixColor(fx.color, 0x58B8FF, 0.55f);
        int iceLight = mixColor(ice, 0xEAF8FF, 0.7f);
        float grow = easeOutCubic(clamp(p / 0.2f));
        float whole = 1f - smoothstep((p - 0.64f) / 0.025f);
        float all = 1f - smoothstep((p - 0.85f) / 0.15f);

        additive();
        BufferBuilder b = beginColor();
        // Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  РЎвЂ Р РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р  Р’В°, Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В±Р  РЎвЂР  Р вЂ Р  Р’В°Р РЋР вЂ№Р РЋРІР‚В°Р  РЎвЂР  Р’ВµР РЋР С“Р РЋР РЏ Р  РЎвЂР  Р’В· Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р  Р’В°
        for (int i = 0; i < 14; i++) {
            float ang = i * TAU / 14f + t * 0.25f;
            float y = h * (0.3f + hash(i * 3.3f) * 0.8f);
            float len = (2.5f + hash(i * 1.7f) * 1.9f) * s * grow;
            float a = (0.5f + 0.5f * sin(t * 3f + i)) * whole * grow;
            beam(b, c, 0f, y, 0f, cos(ang) * len, y + (hash(i * 5.1f) - 0.3f) * len, sin(ang) * len,
                    0.14f * s, ice, 0.12f * a);
        }
        // Р  Р’В»Р  Р’ВµР  РўвЂР РЋР РЏР  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂўР РЋР вЂљР  Р’В±Р  РЎвЂР РЋРІР‚С™Р РЋРІР‚в„–
        for (int k = 0; k < 3; k++) {
            c.m.push();
            c.m.translate(0f, h * 0.55f, 0f);
            c.m.multiply(RotationAxis.POSITIVE_Y.rotation(t * (0.6f + k * 0.3f) + k * 2f));
            c.m.multiply(RotationAxis.POSITIVE_X.rotation(0.5f + k * 0.45f));
            ring(b, c.mat(), (1.1f + k * 0.35f) * s * grow, 0.018f * s, 0f,
                    t * 2f, TAU * 0.65f, k == 1 ? iceLight : ice, 0.4f * whole, 72);
            c.m.pop();
        }
        drawColor(b);

        BufferBuilder g = beginGlow();
        // Р РЋР С“Р  Р вЂ¦Р  Р’ВµР  Р’В¶Р  РЎвЂР  Р вЂ¦Р  РЎвЂќР  РЎвЂ
        // Р·РІС‘Р·РґС‹-Р±Р»РёРєРё РЅР° РІРµСЂС…СѓС€РєР°С… РІС‹СЂРѕСЃС€РёС… РєСЂРёСЃС‚Р°Р»Р»РѕРІ вЂ” В«РёРіСЂР° РіСЂР°РЅРµР№В»
        for (int i = 0; i < fx.crystals.length; i++) {
            Crystal cr = fx.crystals[i];
            float growK = easeOutCubic(clamp((p - cr.delay()) / 0.14f));
            if (growK < 0.85f) continue;
            float tw = (float) Math.pow(0.5f + 0.5f * sin(t * 7f + i * 2.3f), 4.0);
            float ty = cr.height() * 0.96f * growK;
            starFlare(g, c, cr.x(), ty, cr.z(), (0.4f + 0.5f * tw) * s,
                    iceLight, (0.18f + 0.7f * tw) * whole, 4, t * 0.8f + i);
            glow(g, c, cr.x(), ty, cr.z(), 0.22f * s, 0.22f * s, 0xFFFFFF, tw * 0.7f * whole);
        }
        for (int i = 0; i < 110; i++) {
            float hs = hash(i * 3.71f + fx.seedF);
            float ph = frac(t * 0.25f + hs);
            float ang = hs * TAU * 5f + t * 0.3f;
            float r = (0.4f + hash(i * 1.9f) * 3.1f) * s;
            float y = (1f - ph) * (h + 2f * s) + sin(t * 2f + i) * 0.1f;
            float a = sin(ph * PI) * all * 0.5f;
            float tw = 0.4f + 0.6f * sin(t * 9f + i * 1.7f) * sin(t * 9f + i * 1.7f);
            int tint = (i % 4 == 0) ? mixColor(iceLight, 0xB08CFF, 0.35f) : iceLight;
            glow(g, c, cos(ang) * r, y, sin(ang) * r, 0.055f * s, 0.055f * s, tint, a * tw);
        }
        // Р  РЎВР  РЎвЂўР РЋР вЂљР  РЎвЂўР  Р’В·Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  РўвЂР РЋРІР‚в„–Р  РЎВР  РЎвЂќР  Р’В° Р РЋРЎвЂњ Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  РЎвЂ
        glow(g, c, 0f, 0.2f, 0f, 4.6f * s * grow, 0.9f * s, iceDeep, 0.16f * all);
        glow(g, c, 0f, 0.16f, 0f, 3.2f * s * grow, 0.7f * s, iceLight, 0.2f * all);
        float flash = 1f - smoothstep(Math.abs(p - 0.64f) / 0.05f);
        starFlare(g, c, 0f, h * 0.55f, 0f, 7f * s, iceLight, flash, 10, 0.22f);
        starFlare(g, c, 0f, h * 0.55f, 0f, 3.5f * s, 0xFFFFFF, flash, 6, 0.15f);
        float form = 1f - smoothstep(Math.abs(p - 0.04f) / 0.05f);
        glow(g, c, 0f, h * 0.5f, 0f, 2.2f * s, 3.0f * s, iceLight, form * 0.6f);
        drawGlow(g);
    }


    private void soulExtra(Ctx c) {
        ActiveEffect fx = c.fx;
        float p = c.p, t = c.t, s = fx.scale, h = fx.height;
        int spirit = mixColor(fx.color, 0x8CFFE0, 0.45f);
        int spiritLight = mixColor(spirit, 0xFFFFFF, 0.6f);
        float draw = easeOutCubic(clamp((p - 0.1f) / 0.25f));
        float circle = 1f - smoothstep((p - 0.78f) / 0.22f);
        float rise = smoothstep((p - 0.18f) / 0.62f);
        float soulY = rise * 4.2f * s;

        additive();
        BufferBuilder b = beginColor();
        // Р  Р вЂ Р РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР  РЎвЂўР  РІвЂћвЂ“ Р  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р  РЎвЂ”Р  Р’В°Р РЋР вЂљР  РЎвЂР РЋРІР‚С™ Р  Р вЂ¦Р  Р’В°Р  РўвЂ Р  РЎвЂ“Р  РЎвЂўР  Р’В»Р  РЎвЂўР  Р вЂ Р  РЎвЂўР  РІвЂћвЂ“ Р  РЎвЂ Р  Р вЂ Р РЋР вЂљР  Р’В°Р РЋРІР‚В°Р  Р’В°Р  Р’ВµР РЋРІР‚С™Р РЋР С“Р РЋР РЏ Р  Р вЂ¦Р  Р’В°Р  Р вЂ Р РЋР С“Р РЋРІР‚С™Р РЋР вЂљР  Р’ВµР РЋРІР‚РЋР РЋРЎвЂњ
        c.m.push();
        c.m.translate(0f, h + 0.5f * s + soulY * 0.5f, 0f);
        Matrix4f m = c.mat();
        float a0 = circle * draw;
        ring(b, m, 0.9f * s, 0.02f * s, 0f, -t * 0.8f, TAU * draw, spirit, 0.8f * a0, 72);
        ring(b, m, 0.7f * s, 0.012f * s, 0f, t * 1.2f, TAU * draw, spiritLight, 0.7f * a0, 64);
        for (int k = 0; k < 6; k++) {
            float aa = -t * 0.8f + k * TAU / 6f;
            float bb = aa + TAU / 3f;
            flatLine(b, m, cos(aa) * 0.7f * s, sin(aa) * 0.7f * s,
                    cos(bb) * 0.7f * s, sin(bb) * 0.7f * s, 0f, 0.035f * s, spiritLight, 0.6f * a0);
        }
        c.m.pop();
        // Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚С™Р  РЎвЂР  РЎвЂќР  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  Р’В»Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р РЋРІР‚в„– Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“ Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР  Р’В»Р  Р’В±Р  Р’В°
        for (int i = 0; i < 6; i++) {
            float ang = i * TAU / 6f + t * 0.9f;
            float r = 0.9f * s;
            float x = cos(ang) * r, z = sin(ang) * r;
            float wav = sin(t * 3f + i) * 0.15f * s;
            beam(b, c, x, 0f, z, x * 0.3f + wav, 4f * s, z * 0.3f, 0.12f * s, spirit,
                    0.3f * circle * draw);
        }
        drawColor(b);

        BufferBuilder g = beginGlow();
        float burst = clamp((p - 0.8f) / 0.2f);
        if (burst > 0f) {
            starFlare(g, c, 0f, soulY + h * 0.6f, 0f, (3f + burst * 7f) * s, spiritLight,
                    1f - smoothstep(burst), 6, t * 0.3f);
        }
        glow(g, c, 0f, 0.1f, 0f, 4f * s, 0.6f * s, spirit, 0.35f * circle * draw);
        drawGlow(g);
    }

    // =====================================================================
    //  Р  РўС’Р  Р’ВµР  Р’В»Р  РЎвЂ”Р  Р’ВµР РЋР вЂљР РЋРІР‚в„– Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  РўвЂР  Р’ВµР РЋР вЂљР  Р’В°
    // =====================================================================

    // =====================================================================
    //  Р‘РЈРЎРРљ: С„СѓСЂРіРѕРЅ РїРѕРґСЉРµР·Р¶Р°РµС‚ Р‘РћРљРћРњ Рє С‚РµР»Сѓ (РЅРµ С‡РµСЂРµР· РЅРµРіРѕ), СЃРґРІРёР¶РЅР°СЏ РґРІРµСЂСЊ
    //  РѕС‚СЉРµР·Р¶Р°РµС‚, РґРІРѕРµ Р±РѕР№С†РѕРІ СЃРїСЂС‹РіРёРІР°СЋС‚, РѕР±С…РѕРґСЏС‚ С‚РµР»Рѕ, СѓРєР»Р°РґС‹РІР°СЋС‚, РЅРµСЃСѓС‚,
    //  Р·Р°РґРІРёРіР°СЋС‚ РІ СЃР°Р»РѕРЅ, Р·Р°Р»РµР·Р°СЋС‚ СЃР°РјРё, РґРІРµСЂСЊ Р·Р°РєСЂС‹РІР°РµС‚СЃСЏ, Р±СѓСЃРёРє СѓРµР·Р¶Р°РµС‚.
    //  Р’СЃРµ РєРѕРѕСЂРґРёРЅР°С‚С‹ СЃС†РµРЅС‹ РІ Р±Р»РѕРєР°С…; РѕСЃСЊ Z = РѕСЃСЊ РґРІРёР¶РµРЅРёСЏ Р±СѓСЃРёРєР°.
    //  РЎС‚РѕСЂРѕРЅСѓ СЃС†РµРЅС‹ РІС‹Р±РёСЂР°РµРј РїРѕ Р±Р»РѕРєР°Рј РІРѕРєСЂСѓРі, С‡С‚РѕР±С‹ Р±СѓСЃРёРє РЅРµ РІСЉРµР·Р¶Р°Р» РІ СЃС‚РµРЅС‹.
    // =====================================================================

    private static final float V_ARRIVE = 1.3f;
    private static final float V_LEAVE = 6.0f;
    private static final float V_TIP0 = 3.2f, V_TIP1 = 3.65f, V_CARRY1 = 4.55f, V_PUSH1 = 4.9f;
    private static final float V_GONE = 5.9f;

    private void renderVan(Ctx c) {
        ActiveEffect fx = c.fx;
        float T = c.p * (EffectMode.VAN.duration / 1000f);
        float fade = 1f - smoothstep((c.p - 0.9f) / 0.1f);
        if (fade <= 0.004f) {
            return;
        }

        float s = fx.scale;
        float w = MathHelper.clamp(fx.width, 0.3f, 1.2f);
        float h = fx.height;

        // Р±СѓСЃРёРє СЃС‚РѕРёС‚ СЃР±РѕРєСѓ РѕС‚ С‚РµР»Р° СЃ Р·Р°Р·РѕСЂРѕРј, Р±РѕСЂС‚РѕРј (СЃРґРІРёР¶РЅРѕР№ РґРІРµСЂСЊСЋ) Рє РЅРµРјСѓ
        float xv = w * 0.5f + 0.8f * s + 0.95f * s;
        if (Float.isNaN(fx.vanYaw)) {
            fx.vanYaw = pickVanYaw(c, xv, s, w, h);
        }
        float yawDeg = fx.vanYaw;
        float yr = yawDeg * PI / 180f;
        float cs = cos(yr), sn = sin(yr);

        // ---- РґРІРёР¶РµРЅРёРµ Р±СѓСЃРёРєР° ----
        float vanZ;
        float speedK;
        if (T < V_ARRIVE) {
            float k = T / V_ARRIVE;
            vanZ = 14f * s * (1f - easeOutCubic(k));
            speedK = (1f - k) * (1f - k);
        } else if (T < V_LEAVE) {
            vanZ = 0f;
            speedK = 0f;
        } else {
            float k = (T - V_LEAVE) / 1.5f;
            vanZ = -18f * s * k * k;
            speedK = clamp(k * 1.5f);
        }
        boolean moving = speedK > 0.01f;
        float wheelAng = -(14f * s - vanZ) / (0.36f * s);
        float pitch = 0f;
        float brake = clamp((T - 0.55f) / 1.0f);
        if (brake > 0f && brake < 1f) pitch -= 1.8f * sin(PI * brake);
        float launch = clamp((T - V_LEAVE) / 0.8f);
        if (launch > 0f && launch < 1f) pitch += 1.5f * sin(PI * launch);
        float bob = moving ? sin(c.t * 38f) * 0.006f * s * speedK : 0f;

        // ---- СЃРґРІРёР¶РЅР°СЏ РґРІРµСЂСЊ: СЃРЅР°С‡Р°Р»Р° РѕС‚С…РѕРґРёС‚ РЅР°СЂСѓР¶Сѓ, РїРѕС‚РѕРј РµРґРµС‚ РЅР°Р·Р°Рґ РІРґРѕР»СЊ Р±РѕСЂС‚Р° ----
        float doorK = smoothstep((T - 1.35f) / 0.45f) * (1f - smoothstep((T - 5.45f) / 0.45f));
        float doorDX = -0.1f * smoothstep(doorK / 0.3f);
        float doorDZ = 1.9f * smoothstep((doorK - 0.25f) / 0.75f);

        // ---- РѕРїРѕСЂРЅС‹Рµ С‚РѕС‡РєРё СЃС†РµРЅС‹ (Р±Р»РѕРєРё) ----
        float doorX = xv - 0.95f * s;           // РїР»РѕСЃРєРѕСЃС‚СЊ Р»РµРІРѕРіРѕ Р±РѕСЂС‚Р°
        float groundOut = doorX - 0.45f * s;    // РєСѓРґР° СЃРїСЂС‹РіРёРІР°СЋС‚
        float insideX = xv - 0.62f * s;         // РіРґРµ СЃС‚РѕСЏС‚ РІ СЃР°Р»РѕРЅРµ
        float floorY = 0.63f * s;
        float zc = 0.1f * s;                    // С†РµРЅС‚СЂ РїСЂРѕС‘РјР° РґРІРµСЂРё
        float dz = Math.min(0.27f * h, 0.7f * s);
        float carryY = Math.max(1.05f * s, 0.72f * s + w * 0.5f);
        float bxDoor = doorX - 0.42f * s + w * 0.5f + 0.3f * s;
        float bxIn = Math.min(xv + 0.02f * s, xv + 0.48f * s - w * 0.5f);
        float lieY = floorY + w * 0.45f;
        float sideOff = w * 0.5f + 0.3f * s;    // Р±РѕР№С†С‹ СЃС‚РѕСЏС‚ СЃ РІРЅРµС€РЅРµР№ СЃС‚РѕСЂРѕРЅС‹ С‚РµР»Р°

        // ---- СЃРѕСЃС‚РѕСЏРЅРёСЏ Р±РѕР№С†РѕРІ ----
        // [x, y, z, yaw(rad), phase, amp, arm, inVan(0/1), visible(0/1)]
        float[][] crew = new float[2][9];
        for (int i = 0; i < 2; i++) {
            float sg = i == 0 ? -1f : 1f;
            float step0 = i == 0 ? 1.75f : 1.9f;
            float walkEnd = i == 0 ? 3.05f : 3.1f;
            float climb0 = i == 0 ? 4.9f : 5.0f;
            float zS = zc + sg * dz;
            float p0x = -sideOff;
            float p0z = -h * 0.5f + sg * dz;
            float faceIn = HALF_PI, faceOut = -HALF_PI;
            float[] st = crew[i];
            st[8] = (T > 1.4f && T < V_GONE) ? 1f : 0f;

            float[] path;
            float lastHeading;
            if (i == 0) {
                path = new float[]{groundOut, zS, groundOut, p0z, p0x, p0z};
                lastHeading = faceOut;
            } else {
                float zPass = Math.max(zS, w * 0.5f + 0.4f * s);
                path = new float[]{groundOut, zS, groundOut, zPass, p0x, zPass, p0x, p0z};
                lastHeading = PI;
            }

            float x, y = 0f, z, yaw, ph = 0f, amp = 0f, arm = 0f, inVan = 0f;
            if (T < step0) {
                x = insideX; y = floorY; z = zS; yaw = faceOut; inVan = 1f;
            } else if (T < step0 + 0.35f) {
                float k = (T - step0) / 0.35f, e = smoothstep(k);
                x = lerp(insideX, groundOut, e);
                y = lerp(floorY, 0f, e) + 0.12f * s * sin(PI * k);
                z = zS; yaw = faceOut; ph = k * PI; amp = 1f;
            } else if (T < walkEnd) {
                float raw = (T - step0 - 0.35f) / (walkEnd - step0 - 0.35f);
                float[] pp = pathAt(path, smoothstep(raw));
                x = pp[0]; z = pp[1]; yaw = pp[2];
                ph = pp[3] / (1.62f * s) * TAU;
                amp = clamp(Math.min(raw, 1f - raw) * 6f);
            } else if (T < V_TIP1) {
                x = p0x; z = p0z;
                yaw = lerpAngle(lastHeading, faceIn, smoothstep((T - walkEnd) / 0.25f));
                arm = smoothstep((T - V_TIP0 + 0.15f) / 0.4f);
            } else if (T < V_CARRY1) {
                float raw = (T - V_TIP1) / (V_CARRY1 - V_TIP1);
                float q = smoothstep(raw);
                float bcx = lerp(0f, bxDoor, q);
                float bcz = lerp(-h * 0.5f, zc, q);
                x = bcx - sideOff; z = bcz + sg * dz; yaw = faceIn;
                float len = (float) Math.hypot(bxDoor, zc + h * 0.5f);
                ph = q * len / (1.62f * s) * TAU + i * 1.3f;
                amp = clamp(Math.min(raw, 1f - raw) * 6f);
                arm = 1f;
            } else if (T < climb0) {
                float q = smoothstep((T - V_CARRY1) / (V_PUSH1 - V_CARRY1));
                x = bxDoor - sideOff + 0.12f * s * q; z = zS; yaw = faceIn;
                ph = q * PI; amp = 0.4f * sin(PI * q);
                arm = T < V_PUSH1 ? 1f : 1f - smoothstep((T - V_PUSH1) / 0.15f);
            } else if (T < climb0 + 0.35f) {
                float k = (T - climb0) / 0.35f, e = smoothstep(k);
                x = lerp(doorX - 0.3f * s, insideX, e);
                y = floorY * e + 0.14f * s * sin(PI * k);
                z = zS; yaw = faceIn; ph = k * PI; amp = 1f; inVan = 1f;
            } else {
                x = insideX; y = floorY; z = zS; inVan = 1f;
                yaw = lerpAngle(faceIn, faceOut, smoothstep((T - climb0 - 0.35f) / 0.25f));
            }
            st[0] = x; st[1] = y; st[2] = z; st[3] = yaw; st[4] = ph; st[5] = amp; st[6] = arm; st[7] = inVan;
        }

        // ---- С‚РµРЅРё (Р±РµР· Р·Р°РїРёСЃРё РіР»СѓР±РёРЅС‹) ----
        translucent();
        BufferBuilder sh = beginColor();
        c.m.push();
        c.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawDeg));
        c.m.push();
        c.m.translate(xv, 0f, vanZ);
        c.m.scale(s, s, s);
        softRect(sh, c.mat(), -0.98f, -2.45f, 0.98f, 2.85f, 0.45f, 0.012f, 0x05060A, 0.5f * fade);
        c.m.pop();
        for (float[] st : crew) {
            if (st[8] < 0.5f || st[1] > 0.05f || st[7] > 0.5f) continue;
            c.m.push();
            c.m.translate(st[0], 0f, st[2]);
            flatDisc(sh, c.mat(), 0.34f * s, 0.014f, 0x05060A, 0.42f * fade, 0x05060A, 0f, 20);
            c.m.pop();
        }
        if (T < V_TIP0) {
            flatDisc(sh, c.mat(), (w * 0.5f + 0.15f) * 1.2f, 0.013f, 0x05060A, 0.35f * fade, 0x05060A, 0f, 20);
        }
        c.m.pop();
        drawColor(sh);

        // ---- С‚РµР»Рѕ РјРѕР±Р° ----
        if (T < V_GONE) {
            c.m.push();
            c.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawDeg));
            if (T < V_TIP0) {
                float grab = clamp((T - 3.0f) / 0.2f);
                float jit = grab > 0f ? 0.02f * s : 0f;
                float jx = (hash((float) Math.floor(c.t * 40f)) - 0.5f) * jit;
                float jz = (hash((float) Math.floor(c.t * 40f) + 9f) - 0.5f) * jit;
                c.m.translate(jx, 0f, jz);
                c.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yawDeg));
            } else {
                float k = smoothstep((T - V_TIP0) / (V_TIP1 - V_TIP0));
                float px, py, pz;
                if (T < V_TIP1) {
                    px = 0f; py = carryY * k; pz = 0f;
                } else if (T < V_CARRY1) {
                    float q = smoothstep((T - V_TIP1) / (V_CARRY1 - V_TIP1));
                    px = lerp(0f, bxDoor, q); py = carryY; pz = lerp(0f, zc + h * 0.5f, q);
                } else {
                    float q = smoothstep((T - V_CARRY1) / (V_PUSH1 - V_CARRY1));
                    px = lerp(bxDoor, bxIn, q); py = lerp(carryY, lieY, q); pz = zc + h * 0.5f;
                }
                if (T >= V_PUSH1) {
                    vanFrame(c.m, xv, bob, vanZ, pitch);
                }
                // РІСЂР°С‰РµРЅРёРµ РІРѕРєСЂСѓРі РїСЏС‚РѕРє: РіРѕР»РѕРІР° СѓС…РѕРґРёС‚ РІ -Z, Р»РёС†РѕРј РІРІРµСЂС…
                c.m.translate(px, py, pz);
                c.m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f * k));
                c.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                        lerpAngle(-yr, fx.bodyYaw * PI / 180f, k) * 180f / PI));
            }
            drawBody(c, 1f, 1f, 1f);
            c.m.pop();
        }

        // ---- С‚РІС‘СЂРґР°СЏ РіРµРѕРјРµС‚СЂРёСЏ: Р±СѓСЃРёРє + Р±РѕР№С†С‹, СЃ Р·Р°РїРёСЃСЊСЋ РіР»СѓР±РёРЅС‹ ----
        translucent();
        BufferBuilder b = beginColor();
        float tail = 0.35f + 0.65f * smoothstep((T - 0.5f) / 0.1f) * (1f - smoothstep((T - 1.6f) / 0.2f));
        float head = 0.3f + 0.7f * Math.max(1f - smoothstep((T - 1.2f) / 0.4f), smoothstep((T - 5.9f) / 0.3f));

        c.m.push();
        c.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawDeg));
        c.m.push();
        c.m.translate(xv, bob, vanZ);
        c.m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        c.m.scale(s, s, s);
        drawVanModel(b, c.mat(), fx.color, doorDX, doorDZ, wheelAng, head, tail, doorK, fade);
        c.m.pop();

        for (float[] st : crew) {
            if (st[8] < 0.5f) continue;
            c.m.push();
            if (st[7] > 0.5f) {
                vanFrame(c.m, xv, bob, vanZ, pitch);
            }
            c.m.translate(st[0], st[1], st[2]);
            c.m.multiply(RotationAxis.POSITIVE_Y.rotation(st[3]));
            c.m.scale(s, s, s);
            drawSoldier(b, c, fx.color, st[4], st[5], st[6], fade);
            c.m.pop();
        }
        c.m.pop();

        RenderSystem.depthMask(true);
        drawColor(b);
        RenderSystem.depthMask(false);

        // ---- СЃРІРµС‚ (additive, РІ РєР°РґСЂРµ СЌС„С„РµРєС‚Р° Р±РµР· РїРѕРІРѕСЂРѕС‚Р°: Р±РёР»Р±РѕСЂРґС‹ РЅРµ Р»РѕРјР°СЋС‚СЃСЏ) ----
        additive();
        float headK = head * fade;
        float beamK = clamp((head - 0.3f) / 0.7f) * fade;

        if (beamK > 0.01f) {
            BufferBuilder beams = beginColor();
            for (int sx = -1; sx <= 1; sx += 2) {
                float[] a0 = sceneToFx(xv + sx * 0.68f * s, bob + 1.02f * s, vanZ - 2.42f * s, cs, sn);
                float[] a1 = sceneToFx(xv + sx * 1.1f * s, 0.2f * s, vanZ - 8.5f * s, cs, sn);
                beam(beams, c, a0[0], a0[1], a0[2], a1[0], a1[1], a1[2], 0.55f * s, 0xFFF2C8, 0.13f * beamK);
            }
            drawColor(beams);
        }

        if (doorK > 0.02f) {
            float[] spill = sceneToFx(doorX - 0.55f * s, 0f, zc, cs, sn);
            BufferBuilder pool = beginColor();
            c.m.push();
            c.m.translate(spill[0], 0f, spill[2]);
            flatDisc(pool, c.mat(), 1.2f * s, 0.02f, 0xFFD9A0, 0.14f * doorK * fade, 0xFFD9A0, 0f, 28);
            c.m.pop();
            drawColor(pool);
        }

        BufferBuilder g = beginGlow();
        for (int sx = -1; sx <= 1; sx += 2) {
            float[] hp = sceneToFx(xv + sx * 0.68f * s, bob + 1.02f * s, vanZ - 2.4f * s, cs, sn);
            glow(g, c, hp[0], hp[1], hp[2], 0.5f * s, 0.5f * s, 0xFFF3D2, 0.9f * headK);
            glow(g, c, hp[0], hp[1], hp[2], 1.5f * s, 1.1f * s, 0xFFE8B8, 0.22f * headK);
            float[] tp = sceneToFx(xv + sx * 0.865f * s, bob + 1.4f * s, vanZ + 2.8f * s, cs, sn);
            glow(g, c, tp[0], tp[1], tp[2], 0.38f * s, 0.7f * s, 0xFF2A1A, 0.75f * tail * fade);
        }
        if (doorK > 0.02f) {
            float[] lp = sceneToFx(xv, 2.2f * s, vanZ + 0.15f * s, cs, sn);
            glow(g, c, lp[0], lp[1], lp[2], 1.6f * s, 1.3f * s, 0xFFE2B0, 0.4f * doorK * fade);
        }
        if (moving) {
            for (int i = 0; i < 10; i++) {
                float hs = hash(i * 3.17f + (float) Math.floor(c.t * 16f) * 0.7f);
                float lx = (i % 2 == 0 ? -0.86f : 0.86f) + (hs - 0.5f) * 0.5f;
                float lz = 2.3f + hash(i * 7.7f) * 0.9f;
                float[] dp = sceneToFx(xv + lx * s, (0.08f + hs * 0.15f) * s, vanZ + lz * s, cs, sn);
                glow(g, c, dp[0], dp[1], dp[2], 0.45f * s, 0.26f * s, 0x9A9182, 0.22f * speedK * fade);
            }
        }
        drawGlow(g);
    }

    /** РљР°РґСЂ СЃР°Р»РѕРЅР°: РІСЃС‘, С‡С‚Рѕ РІРЅСѓС‚СЂРё Р±СѓСЃРёРєР°, РµРґРµС‚ РІРјРµСЃС‚Рµ СЃ РЅРёРј. РљРѕРѕСЂРґРёРЅР°С‚С‹ РѕСЃС‚Р°СЋС‚СЃСЏ СЃС†РµРЅРѕРІС‹РјРё. */
    private static void vanFrame(MatrixStack m, float xv, float bob, float vanZ, float pitch) {
        m.translate(xv, bob, vanZ);
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        m.translate(-xv, 0f, 0f);
    }

    private static float[] sceneToFx(float x, float y, float z, float cs, float sn) {
        return new float[]{x * cs + z * sn, y, -x * sn + z * cs};
    }

    private static float lerpAngle(float a, float b, float k) {
        float d = (float) Math.atan2(Math.sin(b - a), Math.cos(b - a));
        return a + d * k;
    }

    /** РўРѕС‡РєР° РЅР° Р»РѕРјР°РЅРѕР№ РїРѕ РґРѕР»Рµ РґР»РёРЅС‹: x, z, РєСѓСЂСЃ (СЂР°Рґ), РїСЂРѕР№РґРµРЅРЅР°СЏ РґРёСЃС‚Р°РЅС†РёСЏ. */
    private static float[] pathAt(float[] pts, float k) {
        int n = pts.length / 2;
        float total = 0f;
        float[] seg = new float[n - 1];
        for (int i = 0; i < n - 1; i++) {
            seg[i] = (float) Math.hypot(pts[2 * i + 2] - pts[2 * i], pts[2 * i + 3] - pts[2 * i + 1]);
            total += seg[i];
        }
        float d = clamp(k) * total;
        float travelled = d;
        for (int i = 0; i < n - 1; i++) {
            if (d <= seg[i] || i == n - 2) {
                float f = seg[i] > 1e-5f ? clamp(d / seg[i]) : 1f;
                float dx = pts[2 * i + 2] - pts[2 * i];
                float dzz = pts[2 * i + 3] - pts[2 * i + 1];
                return new float[]{
                        pts[2 * i] + dx * f,
                        pts[2 * i + 1] + dzz * f,
                        (float) Math.atan2(dx, dzz),
                        travelled
                };
            }
            d -= seg[i];
        }
        return new float[]{pts[0], pts[1], 0f, 0f};
    }

    /**
     * Р’С‹Р±РёСЂР°РµРј РѕРґРЅСѓ РёР· 4 СЃС‚РѕСЂРѕРЅ, РіРґРµ РїСѓС‚СЊ Р±СѓСЃРёРєР° Рё РјРµСЃС‚Рѕ СЃС‚РѕСЏРЅРєРё СЃРІРѕР±РѕРґРЅС‹ РѕС‚ Р±Р»РѕРєРѕРІ.
     * РџСЂРё СЂР°РІРµРЅСЃС‚РІРµ РїСЂРµРґРїРѕС‡РёС‚Р°РµРј СЃС‚Р°РІРёС‚СЊ Р±СѓСЃРёРє Р—Рђ С‚РµР»РѕРј РѕС‚РЅРѕСЃРёС‚РµР»СЊРЅРѕ РєР°РјРµСЂС‹, С‡С‚РѕР±С‹ РѕРЅ РЅРµ Р·Р°РєСЂС‹РІР°Р» СЃС†РµРЅСѓ.
     */
    private float pickVanYaw(Ctx c, float xv, float s, float w, float h) {
        if (mc.world == null) {
            return 0f;
        }
        Vec3d o = c.fx.position;
        BlockPos.Mutable bp = new BlockPos.Mutable();
        float best = 0f;
        float bestScore = Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float yaw = i * 90f;
            float yr = yaw * PI / 180f;
            float cs = cos(yr), sn = sin(yr);
            float score = 0f;
            for (float lz = -9f; lz <= 14f; lz += 1f) {
                for (int lx = -1; lx <= 1; lx++) {
                    for (float ly = 0.5f; ly < 2.7f; ly += 1f) {
                        float x = xv + lx * 0.85f * s;
                        float z = lz * s;
                        bp.set(o.x + x * cs + z * sn, o.y + ly * s, o.z - x * sn + z * cs);
                        if (!mc.world.getBlockState(bp).getCollisionShape(mc.world, bp).isEmpty()) {
                            score += Math.abs(lz) <= 3f ? 4f : 1f;
                        }
                    }
                }
            }
            // Р·РѕРЅР°, РіРґРµ С…РѕРґСЏС‚ Р±РѕР№С†С‹ Рё РЅРµСЃСѓС‚ С‚РµР»Рѕ
            for (float x = -w * 0.5f - 0.5f * s; x <= xv - 0.9f * s; x += 0.5f) {
                for (float z = -h - 0.6f * s; z <= 1.2f * s; z += 0.5f) {
                    bp.set(o.x + x * cs + z * sn, o.y + 0.5, o.z - x * sn + z * cs);
                    if (!mc.world.getBlockState(bp).getCollisionShape(mc.world, bp).isEmpty()) {
                        score += 2f;
                    }
                }
            }
            float vx = xv * cs, vz = -xv * sn;
            if (vx * c.cx + vz * c.cz > 0f) {
                score += 0.5f;
            }
            if (score < bestScore) {
                bestScore = score;
                best = yaw;
            }
        }
        return best;
    }

    /**
     * РњРѕРґРµР»СЊ Р±СѓСЃРёРєР° РІ Р»РѕРєР°Р»СЊРЅС‹С… РµРґРёРЅРёС†Р°С… (РїРµСЂРµРґ scale): РїРµСЂС‘Рґ СЃРјРѕС‚СЂРёС‚ РІ -Z, С†РµРЅС‚СЂ РЅР° Р·РµРјР»Рµ.
     * Р“СЂСѓР·РѕРІРѕР№ РѕС‚СЃРµРє РїРѕР»С‹Р№: РїРѕР», РєСЂС‹С€Р°, Р±РѕСЂС‚Р° Рё СЃРґРІРёР¶РЅРѕР№ РїСЂРѕС‘Рј, РІРЅСѓС‚СЂРё РѕР±С€РёРІРєР° Рё Р»Р°РІРєР°,
     * РїРѕСЌС‚РѕРјСѓ С‚РµР»Рѕ Рё Р±РѕР№С†С‹ СЂРµР°Р»СЊРЅРѕ Р·Р°С…РѕРґСЏС‚ РІРЅСѓС‚СЂСЊ, Р° РЅРµ СЃРєРІРѕР·СЊ СЃС‚РµРЅРєСѓ.
     */
    private static void drawVanModel(BufferBuilder b, Matrix4f m, int accent, float ddx, float ddz,
                                     float wheelAng, float head, float tail, float lamp, float a) {
        int paint = 0x282E39;
        int trim = 0x131519;
        int chrome = 0x9AA2AD;
        int glass = 0x111820;
        int lining = 0x4C4842;
        int floorC = 0x2F2C29;
        int benchC = 0x3C3732;
        int stripe = accent & 0xFFFFFF;

        // РєР°Р±РёРЅР°: РїСЂРѕС„РёР»СЊ РєР°РїРѕС‚ -> РЅР°РєР»РѕРЅРЅРѕРµ Р»РѕР±РѕРІРѕРµ -> РєСЂС‹С€Р°
        extrudeX(b, m, -0.95f, 0.95f, new float[]{
                -2.35f, 0.48f, -2.35f, 1.15f, -1.8f, 1.32f, -1.05f, 2.62f, -0.95f, 2.62f, -0.95f, 0.48f
        }, -1.3f, 1.0f, paint, a);

        // РіСЂСѓР·РѕРІРѕР№ РѕС‚СЃРµРє (РїРѕР»С‹Р№)
        vbox(b, m, -0.95f, 0.48f, -0.95f, 0.95f, 0.62f, 2.75f, paint, a);      // РїРѕР»
        vbox(b, m, -0.95f, 2.5f, -0.95f, 0.95f, 2.62f, 2.75f, paint, a);       // РєСЂС‹С€Р°
        vbox(b, m, 0.89f, 0.62f, -0.95f, 0.95f, 2.5f, 2.75f, paint, a);        // РїСЂР°РІС‹Р№ Р±РѕСЂС‚
        vbox(b, m, -0.89f, 0.62f, 2.69f, 0.89f, 2.5f, 2.75f, paint, a);        // Р·Р°Рґ
        vbox(b, m, -0.95f, 0.62f, -0.95f, -0.89f, 2.5f, -0.85f, paint, a);     // Р»РµРІС‹Р№ Р±РѕСЂС‚ РґРѕ РїСЂРѕС‘РјР°
        vbox(b, m, -0.95f, 0.62f, 1.05f, -0.89f, 2.5f, 2.75f, paint, a);       // Р»РµРІС‹Р№ Р±РѕСЂС‚ РїРѕСЃР»Рµ РїСЂРѕС‘РјР°

        // РѕР±С€РёРІРєР° СЃР°Р»РѕРЅР°
        vbox(b, m, -0.89f, 0.62f, -0.93f, 0.89f, 0.635f, 2.69f, floorC, a);
        for (float rz = -0.7f; rz < 2.6f; rz += 0.35f) {
            vbox(b, m, -0.85f, 0.635f, rz, 0.85f, 0.645f, rz + 0.05f, trim, a);
        }
        vbox(b, m, 0.87f, 0.635f, -0.93f, 0.89f, 2.48f, 2.69f, lining, a);
        vbox(b, m, -0.89f, 2.48f, -0.93f, 0.89f, 2.5f, 2.69f, lining, a);
        vbox(b, m, -0.89f, 0.635f, -0.95f, 0.89f, 2.48f, -0.93f, lining, a);
        vbox(b, m, -0.89f, 0.635f, 2.67f, 0.89f, 2.48f, 2.69f, lining, a);
        vbox(b, m, -0.89f, 0.635f, 1.05f, -0.87f, 2.48f, 2.67f, lining, a);
        vbox(b, m, 0.5f, 0.635f, 1.2f, 0.87f, 1.05f, 2.6f, benchC, a);         // Р»Р°РІРєР°
        vbox(b, m, 0.8f, 1.05f, 1.2f, 0.87f, 1.6f, 2.6f, benchC, a);
        vbox(b, m, -0.18f, 2.46f, 0.0f, 0.18f, 2.48f, 0.3f,
                mixColor(lining, 0xFFE9B8, clamp(lamp)), a);                    // РїР»Р°С„РѕРЅ

        // СЃРґРІРёР¶РЅР°СЏ РґРІРµСЂСЊ + РЅР°РїСЂР°РІР»СЏСЋС‰Р°СЏ
        vbox(b, m, -0.95f + ddx, 0.62f, -0.85f + ddz, -0.895f + ddx, 2.49f, 1.05f + ddz, paint, a);
        vbox(b, m, -0.958f + ddx, 1.0f, -0.85f + ddz, -0.95f + ddx, 1.08f, 1.05f + ddz, stripe, a);
        vbox(b, m, -0.975f + ddx, 1.42f, -0.72f + ddz, -0.95f + ddx, 1.47f, -0.55f + ddz, chrome, a);
        vbox(b, m, -0.957f, 1.6f, 1.05f, -0.95f, 1.63f, 2.65f, trim, a);
        vbox(b, m, -1.0f, 0.36f, -0.8f, -0.9f, 0.42f, 1.0f, trim, a);          // РїРѕРґРЅРѕР¶РєР°

        // С†РІРµС‚РЅР°СЏ РїРѕР»РѕСЃР° РїРѕ Р±РѕСЂС‚Р°Рј
        vbox(b, m, 0.95f, 1.0f, -2.33f, 0.958f, 1.08f, 2.73f, stripe, a);
        vbox(b, m, -0.958f, 1.0f, -2.33f, -0.95f, 1.08f, -0.85f, stripe, a);
        vbox(b, m, -0.958f, 1.0f, 1.05f, -0.95f, 1.08f, 2.73f, stripe, a);

        // Р»РѕР±РѕРІРѕРµ СЃС‚РµРєР»Рѕ (С‡СѓС‚СЊ РІС‹РЅРµСЃРµРЅРѕ РїРѕ РЅРѕСЂРјР°Р»Рё) + Р±Р»РёРє
        float nz = -0.866f * 0.012f, ny = 0.5f * 0.012f;
        float wz0 = -1.8f + 0.75f * 0.07f + nz, wy0 = 1.32f + 1.3f * 0.07f + ny;
        float wz1 = -1.8f + 0.75f * 0.93f + nz, wy1 = 1.32f + 1.3f * 0.93f + ny;
        int gc = shade(glass, 1f, a);
        quad(b, m, -0.84f, wy0, wz0, gc, 0.84f, wy0, wz0, gc, 0.84f, wy1, wz1, gc, -0.84f, wy1, wz1, gc);
        int hi = withAlpha(0x8FA3B8, 0.28f * a);
        float hz0 = wz0 + nz * 0.4f, hz1 = wz1 + nz * 0.4f, hy0 = wy0 + ny * 0.4f, hy1 = wy1 + ny * 0.4f;
        quad(b, m, -0.62f, hy0, hz0, hi, -0.42f, hy0, hz0, hi, -0.12f, hy1, hz1, hi, -0.32f, hy1, hz1, hi);
        vbox(b, m, -0.62f, 1.37f, -1.78f, -0.06f, 1.39f, -1.74f, trim, a);      // РґРІРѕСЂРЅРёРєРё
        vbox(b, m, 0.06f, 1.37f, -1.78f, 0.62f, 1.39f, -1.74f, trim, a);

        // Р±РѕРєРѕРІС‹Рµ СЃС‚С‘РєР»Р° РєР°Р±РёРЅС‹
        for (int sx = -1; sx <= 1; sx += 2) {
            float x = sx * 0.953f;
            quad(b, m, x, 1.5f, -1.66f, gc, x, 1.5f, -1.02f, gc, x, 2.42f, -1.02f, gc, x, 2.42f, -1.135f, gc);
            float xh = sx * 0.955f;
            quad(b, m, xh, 1.62f, -1.5f, hi, xh, 1.62f, -1.4f, hi, xh, 2.3f, -1.15f, hi, xh, 2.3f, -1.25f, hi);
            // Р·РµСЂРєР°Р»Р°
            vbox(b, m, sx * 0.95f, 1.72f, -1.6f, sx * 1.07f, 1.76f, -1.56f, trim, a);
            vbox(b, m, sx * 1.04f, 1.6f, -1.62f, sx * 1.13f, 1.9f, -1.54f, trim, a);
            // СЂРµР№Р»РёРЅРіРё
            vbox(b, m, sx * 0.7f, 2.62f, -0.6f, sx * 0.76f, 2.67f, 2.5f, trim, a);
        }

        // РјРѕСЂРґР°
        vbox(b, m, -0.98f, 0.34f, -2.47f, 0.98f, 0.6f, -2.3f, trim, a);        // Р±Р°РјРїРµСЂ
        vbox(b, m, -0.44f, 0.7f, -2.37f, 0.44f, 1.04f, -2.345f, trim, a);      // СЂРµС€С‘С‚РєР°
        for (float yb = 0.78f; yb < 1.0f; yb += 0.1f) {
            vbox(b, m, -0.42f, yb, -2.378f, 0.42f, yb + 0.025f, -2.366f, chrome, a);
        }
        int headC = mixColor(0x8C939B, 0xFFF6DC, clamp(head));
        for (int sx = -1; sx <= 1; sx += 2) {
            vbox(b, m, sx * 0.5f, 0.93f, -2.37f, sx * 0.86f, 1.1f, -2.345f, headC, a);
            vbox(b, m, sx * 0.62f, 0.42f, -2.48f, sx * 0.8f, 0.5f, -2.46f, mixColor(0x6A6F75, 0xFFF1CC, clamp(head) * 0.6f), a);
        }

        // РєРѕСЂРјР°
        int tailC = mixColor(0x5A0A0E, 0xFF3A28, clamp(tail));
        for (int sx = -1; sx <= 1; sx += 2) {
            vbox(b, m, sx * 0.8f, 1.05f, 2.745f, sx * 0.93f, 1.75f, 2.775f, tailC, a);
            quad(b, m, sx * 0.1f, 1.65f, 2.756f, gc, sx * 0.8f, 1.65f, 2.756f, gc,
                    sx * 0.8f, 2.35f, 2.756f, gc, sx * 0.1f, 2.35f, 2.756f, gc);
        }
        vbox(b, m, -0.008f, 0.62f, 2.75f, 0.008f, 2.5f, 2.758f, trim, a);
        vbox(b, m, 0.05f, 1.35f, 2.75f, 0.2f, 1.39f, 2.775f, chrome, a);
        vbox(b, m, -0.2f, 0.78f, 2.75f, 0.2f, 0.92f, 2.765f, 0xD8D6CC, a);     // РЅРѕРјРµСЂ
        vbox(b, m, -0.98f, 0.34f, 2.7f, 0.98f, 0.6f, 2.86f, trim, a);

        // РїРѕСЂРѕРіРё Рё Р°СЂРєРё
        vbox(b, m, -0.95f, 0.34f, -2.35f, 0.95f, 0.48f, -2.0f, trim, a);
        vbox(b, m, -0.95f, 0.34f, -1.2f, 0.95f, 0.48f, 1.5f, trim, a);
        vbox(b, m, -0.95f, 0.34f, 2.3f, 0.95f, 0.48f, 2.75f, trim, a);
        float[] wz = {-1.6f, 1.9f};
        for (float z : wz) {
            for (int sx = -1; sx <= 1; sx += 2) {
                vbox(b, m, sx * 0.95f, 0.72f, z - 0.46f, sx * 0.978f, 0.8f, z + 0.46f, trim, a);
                vbox(b, m, sx * 0.95f, 0.48f, z - 0.46f, sx * 0.978f, 0.72f, z - 0.4f, trim, a);
                vbox(b, m, sx * 0.95f, 0.48f, z + 0.4f, sx * 0.978f, 0.72f, z + 0.46f, trim, a);
                wheel(b, m, sx * 0.86f, 0.36f, z, 0.36f, 0.11f, wheelAng, a);
            }
        }
    }

    /** РљРѕР»РµСЃРѕ: РїСЂРѕС‚РµРєС‚РѕСЂ, Р±РѕРєРѕРІРёРЅР°, СѓС‚РѕРїР»РµРЅРЅС‹Р№ РґРёСЃРє СЃ РѕС‚РІРµСЂСЃС‚РёСЏРјРё (РІРёРґРЅРѕ РІСЂР°С‰РµРЅРёРµ). */
    private static void wheel(BufferBuilder b, Matrix4f m, float x, float y, float z,
                              float r, float hw, float ang, float a) {
        float so = Math.signum(x);
        float xo = x + so * hw, xi = x - so * hw;
        float ri = r * 0.64f;
        float xr = xo - so * 0.02f;
        int side = shade(0x17181B, 0.85f, a);
        int rimC = shade(0x959CA5, 1f, a);
        int rimD = shade(0x5B6169, 1f, a);
        int holeC = shade(0x0C0D0F, 1f, a);
        int seg = 18;
        for (int i = 0; i < seg; i++) {
            float a0 = TAU * i / seg, a1 = TAU * (i + 1) / seg;
            float c0 = cos(a0), s0 = sin(a0), c1 = cos(a1), s1 = sin(a1);
            float y0 = y + c0 * r, z0 = z + s0 * r, y1 = y + c1 * r, z1 = z + s1 * r;
            int tc = shade(0x1B1C1F, 0.55f + 0.35f * cos((a0 + a1) * 0.5f), a);
            quad(b, m, xi, y0, z0, tc, xo, y0, z0, tc, xo, y1, z1, tc, xi, y1, z1, tc);
            quad(b, m, xo, y0, z0, side, xo, y1, z1, side,
                    xo, y + c1 * ri, z + s1 * ri, side, xo, y + c0 * ri, z + s0 * ri, side);
            quad(b, m, xo, y + c0 * ri, z + s0 * ri, side, xo, y + c1 * ri, z + s1 * ri, side,
                    xr, y + c1 * ri, z + s1 * ri, side, xr, y + c0 * ri, z + s0 * ri, side);
            vertex(b, m, xr, y, z, rimC);
            vertex(b, m, xr, y + c0 * ri, z + s0 * ri, rimD);
            vertex(b, m, xr, y + c1 * ri, z + s1 * ri, rimD);
            vertex(b, m, xi, y, z, side);
            vertex(b, m, xi, y0, z0, side);
            vertex(b, m, xi, y1, z1, side);
        }
        float xh = xr + so * 0.004f;
        for (int k = 0; k < 5; k++) {
            float an = ang + TAU * k / 5f;
            float hy = y + cos(an) * r * 0.4f, hz = z + sin(an) * r * 0.4f, hs = r * 0.09f;
            quad(b, m, xh, hy - hs, hz - hs, holeC, xh, hy + hs, hz - hs, holeC,
                    xh, hy + hs, hz + hs, holeC, xh, hy - hs, hz + hs, holeC);
        }
        float hc = r * 0.1f;
        int hubC = shade(0xC4CAD2, 1f, a);
        quad(b, m, xh, y - hc, z - hc, hubC, xh, y + hc, z - hc, hubC, xh, y + hc, z + hc, hubC, xh, y - hc, z + hc, hubC);
    }

    /** РљРѕСЂРѕР±РєР° РїРѕ РґРІСѓРј СѓРіР»Р°Рј, СЃ РїСЂРѕСЃС‚С‹Рј РЅР°РїСЂР°РІР»РµРЅРЅС‹Рј Р·Р°С‚РµРЅРµРЅРёРµРј РіСЂР°РЅРµР№. */
    private static void vbox(BufferBuilder b, Matrix4f m, float x0, float y0, float z0,
                             float x1, float y1, float z1, int col, float a) {
        if (a <= 0.004f) return;
        int top = shade(col, 1.0f, a);
        int bot = shade(col, 0.42f, a);
        int sx = shade(col, 0.8f, a);
        int sz = shade(col, 0.64f, a);
        quad(b, m, x0, y1, z0, top, x0, y1, z1, top, x1, y1, z1, top, x1, y1, z0, top);
        quad(b, m, x0, y0, z0, bot, x1, y0, z0, bot, x1, y0, z1, bot, x0, y0, z1, bot);
        quad(b, m, x1, y0, z0, sx, x1, y1, z0, sx, x1, y1, z1, sx, x1, y0, z1, sx);
        quad(b, m, x0, y0, z0, sx, x0, y0, z1, sx, x0, y1, z1, sx, x0, y1, z0, sx);
        quad(b, m, x0, y0, z1, sz, x1, y0, z1, sz, x1, y1, z1, sz, x0, y1, z1, sz);
        quad(b, m, x0, y0, z0, sz, x0, y1, z0, sz, x1, y1, z0, sz, x1, y0, z0, sz);
    }

    /** Р’С‹РґР°РІР»РёРІР°РЅРёРµ РїСЂРѕС„РёР»СЏ (РїР°СЂС‹ z,y) РІРґРѕР»СЊ X. (cz, cy) вЂ” С‚РѕС‡РєР°, РёР· РєРѕС‚РѕСЂРѕР№ РІРёРґРµРЅ РІРµСЃСЊ РєРѕРЅС‚СѓСЂ. */
    private static void extrudeX(BufferBuilder b, Matrix4f m, float x0, float x1, float[] zy,
                                 float cz, float cy, int col, float a) {
        if (a <= 0.004f) return;
        int n = zy.length / 2;
        int cap = shade(col, 0.8f, a);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float z1 = zy[2 * i], y1 = zy[2 * i + 1], z2 = zy[2 * j], y2 = zy[2 * j + 1];
            vertex(b, m, x0, cy, cz, cap);
            vertex(b, m, x0, y1, z1, cap);
            vertex(b, m, x0, y2, z2, cap);
            vertex(b, m, x1, cy, cz, cap);
            vertex(b, m, x1, y1, z1, cap);
            vertex(b, m, x1, y2, z2, cap);
            float ny = -(z2 - z1), nzz = y2 - y1;
            float mz = (z1 + z2) * 0.5f - cz, my = (y1 + y2) * 0.5f - cy;
            if (ny * my + nzz * mz < 0f) { ny = -ny; nzz = -nzz; }
            float l = (float) Math.sqrt(ny * ny + nzz * nzz);
            if (l < 1e-6f) continue;
            ny /= l;
            float k = ny >= 0f ? 0.64f + 0.36f * ny : 0.64f + 0.22f * ny;
            int cc = shade(col, k, a);
            quad(b, m, x0, y1, z1, cc, x1, y1, z1, cc, x1, y2, z2, cc, x0, y2, z2, cc);
        }
    }

    /** РњСЏРіРєР°СЏ РїСЂСЏРјРѕСѓРіРѕР»СЊРЅР°СЏ С‚РµРЅСЊ РЅР° Р·РµРјР»Рµ. */
    private static void softRect(BufferBuilder b, Matrix4f m, float x0, float z0, float x1, float z1,
                                 float f, float y, int col, float alpha) {
        int in = withAlpha(col, alpha), out = withAlpha(col, 0f);
        quad(b, m, x0, y, z0, in, x1, y, z0, in, x1, y, z1, in, x0, y, z1, in);
        quad(b, m, x0 - f, y, z0 - f, out, x1 + f, y, z0 - f, out, x1, y, z0, in, x0, y, z0, in);
        quad(b, m, x0, y, z1, in, x1, y, z1, in, x1 + f, y, z1 + f, out, x0 - f, y, z1 + f, out);
        quad(b, m, x0 - f, y, z0 - f, out, x0, y, z0, in, x0, y, z1, in, x0 - f, y, z1 + f, out);
        quad(b, m, x1, y, z0, in, x1 + f, y, z0 - f, out, x1 + f, y, z1 + f, out, x1, y, z1, in);
    }

    /**
     * Р‘РѕРµС†. РљР°Р¶РґР°СЏ РєРѕРЅРµС‡РЅРѕСЃС‚СЊ РєСЂСѓС‚РёС‚СЃСЏ РІРѕРєСЂСѓРі РЎР’РћР•Р“Рћ С€Р°СЂРЅРёСЂР° (Р±РµРґСЂРѕ / РїР»РµС‡Рѕ) Рё СЃРІРёСЃР°РµС‚ РѕС‚ РЅРµРіРѕ,
     * РїРѕСЌС‚РѕРјСѓ РЅРѕРіРё РЅРµ С‚РѕСЂС‡Р°С‚ СЃРєРІРѕР·СЊ С‚РѕСЂСЃ. РџРµСЂС‘Рґ = +Z. Р‘С‘РґСЂР° РѕРїСѓСЃРєР°СЋС‚СЃСЏ РЅР° РІРµР»РёС‡РёРЅСѓ РїРѕРґСЉС‘РјР° СЃС‚РѕРї,
     * С‡С‚РѕР±С‹ РѕР±Рµ РЅРѕРіРё РІСЃРµРіРґР° СЃС‚РѕСЏР»Рё РЅР° Р·РµРјР»Рµ. РќР° Р»РµРІРѕРј РїР»РµС‡Рµ СѓРєСЂ С€РµРІСЂРѕРЅ, РЅР° Р·Р°РїСЏСЃС‚СЊСЏС… РїРѕР»РѕСЃС‹.
     *   phase вЂ” С„Р°Р·Р° С€Р°РіР°, amp вЂ” СЃРёР»Р° С€Р°РіР° (0 = СЃС‚РѕРёС‚), arm вЂ” 0 СЂСѓРєРё РІРґРѕР»СЊ С‚РµР»Р°, 1 РґРµСЂР¶РёС‚ РїРµСЂРµРґ СЃРѕР±РѕР№.
     */
    private void drawSoldier(BufferBuilder b, Ctx c, int accent, float phase, float amp, float arm, float alpha) {
        if (alpha <= 0.004f) return;
        int uniform = 0x3B4432;
        int vest = 0x242A1E;
        int boots = 0x151713;
        int gloves = 0x1B1D19;
        int helmet = 0x303828;
        int mask = 0x17191B;
        int goggles = 0x0E1318;
        int yellow = 0xFFC500;   // СѓРєСЂ Р¶С‘Р»С‚С‹Р№
        int blue = 0x0057B8;     // СѓРєСЂ СЃРёРЅРёР№
        int patch = accent & 0xFFFFFF;

        float legA = sin(phase) * 0.5f * amp;
        float drop = 0.78f * (1f - cos(Math.abs(legA)));

        c.m.push();
        c.m.translate(0f, -drop, 0f);

        for (int side = -1; side <= 1; side += 2) {
            c.m.push();
            c.m.translate(side * 0.105f, 0.78f, 0f);
            c.m.multiply(RotationAxis.POSITIVE_X.rotation(legA * side));
            Matrix4f lm = c.mat();
            vbox(b, lm, -0.08f, -0.66f, -0.09f, 0.08f, 0.02f, 0.09f, uniform, alpha);
            vbox(b, lm, -0.085f, -0.78f, -0.11f, 0.085f, -0.64f, 0.15f, boots, alpha);
            c.m.pop();
        }

        Matrix4f m = c.mat();
        vbox(b, m, -0.2f, 0.76f, -0.115f, 0.2f, 1.44f, 0.115f, uniform, alpha);
        vbox(b, m, -0.215f, 0.93f, -0.135f, 0.215f, 1.4f, 0.14f, vest, alpha);
        vbox(b, m, -0.205f, 0.76f, -0.125f, 0.205f, 0.84f, 0.125f, boots, alpha);
        vbox(b, m, -0.15f, 1.0f, 0.14f, -0.03f, 1.15f, 0.18f, vest, alpha);
        vbox(b, m, 0.03f, 1.0f, 0.14f, 0.15f, 1.15f, 0.18f, vest, alpha);
        vbox(b, m, -0.13f, 1.44f, -0.13f, 0.13f, 1.74f, 0.13f, mask, alpha);
        vbox(b, m, -0.15f, 1.65f, -0.155f, 0.15f, 1.84f, 0.15f, helmet, alpha);
        vbox(b, m, -0.155f, 1.63f, -0.16f, 0.155f, 1.67f, 0.16f, helmet, alpha);
        vbox(b, m, -0.11f, 1.56f, 0.13f, 0.11f, 1.62f, 0.148f, goggles, alpha);

        // СѓРєСЂ С€РµРІСЂРѕРЅ РЅР° Р»РµРІРѕРј РїР»РµС‡Рµ: Р¶С‘Р»С‚Рѕ-СЃРёРЅСЏСЏ СЃС‚СЂРµР»Р° РІРІРµСЂС…
        vbox(b, m, -0.215f, 1.15f, 0.06f, -0.15f, 1.32f, 0.12f, yellow, alpha);
        vbox(b, m, -0.165f, 1.15f, 0.065f, -0.2f, 1.32f, 0.125f, blue, alpha);
        vbox(b, m, -0.22f, 1.3f, 0.055f, -0.14f, 1.315f, 0.075f, blue, alpha);

        // РїР°С‚С‡ Р°РєС†РµРЅС‚Р° РЅР° Р¶РёР»РµС‚Рµ
        vbox(b, m, -0.13f, 1.0f, 0.145f, -0.03f, 1.08f, 0.165f, patch, alpha);

        float hold = -1.05f * arm;
        for (int side = -1; side <= 1; side += 2) {
            float swing = -legA * side * 0.9f * (1f - arm);
            c.m.push();
            c.m.translate(side * 0.27f, 1.38f, 0f);
            c.m.multiply(RotationAxis.POSITIVE_X.rotation(hold + swing));
            c.m.multiply(RotationAxis.POSITIVE_Z.rotation(side * 0.06f * (1f - arm)));
            Matrix4f am = c.mat();
            vbox(b, am, -0.065f, -0.6f, -0.07f, 0.065f, 0.05f, 0.07f, uniform, alpha);
            vbox(b, am, -0.06f, -0.7f, -0.065f, 0.06f, -0.58f, 0.065f, gloves, alpha);
            // РЅР°РїСѓР»СЊСЃРЅРёРєРё (С‚Р°РєС‚РёС‡РµСЃРєРёРµ РїРѕР»РѕСЃС‹ РЅР° Р·Р°РїСЏСЃС‚СЊРµ)
            vbox(b, am, side * 0.042f, -0.6f, -0.065f, side * 0.072f, -0.55f, 0.065f, yellow, alpha);
            vbox(b, am, side * 0.055f, -0.57f, -0.062f, side * 0.072f, -0.545f, 0.062f, blue, alpha);
            c.m.pop();
        }
        c.m.pop();
    }

    private static void additive() {
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }

    private static void translucent() {
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static BufferBuilder beginColor() {
        return Tessellator.getInstance().begin(
                VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
    }

    private static BufferBuilder beginGlow() {
        return Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    private static void drawColor(BufferBuilder buffer) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private static void drawGlow(BufferBuilder buffer) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, GLOW_TEX);
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void glow(BufferBuilder b, Ctx c, float x, float y, float z,
                      float w, float h, int color, float alpha) {
        glow(b, c.m, c.e, x, y, z, w, h, color, alpha);
    }

    /** Р  РІР‚С”Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р  Р’В°, Р  Р вЂ Р РЋР С“Р  Р’ВµР  РЎвЂ“Р  РўвЂР  Р’В° Р  РЎвЂ”Р  РЎвЂўР  Р вЂ Р РЋРІР‚ВР РЋР вЂљР  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р  Р’В°Р РЋР РЏ Р  РЎвЂќ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР  Р’Вµ: Р РЋР РЏР РЋР вЂљР  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р РЋРІР‚ Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р РЋР вЂљ, Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР  Р’В·Р РЋР вЂљР  Р’В°Р РЋРІР‚РЋР  Р вЂ¦Р РЋРІР‚в„–Р  Р’Вµ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР РЏ. */
    private void beam(BufferBuilder b, Ctx c,
                      float ax, float ay, float az,
                      float bx, float by, float bz,
                      float width, int color, float alpha) {
        if (alpha <= 0.002f) return;
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float vx = c.cx - (ax + bx) * 0.5f;
        float vy = c.cy - (ay + by) * 0.5f;
        float vz = c.cz - (az + bz) * 0.5f;
        float sx = dy * vz - dz * vy;
        float sy = dz * vx - dx * vz;
        float sz = dx * vy - dy * vx;
        float len = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (len < 1e-5f) return;
        float k = width * 0.5f / len;
        sx *= k; sy *= k; sz *= k;

        Matrix4f m = c.mat();
        int coreC = withAlpha(mixColor(color, 0xFFFFFF, 0.55f), alpha);
        int edge = withAlpha(color, 0f);

        quad(b, m, ax, ay, az, coreC, bx, by, bz, coreC,
                bx + sx, by + sy, bz + sz, edge, ax + sx, ay + sy, az + sz, edge);
        quad(b, m, ax, ay, az, coreC, bx, by, bz, coreC,
                bx - sx, by - sy, bz - sz, edge, ax - sx, ay - sy, az - sz, edge);
    }

    /** Р  РЎС™Р РЋР РЏР  РЎвЂ“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РўвЂР  РЎвЂР РЋР С“Р  РЎвЂќ, Р  РЎвЂ”Р  РЎвЂўР  Р вЂ Р РЋРІР‚ВР РЋР вЂљР  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  РЎвЂќ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР  Р’Вµ (Р  РўвЂР  Р’В»Р РЋР РЏ Р РЋРІР‚РЋР  Р’ВµР РЋР вЂљР  Р вЂ¦Р  РЎвЂўР РЋРІР‚С™Р РЋРІР‚в„– Р  РЎвЂ Р  РЎвЂўР  Р’В±Р  Р’В»Р  Р’В°Р  РЎвЂќР  РЎвЂўР  Р вЂ ). */
    private void softDisc(BufferBuilder b, Ctx c, float x, float y, float z,
                          float radius, float feather, int color, float alpha, int seg) {
        if (alpha <= 0.002f || radius <= 0f) return;
        c.m.push();
        c.m.translate(x, y, z);
        c.m.multiply(c.e.getCamera().getRotation());
        Matrix4f m = c.mat();
        int solid = withAlpha(color, alpha);
        int clear = withAlpha(color, 0f);
        float outer = radius + feather;
        for (int i = 0; i < seg; i++) {
            float a = TAU * i / seg;
            float n = TAU * (i + 1) / seg;
            float ca = cos(a), sa = sin(a), cn = cos(n), sn = sin(n);
            vertex(b, m, 0f, 0f, 0f, solid);
            vertex(b, m, ca * radius, sa * radius, 0f, solid);
            vertex(b, m, cn * radius, sn * radius, 0f, solid);
            quad(b, m, ca * radius, sa * radius, 0f, solid,
                    ca * outer, sa * outer, 0f, clear,
                    cn * outer, sn * outer, 0f, clear,
                    cn * radius, sn * radius, 0f, solid);
        }
        c.m.pop();
    }

    /** Р  РЎв„ўР  РЎвЂўР  Р’В»Р РЋР Р‰Р РЋРІР‚ Р  РЎвЂў, Р  РЎвЂ”Р  РЎвЂўР  Р вЂ Р РЋРІР‚ВР РЋР вЂљР  Р вЂ¦Р РЋРЎвЂњР РЋРІР‚С™Р  РЎвЂўР  Р’Вµ Р  РЎвЂќ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР  Р’Вµ. */
    private void billboardRing(BufferBuilder b, Ctx c, float x, float y, float z,
                               float radius, float width, float start, float length,
                               int color, float alpha, int seg) {
        if (alpha <= 0.002f || radius <= 0f) return;
        c.m.push();
        c.m.translate(x, y, z);
        c.m.multiply(c.e.getCamera().getRotation());
        c.m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
        ring(b, c.mat(), radius, width, 0f, start, length, color, alpha, seg);
        c.m.pop();
    }

    /** Р  Р Р‹Р  Р вЂ Р  Р’ВµР РЋРІР‚С™Р РЋР РЏР РЋРІР‚В°Р  Р’В°Р РЋР РЏР РЋР С“Р РЋР РЏ Р  Р’В»Р  РЎвЂР  Р вЂ¦Р  РЎвЂР РЋР РЏ, Р  Р’В»Р  Р’ВµР  Р’В¶Р  Р’В°Р РЋРІР‚В°Р  Р’В°Р РЋР РЏ Р  Р вЂ¦Р  Р’В° Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  Р’Вµ. */
    private static void flatLine(BufferBuilder b, Matrix4f m,
                                 float ax, float az, float bx, float bz, float y,
                                 float width, int color, float alpha) {
        if (alpha <= 0.002f) return;
        float dx = bx - ax, dz = bz - az;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-5f) return;
        float nx = -dz / len * width * 0.5f;
        float nz = dx / len * width * 0.5f;
        int coreC = withAlpha(mixColor(color, 0xFFFFFF, 0.5f), alpha);
        int edge = withAlpha(color, 0f);
        quad(b, m, ax, y, az, coreC, bx, y, bz, coreC, bx + nx, y, bz + nz, edge, ax + nx, y, az + nz, edge);
        quad(b, m, ax, y, az, coreC, bx, y, bz, coreC, bx - nx, y, bz - nz, edge, ax - nx, y, az - nz, edge);
    }

    /** Р  РЎСџР  Р’В»Р  РЎвЂўР РЋР С“Р  РЎвЂќР  РЎвЂР  РІвЂћвЂ“ Р  РўвЂР  РЎвЂР РЋР С“Р  РЎвЂќ Р  Р вЂ¦Р  Р’В° Р  Р’В·Р  Р’ВµР  РЎВР  Р’В»Р  Р’Вµ Р РЋР С“ Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  РўвЂР  РЎвЂР  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р  РЎвЂўР  РЎВ Р  РЎвЂўР РЋРІР‚С™ Р РЋРІР‚ Р  Р’ВµР  Р вЂ¦Р РЋРІР‚С™Р РЋР вЂљР  Р’В° Р  РЎвЂќ Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР вЂ№. */
    private static void flatDisc(BufferBuilder b, Matrix4f m, float radius, float y,
                                 int inner, float innerA, int outer, float outerA, int seg) {
        if (radius <= 0f) return;
        int ci = withAlpha(inner, innerA);
        int co = withAlpha(outer, outerA);
        for (int i = 0; i < seg; i++) {
            float a = TAU * i / seg;
            float n = TAU * (i + 1) / seg;
            vertex(b, m, 0f, y, 0f, ci);
            vertex(b, m, cos(a) * radius, y, sin(a) * radius, co);
            vertex(b, m, cos(n) * radius, y, sin(n) * radius, co);
        }
    }

    /** Р  РЎв„ўР РЋРЎвЂњР  Р’В± Р РЋР С“ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋРІР‚в„–Р  РЎВ Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р  Р’ВµР  Р вЂ¦Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’ВµР  РЎВ Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р  Р’ВµР  РІвЂћвЂ“. */
    private static void box(BufferBuilder b, Matrix4f m, float hx, float hy, float hz,
                            int color, float alpha) {
        int top = shade(color, 1f, alpha);
        int bot = shade(color, 0.45f, alpha);
        int xs = shade(color, 0.78f, alpha);
        int zs = shade(color, 0.62f, alpha);
        quad(b, m, -hx, hy, -hz, top, -hx, hy, hz, top, hx, hy, hz, top, hx, hy, -hz, top);
        quad(b, m, -hx, -hy, -hz, bot, hx, -hy, -hz, bot, hx, -hy, hz, bot, -hx, -hy, hz, bot);
        quad(b, m, hx, -hy, -hz, xs, hx, hy, -hz, xs, hx, hy, hz, xs, hx, -hy, hz, xs);
        quad(b, m, -hx, -hy, -hz, xs, -hx, -hy, hz, xs, -hx, hy, hz, xs, -hx, hy, -hz, xs);
        quad(b, m, -hx, -hy, hz, zs, hx, -hy, hz, zs, hx, hy, hz, zs, -hx, hy, hz, zs);
        quad(b, m, -hx, -hy, -hz, zs, -hx, hy, -hz, zs, hx, hy, -hz, zs, hx, -hy, -hz, zs);
    }

    /** Р  РЎв„ўР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»: Р  Р вЂ¦Р  РЎвЂР  Р’В¶Р  Р вЂ¦Р  РЎвЂР  РІвЂћвЂ“ Р РЋРІвЂљВ¬Р  РЎвЂ”Р  РЎвЂР  Р’В»Р РЋР Р‰, Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋРІР‚ВР  Р вЂ¦Р  РЎвЂўР  Р’Вµ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  РЎвЂў, Р  Р вЂ Р  Р’ВµР РЋР вЂљР РЋРІР‚В¦Р  Р вЂ¦Р  РЎвЂР  РІвЂћвЂ“ Р РЋРІвЂљВ¬Р  РЎвЂ”Р  РЎвЂР  Р’В»Р РЋР Р‰. */
    private static void crystalMesh(BufferBuilder b, Matrix4f m, float radius, float height,
                                    int sides, float twist, int color, int accent,
                                    float alpha, float shimmer) {
        if (alpha <= 0.002f) return;
        float tipB = Math.min(radius * 0.9f, height * 0.3f);
        float tipT = height * 0.28f;
        float y1 = tipB;
        float y2 = Math.max(y1, height - tipT);
        float topR = radius * 0.92f;
        int tip = withAlpha(mixColor(accent, 0xFFFFFF, 0.6f), alpha);

        for (int i = 0; i < sides; i++) {
            float a0 = twist + TAU * i / sides;
            float a1 = twist + TAU * (i + 1) / sides;
            float x0 = cos(a0), z0 = sin(a0), x1 = cos(a1), z1 = sin(a1);

            float lum = 0.55f + 0.45f * (0.5f + 0.5f * sin(i * 2.3f + shimmer));
            int base = mixColor(color, accent, (i % 2) * 0.45f);
            int face = shade(base, lum, alpha);
            int faceTop = shade(mixColor(base, 0xFFFFFF, 0.35f), Math.min(1f, lum + 0.15f), alpha);
            int dim = shade(base, lum * 0.6f, alpha);

            vertex(b, m, 0f, 0f, 0f, dim);
            vertex(b, m, x0 * radius, y1, z0 * radius, face);
            vertex(b, m, x1 * radius, y1, z1 * radius, face);

            quad(b, m, x0 * radius, y1, z0 * radius, face,
                    x1 * radius, y1, z1 * radius, face,
                    x1 * topR, y2, z1 * topR, faceTop,
                    x0 * topR, y2, z0 * topR, faceTop);

            vertex(b, m, x0 * topR, y2, z0 * topR, faceTop);
            vertex(b, m, x1 * topR, y2, z1 * topR, faceTop);
            vertex(b, m, 0f, height, 0f, tip);
        }
    }

    private static void quad(BufferBuilder b, Matrix4f m,
                             float x1, float y1, float z1, int c1,
                             float x2, float y2, float z2, int c2,
                             float x3, float y3, float z3, int c3,
                             float x4, float y4, float z4, int c4) {
        vertex(b, m, x1, y1, z1, c1);
        vertex(b, m, x2, y2, z2, c2);
        vertex(b, m, x3, y3, z3, c3);
        vertex(b, m, x1, y1, z1, c1);
        vertex(b, m, x3, y3, z3, c3);
        vertex(b, m, x4, y4, z4, c4);
    }

    private static int shade(int color, float k, float alpha) {
        int r = Math.min(255, (int) (((color >> 16) & 255) * k));
        int g = Math.min(255, (int) (((color >> 8) & 255) * k));
        int bl = Math.min(255, (int) ((color & 255) * k));
        return withAlpha((r << 16) | (g << 8) | bl, alpha);
    }

    private static float sin(float v) {
        return (float) Math.sin(v);
    }

    private static float cos(float v) {
        return (float) Math.cos(v);
    }

    private static float frac(float v) {
        return v - (float) Math.floor(v);
    }

    private static float hash(float n) {
        float x = (float) Math.sin(n * 127.1f) * 43758.547f;
        return x - (float) Math.floor(x);
    }

    // =====================================================================
    //  3D-Р  РЎВР  Р’ВµР РЋРІвЂљВ¬Р  РЎвЂ Р РЋР С“ Р  Р вЂ¦Р  Р’В°Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР РЋР РЏР РЋРІР‚В°Р  РЎвЂР  РЎВ Р  РЎвЂўР РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚В°Р  Р’ВµР  Р вЂ¦Р  РЎвЂР  Р’ВµР  РЎВ: Р  РўвЂР  РЎвЂР РЋРІР‚С›Р РЋРІР‚С›Р РЋРЎвЂњР  Р’В·, Р РЋРІР‚С›Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  Р’ВµР  Р’В»Р РЋР Р‰ Р  РЎвЂ Р  Р’В±Р  Р’В»Р  РЎвЂР  РЎвЂќ Р  РЎвЂќ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР  Р’Вµ
    // =====================================================================

    private static Vector3f pt(Quaternionf q, float x, float y, float z,
                               float px, float py, float pz) {
        Vector3f v = new Vector3f(x, y, z);
        if (q != null) {
            q.transform(v);
        }
        return v.add(px, py, pz);
    }

    /** Р  РЎС›Р РЋР вЂљР  Р’ВµР РЋРЎвЂњР  РЎвЂ“Р  РЎвЂўР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  РЎвЂР  РЎвЂќ, Р  Р’В·Р  Р’В°Р РЋРІР‚С™Р  Р’ВµР  Р вЂ¦Р РЋРІР‚ВР  Р вЂ¦Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р  РЎвЂ”Р  РЎвЂў Р  Р вЂ¦Р  РЎвЂўР РЋР вЂљР  РЎВР  Р’В°Р  Р’В»Р  РЎвЂ. glass = Р  РЎвЂќР РЋР вЂљР  Р’В°Р РЋР РЏ Р РЋР РЏР РЋР вЂљР РЋРІР‚РЋР  Р’Вµ Р  РЎвЂ Р  РЎвЂ”Р  Р’В»Р  РЎвЂўР РЋРІР‚С™Р  Р вЂ¦Р  Р’ВµР  Р’Вµ (Р РЋР РЉР РЋРІР‚С›Р РЋРІР‚С›Р  Р’ВµР  РЎвЂќР РЋРІР‚С™ Р  Р’В¤Р РЋР вЂљР  Р’ВµР  Р вЂ¦Р  Р’ВµР  Р’В»Р РЋР РЏ). */
    private void litTri(BufferBuilder b, Ctx c, Matrix4f m,
                        Vector3f p0, Vector3f p1, Vector3f p2,
                        int base, int spec, float alpha, float gloss, boolean glass) {
        float ux = p1.x - p0.x, uy = p1.y - p0.y, uz = p1.z - p0.z;
        float vx = p2.x - p0.x, vy = p2.y - p0.y, vz = p2.z - p0.z;
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nl < 1e-8f) return;
        nx /= nl; ny /= nl; nz /= nl;

        float ex = c.cx - (p0.x + p1.x + p2.x) / 3f;
        float ey = c.cy - (p0.y + p1.y + p2.y) / 3f;
        float ez = c.cz - (p0.z + p1.z + p2.z) / 3f;
        float el = (float) Math.sqrt(ex * ex + ey * ey + ez * ez) + 1e-6f;
        ex /= el; ey /= el; ez /= el;

        float nv = nx * ex + ny * ey + nz * ez;
        if (nv < 0f) {
            nx = -nx; ny = -ny; nz = -nz;
            nv = -nv;
        }
        float nd = nx * LX + ny * LY + nz * LZ;
        float diff = 0.34f + 0.66f * Math.max(0f, nd);

        float hx = LX + ex, hy = LY + ey, hz = LZ + ez;
        float hl = (float) Math.sqrt(hx * hx + hy * hy + hz * hz) + 1e-6f;
        float nh = Math.max(0f, (nx * hx + ny * hy + nz * hz) / hl);
        float sp = (float) Math.pow(nh, 36.0) * gloss;
        float fres = 1f - nv;
        fres = fres * fres * fres;

        int lit = mixColor(shade(base, diff, 1f), spec, clamp(sp + fres * (glass ? 0.7f : 0.2f)));
        float a = glass ? alpha * (0.45f + 0.55f * fres + sp * 0.4f) : alpha;
        int rgba = withAlpha(lit, a);

        vertex(b, m, p0.x, p0.y, p0.z, rgba);
        vertex(b, m, p1.x, p1.y, p1.z, rgba);
        vertex(b, m, p2.x, p2.y, p2.z, rgba);
    }

    /** Р  РЎСљР  Р’ВµР РЋР вЂљР  РЎвЂўР  Р вЂ Р  Р вЂ¦Р РЋРІР‚в„–Р  РІвЂћвЂ“ Р РЋР С“Р  Р’В°Р  РЎВР  РЎвЂўР РЋРІР‚ Р  Р вЂ Р  Р’ВµР РЋРІР‚С™/Р  РЎвЂўР  Р’В±Р  Р’В»Р  РЎвЂўР  РЎВР  РЎвЂўР  РЎвЂќ: Р  Р’В±Р  РЎвЂР  РЎвЂ”Р  РЎвЂР РЋР вЂљР  Р’В°Р  РЎВР  РЎвЂР  РўвЂР  Р’В° Р РЋР С“Р  РЎвЂў Р РЋР С“Р  Р’В»Р РЋРЎвЂњР РЋРІР‚РЋР  Р’В°Р  РІвЂћвЂ“Р  Р вЂ¦Р РЋРІР‚в„–Р  РЎВР  РЎвЂ Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋР РЏР  РЎВР  РЎвЂ. */
    private void gem(BufferBuilder b, Ctx c, float px, float py, float pz, Quaternionf q,
                     float r, float len, int sides, float seed,
                     int base, int spec, float alpha, float gloss, boolean glass) {
        if (alpha <= 0.002f || r <= 0.0005f) return;
        Matrix4f m = c.mat();
        Vector3f top = pt(q, 0f, len * 0.5f, 0f, px, py, pz);
        Vector3f bot = pt(q, 0f, -len * (0.3f + 0.2f * hash(seed)), 0f, px, py, pz);
        Vector3f[] ring = new Vector3f[sides];
        for (int i = 0; i < sides; i++) {
            float a = TAU * i / sides + (hash(seed + i * 1.7f) - 0.5f) * 0.7f;
            float rr = r * (0.65f + 0.5f * hash(seed * 1.3f + i * 3.1f));
            float yy = (hash(seed * 2.3f + i) - 0.5f) * len * 0.25f;
            ring[i] = pt(q, cos(a) * rr, yy, sin(a) * rr, px, py, pz);
        }
        for (int i = 0; i < sides; i++) {
            int j = (i + 1) % sides;
            litTri(b, c, m, top, ring[i], ring[j], base, spec, alpha, gloss, glass);
            litTri(b, c, m, bot, ring[j], ring[i], base, spec, alpha, gloss, glass);
        }
    }

    /** Р  РЎв„ўР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р  РЎвЂР РЋРІР‚РЋР  Р’ВµР РЋР С“Р  РЎвЂќР  Р’В°Р РЋР РЏ Р  РЎвЂ”Р РЋР вЂљР  РЎвЂР  Р’В·Р  РЎВР  Р’В° Р РЋР С“ Р  Р’В·Р  Р’В°Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР вЂљР РЋРІР‚ВР  Р вЂ¦Р  Р вЂ¦Р РЋРІР‚в„–Р  РЎВР  РЎвЂ Р  РЎвЂќР  РЎвЂўР  Р вЂ¦Р РЋРІР‚ Р  Р’В°Р  РЎВР  РЎвЂ, Р  РЎвЂўР РЋР С“Р  Р вЂ Р  Р’ВµР РЋРІР‚В°Р РЋРІР‚ВР  Р вЂ¦Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р  РЎвЂ”Р  РЎвЂў Р  РЎвЂ“Р РЋР вЂљР  Р’В°Р  Р вЂ¦Р РЋР РЏР  РЎВ. */
    private void prism(BufferBuilder b, Ctx c, float px, float py, float pz, Quaternionf q,
                       float r, float height, int sides, float twist,
                       int base, int spec, float alpha, float gloss, boolean glass) {
        if (alpha <= 0.002f || height <= 0.001f || r <= 0.001f) return;
        Matrix4f m = c.mat();
        float tipB = Math.min(r * 0.8f, height * 0.25f);
        float tipT = Math.min(r * 1.6f, height * 0.3f);
        Vector3f bottom = pt(q, 0f, 0f, 0f, px, py, pz);
        Vector3f top = pt(q, 0f, height, 0f, px, py, pz);
        Vector3f[] lo = new Vector3f[sides];
        Vector3f[] hi = new Vector3f[sides];
        for (int i = 0; i < sides; i++) {
            float a = twist + TAU * i / sides;
            lo[i] = pt(q, cos(a) * r, tipB, sin(a) * r, px, py, pz);
            hi[i] = pt(q, cos(a) * r * 0.9f, height - tipT, sin(a) * r * 0.9f, px, py, pz);
        }
        for (int i = 0; i < sides; i++) {
            int j = (i + 1) % sides;
            int face = mixColor(base, spec, (i % 2) * 0.18f);
            litTri(b, c, m, bottom, lo[j], lo[i], face, spec, alpha, gloss, glass);
            litTri(b, c, m, lo[i], lo[j], hi[j], face, spec, alpha, gloss, glass);
            litTri(b, c, m, lo[i], hi[j], hi[i], face, spec, alpha, gloss, glass);
            litTri(b, c, m, hi[i], hi[j], top, face, spec, alpha, gloss, glass);
        }
    }

    private static final class Ctx {
        final MatrixStack m;
        final Event3DRender e;
        final ActiveEffect fx;
        final float p;
        final float t;
        /** Р  РЎвЂ”Р  РЎвЂўР  Р’В·Р  РЎвЂР РЋРІР‚ Р  РЎвЂР РЋР РЏ Р  РЎвЂќР  Р’В°Р  РЎВР  Р’ВµР РЋР вЂљР РЋРІР‚в„– Р  РЎвЂўР РЋРІР‚С™Р  Р вЂ¦Р  РЎвЂўР РЋР С“Р  РЎвЂР РЋРІР‚С™Р  Р’ВµР  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  РЎвЂў Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР РЋРЎвЂњР РЋРІР‚В°Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р  Р вЂ¦Р  Р’В°Р РЋРІР‚РЋР  Р’В°Р  Р’В»Р  Р’В° Р  РЎвЂќР  РЎвЂўР  РЎвЂўР РЋР вЂљР  РўвЂР  РЎвЂР  Р вЂ¦Р  Р’В°Р РЋРІР‚С™ */
        final float cx;
        final float cy;
        final float cz;

        Ctx(MatrixStack m, Event3DRender e, ActiveEffect fx, float p, float t,
            float cx, float cy, float cz) {
            this.m = m;
            this.e = e;
            this.fx = fx;
            this.p = p;
            this.t = t;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
        }

        /** Р  РЎС›Р  РЎвЂўР РЋРІР‚С™ Р  Р’В¶Р  Р’Вµ Р  РЎвЂќР  РЎвЂўР  Р вЂ¦Р РЋРІР‚С™Р  Р’ВµР  РЎвЂќР РЋР С“Р РЋРІР‚С™ Р  РЎвЂ”Р  РЎвЂўР РЋР С“Р  Р’В»Р  Р’Вµ matrices.translate(dx, dy, dz). */
        Ctx shift(float dx, float dy, float dz) {
            return new Ctx(m, e, fx, p, t, cx - dx, cy - dy, cz - dz);
        }

        Matrix4f mat() {
            return m.peek().getPositionMatrix();
        }
    }

    private static final class TrackedTarget {

        private Vec3d position;
        private float width;
        private float height;
        private float bodyYaw;
        private float headYaw;
        private float pitch;
        private final long lastAttack;

        private TrackedTarget(Entity entity, long lastAttack) {
            this.lastAttack = lastAttack;
            update(entity);
        }

        private void update(Entity entity) {
            this.position = entity.getPos();
            this.width = MathHelper.clamp(entity.getWidth(), 0.3f, 2.5f);
            this.height = MathHelper.clamp(entity.getHeight(), 0.5f, 4f);
            this.pitch = entity.getPitch();
            if (entity instanceof LivingEntity living) {
                this.bodyYaw = living.bodyYaw;
                this.headYaw = living.headYaw;
            } else {
                this.bodyYaw = entity.getYaw();
                this.headYaw = entity.getYaw();
            }
        }
    }

    private static final class ActiveEffect {

        private final Vec3d position;
        private final long startTime;
        private final int color;
        private final int accent;
        private final EffectMode mode;
        private final float duration;
        private final float width;
        private final float height;
        private final float scale;
        private final long seed;
        private final float seedF;
        private final Spark[] sparks = new Spark[PARTICLE_COUNT];

        /** Р  РЎСљР  Р’В°Р РЋР С“Р РЋРІР‚С™Р  РЎвЂўР РЋР РЏР РЋРІР‚В°Р  Р’В°Р РЋР РЏ Р РЋР С“Р РЋРЎвЂњР РЋРІР‚В°Р  Р вЂ¦Р  РЎвЂўР РЋР С“Р РЋРІР‚С™Р РЋР Р‰, Р  РЎвЂќР  РЎвЂўР РЋРІР‚С™Р  РЎвЂўР РЋР вЂљР РЋРЎвЂњР РЋР вЂ№ Р РЋР вЂљР  РЎвЂР РЋР С“Р РЋРЎвЂњР  Р’ВµР  РЎВ Р  РЎвЂќР  Р’В°Р  РЎвЂќ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР РЋР вЂ№ (null = Р  Р’В±Р  Р’ВµР  Р’В· Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В°). */
        private final LivingEntity body;
        private final float bodyYaw;
        private final float headYaw;
        private final float pitch;
        private boolean hidden;
        /** Р­РєСЂР°РЅРЅС‹Р№ С„Р»СЌС€ С„РёРЅР°Р»Р° (РІР·СЂС‹РІ/СЂР°СЃРїР°Рґ) вЂ” СЃСЂР°Р±Р°С‚С‹РІР°РµС‚ РѕРґРёРЅ СЂР°Р·. */
        private boolean finishFlash;
        /** РЎС‚РѕСЂРѕРЅР° СЃС†РµРЅС‹ Р±СѓСЃРёРєР° (РіСЂР°РґСѓСЃС‹), РІС‹Р±РёСЂР°РµС‚СЃСЏ РѕРґРёРЅ СЂР°Р· РїРѕ Р±Р»РѕРєР°Рј РІРѕРєСЂСѓРі. */
        private float vanYaw = Float.NaN;

        private Mote[] motes = new Mote[0];
        private Mote[] dust = new Mote[0];
        private Chunk[] chunks = new Chunk[0];
        private Crystal[] crystals = new Crystal[0];
        private Shard[] shards = new Shard[0];
        private int bodyShards = 0;
        private Fly[] flies = new Fly[0];
        /** Паутина трещин на стекле кокона: полилинии {ang, y} в долях высоты. */
        private List<float[][]> webCracks = new ArrayList<>();

        private ActiveEffect(Vec3d position, long startTime, int color,
                             EffectMode mode, float width, float height,
                             float sizeMul, float speedMul,
                             LivingEntity body, float bodyYaw, float headYaw, float pitch) {
            this.position = position;
            this.startTime = startTime;
            this.color = color;
            this.accent = mixColor(color, 0xFFFFFF, 0.72f);
            this.mode = mode;
            this.duration = mode.duration / Math.max(0.1f, speedMul);
            this.width = width;
            this.height = height;
            this.scale = Math.max(0.75f, height / 1.8f) * sizeMul;
            this.body = body;
            this.bodyYaw = bodyYaw;
            this.headYaw = headYaw;
            this.pitch = pitch;
            this.seed = startTime ^ Double.doubleToLongBits(position.z)
                    ^ (Double.doubleToLongBits(position.x) << 1);

            Random random = new Random(seed);
            this.seedF = random.nextFloat() * 100f;

            for (int i = 0; i < sparks.length; i++) {
                sparks[i] = new Spark(
                        TAU * (i + random.nextFloat()) / sparks.length,
                        0.65f + random.nextFloat() * 1.9f,
                        0.9f + random.nextFloat() * 2.8f,
                        (random.nextFloat() - 0.5f) * 0.65f,
                        0.045f + random.nextFloat() * 0.055f,
                        random.nextFloat() * 0.13f,
                        random.nextFloat()
                );
            }

            switch (mode) {
                case BLACK_HOLE -> buildBlackHole(random);
                case MAGMA -> buildMagma(random);
                case CRYSTAL -> buildCrystal(random);
                case AURORA -> buildAurora(random);
                case SOUL -> buildSoul(random);
                default -> { }
            }
        }

        private void buildBlackHole(Random r) {
            motes = new Mote[150];
            for (int i = 0; i < motes.length; i++) {
                float ang = r.nextFloat() * TAU;
                float rad = (float) Math.sqrt(r.nextFloat()) * width * 0.55f;
                motes[i] = new Mote(cos(ang) * rad, r.nextFloat() * height, sin(ang) * rad,
                        0f, 0f, 0f, 0f,
                        (0.05f + r.nextFloat() * 0.05f) * scale,
                        r.nextFloat(), r.nextFloat(), r.nextFloat());
            }
            dust = new Mote[160];
            for (int i = 0; i < dust.length; i++) {
                dust[i] = new Mote((1.4f + r.nextFloat() * 2.4f) * scale,
                        (r.nextFloat() - 0.4f) * 2.2f * scale, 0f,
                        0f, r.nextFloat() * TAU, 0f,
                        0.35f + r.nextFloat() * 0.5f,
                        (0.03f + r.nextFloat() * 0.04f) * scale,
                        0f, r.nextFloat(), r.nextFloat());
            }
        }

        private void buildMagma(Random r) {
            // Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р РЋР С“Р РЋРІР‚С™Р  Р’В°Р РЋРІР‚С™Р РЋРЎвЂњР  РЎвЂ Р  Р’В·Р  Р’В°Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р РЋР РЏР РЋР вЂ№Р РЋРІР‚С™ Р  РЎвЂўР  Р’В±Р РЋР вЂ°Р РЋРІР‚ВР  РЎВ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В° Р  РЎвЂ”Р  РЎвЂў Р  Р’ВµР  РЎвЂ“Р  РЎвЂў Р РЋР вЂљР  Р’ВµР  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  РЎвЂўР  РЎВР РЋРЎвЂњ Р РЋР вЂљР  Р’В°Р  Р’В·Р  РЎВР  Р’ВµР РЋР вЂљР РЋРЎвЂњ
            chunks = new Chunk[64];
            for (int i = 0; i < chunks.length; i++) {
                float y = r.nextFloat() * height;
                float ang = r.nextFloat() * TAU;
                float rad = (float) Math.sqrt(r.nextFloat()) * width * 0.5f * prof(y / height);
                chunks[i] = new Chunk(cos(ang) * rad, y, sin(ang) * rad,
                        (0.09f + r.nextFloat() * 0.08f) * Math.max(0.8f, width / 0.6f * 0.7f),
                        r.nextFloat() * TAU, r.nextFloat() * TAU,
                        0.3f + r.nextFloat() * 0.9f, r.nextFloat(),
                        (r.nextFloat() - 0.5f) * 2f);
            }
            dust = new Mote[60];
            for (int i = 0; i < dust.length; i++) {
                dust[i] = new Mote(r.nextFloat(), r.nextFloat(), 0f,
                        0f, r.nextFloat() * TAU, 0f,
                        0.4f + r.nextFloat() * 0.5f,
                        (0.035f + r.nextFloat() * 0.045f) * scale,
                        0f, r.nextFloat(), r.nextFloat());
            }
        }

        private void buildCrystal(Random r) {
            // РёРµСЂР°СЂС…РёСЏ: РЅРµСЃРєРѕР»СЊРєРѕ Р‘РћР›Р¬РЁРРҐ С€РёРїРѕРІ + СЂРѕР№ РјРµР»РєРёС… вЂ” РєР»Р°СЃС‚РµСЂ, Р° РЅРµ Р·Р°Р±РѕСЂ
            crystals = new Crystal[11];
            for (int i = 0; i < crystals.length; i++) {
                boolean big = i < 4;
                float ang = TAU * i / crystals.length + (r.nextFloat() - 0.5f) * 0.5f;
                float dist = (big ? (0.55f + r.nextFloat() * 0.4f) : (0.85f + r.nextFloat() * 0.5f)) * scale;
                crystals[i] = new Crystal(cos(ang) * dist, sin(ang) * dist, ang,
                        0.25f + r.nextFloat() * 0.35f,
                        (big ? (1.0f + r.nextFloat() * 0.6f) : (0.35f + r.nextFloat() * 0.35f)) * scale,
                        (big ? (0.14f + r.nextFloat() * 0.07f) : (0.06f + r.nextFloat() * 0.05f)) * scale,
                        big ? (0.02f + i * 0.012f) : (0.06f + r.nextFloat() * 0.1f),
                        r.nextFloat() * TAU);
            }

            // паутина трещин: 8 полилиний по стеклу, растут с напряжением
            webCracks = new ArrayList<>();
            for (int wci = 0; wci < 8; wci++) {
                float wang = r.nextFloat() * TAU;
                float wy = 0.15f + r.nextFloat() * 0.7f;
                List<float[]> line = new ArrayList<>();
                line.add(new float[]{wang, wy});
                int wn = 5 + r.nextInt(3);
                for (int wk = 0; wk < wn; wk++) {
                    wang += (r.nextFloat() - 0.5f) * 1.3f;
                    wy = MathHelper.clamp(wy + (r.nextFloat() - 0.45f) * 0.3f, 0.04f, 0.96f);
                    line.add(new float[]{wang, wy});
                }
                webCracks.add(line.toArray(new float[0][]));
            }

            float bigR = width * 0.62f + 0.2f * scale;
            List<Shard> list = new ArrayList<>();

            // 1) Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р РЋР С“Р  Р’В°Р  РЎВР  РЎвЂўР  РЎвЂ“Р  РЎвЂў Р  РЎВР  РЎвЂўР  Р’В±Р  Р’В°: Р  Р’В·Р  Р’В°Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р  Р вЂ¦Р РЋР РЏР РЋР вЂ№Р РЋРІР‚С™ Р  РЎвЂўР  Р’В±Р РЋР вЂ°Р РЋРІР‚ВР  РЎВ Р РЋРІР‚С™Р  Р’ВµР  Р’В»Р  Р’В°
            for (int i = 0; i < 90; i++) {
                float y = 0.05f + r.nextFloat() * height;
                float a = r.nextFloat() * TAU;
                float rr = (float) Math.sqrt(r.nextFloat()) * width * 0.5f * prof(y / height);
                list.add(shard(r, cos(a) * rr, y, sin(a) * rr,
                        (1.4f + r.nextFloat() * 2.6f) * scale,
                        (0.07f + r.nextFloat() * 0.08f) * Math.max(0.9f, scale)));
            }
            bodyShards = list.size();

            // 2) Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р  РЎвЂќР  РЎвЂўР  РЎвЂќР  РЎвЂўР  Р вЂ¦Р  Р’В°
            for (int i = 0; i < 60; i++) {
                float a = r.nextFloat() * TAU;
                float y = 0.05f + r.nextFloat() * (height + 0.3f * scale);
                list.add(shard(r, cos(a) * bigR, y, sin(a) * bigR,
                        (1.8f + r.nextFloat() * 3f) * scale,
                        (0.1f + r.nextFloat() * 0.12f) * scale));
            }
            // 3) Р  РЎвЂќР РЋРЎвЂњР РЋР С“Р  РЎвЂќР  РЎвЂ Р  РЎвЂќР РЋР вЂљР  РЎвЂР РЋР С“Р РЋРІР‚С™Р  Р’В°Р  Р’В»Р  Р’В»Р  РЎвЂўР  Р вЂ  Р  Р вЂ Р  РЎвЂўР  РЎвЂќР РЋР вЂљР РЋРЎвЂњР  РЎвЂ“
            for (int i = 0; i < 36; i++) {
                Crystal cr = crystals[r.nextInt(crystals.length)];
                list.add(shard(r, cr.x(), r.nextFloat() * cr.height() * 0.8f, cr.z(),
                        (1.2f + r.nextFloat() * 2f) * scale,
                        (0.08f + r.nextFloat() * 0.1f) * scale));
            }
            shards = list.toArray(new Shard[0]);

            dust = new Mote[130];
            for (int i = 0; i < dust.length; i++) {
                float a = r.nextFloat() * TAU;
                float rr = (float) Math.sqrt(r.nextFloat()) * bigR;
                dust[i] = new Mote(cos(a) * rr, r.nextFloat() * height * 1.2f, sin(a) * rr,
                        r.nextFloat() - 0.5f, r.nextFloat() * 0.8f, r.nextFloat() - 0.5f,
                        (0.8f + r.nextFloat() * 2f) * scale,
                        (0.03f + r.nextFloat() * 0.045f) * scale,
                        r.nextFloat(), r.nextFloat(), r.nextFloat());
            }
        }

        private static Shard shard(Random r, float ox, float oy, float oz, float speed, float size) {
            float hl = (float) Math.sqrt(ox * ox + oz * oz) + 0.001f;
            float dx = ox / hl + (r.nextFloat() - 0.5f) * 0.7f;
            float dz = oz / hl + (r.nextFloat() - 0.5f) * 0.7f;
            float dy = 0.35f + r.nextFloat() * 1.1f;
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            return new Shard(ox, oy, oz, dx / len, dy / len, dz / len,
                    speed, size, r.nextFloat() * TAU, r.nextFloat() * TAU, r.nextFloat());
        }

        private void buildAurora(Random r) {
            flies = new Fly[90];
            for (int i = 0; i < flies.length; i++) {
                float y0 = r.nextFloat();
                flies[i] = new Fly(
                        r.nextFloat() * TAU,
                        y0,
                        width * 0.5f * prof(y0) + 0.05f,
                        0.45f + r.nextFloat() * 0.9f,
                        0.2f + r.nextFloat() * 0.55f,
                        (r.nextBoolean() ? 1f : -1f) * (0.5f + r.nextFloat() * 1.3f),
                        r.nextFloat(),
                        (0.1f + r.nextFloat() * 0.08f) * scale,
                        r.nextFloat());
            }
            dust = new Mote[70];
            for (int i = 0; i < dust.length; i++) {
                dust[i] = new Mote(r.nextFloat(), r.nextFloat(), 0f,
                        0f, r.nextFloat() * TAU, 0f,
                        0.3f + r.nextFloat() * 0.4f,
                        (0.035f + r.nextFloat() * 0.04f) * scale,
                        0f, r.nextFloat(), r.nextFloat());
            }
        }

        private void buildSoul(Random r) {
            dust = new Mote[80];
            for (int i = 0; i < dust.length; i++) {
                dust[i] = new Mote(r.nextFloat(), 0f, 0f,
                        0f, r.nextFloat() * TAU, 0f,
                        0.5f + r.nextFloat() * 0.5f,
                        (0.045f + r.nextFloat() * 0.045f) * scale,
                        0f, r.nextFloat(), r.nextFloat());
            }
            motes = new Mote[50];
            for (int i = 0; i < motes.length; i++) {
                motes[i] = new Mote(r.nextFloat() * TAU, r.nextFloat(), r.nextFloat(),
                        0f, 0f, 0f, 0f,
                        (0.03f + r.nextFloat() * 0.04f) * scale,
                        r.nextFloat(), r.nextFloat(), r.nextFloat());
            }
        }
    }

    private record Spark(
            float angle,
            float distance,
            float height,
            float spin,
            float size,
            float delay,
            float tint
    ) {
    }

    /** Р  Р в‚¬Р  Р вЂ¦Р  РЎвЂР  Р вЂ Р  Р’ВµР РЋР вЂљР РЋР С“Р  Р’В°Р  Р’В»Р РЋР Р‰Р  Р вЂ¦Р  Р’В°Р РЋР РЏ Р РЋРІР‚РЋР  Р’В°Р РЋР С“Р РЋРІР‚С™Р  РЎвЂР РЋРІР‚ Р  Р’В°; Р РЋР С“Р  РЎВР РЋРІР‚в„–Р РЋР С“Р  Р’В» Р  РЎвЂ”Р  РЎвЂўР  Р’В»Р  Р’ВµР  РІвЂћвЂ“ Р  Р’В·Р  Р’В°Р  Р вЂ Р  РЎвЂР РЋР С“Р  РЎвЂР РЋРІР‚С™ Р  РЎвЂўР РЋРІР‚С™ Р РЋР вЂљР  Р’ВµР  Р’В¶Р  РЎвЂР  РЎВР  Р’В°. */
    private record Mote(
            float x, float y, float z,
            float dx, float dy, float dz,
            float speed, float size, float delay, float tint, float phase
    ) {
    }

    private record Chunk(
            float x, float y, float z, float size,
            float rx, float ry, float drift, float delay, float spin
    ) {
    }

    private record Crystal(
            float x, float z, float angle, float tilt,
            float height, float radius, float delay, float twist
    ) {
    }

    private record Shard(
            float x, float y, float z,
            float dx, float dy, float dz,
            float speed, float size, float spin, float axis, float tint
    ) {
    }

    /** Р  РІР‚ВР  Р’В°Р  Р’В±Р  РЎвЂўР РЋРІР‚РЋР  РЎвЂќР  Р’В° Р РЋР вЂљР  Р’ВµР  Р’В¶Р  РЎвЂР  РЎВР  Р’В° Р вЂ™Р’В«Р  Р Р‹Р  РЎвЂР РЋР РЏР  Р вЂ¦Р  РЎвЂР  Р’ВµР вЂ™Р’В». */
    private record Fly(
            float angle, float y0, float r0,
            float rise, float orbit, float spin,
            float phase, float size, float tint
    ) {
    }

    private static void drawBuilt(BuiltBuffer built) {
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }
}