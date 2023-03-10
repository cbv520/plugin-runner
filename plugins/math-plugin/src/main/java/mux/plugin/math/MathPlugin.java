package mux.plugin.math;

import com.google.code.mathparser.MathParser;
import com.google.code.mathparser.MathParserFactory;
import lombok.extern.slf4j.Slf4j;
import mux.plugin.runner.MuxContext;
import mux.plugin.runner.MuxPlugin;

@Slf4j
public class MathPlugin implements MuxPlugin {

    MathParser mathParser = MathParserFactory.create();

    @Override
    public void init(MuxContext ctx) {
        log.info("Hi from math! {}", ctx);
        ctx.getFunctionManager().register("math:eval", this::evaluate);
        ctx.getFunctionManager().register("math:evaluate", this::evaluate);
    }

    public double evaluate(String expression) {
        return mathParser.calculate(expression).doubleValue();
    }
}
