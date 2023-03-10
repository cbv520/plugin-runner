package mux.plugin.runner.functions;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FunctionManager {

    private static final String wordRegex = "[a-zA-Z][a-zA-Z0-9]*";
    private static final String functionNameRegex = wordRegex + ":" + wordRegex;
    private static final String functionCallRegex = "^("+functionNameRegex+")\\((.*)\\)$";
    private static final Pattern functionCallPattern = Pattern.compile(functionCallRegex);

    private final Map<String, Function<Object, Object>> functions = new HashMap<>();

    public <T,R> void register(String functionName, Function<T,R> function) {
        log.info("Adding function: {}", functionName);
        functions.put(functionName, (Function<Object, Object>) function);
    }

    public Object call(String functionName, Object arg) {
        var function = functions.get(functionName);
        if (function == null) {
            return null;
        }
        log.debug("Calling {}({})", functionName, arg);
        return functions.get(functionName).apply(arg);
    }

    public Object parse(String str) {
        Matcher m = functionCallPattern.matcher(str);
        if (!m.matches()) {
            return null;
        }
        return call(m.group(1), m.group(2));
    }
}
