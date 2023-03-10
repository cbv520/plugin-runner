package mux.plugin.runner;

import lombok.extern.slf4j.Slf4j;
import mux.eventbus.Event;
import mux.eventbus.events.InputEvent;

@Slf4j
public class Main {

    public static void main(String[] args) {
        MuxContext ctx = new MuxContext("runner");
        MuxPluginLoader pluginLoader = new MuxPluginLoader(ctx);
        pluginLoader.loadPluginsFromDir("runner/src/main/resources/plugins");
        pluginLoader.initialize();
        ctx.getEventBus().subscribe(InputEvent.class, e -> {
            var result = ctx.getFunctionManager().parse(e.getInput());
            if (result != null) {
                log.info("{}", result);
            }
        });
        ctx.getEventBus().publish(new InputEvent("cruxly"));
        log.info(ctx.getFunctionManager().call("wiki:summarize", "blakey boy").toString());
        log.info(ctx.getFunctionManager().call("math:eval", "6/5").toString());
        ctx.getEventBus().publish(new InputEvent("math:eval(5+1)"));
    }

}
