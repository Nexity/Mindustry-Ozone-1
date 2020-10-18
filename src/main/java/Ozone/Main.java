package Ozone;

import Atom.Utility.Random;
import Atom.Utility.Utility;
import Ozone.Commands.BotInterface;
import Ozone.Commands.Commands;
import Ozone.Event.Internal;
import Ozone.Patch.ChatFragment;
import Ozone.Patch.DesktopInput;
import Ozone.Patch.SettingsDialog;
import Ozone.Patch.Translation;
import Ozone.UI.CommandsListFrag;
import Ozone.UI.OzoneMenu;
import Settings.Core;
import arc.Events;
import arc.scene.ui.Dialog;
import arc.struct.ObjectMap;
import arc.util.ColorCodes;
import arc.util.Log;
import arc.util.OS;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import java.lang.reflect.Field;

import static mindustry.Vars.*;

public class Main {

    public static void init() {
        Log.infoTag("Ozone", "Hail o7");
        loadSettings();
        patch();
        initUI();
        BotInterface.init();
        Commands.init();
        initEvent();
    }


    public static void loadContent() {

    }

    private static void patchLast() {

    }

    private static void initEvent() {
        Vars.loadLogger();
        Events.on(EventType.ClientLoadEvent.class, s -> {
            arc.Core.settings.getBoolOnce("ozoneEpilepsyWarning", () -> {
                Vars.ui.showCustomConfirm("[royal]Ozone[white]-[red]Warning",
                        "A very small percentage of people may experience a seizure when exposed to certain visual images, " +
                                "including flashing lights or patterns that may appear on certain UI element in the game.",
                        "Accept", "Decline", () -> {
                        }, () -> {
                            Core.colorPatch = false;
                            arc.Core.settings.put("ozone.colorPatch", false);
                            arc.Core.settings.forceSave();
                        });
            });
            setOzoneLogger();
        });

        Events.on(EventType.ClientPreConnectEvent.class, s -> {
            Log.debug("Ozone-@ @:@ = @",
                    s.getClass().getSimpleName(),
                    s.host.address,
                    s.host.port,
                    s.host.name);
        });
        Events.on(EventType.CommandIssueEvent.class, s -> {
            Log.debug("Ozone-@: new issue @ at @",
                    s.getClass().getSimpleName(),
                    s.command.toString(),
                    s.tile.tile.toString());
        });
        Events.on(EventType.DepositEvent.class, s -> {
            if (s.player == null) return;
            Log.debug("Ozone-@: @ deposited @ @ at @",
                    s.getClass().getSimpleName(),
                    s.player.name(),
                    s.amount,
                    s.item.toString(),
                    s.tile.tile().toString());
        });
        Events.on(EventType.WithdrawEvent.class, s -> {
            if (s.player == null) return;
            Log.debug("Ozone-@: @ withdrawn @ @ at @",
                    s.getClass().getSimpleName(),
                    s.player.name(),
                    s.amount,
                    s.item.toString(),
                    s.tile.tile().toString());
        });
        Events.on(EventType.StateChangeEvent.class, s -> {
            Log.debug("Ozone-@: State changed from @ to @",
                    s.getClass().getSimpleName(), s.from, s.to);
        });
        Events.on(EventType.UnitCreateEvent.class, s -> {
            Log.debug("Ozone-@: A @ created at @,@",
                    s.getClass().getSimpleName(),
                    s.unit.getClass().getSimpleName(),
                    s.unit.x(),
                    s.unit.y());
        });
        Events.on(EventType.UnitChangeEvent.class, s -> {
            Log.debug("Ozone-@: player @ changing into @ at @,@",
                    s.getClass().getSimpleName(),
                    s.player.name(),
                    s.unit.getClass().getSimpleName(),
                    s.unit.x(),
                    s.unit.y());
        });
        Events.on(EventType.UnitDestroyEvent.class, s -> {
            Log.debug("Ozone-@: A @ destroyed at @,@",
                    s.getClass().getSimpleName(),
                    s.unit.getClass().getSimpleName(),
                    s.unit.x(),
                    s.unit.y());
        });
        Events.on(EventType.UnitDrownEvent.class, s -> {
            Log.debug("Ozone-@: A @ drowned at @,@",
                    s.getClass().getSimpleName(),
                    s.unit.getClass().getSimpleName(),
                    s.unit.x(),
                    s.unit.y());
        });

        Events.on(EventType.BlockBuildEndEvent.class, s -> {
            if (s.unit.getPlayer() == null) return;//boring
            if (s.breaking)
                Log.debug("Ozone-@: @ successfully deconstructed @ with configuration \"@\" on team @",
                        s.getClass().getSimpleName(),
                        s.unit.getPlayer().name(),
                        s.tile.toString(),
                        s.config,
                        s.team);
            else
                Log.debug("Ozone-@: @ successfully constructed @ with configuration \"@\" on team @",
                        s.getClass().getSimpleName(),
                        s.unit.getPlayer().name(),
                        s.tile.toString(),
                        s.config,
                        s.team);
        });
        Events.on(EventType.PlayerJoin.class, s -> {
            if (s.player == null) return;
            Log.debug("Ozone-@: @",
                    s.getClass().getSimpleName(),
                    s.player.name());
        });
        Events.on(EventType.BlockDestroyEvent.class, s -> {
            Log.debug("Ozone-@: @",
                    s.getClass().getSimpleName(),
                    s.tile.toString());
        });
        Events.on(EventType.BlockBuildBeginEvent.class, s -> {
            if (s.breaking)
                Log.debug("Ozone-@: someone begin building @ at team @",
                        s.getClass().getSimpleName(),
                        s.tile.toString(),
                        s.team);
            else
                Log.debug("Ozone-@: someone begin breaking @ at team @",
                        s.getClass().getSimpleName(),
                        s.tile.toString(),
                        s.team);
        });
        Events.on(EventType.PlayerLeave.class, s -> {
            if (s.player == null) return;//bruh
            Log.debug("Ozone-@: @",
                    s.getClass().getSimpleName(),
                    s.player.name());
        });
        Events.on(EventType.ConfigEvent.class, s -> {
            Log.debug("Ozone-@: @ has been changed to @ by player @",
                    s.getClass().getSimpleName(),
                    s.tile.tile.toString(),
                    s.value,
                    s.player.name());
        });
    }

