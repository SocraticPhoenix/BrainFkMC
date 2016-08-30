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

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.gmail.socraticphoenix.sponge.brainfkmc.BrainFkMC;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class Tape {
    private static List<Vector2i> owned = new ArrayList<>();

    private World world;
    private Vector3i center;
    private Axis axis;
    private BlockType type;
    private int current;
    private List<BlockSnapshot> changed;

    public Tape(Location<World> center, Axis axis, BlockType type) {
        this.center = center.getBlockPosition();
        this.world = center.getExtent();
        this.axis = axis;
        this.current = 0;
        this.type = type;
        this.changed = new ArrayList<>();
        if (type == BlockTypes.AIR) {
            throw new IllegalArgumentException("Value block must not be air");
        }
        if (axis == Axis.Y) {
            throw new IllegalArgumentException("Cannot operate tape on the Y axis");
        }
        Tape.getOwned().add(this.getBase().toVector2(true));
        this.setBlockType(this.getBase(), BlockTypes.BEDROCK);
    }

    public static List<Vector2i> getOwned() {
        return owned;
    }

    public Vector3i getCenter() {
        return this.center;
    }

    public int getCurrent() {
        return this.current;
    }

    public void incrementPointer() {
        this.setBlockType(this.getBase(), BlockTypes.BEDROCK);
        this.current++;
        this.setBlockType(this.getBase(), BlockTypes.DIAMOND_BLOCK);
        if (!Tape.getOwned().contains(this.getBase().toVector2(true))) {
            Tape.getOwned().add(this.getBase().toVector2(true));
        }
    }

    public void decrementPointer() {
        this.setBlockType(this.getBase(), BlockTypes.BEDROCK);
        this.current--;
        this.setBlockType(this.getBase(), BlockTypes.DIAMOND_BLOCK);
        if (!Tape.getOwned().contains(this.getBase().toVector2(true))) {
            Tape.getOwned().add(this.getBase().toVector2(true));
        }
    }

    public void increment() {
        Vector3i target = new Vector3i(this.getBase().getX(), this.getHeight() + 1, this.getBase().getZ());
        if (this.world.containsBlock(target)) {
            this.setBlockType(target, this.type);
        } else {
            this.setVal(0);
        }
    }

    public void decrement() {
        Vector3i target = new Vector3i(this.getBase().getX(), this.getHeight(), this.getBase().getZ());
        if (this.world.containsBlock(target)) {
            this.setBlockType(target, BlockTypes.AIR);
        } else {
            this.setVal(0);
        }
    }

    public int getHeight() {
        int tallest = 0;
        int x = this.getBase().getX();
        int z = this.getBase().getZ();
        for (int i = this.world.getBlockMax().getY(); i >= 0; i--) {
            if (this.world.getBlockType(x, i, z) != BlockTypes.AIR) {
                tallest = i;
                break;
            }
        }

        return tallest;
    }

    private void setBlockType(int x, int y, int z, BlockType type) {
        this.changed.add(0, this.world.createSnapshot(x, y, z));
        this.world.setBlockType(x, y, z, type, false, Cause.builder().named("root", BrainFkMC.getContainer()).named("plugin", BrainFkMC.getOperatingInstance()).build());
    }

    private void setBlockType(Vector3i vector3i, BlockType type) {
        this.setBlockType(vector3i.getX(), vector3i.getY(), vector3i.getZ(), type);
    }

    public Vector3i getBase() {
        return this.center.add(this.axis == Axis.X ? this.current : 0, 0, this.axis == Axis.Z ? this.current : 0);
    }

    public int getVal() {
        return this.getHeight() - this.getBase().getY();
    }

    public void setVal(int val) {
        for (int i = this.getBase().getY() + 1; i <= this.world.getBlockMax().getY(); i++) {
            int currentVal = i - this.getBase().getY();
            if (currentVal <= val) {
                this.setBlockType(this.getBase().getX(), i, this.getBase().getZ(), this.type);
            } else {
                this.setBlockType(this.getBase().getX(), i, this.getBase().getZ(), BlockTypes.AIR);
            }
        }
    }

    public void reset() {
        this.changed.stream().filter(snap -> snap.getLocation().isPresent()).map(snap -> snap.getLocation().get().getBlockPosition().toVector2(true)).forEach(Tape.getOwned()::remove);
        this.changed.forEach(blockSnapshot -> blockSnapshot.restore(true, false));
    }
}
