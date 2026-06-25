package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoMineTimerMod implements ClientModInitializer {
    public static final String MOD_ID = "autominetimer";
    private static KeyBinding menuKeyBinding;
    public static String currentAnarchy = "Соло";

    @Override
    public void onInitializeClient() {
        // Регистрация кнопки меню на букву M (открывает выбор анархий)
        menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automine.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.automine"
        ));

        // Отслеживание нажатия кнопки для открытия GUI меню
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKeyBinding.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new AnarchySelectionScreen());
                }
            }
        });

        // Слушатель чата HolyWorld для авто-захвата таймеров шахт
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String plainText = message.getString();
            // Проверка: относится ли сообщение в чате к обновлению шахты
            if (plainText.toLowerCase().contains("шахта") || plainText.toLowerCase().contains("обновится")) {
                MineTimerManager.handleChatMessage(plainText);
            }
        });

        // Рендеринг текста на экране (HUD)
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            int y = 10;
            // Отображение текущего выбранного режима (Соло/Дуо/Трио/Клан)
            drawContext.drawText(client.textRenderer, Text.literal("§6[HolyWorld] Режим: §b" + currentAnarchy), 10, y, 0xFFFFFF, true);
            y += 14;

            // Отрисовка таймеров для каждого типа шахты
            for (MineTimerManager.MineType type : MineTimerManager.MineType.values()) {
                String timeStr = MineTimerManager.getFormattedTime(type);
                String rarityStr = MineTimerManager.getRarityColor(type) + MineTimerManager.getMineRarity(type);
                
                String text = String.format("§f%s (%s§f): %s", type.displayName, rarityStr, timeStr);
                drawContext.drawText(client.textRenderer, Text.literal(text), 10, y, 0xFFFFFF, true);
                y += 11;
            }
        });
    }
}
