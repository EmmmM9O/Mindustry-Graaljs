package gjs;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class gjsPlugin extends Plugin {
    @Override
    public void init() {
        Log.info("[Mod][Start]");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("gjs","run js with Graaljs",(args, parameter) ->{
            var str=new StringBuilder();
            for(var i:args){
                str.append(i);
            }
            ScriptEngine eng=new ScriptEngineManager().getEngineByName("js");
            try {
                var res=eng.eval(str.toString());
                Log.info("[GLS]:@",res.toString());
            } catch (ScriptException e) {
                Log.err("[GJS]:@",e.getMessage());
            }

        });
    }
}
