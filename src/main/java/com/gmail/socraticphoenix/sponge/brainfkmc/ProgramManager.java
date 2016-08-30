package com.gmail.socraticphoenix.sponge.brainfkmc;

import com.gmail.socraticphoenix.sponge.brainfkmc.interpreter.Program;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ProgramManager {
    private static ProgramManager operatingInstance = new ProgramManager();

    private Map<String, String> inserts;
    private Map<UUID, Program> programs;

    private ProgramManager() {
        this.programs = new HashMap<>();
        this.inserts = new HashMap<>();
        this.inserts.put("printval", "[>>+>+<<<-]>>>[<<<+>>>-]<<+>[<->[>++++++++++<[->-[>+>>]>[+[-<+>]>+>>]<<<<<]>[-]++++++++[<++++++>-]>[<<+>>-]>[<<+>>-]<<]>]<[->>++++++++[<++++++>-]]<[.[-]<]<");
        StringBuilder b = new StringBuilder();
        b.append("[-]");
        for (int i = 1; i <= 256; i++) {
            b.append("+");
            this.inserts.put(String.valueOf(i), b.toString());
        }
        String str = "abcdefghijklmnopqrstuvwxyz";
        str += str.toUpperCase();
        str += "!\"#$5&'()*+-./0123456789:;<=>?@[\\]^_`~\n\t ";
        for(char c : str.toCharArray()) {
            StringBuilder instruction = new StringBuilder();
            instruction.append("[-]");
            for (int i = 0; i < c; i++) {
                instruction.append("+");
            }
            this.inserts.put("load" + String.valueOf(c).replaceAll("\n", "\\n").replaceAll("\t", "\\t"), instruction.toString());
            this.inserts.put("print" + String.valueOf(c).replaceAll("\n", "\\n").replaceAll("\t", "\\t"), instruction.append(".[-]").toString());
        }
    }

    public Map<String, String> getInserts() {
        return this.inserts;
    }

    public Optional<Program> get(UUID owner) {
        return Optional.ofNullable(this.programs.get(owner));
    }

    public void terminate(UUID owner) {
        this.get(owner).ifPresent(p -> p.getTask().setTerminated(true));
        this.programs.remove(owner);
    }

    public static ProgramManager getOperatingInstance() {
        return ProgramManager.operatingInstance;
    }

    public Map<UUID, Program> getPrograms() {
        return this.programs;
    }

    public void put(UUID uniqueId, Program program) {
        this.terminate(uniqueId);
        this.programs.put(uniqueId, program);
    }
}
