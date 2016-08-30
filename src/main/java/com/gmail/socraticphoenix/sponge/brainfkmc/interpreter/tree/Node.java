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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Node {

    boolean isMarked();

    void setMarked(boolean marked);

    void traverse(Consumer<Node> action);

    Node parent();

    void setParent(Node node);

    Optional<Node> obtain(Predicate<Node> node);

    static Node parse(String s) {
        if(s.contains("[")) {
            Block block = new Block();
            StringBuilder builder = new StringBuilder();
            boolean inLoop = false;
            int bracket = 0;
            for(char c : s.toCharArray()) {
                if(c == '[') {
                    if(bracket == 0 && !inLoop && builder.length() > 0) {
                        block.addNode(new InstructionSet(builder.toString()));
                        builder = new StringBuilder();
                    }

                    inLoop = true;
                    bracket++;
                    builder.append(c);
                } else if (c == ']') {
                    builder.append(c);
                    bracket--;
                    if(bracket == 0) {
                        inLoop = false;
                        Node node = Node.parse(cutFirstChar(cutLastChar(builder.toString())));
                        builder = new StringBuilder();
                        Loop actual = new Loop();
                        if(node instanceof Block) {
                            ((Block) node).getPieces().forEach(actual::addNode);
                        } else {
                            actual.addNode(node);
                        }
                        block.addNode(actual);
                    }
                } else {
                    builder.append(c);
                }
            }
            if(bracket == 0 && builder.length() > 0) {
                block.addNode(new InstructionSet(builder.toString()));
            }
            return block;
        } else {
            return new InstructionSet(s);
        }
    }

    static String cutLastChar(String string) {
        if (string.length() <= 0) {
            return string;
        } else {
            return string.substring(0, string.length() - 1);
        }
    }

    static String cutFirstChar(String string) {
        if (string.length() <= 0) {
            return string;
        } else {
            return string.substring(1, string.length());
        }
    }

}
