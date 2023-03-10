package mux.plugin.geo;

import lombok.extern.slf4j.Slf4j;
import mux.eventbus.Event;
import mux.eventbus.events.InputEvent;
import mux.plugin.runner.MuxContext;
import mux.plugin.runner.MuxPlugin;

@Slf4j
public class GeoPlugin implements MuxPlugin {
    @Override
    public void init(MuxContext ctx) {
        log.info("Hi from geo! {}", ctx);
        ctx.getEventBus().subscribe(InputEvent.class, e -> {
            log.info("Checking to see if {} has geo info...", e.getInput());
        });
    }
}
