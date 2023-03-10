package mux.plugin.wiki;

import lombok.extern.slf4j.Slf4j;
import mux.eventbus.events.InputEvent;
import mux.plugin.runner.MuxContext;
import mux.plugin.runner.MuxPlugin;

@Slf4j
public class WikiPlugin implements MuxPlugin {
    @Override
    public void init(MuxContext ctx) {
        log.info("Hi from wiki! {}", ctx);
        ctx.getEventBus().subscribe(InputEvent.class, e -> {
            log.info("Checking wikipedia for articles about {}", e.getInput());
        });
        ctx.getFunctionManager().register("wiki:summarize", this::summarize);
    }

    public String summarize(Object article) {
        return article.toString() + " is a bitch.";
    }
}
