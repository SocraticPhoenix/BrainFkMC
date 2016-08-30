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
package com.gmail.socraticphoenix.sponge.brainfkmc;

import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.Tape;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrainFkListener {
    private List<UUID> inputRequired = new ArrayList<>();

    @Listener
    public void onBlock(ChangeBlockEvent ev) {
        if(!ev.getCause().containsType(BrainFkMC.class)) {
            ev.filter(loc -> !Tape.getOwned().contains(loc.getBlockPosition().toVector2(true)));
        }
    }

    @Listener
    public void onChat(MessageChannelEvent ev, @Root Player player) {
        if (ProgramManager.getOperatingInstance().get(player.getUniqueId()).isPresent()) {
            if (this.inputRequired.contains(player.getUniqueId())) {
                String piece = ev.getOriginalMessage().toPlain().split(">", 2)[1];
                while (piece.contains("  ")) {
                    piece = piece.replaceAll("  ", " ");
                }
                piece = piece.trim();
                if(piece.equals("EOF")) {
                    ProgramManager.getOperatingInstance().get(player.getUniqueId()).get().getTape().setVal(0);
                    ProgramManager.getOperatingInstance().get(player.getUniqueId()).get().getTask().setPaused(false);
                } else {
                    char c = piece.length() > 0 ? piece.charAt(0) : ' ';
                    ProgramManager.getOperatingInstance().get(player.getUniqueId()).get().getTerminal().giveInput(c);
                    ProgramManager.getOperatingInstance().get(player.getUniqueId()).get().getTape().setVal(c);
                    ProgramManager.getOperatingInstance().get(player.getUniqueId()).get().getTask().setPaused(false);
                }
            }
        }

        this.inputRequired.remove(player.getUniqueId());
    }

    public void requestInput(UUID player) {
        this.inputRequired.add(player);
    }

    public boolean hasInput(UUID player) {
        return this.inputRequired.contains(player);
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect ev, @Root Player player) {
        ProgramManager.getOperatingInstance().terminate(player.getUniqueId());
        this.inputRequired.remove(player.getUniqueId());
    }

}
