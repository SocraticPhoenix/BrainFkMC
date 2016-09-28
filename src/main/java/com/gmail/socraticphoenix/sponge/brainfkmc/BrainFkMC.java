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

import com.gmail.socraticphoenix.sponge.brainfkmc.command.ExecuteCommand;
import com.gmail.socraticphoenix.sponge.brainfkmc.command.PreLoadedExecuteCommand;
import com.gmail.socraticphoenix.sponge.brainfkmc.command.ReloadCommand;
import com.gmail.socraticphoenix.sponge.brainfkmc.command.ViewCommand;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Plugin(id = "com.gmail.socraticphoenix.sponge.brainfkmc", name = "BrainFkMC", version = "1", authors = "Socratic_Phoenix")
public class BrainFkMC {
    private static BrainFkMC operatingInstance;

    private File config;
    private CommentedConfigurationNode node;

    private BrainFkListener listener;

    public BrainFkMC() {
        BrainFkMC.operatingInstance = this;
        this.listener = new BrainFkListener();
        this.config = new File("config", "com.gmail.socraticphoenix.sponge.brainfkmc.conf");
    }

    public static PluginContainer getContainer() {
        return Sponge.getPluginManager().fromInstance(BrainFkMC.getOperatingInstance()).get();
    }

    public static BrainFkMC getOperatingInstance() {
        return operatingInstance;
    }

    public void loadConfig() throws IOException {
        if(!this.config.exists()) {
            this.config.getAbsoluteFile().getParentFile().mkdirs();
            this.config.createNewFile();
            CommentedConfigurationNode node = HoconConfigurationLoader.builder().build().createEmptyNode();
            node.getNode("instructions_per_tick").setComment("The number of instructions in a program to execute each tick. Higher values will mean more lag, lower values will me slower programs").setValue(5);
            node.getNode("max_instructions").setComment("The maximum number of instructions a program can execute, or negative if there is no limit. If this is set to a positive number, it will prevent infinite loops from running forever").setValue(-1);
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(this.config.toPath()).build();
            loader.save(node);
            this.node = node;
        } else {
            this.node = HoconConfigurationLoader.builder().setPath(this.config.toPath()).build().load();
        }
    }

    @Listener
    public void onInitialization(GameInitializationEvent ev) throws IOException {
        Game game = Sponge.getGame();
        game.getEventManager().registerListeners(this, this.listener);

        this.loadConfig();

        PreLoadedExecuteCommand preLoadedExecuteCommand = new PreLoadedExecuteCommand();
        Map<String, String> preLoaded = preLoadedExecuteCommand.getPreLoaded();
        preLoaded.put("hello_world", "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.");
        preLoaded.put("hello_world2", "%printH%%printe%%printl%%printl%%printo%%print %%printW%%printo%%printr%%printl%%printd%");
        preLoaded.put("trump", "++++++[>++++++]");
        preLoaded.put("trump2", "++++++[[>]++++++[<]++++++]");

        CommandSpec main = CommandSpec.builder()
                .permission("brainfkmc")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand())
                        .permission("brainfkmc.admin")
                        .description(Text.of("Reloads the config"))
                        .extendedDescription(Text.of("Reloads the config"))
                        .build(), "reload", "re", "r")
                .child(CommandSpec.builder()
                        .executor(new ViewCommand())
                        .permission("brainfkmc.admin")
                        .description(Text.of("Displays all currently running BrainF**k programs"))
                        .extendedDescription(Text.of("Displays all currently running BrainF**k programs, and allows termination, pausing, and un-pausing of each"))
                        .build(), "view", "v", "list", "l")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.remainingJoinedStrings(Text.of("name")))
                        .executor(preLoadedExecuteCommand)
                        .permission("brainfkmc.run")
                        .description(Text.of("Runs a BrainF**k program"))
                        .extendedDescription(Text.of("Runs a BrainF**k program which has been pre-loaded"))
                        .build(), "executepre, execpre", "exep")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.remainingJoinedStrings(Text.of("program")))
                        .executor(new ExecuteCommand())
                        .permission("brainfkmc.run")
                        .description(Text.of("Runs a BrainF**k program"))
                        .extendedDescription(Text.of("Runs a BrainF**k program, it's registers are represented by columns of blocks extending out from the given location. If no location is provided, the player's location will be used."))
                        .build(), "execute", "exec", "exe")
                .build();
        game.getCommandManager().register(this, main, "brainfkmc", "bfm", "bf");
    }

    public int getInstructionsPerTick() {
        return this.node.getNode("instructions_per_tick").getInt(5);
    }

    public int getMaxInstructions() {
        return this.node.getNode("max_instructions").getInt(10_000);
    }

    public BrainFkListener getListener() {
        return this.listener;
    }
}
