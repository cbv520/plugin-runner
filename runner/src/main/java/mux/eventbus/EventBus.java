package mux.eventbus;

import lombok.AllArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EventBus {

    private final Map<Class<?>, Sinks.Many<Event>> eventSinks = new HashMap<>();
    private final ThreadLocal<Queue<EventDispatch>> queue = ThreadLocal.withInitial(LinkedList::new);
    private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

    public void publish(Event event) {
        var sinks = getSinks(event.getClass());
        if (sinks.size() > 0) {
            var queue = this.queue.get();
            queue.offer(new EventDispatch(event, sinks));
            if (!dispatching.get()) {
                dispatching.set(true);
                EventDispatch eventDispatch = queue.poll();
                while (eventDispatch != null) {
                    eventDispatch.dispatch();
                    eventDispatch = queue.poll();
                }
                dispatching.set(false);
            }
        }
    }

    public <T extends Event> void subscribe(Class<T> eventClass, Consumer<T> consumer) {
        forEventType(eventClass).subscribe(consumer);
    }

    public <T extends Event> Flux<T> forEventType(Class<T> eventClass) {
        return (Flux<T>) eventSinks
                .computeIfAbsent(eventClass, k -> Sinks.many().multicast().onBackpressureBuffer(1000))
                .asFlux();

    }

    private List<Sinks.Many<Event>> getSinks(Class<?> eventType) {
        return getAllSuperClassesAndInterfaces(eventType).stream()
                .map(eventSinks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<Class<?>> getAllSuperClassesAndInterfaces(Class<?> clazz) {
        return getAllSuperClassesAndInterfaces(clazz, new LinkedHashSet<>());
    }

    private Set<Class<?>> getAllSuperClassesAndInterfaces(Class<?> clazz, Set<Class<?>> classSet) {
        while (clazz != null) {
            classSet.add(clazz);
            var interfaces = clazz.getInterfaces();
            Collections.addAll(classSet, interfaces);
            for (var directInterface : interfaces) {
                getAllSuperClassesAndInterfaces(directInterface, classSet);
            }
            clazz = clazz.getSuperclass();
        }
        return classSet;
    }

    @AllArgsConstructor
    private static class EventDispatch {

        private final Event event;
        private final List<Sinks.Many<Event>> sinks;

        public void dispatch() {
            sinks.forEach(sink -> sink.tryEmitNext(event));
        }
    }
}