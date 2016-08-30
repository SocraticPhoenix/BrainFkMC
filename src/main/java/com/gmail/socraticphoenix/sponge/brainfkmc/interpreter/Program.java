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
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.Node;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class Program {
    private Node content;
    private Player player;
    private Location<World> center;
    private Tape tape;
    private Terminal terminal;
    private ProgramTask task;
    private Task taskObj;

    public Program(String content, Player player, Location<World> center, Axis axis, BlockType type) {
        int brack = 0;
        for (char c : content.toCharArray()) {
            if (c == '[') {
                brack++;
            } else if (c == ']') {
                brack--;
                if (brack < 0) {
                    throw new IllegalArgumentException("Unbalanced brackets");
                }
            }
        }

        if (brack != 0) {
            throw new IllegalArgumentException("Unbalanced brackets");
        }

        this.player = player;
        this.center = center;
        this.tape = new Tape(this.center, axis, type);
        this.terminal = new Terminal(this.player);
        for (Map.Entry<String, String> entry : ProgramManager.getOperatingInstance().getInserts().entrySet()) {
            content = content.replaceAll(Pattern.quote("%" + entry.getKey() + "%"), entry.getValue());
        }
        this.content = Node.parse(content.replaceAll("[^o><+-\\.,\\]\\[]", ""));
        this.task = new ProgramTask(this);
        this.register();
    }

    public void register() {
        ProgramManager.getOperatingInstance().put(this.player.getUniqueId(), this);
    }

    public ProgramTask getTask() {
        return this.task;
    }

    public Terminal getTerminal() {
        return this.terminal;
    }

    public Optional<Node> getMarked() {
        return this.content.obtain(Node::isMarked);
    }

    public void mark(Node node) {
        this.content.traverse(n -> n.setMarked(false));
        this.content.traverse(n -> {
            if (n == node) {
                n.setMarked(true);
            }
        });
    }

    public Node getContent() {
        return this.content;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location<World> getCenter() {
        return this.center;
    }

    public Tape getTape() {
        return this.tape;
    }

    public void startTask() {
        this.taskObj = Sponge.getScheduler().createTaskBuilder()
                .name("BrainFkMC$program$" + this.player.getName())
                .intervalTicks(1)
                .execute(this.task)
                .submit(BrainFkMC.getOperatingInstance());
    }

    public void terminateTask() {
        if (this.taskObj != null) {
            this.taskObj.cancel();
        }
        ProgramManager.getOperatingInstance().terminate(this.player.getUniqueId());
    }
}
