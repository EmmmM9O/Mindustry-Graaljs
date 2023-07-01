package gjs;

import arc.files.Fi;
import arc.func.Cons;
import arc.func.Func;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.ClassGraph;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class jsEnv {
    private static Integer count = 0;
    private final Integer id = count++;
    private static final HashMap<Integer, jsEnv> envs = new HashMap<>();

    public static HashMap<Integer, jsEnv> getEnvs() {
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

    public jsEnv() {
        logDir = null;
        context = Context.newBuilder("js").logHandler(logHandler).build();
        put();
    }

    public jsEnv(boolean allow) {
        logDir = null;
        context = Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
        put();
    }

    public jsEnv(Fi path, boolean allow) {
        logDir = path;
        context = Context.newBuilder("js").allowAllAccess(allow).logHandler(logHandler).build();
        put();
    }

    public jsEnv(Fi path) {
        logDir = path;
        context = Context.newBuilder("js").logHandler(logHandler).build();
        put();
    }

    public jsEnv(Fi path, Func<Context.Builder, Context.Builder> build) {
        logDir = path;
        context = build.get(Context.newBuilder("js").logHandler(logHandler)).build();
        put();
    }

    public jsEnv(Func<Context.Builder, Context.Builder> build) {
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
            try {
                eval(str.toString());
            } catch (PolyglotException err) {
                eval((flag ? "var " : "") + str + " = {}");
            }
            str.append('.');
            flag = false;
        }
    }
    public void Import(String className) throws PolyglotException,ClassNotFoundException{
        Import(Class.forName(className));
    }
    public void Import(Class<?> c) throws PolyglotException {
        var packet = c.getPackage();
        ImportPath(packet.getName());
        eval(c.getName() + " = Java.type(" + c.getName() + ")");
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
                        eval(packageName + '.' + classInfo.getSimpleName() + " = Java.type(" + packageName + "." + classInfo.getSimpleName() + ")");
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
        Cons<String> func = (String str) -> {
            if (str.endsWith(".*")) {
                var packageName = str.substring(0, str.length() - 2);
                importPackage(packageName);
            } else {
                try {
                    Import(Class.forName(str));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("[GJS][Import][No find class][" + str + "]");
                } catch (PolyglotException e) {
                    throw new RuntimeException("[GJS][Import][Js Error][" + e.getMessage() + "]");
                }
            }
        };
        context.getBindings("js").putMember("Import", context.asValue(func));
    }
}
