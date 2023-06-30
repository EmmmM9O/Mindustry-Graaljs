package gjs;

import arc.files.Fi;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.mod.Plugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;

import java.util.*;

public class gjsPlugin extends Plugin {

    public HashMap<String,Context> envs=new HashMap<>();
    public Fi serverFi=Fi.get("/");
    public Fi LogFi=Fi.get("/");

    public void startLogTime(){
        long time=60*60*12;
        Timer.schedule(()->{
            Log.info("[GJS]--------[arrange all logs]");
            for(var f:LogFi.list()){
                if(f.name().endsWith(".nlog")){
                    var tmpFi=LogFi.child(f.name().substring(0,f.name().length()-4)+'-'+new Date().getTime()+ ".log");
                    f.moveTo(tmpFi);

                    Log.info("[GJS]:@",tmpFi.name());

                }
            }
        },10,time);
    }
    @Override
    public void init() {
        Log.info("[GJS][Start]");
        serverFi= Vars.mods.getMod(gjsPlugin.class).file.parent().parent();
        LogFi=serverFi.child("js-logs");
        if(!LogFi.exists()){
            LogFi.mkdirs();
        }
        Log.info("[GJS][will log in @]",LogFi.path());
        startLogTime();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("gjs","<env> <args...>","run js with Graaljs",(args) ->{
            var env=args[0];
            var str=new StringBuilder();
            for(int k=1;k<args.length;k++){
                str.append(args[k]);
            }
            if(!envs.containsKey(env)) {
                String logFile=LogFi.child("log-"+env+ ".nlog").absolutePath();
                envs.put(env,Context.newBuilder("js").option("log.file",logFile).build());
                Log.info("[GJS]:[New Environment<@>Start]",env);
                Log.info("[GJS]:[Log in @ ]",logFile);
            }
            var context=envs.get(env);
            try {
                var res = context.eval("js", str.toString());
                Log.info("[GJS]:@", res.toString());
            }catch(PolyglotException error){
                Log.err("[GJS]:@",error.getMessage());
            }
        });
        handler.register("gjp","[class/packet] [env] [data]","",(args)->{
            if(!envs.containsKey(args[1])){
                Log.err("[GJP][No Env]");
                return;
            }
            var env=envs.get(args[1]);
            var data=args[2];
            switch (args[0]){
                case "class":
                    try {
                        var tmp=Class.forName(data);
                        env.getBindings("js").putMember(data,tmp);

                    } catch (ClassNotFoundException e) {
                        Log.err("[GJP][Class][@]",e.getMessage());
                    }
                    break;
                case "packet":

                    break;
                default:
                    Log.err("[GJP][No Action]");
            }
        });
    }
}
