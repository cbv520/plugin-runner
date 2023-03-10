package mux.eventbus.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import mux.eventbus.Event;

@RequiredArgsConstructor
@Data
public class InputEvent implements Event {
    private final String input;
}
