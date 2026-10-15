package fr.rise;

import com.mojang.blaze3d.platform.InputConstants;
import fr.rise.ui.ClickGui;
import fr.rise.ui.Hud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class RiseClient implements ClientModInitializer {
    public static KeyMapping guiKey;

    @Override
    public void onInitializeClient() {
        Modules.init();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("rise", "main"));
        guiKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.rise.gui", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.consumeClick()) {
                if (Compat.screen() == null && mc.player != null) Compat.setScreen(new ClickGui());
            }
            if (mc.player == null || mc.level == null) return;
            for (Module m : Modules.all()) if (m.isEnabled()) m.onTick();
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("rise", "hud"), (graphics, tracker) -> Hud.render(graphics));

        WorldRender.init();
    }
}
