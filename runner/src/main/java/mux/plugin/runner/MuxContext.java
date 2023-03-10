package mux.plugin.runner;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import mux.eventbus.EventBus;
import mux.plugin.runner.functions.FunctionManager;

@RequiredArgsConstructor
@Data
public class MuxContext {
    private final String value;
    private final EventBus eventBus = new EventBus();
    private final FunctionManager functionManager = new FunctionManager();
}
