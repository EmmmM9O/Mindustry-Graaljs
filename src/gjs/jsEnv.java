package gjs;

import arc.files.Fi;
import arc.func.Func;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class jsEnv {
    private static Integer count=0;
    private final Integer id=count++;
    private static HashMap<Integer,jsEnv> envs=new HashMap<>();

    public static HashMap<Integer, jsEnv> getEnvs() {
        return envs;
    }

    public static int getCount() {
        return count;
    }

    public int getId(){
        return id;
    }
    private final Context context;
    protected Handler logHandler=new Handler() {
        @Override
        public void publish(LogRecord record) {
            if(logDir==null) return;
            var file=logDir.child("js-env-"+id+"-"+ LocalDate.now()+".log");
            file.writeString(record.getMessage(),true);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

    };
    public Fi logDir;
    public jsEnv(){
        logDir=null;
        context= Context.newBuilder("js").logHandler(logHandler).build();
    }
    public jsEnv(boolean allow){
        logDir=null;
         context= Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
    }
    public jsEnv(Fi path,boolean allow){
        logDir=path;
        context=Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
    }
    public jsEnv(Fi path){
    logDir=path;
    context=Context.newBuilder("js").logHandler(logHandler).build();
    }
    public jsEnv(Fi path, Func<Context.Builder, Context.Builder> build){
        logDir=path;
        context=build.get(Context.newBuilder("js").logHandler(logHandler)).build();
    }
    public jsEnv(Func<Context.Builder, Context.Builder> build){
        logDir=null;
        context=build.get(Context.newBuilder("js").logHandler(logHandler)).build();
    }
    public Value eval(String str) throws PolyglotException {
        return context.eval("js",str);
    }
    public void clean(){
        context.close();
    }

}
