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

import com.gmail.socraticphoenix.sponge.brainfkmc.ProgramManager;
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.Program;
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.Node;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecuteCommand implements CommandExecutor {
    private Map<UUID, InfoPacket> cache = new HashMap<>();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            if (ProgramManager.getOperatingInstance().get(player.getUniqueId()).isPresent()) {
                player.sendTitle(Title.builder().title(Text.EMPTY).subtitle(Text.of(TextColors.RED, "You are already running a program!")).fadeIn(10).stay(20).fadeOut(10).build());
            } else {
                String content = (String) args.getOne("program").get();
                if (!this.cache.containsKey(player.getUniqueId())) {
                    this.cache.put(player.getUniqueId(), new InfoPacket(player.getLocation().sub(0, 0, 2), Axis.X, BlockTypes.STONE, new StringBuilder(content.endsWith("#") ? Node.cutLastChar(content) : content)));
                } else {
                    this.cache.get(player.getUniqueId()).getContent().append(content.endsWith("#") ? Node.cutLastChar(content) : content);
                }

                if (!content.endsWith("#")) {
                    InfoPacket packet = this.cache.get(player.getUniqueId());
                    this.cache.remove(player.getUniqueId());
                    try {
                        Program program = new Program(packet.getContent().toString(), player, packet.getBase(), packet.getAxis(), packet.getType());
                        program.startTask();
                    } catch (IllegalArgumentException e) {
                        src.sendMessage(Text.of(TextColors.RED, "Failed parsing: " + e.getMessage()));
                    }
                }
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You're not a player!"));
        }
        return CommandResult.success();
    }

    public static class InfoPacket {
        private Location<World> base;
        private Axis axis;
        private BlockType type;
        private StringBuilder content;

        public InfoPacket(Location<World> base, Axis axis, BlockType type, StringBuilder content) {
            this.base = base;
            this.axis = axis;
            this.type = type;
            this.content = content;
        }

        public Location<World> getBase() {
            return this.base;
        }

        public Axis getAxis() {
            return this.axis;
        }

        public BlockType getType() {
            return this.type;
        }

        public StringBuilder getContent() {
            return this.content;
        }
    }

}
