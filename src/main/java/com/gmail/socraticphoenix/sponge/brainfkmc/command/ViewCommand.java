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
package com.gmail.socraticphoenix.sponge.brainfkmc.command;

import com.gmail.socraticphoenix.sponge.brainfkmc.BrainFkMC;
import com.gmail.socraticphoenix.sponge.brainfkmc.ProgramManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.title.Title;

import java.util.UUID;
import java.util.stream.Collectors;

public class ViewCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                .title(Text.of(TextColors.BLUE, "Running Programs"))
                .padding(Text.of(TextColors.DARK_GREEN, "="))
                .contents(ProgramManager.getOperatingInstance().getPrograms().entrySet().stream().map(entry -> Text.of(" - ", entry.getValue().getPlayer().getName(), ": ", this.pauseButton(entry.getKey()), ", ", this.unpauseButton(entry.getKey()), ", ", this.killButton(entry.getKey()), ", ", this.teleportButton(entry.getKey()))).collect(Collectors.toList()))
                .build().sendTo(src);
        return CommandResult.success();
    }

    private Text teleportButton(UUID target) {
        return Text.builder("Teleport").color(TextColors.LIGHT_PURPLE).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                if (ProgramManager.getOperatingInstance().get(target).isPresent()) {
                    ProgramManager.getOperatingInstance().get(target).ifPresent(program -> player.setLocation(program.getCenter().add(0, 0, 2)));
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Teleported To Program")).fadeIn(10).stay(20).fadeOut(10).build());
                } else {
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "That program no longer exists")).fadeIn(10).stay(20).fadeOut(10).build());
                }
            }
        })).build();
    }

    private Text pauseButton(UUID target) {
        return Text.builder("Kill").color(TextColors.DARK_RED).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                if (ProgramManager.getOperatingInstance().get(target).isPresent()) {
                    ProgramManager.getOperatingInstance().get(target).ifPresent(program -> program.getTask().setTerminated(true));
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Killed Program")).fadeIn(10).stay(20).fadeOut(10).build());
                } else {
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "That program no longer exists")).fadeIn(10).stay(20).fadeOut(10).build());
                }
            }
        })).build();
    }

    private Text unpauseButton(UUID target) {
        return Text.builder("Un-Pause").color(TextColors.GREEN).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                if (ProgramManager.getOperatingInstance().get(target).isPresent()) {
                    ProgramManager.getOperatingInstance().get(target).ifPresent(program -> {
                        if (BrainFkMC.getOperatingInstance().getListener().hasInput(target)) {
                            player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "The program is waiting for input!")).fadeIn(10).stay(20).fadeOut(10).build());
                        } else {
                            program.getTask().setPaused(false);
                            player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Un-Paused Program")).fadeIn(10).stay(20).fadeOut(10).build());
                        }
                    });
                } else {
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "That program no longer exists")).fadeIn(10).stay(20).fadeOut(10).build());
                }
            }
        })).build();
    }

    private Text killButton(UUID target) {
        return Text.builder("Pause").color(TextColors.YELLOW).style(TextStyles.BOLD).onClick(TextActions.executeCallback(c -> {
            if (c instanceof Player) {
                Player player = (Player) c;
                if (ProgramManager.getOperatingInstance().get(target).isPresent()) {
                    ProgramManager.getOperatingInstance().get(target).ifPresent(program -> program.getTask().setPaused(true));
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.BLUE, "Paused Program")).fadeIn(10).stay(20).fadeOut(10).build());
                } else {
                    player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "That program no longer exists")).fadeIn(10).stay(20).fadeOut(10).build());
                }
            }
        })).build();
    }
}