    private static void setOzoneLogger() {

        String[] tags = {"[green][D][]", "[royal][I][]", "[yellow][W][]", "[scarlet][E][]", ""};
        String[] stags = {"&lc&fb[D]", "&lg&fb[I]", "&ly&fb[W]", "&lr&fb[E]", ""};

        Log.setLogger((level, text) -> {
            String result = text;
            String rawText = Log.format(stags[level.ordinal()] + "[" + Utility.getDate() + "]" + "&fr " + text);
            System.out.println(rawText);

            result = tags[level.ordinal()] + " " + result;

            if (!headless) {
                if (!OS.isWindows) {
                    for (String code : ColorCodes.values) {
                        result = result.replace(code, "");
                    }
                }

                ui.scriptfrag.addMessage(Log.removeCodes(result));
            }
        });

        loadedLogger = true;
    }

    public static void update() {

    }

    protected static void loadSettings() {
        Manifest.settings.add(Core.class);
        Events.fire(Internal.Init.SettingsRegister);
        arc.Core.settings.put("crashreport", false);
        for (Field f : Manifest.getSettings()) {
            try {
                if (boolean.class.equals(f.getType())) {
                    f.setBoolean(null, arc.Core.settings.getBool("ozone." + f.getName(), f.getBoolean(null)));
                } else if (String.class.equals(f.getType())) {
                    f.set(null, arc.Core.settings.getString("ozone." + f.getName(), (String) f.get(null)));
                } else if (int.class.equals(f.getType())) {
                    f.setInt(null, arc.Core.settings.getInt("ozone." + f.getName(), f.getInt(null)));
                } else if (long.class.equals(f.getType())) {
                    f.setLong(null, arc.Core.settings.getLong("ozone." + f.getName()));
                } else if (float.class.equals(f.getType())) {
                    f.setFloat(null, arc.Core.settings.getFloat("ozone." + f.getName(), f.getFloat(null)));
                }
            } catch (Throwable t) {
                Log.errTag("Ozone-Settings", "Couldn't load settings for: ozone." + f.getName());
                Log.err(t);
            }
        }

    }

    protected static void patch() {
        try {
            Log.infoTag("Ozone", "Patching");
            Events.fire(Internal.Init.PatchRegister);
            Translation.patch();
            Time.mark();
            patchTranslation();
            Log.infoTag("Ozone", "Patching translation done: " + Time.elapsed());
            Log.infoTag("Ozone", "Patching DesktopInput");
            Vars.control.input = new DesktopInput();
            Log.infoTag("Ozone", "Patching Settings");
            Vars.ui.settings = new SettingsDialog();
            Log.infoTag("Ozone", "Patching ChatFragment");
            arc.Core.scene.root.removeChild(Vars.ui.chatfrag);
            Vars.ui.chatfrag = new ChatFragment();
            Vars.ui.chatfrag.container().build(Vars.ui.hudGroup);
            Vars.enableConsole = true;
            Log.infoTag("Ozone", "Patching Complete");
            if (Core.debugMode) Log.setLogLevel(Log.LogLevel.debug);
            Log.debug("Ozone-Debug", "Debugs, peoples, debugs");
        } catch (Throwable t) {
            Log.infoTag("Ozone", "Patch failed");
            Log.err(t);
        }
    }

    public static void patchTranslation() {
        ObjectMap<String, String> modified = arc.Core.bundle.getProperties();
        for (ObjectMap.Entry<String, String> s : Interface.bundle) {
            modified.put(s.key, s.value);
        }
        if (Core.colorPatch)
            for (String s : arc.Core.bundle.getKeys()) {
                modified.put(s, getRandomHexColor() + modified.get(s) + "[white]");
            }
        arc.Core.bundle.setProperties(modified);
    }

    protected static void initUI() {
        Dialog.DialogStyle ozoneStyle = new Dialog.DialogStyle() {
            {
                stageBackground = Styles.none;
                titleFont = Fonts.def;
                background = Tex.windowEmpty;
                titleFontColor = Pal.accent;
            }
        };
        Manifest.commFrag = new CommandsListFrag();
        Manifest.menu = new OzoneMenu(arc.Core.bundle.get("ozone.hud"), ozoneStyle);
        Manifest.commFrag.build(Vars.ui.hudGroup);

    }

    public static String getRandomHexColor() {
        return "[" + Random.getRandomHexColor() + "]";
    }

}
