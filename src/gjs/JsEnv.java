package gjs;

import arc.files.Fi;
import arc.func.Func;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class JsEnv {
    private static Integer count = 0;
    private final Integer id = count++;
    private static final HashMap<Integer, JsEnv> envs = new HashMap<>();

    public static HashMap<Integer, JsEnv> getEnvs() {
        return envs;
    }

    public static int getCount() {
        return count;
    }

    public int getId() {
        return id;
    }

    private final Context context;
    protected Handler logHandler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (logDir == null) return;
            var file = logDir.child("js-env-" + id + "-" + LocalDate.now() + ".log");
            file.writeString(record.getMessage(), true);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

    };
    public Fi logDir;

    public JsEnv() {
        logDir = null;
        context = Context.newBuilder("js").logHandler(logHandler).build();
        put();
    }

    public JsEnv(boolean allow) {
        logDir = null;
        context = Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
        put();
    }

    public JsEnv(Fi path, boolean allow) {
        logDir = path;
        context = Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
        put();
    }

    public JsEnv(Fi path) {
        logDir = path;
        context = Context.newBuilder("js").logHandler(logHandler).build();
        put();
    }

    public JsEnv(Fi path, Func<Context.Builder, Context.Builder> build) {
        logDir = path;
        context = build.get(Context.newBuilder("js").logHandler(logHandler)).build();
        put();
    }

    public JsEnv(Func<Context.Builder, Context.Builder> build) {
        logDir = null;
        context = build.get(Context.newBuilder("js").logHandler(logHandler)).build();
        put();
    }

    private void put() {
        envs.put(id, this);
    }

    public Value eval(String str) throws PolyglotException {
        return context.eval("js", str);
    }

    public void clean() {
        context.close();
        envs.put(id, null);
    }

    public void ImportPath(String pname) throws PolyglotException {
        var list = pname.split("\\.");
        StringBuilder str = new StringBuilder();
        boolean flag = true;
        for (var i : list) {
            str.append(i);
            String res = "";
            try {
                res = eval(str.toString()).toString();
            } catch (PolyglotException err) {
                eval((flag ? "var " : "") + str + " = {}");
            }
            if (Objects.equals(res, "undefined")) eval((flag ? "var " : "") + str + " = {}");
            str.append('.');
            flag = false;
        }
    }

    public void Import(String className) throws PolyglotException, ClassNotFoundException {
        Import(Class.forName(className));
    }

    public void Import(Class<?> c) throws PolyglotException {
        var packet = c.getPackage();
        ImportPath(packet.getName());
        eval(c.getName() + " = Java.type('" + c.getName() + "')");
    }

    public void putMember(String str, Object obj) {
        context.getBindings("js").putMember(str, obj);
    }

    public void importPackage(Package p) {
        importPackage(p.getName());
    }

    public void importPackage(String packageName) throws RuntimeException {
        ImportPath(packageName);
        try {
            try (ScanResult scanResult = new ClassGraph().whitelistPackages(packageName).scan()) {
                scanResult.getAllClasses().forEach((ClassInfo classInfo) -> {
                    try {
                        eval(packageName + '.' + classInfo.getSimpleName() + " = Java.type('" + packageName + "." + classInfo.getSimpleName() + "')");
                    } catch (PolyglotException e) {
                        throw new RuntimeException("[GJS][Import][Js Error][" + e.getMessage() + "]");
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void putImport() {
        context.getBindings("js").putMember("GJS",new GJS(this));
        eval("function Import(path){GJS.Import(path)}");
    }
    public void eraseAll(){
        var bindings = context.getBindings("js");
        for(String key:bindings.getMemberKeys()){

            bindings.putMember(key,null);
        }
    }
    public Map<String,Value> Members(){
        var bindings=context.getBindings("js");
        Map<String,Value> map=new HashMap<>();
        for(String key:bindings.getMemberKeys()){
            map.put(key,bindings.getMember(key));
        }
        return map;
    }
    public void putEnv(){
    	context.getBindings("js").putMember("__ENV__",this);
    }
    public static class GJS {
        private final JsEnv env;

        public GJS(JsEnv e) {
            env = e;
        }

        public String Import(String str) {
            if (str.endsWith(".*")) {
                var packageName = str.substring(0, str.length() - 2);
                env.importPackage(packageName);
                return "[success][import][" + str + "]";
            } else {
                try {
                    env.Import(Class.forName(str));
                    return "[success][import][" + str + "]";
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("[GJS][Import][No find class][" + str + "]");
                } catch (PolyglotException e) {
                    throw new RuntimeException("[GJS][Import][Js Error][" + e.getMessage() + "]");
                }
            }

        }
    }

}
