package com.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.kotcrab.vis.ui.VisUI;
import com.runner.exception.LibgdxInitException;
import com.runner.util.ConditionWaiter;
import com.uwsoft.editor.Overlap2D;
import com.uwsoft.editor.utils.AppConfig;
import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class LibgdxRunner extends BlockJUnit4ClassRunner {
    private Random random = new Random();
    private static File prefs;
    private static AtomicBoolean init = new AtomicBoolean(false);
    private LwjglFrame mainFrame;

    public LibgdxRunner(Class<?> klass) throws InitializationError {
        super(klass);
        if (init.compareAndSet(false, true)) {
            initApplication();
        }
    }

    private void initApplication() {
        try {
            /*
            JglfwApplicationConfiguration cfg = new JglfwApplicationConfiguration();
            cfg.preferencesLocation = String.format("tmp/%d/.prefs/", random.nextLong());
            cfg.title = "Libgdx Runner";
            cfg.width = 1;
            cfg.height = 1;
            cfg.forceExit = true;
            new JglfwApplication(new TestApplicationListener(), cfg);
            ConditionWaiter.wait(() -> Gdx.files != null, "Jglfw init failed.", 10);
            prefs = new File(Gdx.files.getExternalStoragePath(), "tmp/");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                safeCleanDir();
                closeGdxApplication();
            }));
            */
            Overlap2D overlap2D = new Overlap2D();
            LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
            cfg.preferencesDirectory = String.format("tmp/%d/.prefs/", random.nextLong());
            cfg.title = "Libgdx Runner";
            cfg.width = 1;
            cfg.height = 1;
            cfg.forceExit = true;
            cfg.backgroundFPS = 0;
            mainFrame = new LwjglFrame(overlap2D, cfg);
            mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            prefs = new File(Gdx.files.getExternalStoragePath(), "tmp/");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                safeCleanDir();
                closeGdxApplication();
            }));
            toggleVisible();

        } catch (Exception ex) {
            throw new LibgdxInitException(ex);
        }
    }
    private void toggleVisible() {
        mainFrame.setVisible(!mainFrame.isVisible());
        if (mainFrame.isVisible()) {
            mainFrame.toFront();
            mainFrame.requestFocus();
            mainFrame.setAlwaysOnTop(true);
            try {
                //remember the last location of mouse
                final Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();

                //simulate a mouse click on title bar of window
                Robot robot = new Robot();
                robot.mouseMove(mainFrame.getX() + 100, mainFrame.getY() + 5);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                //move mouse to old location
                robot.mouseMove((int) oldMouseLocation.getX(), (int) oldMouseLocation.getY());
            } catch (Exception ex) {
                //just ignore exception, or you can handle it as you want
            } finally {
                mainFrame.setAlwaysOnTop(false);
            }
        }
    }

    private void safeCleanDir() {
        try {
            FileUtils.deleteDirectory(prefs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeGdxApplication() {
        Gdx.app.exit();
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (description.getAnnotation(NeedGL.class) != null) {
            final AtomicBoolean running = new AtomicBoolean(true);
            Gdx.app.postRunnable(() -> {
                if (isIgnored(method)) {
                    notifier.fireTestIgnored(description);
                } else {
                    runLeaf(methodBlock(method), description, notifier);
                }
                running.set(false);
            });
            ConditionWaiter.wait(() -> !running.get(), description, 30, () -> {
                closeGdxApplication();
            });
        } else {
            runLeaf(methodBlock(method), description, notifier);
        }
    }

    private class TestApplicationListener extends ApplicationAdapter {
        @Override
        public void create() {
            VisUI.load(Gdx.files.local("overlap2d/assets/style/uiskin.json"));
        }
    }
}
