package gjs;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.mod.Plugin;
import org.graalvm.polyglot.PolyglotException;

public class gjsPlugin extends Plugin {
    private jsEnv GlobalEnv;
    private Fi LogFi;

    @Override
    public void init() {
        Log.info("[GJS][Start]");
        LogFi = Vars.mods.getMod(gjsPlugin.class).file.parent().parent().child("js-log");
        if (!LogFi.exists()) LogFi.mkdirs();
        GlobalEnv = new jsEnv(LogFi, true);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("gjs", "<args...>", "", ((args, parameter) -> {
            StringBuilder str = new StringBuilder();
            for (var i : args) {
                str.append(i);
            }
            try {
                Log.info(GlobalEnv.eval(str.toString()).toString());
            } catch (PolyglotException err) {
                Log.err(err.toString());
            }
        }));
        handler.register("gja","<import/putI/logDir> [args...]","",(args, parameter) -> {
            switch (args[0]) {
                case "import" -> {
                    if (args.length == 1) {
                        Log.err("[GJA][need a className]");
                        return;
                    }
                    if(args[1].endsWith(".*")){
                        try {
                            GlobalEnv.importPackage(args[1].substring(0, args[1].length() - 2));
                            Log.info("[GJA][success import [@]]",args[1]);
                        }catch(RuntimeException e){
                            Log.err("[GJA][Import Err][@]",e.getMessage());
                        }
                    }else{
                    try {
                        GlobalEnv.Import(args[1]);
                        Log.info("[GJA][success import [@]]", args[1]);
                    } catch (ClassNotFoundException err) {
                        Log.err("[GJA][No find class][@]", args[1]);
                    } catch (PolyglotException err) {
                        Log.err("[GJA][running error][@]", err.getMessage());
                    }}
                }
                case "putI" -> {
                    GlobalEnv.putImport();
                    Log.info("[GJA][put Import function]");
                }
                case "logDir" -> Log.info("[GJA][Log Dir][@]", LogFi.absolutePath());
                default -> Log.err("[GJA][Error Action]");
            }
        });
    }
}
