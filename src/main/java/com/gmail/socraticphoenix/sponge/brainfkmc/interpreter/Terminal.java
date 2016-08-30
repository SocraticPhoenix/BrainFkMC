/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 socraticphoenix@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Socratic_Phoenix (socraticphoenix@gmail.com)
 */
package com.gmail.socraticphoenix.sponge.brainfkmc.interpreter;

import com.gmail.socraticphoenix.sponge.brainfkmc.BrainFkMC;
import com.gmail.socraticphoenix.sponge.brainfkmc.ProgramManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Terminal {
    private Player player;
    private StringBuilder output;
    private StringBuilder input;

    public Terminal(Player player) {
        this.player = player;
        this.output = new StringBuilder();
        this.input = new StringBuilder();
    }

    public void output(int i) {
        this.output.append((char) i);
    }

    public void input() {
        this.player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Please enter a character (or 'EOF')")).fadeIn(10).stay(20).fadeOut(10).build());
        BrainFkMC.getOperatingInstance().getListener().requestInput(this.player.getUniqueId());
    }

    public void refresh() {
        List<Text> text = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            text.add(Text.EMPTY);
        }
        text.add(Text.of(TextColors.AQUA, "-------- BrainFkMC Terminal --------"));
        text.add(Text.of(TextColors.DARK_GREEN, "Input: "));
        text.addAll(this.of(this.input.toString()));
        text.add(Text.EMPTY);
        text.add(Text.of(TextColors.LIGHT_PURPLE, "Output: "));
        text.addAll(this.of(this.output.toString()));
        text.add(Text.EMPTY);
        text.add(Text.of(TextColors.BLUE, "Actions: "));
        text.add(Text.of(this.pauseButton(), ", ", this.unpauseButton(), ", ", this.killButton()));
        text.add(Text.of(TextColors.AQUA, "------------------------------------"));
        text.forEach(this.player::sendMessage);
    }

    private Text pauseButton() {
        return Text.builder("Kill").color(TextColors.DARK_RED).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                ProgramManager.getOperatingInstance().get(((Identifiable) c).getUniqueId()).ifPresent(program -> program.getTask().setTerminated(true));
                player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Killed Program")).fadeIn(10).stay(20).fadeOut(10).build());
            }
        })).build();
    }

    private Text unpauseButton() {
        return Text.builder("Un-Pause").color(TextColors.GREEN).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                ProgramManager.getOperatingInstance().get(((Identifiable) c).getUniqueId()).ifPresent(program -> {
                    Player player = (Player) c;
                    if(BrainFkMC.getOperatingInstance().getListener().hasInput(((Identifiable) c).getUniqueId())) {
                        player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "The program is waiting for input!")).fadeIn(10).stay(20).fadeOut(10).build());
                    } else {
                        program.getTask().setPaused(false);
                        player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Un-Paused Program")).fadeIn(10).stay(20).fadeOut(10).build());
                    }
                });
            }
        })).build();
    }

    private Text killButton() {
        return Text.builder("Pause").color(TextColors.YELLOW).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                ProgramManager.getOperatingInstance().get(((Identifiable) c).getUniqueId()).ifPresent(program -> program.getTask().setPaused(true));
                player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Paused Program")).fadeIn(10).stay(20).fadeOut(10).build());
            }
        })).build();
    }

    private List<Text> of(String s) {
        return Stream.of(s.split("\n")).map(Text::of).collect(Collectors.toList());
    }

    public void giveInput(char s) {
        this.input.append(s);
    }
}
