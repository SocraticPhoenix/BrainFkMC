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
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.Block;
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.InstructionSet;
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.Loop;
import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree.Node;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ProgramTask implements Runnable {
    private Program program;
    private boolean paused;
    private boolean terminated;
    private boolean terminatedInf;
    private Node current;
    private int index;
    private int executed;

    public ProgramTask(Program program) {
        this.program = program;
        this.paused = false;
        this.terminated = false;
        this.terminatedInf = false;
        this.program.mark(this.program.getContent());
        this.current = this.program.getMarked().get();
        this.index = 0;
        this.executed = 0;
    }

    @Override
    public void run() {
        if (!this.paused && !this.terminated) {
            for (int i = 0; i < BrainFkMC.getOperatingInstance().getInstructionsPerTick(); i++) {
                if (!this.paused && !this.terminated) {
                    if (this.current instanceof InstructionSet) {
                        char[] array = ((InstructionSet) current).getInstructions().toCharArray();
                        if (array.length > 0) {
                            if (this.index >= array.length) {
                                this.findNext();
                            } else {
                                this.runInstruction(array[this.index]);
                                this.index++;
                            }
                        } else {
                            this.findNext();
                        }
                    } else if (this.current instanceof Loop) {
                        Loop loop = (Loop) current;

                        if (this.program.getTape().getVal() == 0 && this.index == 0) {
                            this.index = loop.getPieces().size();
                        }

                        if (this.index >= loop.getPieces().size()) {
                            this.findNext();
                        } else if (!loop.getPieces().isEmpty()) {
                            this.current = loop.getPieces().get(index);
                            this.index = 0;
                        } else {
                            this.findNext();
                        }
                    } else if (this.current instanceof Block) {
                        Block block = (Block) current;
                        if (this.index >= block.getPieces().size()) {
                            this.findNext();
                        } else if (!block.getPieces().isEmpty()) {
                            this.current = block.getPieces().get(index);
                            this.index = 0;
                        } else {
                            this.findNext();
                        }
                    }
                    this.executed++;
                    if(this.executed > BrainFkMC.getOperatingInstance().getMaxInstructions()) {
                        this.terminated = true;
                        this.terminatedInf = true;
                    }
                } else {
                    break;
                }
            }
        }

        this.program.getTerminal().refresh();

        if (this.terminated) {
            this.program.terminateTask();
            this.program.getPlayer().sendMessage(Text.of(TextColors.RED, "Program executed more than " + BrainFkMC.getOperatingInstance().getMaxInstructions() + ", so it was terminated"));
            Sponge.getScheduler().createTaskBuilder()
                    .execute(() -> this.program.getTape().reset())
                    .delayTicks(20 * 3)
                    .submit(BrainFkMC.getOperatingInstance());
        }
    }

    public Program getProgram() {
        return this.program;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public Node getCurrent() {
        return this.current;
    }

    public int getIndex() {
        return this.index;
    }

    private void findNext() {
        if (this.current.parent() == null) {
            this.terminated = true;
        } else {
            Node parent = this.current.parent();
            if (parent instanceof Loop) {
                Loop loop = (Loop) parent;
                int i;
                for (i = 0; i < loop.getPieces().size(); i++) {
                    if (loop.getPieces().get(i) == this.current) {
                        break;
                    }
                }
                i++;
                if (i >= loop.getPieces().size()) {
                    if (this.program.getTape().getVal() == 0) {
                        this.current = loop;
                        this.findNext();
                    } else {
                        this.index = 0;
                        this.current = loop.getPieces().get(0);
                    }
                } else {
                    this.current = loop;
                    this.index = i;
                }
            } else if (parent instanceof Block) {
                Block block = (Block) parent;
                int i;
                for (i = 0; i < block.getPieces().size(); i++) {
                    if (block.getPieces().get(i) == this.current) {
                        break;
                    }
                }
                i++;
                if (i >= block.getPieces().size()) {
                    this.current = block;
                    this.findNext();
                } else {
                    this.current = block;
                    this.index = i;
                }
            }
        }

        if (this.current instanceof InstructionSet) {
            this.index = 0;
        }
    }

    private void runInstruction(char c) {
        switch (c) {
            case '>':
                this.program.getTape().incrementPointer();
                break;
            case '<':
                this.program.getTape().decrementPointer();
                break;
            case '+':
                this.program.getTape().increment();
                break;
            case '-':
                this.program.getTape().decrement();
                break;
            case '.':
                this.program.getTerminal().output(this.program.getTape().getVal());
                break;
            case ',':
                this.program.getTerminal().input();
                this.paused = true;
                break;
            case 'o':
                for (char z : (this.getProgram().getTape().getVal() + " ").toCharArray()) {
                    this.program.getTerminal().output(z);
                }
                break;
        }
    }
}
