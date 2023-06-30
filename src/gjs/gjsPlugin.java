package gjs;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;
import org.graalvm.polyglot.Context;

public class gjsPlugin extends Plugin {
    @Override
    public void init() {
        Log.info("[Mod][Start]");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("gjs","<args...>","run js with Graaljs",(args, parameter) ->{
            var str=new StringBuilder();
            for(var i:args){
                str.append(i);
            }
            Context context=Context.newBuilder("js").build();
            var res=context.eval("js",str.toString());
            Log.info("[GJS]:@",res.toString());
        });
    }
}
