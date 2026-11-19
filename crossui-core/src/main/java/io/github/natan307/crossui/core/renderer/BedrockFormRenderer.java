/*
 * CrossUI - Cross-platform UI bridge between Java Chest GUIs, Native Dialogs, and Bedrock Forms.
 * Copyright (C) 2026  natan307
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.natan307.crossui.core.renderer;

import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.model.UIButton;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.config.BridgeConfig;
import io.github.natan307.crossui.core.parser.ParsedButton;
import io.github.natan307.crossui.core.parser.TextSanitizer;
import io.github.natan307.crossui.core.session.UISession;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Builds Cumulus forms for converted chest menus and for the CrossUI API builders. */
public final class BedrockFormRenderer {

    private final CrossUIPlugin plugin;
    private final BridgeConfig config;

    public BedrockFormRenderer(CrossUIPlugin plugin, BridgeConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    // ------------------------------------------------------------ converted chest menus

    public Form sessionForm(UISession session, IntConsumer onSelect, Runnable onClosed) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title(session.title())
                .content(config.formContent());

        for (ParsedButton button : session.buttons()) {
            String text = button.name();
            if (config.showLoreInButton() && button.subtext() != null) {
                text += "\n" + TextSanitizer.truncate(button.subtext(), config.loreMaxLength());
            }
            form.button(text);
        }

        form.validResultHandler(response -> onSelect.accept(response.clickedButtonId()));
        form.closedOrInvalidResultHandler(() -> onClosed.run());
        return form.build();
    }

    /** Asks which of the two advertised click actions should be run. */
    public Form dualForm(ParsedButton button, Consumer<Boolean> onChoose, Runnable onBack) {
        String content = button.leftAction() + "\n" + button.rightAction();
        ModalForm.Builder form = ModalForm.builder()
                .title(button.name())
                .content(content)
                .button1("Left-Click")
                .button2("Right-Click");
        form.validResultHandler(response -> onChoose.accept(response.clickedFirst()));
        form.closedOrInvalidResultHandler(() -> onBack.run());
        return form.build();
    }

    // ------------------------------------------------------------ API builders

    public Form menuForm(MenuBuilder menu) {
        Player player = menu.player();
        List<UIButton> buttons = menu.buttons();

        SimpleForm.Builder form = SimpleForm.builder()
                .title(TextSanitizer.toBedrock(menu.title()))
                .content(TextSanitizer.toBedrock(menu.content()));

        for (UIButton button : buttons) {
            String text = TextSanitizer.toBedrock(button.title());
            if (button.subtext() != null && !button.subtext().isEmpty()) {
                text += "\n" + TextSanitizer.toBedrock(button.subtext());
            }
            String icon = button.icon();
            if (icon != null && icon.regionMatches(true, 0, "url:", 0, 4)) {
                form.button(text, FormImage.Type.URL, icon.substring(4));
            } else if (icon != null && icon.regionMatches(true, 0, "path:", 0, 5)) {
                form.button(text, FormImage.Type.PATH, icon.substring(5));
            } else {
                form.button(text);
            }
        }

        form.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index >= 0 && index < buttons.size()) {
                plugin.scheduler().runForPlayer(player, () -> buttons.get(index).action().accept(player));
            }
        });
        form.closedOrInvalidResultHandler(() -> {
            if (menu.onClose() != null) {
                plugin.scheduler().runForPlayer(player, () -> menu.onClose().accept(player));
            }
        });
        return form.build();
    }

    public Form inputForm(InputBuilder input) {
        Player player = input.player();
        CustomForm.Builder form = CustomForm.builder()
                .title(TextSanitizer.toBedrock(input.title()))
                .input(TextSanitizer.toBedrock(input.label()), input.placeholder(), input.defaultValue());

        form.validResultHandler(response -> {
            String text = response.asInput(0);
            String value = text == null ? "" : text;
            plugin.scheduler().runForPlayer(player, () -> input.onSubmit().accept(player, value));
        });
        form.closedOrInvalidResultHandler(() -> {
            if (input.onCancel() != null) {
                plugin.scheduler().runForPlayer(player, () -> input.onCancel().accept(player));
            }
        });
        return form.build();
    }

    public Form confirmForm(ConfirmBuilder confirm) {
        Player player = confirm.player();
        ModalForm.Builder form = ModalForm.builder()
                .title(TextSanitizer.toBedrock(confirm.title()))
                .content(TextSanitizer.toBedrock(confirm.body()))
                .button1(TextSanitizer.toBedrock(confirm.yesText()))
                .button2(TextSanitizer.toBedrock(confirm.noText()));

        form.validResultHandler(response -> {
            Consumer<Player> action = response.clickedFirst() ? confirm.onYes() : confirm.onNo();
            plugin.scheduler().runForPlayer(player, () -> action.accept(player));
        });
        form.closedOrInvalidResultHandler(() ->
                plugin.scheduler().runForPlayer(player, () -> confirm.onNo().accept(player)));
        return form.build();
    }
}
