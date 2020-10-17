package Ozone.Commands;

import Atom.Time.Countdown;
import Atom.Utility.Random;
import Atom.Utility.Utility;
import Garbage.Settings;
import Ozone.Commands.Task.DestructBlock;
import Ozone.Commands.Task.Move;
import Ozone.Commands.Task.Task;
import Ozone.Event.Internal;
import Ozone.Interface;
import Ozone.Main;
import Ozone.Manifest;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.OrderedMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Commands {

    public static final HashMap<String, Command> commandsList = new HashMap<>();
    private static boolean init = false;
    private volatile static boolean falseVote = false;
    public static final Queue<Task> commandsQueue = new Queue<>();
    public static void init() {
        if (init) return;
        init = true;
        Events.run(EventType.Trigger.update, () -> {
            if (commandsQueue.isEmpty()) return;
            commandsQueue.first().update();
            if (commandsQueue.first().isCompleted())
                commandsQueue.removeFirst();
        });
        Events.run(EventType.WorldLoadEvent.class, () -> {

        });

        register("help", new Command(Commands::help));
        register("chaos-kick", new Command(Commands::chaosKick));
        register("task-move", new Command(Commands::taskMove));
        register("info-pos", new Command(Commands::infoPos, Icon.move));
        register("info-pathfinding", new Command(Commands::infoPathfinding));
        register("random-kick", new Command(Commands::randomKick, Icon.hammer));
        register("info-unit", new Command(Commands::infoUnit, Icon.units));
        register("force-exit", new Command(Commands::forceExit));
        register("task-deconstruct", new Command(Commands::taskDeconstruct));
        register("send-colorize", new Command(Commands::sendColorize));
        register("task-clear", new Command(Commands::taskClear));
        register("shuffle-sorter", new Command(Commands::shuffleSorter, Icon.rotate));
        Events.fire(Internal.Init.CommandsRegister);
        Main.patchTranslation();
        for (Map.Entry<String, Command> c : commandsList.entrySet())
            c.getValue().description = getTranslation(c.getKey());
        Log.infoTag("Ozone", "Commands Center Initialized");
    }



    public static void register(String name, Command command) {
        register(name, command, null);
    }

    public static void register(String name, Command command, String description) {
        if (description != null)
            Interface.registerWords("ozone.commands." + name, description);
        commandsList.put(name, command);
    }

    public static void shuffleSorter(ArrayList<String> s) {
        //TODO find random sorter block
    }

    public static void taskClear(ArrayList<String> s) {
        BotInterface.reset();
        tellUser("Task cleared");
    }

    public static void sendColorize(ArrayList<String> s) {
        if (s.isEmpty()) {
            tellUser("Empty ? gabe itch");
            return;
        }
        String text = Utility.joiner(Utility.getArray(s), " ");
        StringBuilder sb = new StringBuilder();
        if (text.length() * 10 > Vars.maxTextLength) {
            OrderedMap<String, Color> map = Colors.getColors();
            ArrayList<String> colors = new ArrayList<>();
            for (String mp : map.keys()) {
                colors.add('[' + mp + ']');
            }
            String[] colorss = new String[colors.size()];
            colorss = colors.toArray(colorss);
            for (char c : text.toCharArray()) {
                if (c != ' ') {
                    sb.append(Random.getRandom(colorss)).append(c);
                } else
                    sb.append(c);
            }
        } else {
            for (char c : text.toCharArray()) {
                if (c != ' ')
                    sb.append("[").append(Random.getRandomHexColor()).append("]").append(c);
                else
                    sb.append(c);
            }
        }
        Call.sendChatMessage(sb.toString());
    }

    public static void taskDeconstruct(ArrayList<String> s) {
        if (s.size() < 2) {
            tellUser("Not enough arguments");
            tellUser("Usage: task-deconstruct x(type: coordinate) y(type: coordinate) half(type: boolean, optional default: false)");
            return;
        }
        try {
            int x = Integer.parseInt(s.get(0));
            int y = Integer.parseInt(s.get(1));
            if (Vars.world.tile(x, y) == null) {
                tellUser("Non existent tiles");
                return;
            }
            boolean half = false;
            // i don't trust user
            if (s.size() == 3) {
                half = true;
            }
            long start = System.currentTimeMillis();
            BotInterface.addTask(new DestructBlock(x, y, half), a -> tellUser("Completed in " + Countdown.result(start, TimeUnit.SECONDS)));
        } catch (NumberFormatException f) {
            tellUser("Failed to parse integer, are you sure that argument was integer ?");
            Vars.ui.showException(f);
        }
    }

    public static void forceExit(ArrayList<String> s) {
        throw new RuntimeException("Force Exit: " + Utility.joiner(Utility.getArray(s), ", "));
    }

    public static void infoUnit(ArrayList<String> s) {
        tellUser(Vars.player.unit().getClass().getCanonicalName());
    }

    public static String getTranslation(String name) {
        return Core.bundle.get("ozone.commands." + name);
    }

    public static boolean call(String message) {
        if (!message.startsWith(Settings.commandsPrefix)) return false;
        String[] arg = message.replaceFirst(",", "").split(" ");
        if (!commandsList.containsKey(arg[0].toLowerCase())) {
            tellUser("Commands not found");
            help(new ArrayList<>());
            return false;
        }
        Command comm = commandsList.get(arg[0].toLowerCase());
        ArrayList<String> args;
        if (message.contains(" ")) {
            message = message.replaceFirst(arg[0], "").replaceFirst(" ", "");
            arg = message.split(" ");
            args = new ArrayList<>(Arrays.asList(arg));
        } else {
            args = new ArrayList<>();
        }
        comm.method.accept(args);
        return true;
    }

    public static void randomKick(ArrayList<String> s) {
        ArrayList<Player> players = new ArrayList<>();
        for (Player p : Groups.player)
            players.add(p);
        Player[] players1 = new Player[players.size()];
        Player p = Random.getRandom(players1);
        Call.sendChatMessage("/votekick " + p.name);
    }

    public static void infoPathfinding(ArrayList<String> s) {
        if (s.size() < 4) {
            tellUser("Not enough arguments");
            tellUser("usage: " + "info-pathfinding x(type: source-coordinate) y(type: source-coordinate) x(type: target-coordinate) y(type: target-coordinate) block(type: Blocks, optional)");
            return;
        }
        try {
            String block = "";
            int xS = Integer.parseInt(s.get(0));
            int yS = Integer.parseInt(s.get(1));
            if (Vars.world.tile(xS, yS) == null) {
                tellUser("Non existent source tiles");
                return;
            }
            int xT = Integer.parseInt(s.get(2));
            int yT = Integer.parseInt(s.get(3));
            if (s.size() == 5) block = s.get(4);
            Block pathfindingBlock = null;
            if (!block.isEmpty()) {
                pathfindingBlock = Vars.content.block(block);
                if (pathfindingBlock == null)
                    tellUser("Nonexistent block, using default block: magmarock/dirtwall");
            }


            Tile target = Vars.world.tile(xT, yT);
            Tile source = Vars.world.tile(xS, yS);
            if (target == null) {
                tellUser("Non existent target tiles");
                return;
            }
            if (source == null) {
                tellUser("Non existent source tiles");
                return;
            }
            Seq<Tile> tiles;
            try {
                tiles = Astar.pathfind(source, target, h -> 0, Tile::passable);
            } catch (Throwable t) {
                tellUser("Pathfinding failed");
                tellUser(t.toString());
                return;
            }
            for (Tile t : tiles) {
                if (t.block() == null)
                    tellUser("Null block: " + t.toString());
                else if (pathfindingBlock != null)
                    t.setOverlay(pathfindingBlock);
                else if (t.block().isFloor())
                    t.setOverlay(Blocks.magmarock);
                else if (t.block().isStatic())
                    t.setOverlay(Blocks.dirtWall);
            }
            tellUser("to clear pathfinding overlay use /sync");
        } catch (NumberFormatException f) {
            tellUser("Failed to parse integer, are you sure that argument was integer ?");
            Vars.ui.showException(f);
        }
    }

    public static void toggleUI() {
        Manifest.menu.hide();
    }

    public static void infoPos(ArrayList<String> a) {
        tellUser("Player x,y: " + Vars.player.x + ", " + Vars.player.y);
        tellUser("Player tile x,y: " + Vars.player.tileX() + ", " + Vars.player.tileY());
    }

    public static void help(ArrayList<String> a) {
        StringBuilder sb = new StringBuilder();
        //sb.append("\n").append("Prefix: ").append(Settings.commandsPrefix).append("\n");
        sb.append("Available Commands:").append("\n");
        for (Map.Entry<String, Command> s : commandsList.entrySet()) {
            sb.append(s.getKey()).append(": ").append(s.getValue().description).append("\n");
        }
        tellUser(sb.toString());
    }

    public static void taskMove(ArrayList<String> s) {
        if (s.size() < 2) {
            tellUser("Not enough arguments");
            tellUser("usage: " + "task-move x(coordinate) y(coordinate)");
            return;
        }
        try {
            int x = Integer.parseInt(s.get(0));
            int y = Integer.parseInt(s.get(1));
            if (Vars.world.tile(x, y) == null) {
                tellUser("Non existent tiles");
                return;
            }
            long start = System.currentTimeMillis();
            BotInterface.addTask(new Move(x, y), a -> tellUser("Reached in " + Countdown.result(start, TimeUnit.SECONDS)));
            toggleUI();
        } catch (NumberFormatException f) {
            tellUser("Failed to parse integer, are you sure that argument was integer ?");
            Vars.ui.showException(f);
        }

    }

    public static void chaosKick(ArrayList<String> unused) {
        falseVote = !falseVote;
        if (falseVote) {
            Thread s1 = new Thread(() -> {
                while (Vars.net.active() && falseVote)
                    for (Player target : Groups.player) {
                        if (!target.name.equals(Vars.player.name)) {
                            Call.sendChatMessage("/votekick " + target.name);
                            try {
                                Thread.sleep(200);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
            });
            s1.start();
            tellUser("kicking started");
        } else {
            tellUser("kicking ended");
        }
    }


    public static void tellUser(String s) {
        if (Vars.ui.scriptfrag.shown())
            Log.infoTag("Ozone", s);
        else
            Vars.ui.chatfrag.addMessage("[white][[[royal]Ozone[white]]: " + s, null);
    }

    public static class Command {
        public final Consumer<ArrayList<String>> method;
        public String description;
        public final TextureRegionDrawable icon;


        public Command(Consumer<ArrayList<String>> method) {
            this.method = method;
            icon = null;
        }

        public Command(Consumer<ArrayList<String>> method, String description) {
            this.method = method;
            this.description = getTranslation(description);
            icon = null;
        }

        public Command(Consumer<ArrayList<String>> method, String description, TextureRegionDrawable icon) {
            this.method = method;
            this.description = getTranslation(description);
            this.icon = icon;
        }


        public Command(Consumer<ArrayList<String>> method, TextureRegionDrawable icon) {
            this.method = method;
            this.icon = icon;
        }
    }
}
