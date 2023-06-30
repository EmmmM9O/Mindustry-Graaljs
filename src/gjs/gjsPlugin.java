package gjs;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;

import java.util.HashMap;

public class gjsPlugin extends Plugin {
    public HashMap<String,Context> envs=new HashMap<>();
    @Override
    public void init() {
        Log.info("[Mod][Start]");
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
                envs.put(env,Context.newBuilder("js").build());
                Log.info("[GJS]:[New Environment Start]");
            }
            var context=envs.get(env);
            try {
                var res = context.eval("js", str.toString());
                Log.info("[GJS]:@", res.toString());
            }catch(PolyglotException error){
                Log.err("[GJS]:@",error.getMessage());
            }
        });
    }
}
