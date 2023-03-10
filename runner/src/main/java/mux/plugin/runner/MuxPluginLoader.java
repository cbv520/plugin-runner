package mux.plugin.runner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class MuxPluginLoader {

	private static final String PLUGINS_LIST_FILENAME = "mux.plugins";

	private final Queue<MuxPlugin> uninitializedPlugins = new LinkedList<>();
	private final Queue<MuxPlugin> initializedPlugins = new LinkedList<>();
	private final MuxContext ctx;

	public MuxPluginLoader(MuxContext ctx) {
		this.ctx = ctx;
	}

	public void initialize() {
		log.info("Initializing plugins: {}", uninitializedPlugins.stream().map(p -> p.getClass().getCanonicalName()).collect(Collectors.toList()));
		MuxPlugin plugin = uninitializedPlugins.poll();
		while (plugin != null) {
			plugin.init(ctx);
			initializedPlugins.add(plugin);
			plugin = uninitializedPlugins.poll();
		}
	}

	public void loadPluginsFromDir(String dir) {
		log.info("Looking for plugins in directory: {}", dir);
		Arrays.stream(Objects.requireNonNull(new File(dir).listFiles()))
				.map(File::toURI)
				.filter(uri -> uri.getPath().endsWith(".jar"))
				.map(this::loadPlugInFromJar)
				.forEach(uninitializedPlugins::addAll);
	}

	public List<MuxPlugin> loadPlugInFromJar(URI uri) {
		List<MuxPlugin> foundPlugins = new ArrayList<>();
		if (!uri.toString().endsWith(".jar")) {
			log.error("URI {} does not refer to a jar", uri);
			return foundPlugins;
		}
		try {
			log.info("Loading from jar {}", uri);
			ClassLoader loader = new URLClassLoader(new URL[]{ uri.toURL() });
			var is = loader.getResourceAsStream(PLUGINS_LIST_FILENAME);
			if (is == null) {
				log.error("{} missing {} file", uri, PLUGINS_LIST_FILENAME);
				return foundPlugins;
			}
			try (var isr = new InputStreamReader(is);
				 var br = new BufferedReader(isr)) {
				String className = br.readLine();
				while (className != null) {
					try {
						if (!className.equals("")) {
							foundPlugins.add(loadClass(className.strip(), loader));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					className = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Found plugins: {}", foundPlugins.stream().map(p -> p.getClass().getCanonicalName()).collect(Collectors.toList()));
		return foundPlugins;
	}

	private MuxPlugin loadClass(String className, ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		Class<MuxPlugin> type = (Class<MuxPlugin>) loader.loadClass(className);
		return type.getConstructor().newInstance();
	}

}
