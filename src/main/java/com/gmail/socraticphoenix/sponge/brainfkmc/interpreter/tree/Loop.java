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
package com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Loop implements Node {
    private List<Node> pieces;
    private boolean marked;
    private Node node;

    public Loop() {
        this.pieces = new ArrayList<>();
        this.marked = false;
    }

    @Override
    public Node parent() {
        return this.node;
    }

    @Override
    public void setParent(Node node) {
        this.node = node;
    }

    public List<Node> getPieces() {
        return this.pieces;
    }

    public void addNode(Node node) {
        node.setParent(this);
        this.pieces.add(node);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append("[");
        this.pieces.forEach(builder::append);
        return builder.append("]").toString();
    }

    @Override
    public boolean isMarked() {
        return this.marked;
    }

    @Override
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public void traverse(Consumer<Node> action) {
        action.accept(this);
        this.pieces.forEach(n -> n.traverse(action));
    }

    @Override
    public Optional<Node> obtain(Predicate<Node> node) {
        if (node.test(this)) {
            return Optional.of(this);
        } else {
            if (!this.pieces.isEmpty()) {
                return this.pieces.stream().map(n -> n.obtain(node)).findFirst().get();
            } else {
                return Optional.empty();
            }
        }
    }
}
