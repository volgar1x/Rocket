package org.rocket.dist;

import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JarRocket loads resource from a JAR file.
 */
public class JarRocket extends Rocket {
    private final JarFile file;

    public JarRocket(JarFile file) {
        this.file = file;
    }

    @Override
    public Config getConfig() {
        try {
            JarEntry entry = file.getJarEntry("application.conf");
            InputStream is = file.getInputStream(entry);
            return ConfigFactory.parseReader(new InputStreamReader(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Module getModule() {
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{
                    new URL(file.getName())});

            Attributes main = file.getManifest().getMainAttributes();
            String moduleClassName = (String) main.get("Module-Class");

            Class<?> moduleClass = loader.loadClass(moduleClassName);
            if (!Module.class.isAssignableFrom(moduleClass)) {
                throw new IllegalStateException(String.format(
                    "%s from %s is not a module",
                        moduleClass,
                        file.getName()));
            }

            return (Module) moduleClass.newInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format(
                "Could not find module of %s",
                    file.getName()));
        } catch (InstantiationException|IllegalAccessException e) {
            throw new IllegalStateException(String.format(
                "Could not create module of %s",
                    file.getName()));
        }
    }
}
