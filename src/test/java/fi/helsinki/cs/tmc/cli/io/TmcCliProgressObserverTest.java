package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Application.class)
public class TmcCliProgressObserverTest {
    private TestIo testIo;

    @Before
    public void setup() {
        this.testIo = new TestIo();
        this.testIo = spy(testIo);
        PowerMockito.mockStatic(Application.class);
        when(Application.getTerminalWidth()).thenReturn(50);
    }

    @Test
    public void progressMessageWorks() {
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(testIo);
        progobs.progress(0, "Hello, world!");
        assertTrue("Prints message", testIo.out().contains(
                "Hello, world!                                     "));
    }

    @Test
    public void progressBarWorks() {
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(testIo);
        progobs.progress(0, 0.5, "Hello, world!");
        assertTrue("Prints message", testIo.out().contains("Hello, world!"));
        assertTrue("Prints the start of the progress bar", testIo.out().contains(
                " 50%["));
        assertTrue("Prints the first part of the progress bar", testIo.out().contains(
                "██████████████████████"));
        assertTrue("Prints the second of the progress bar", testIo.out().contains(
                "░░░░░░░░░░░░░░░░░░░░░░"));
        assertTrue("Prints the end of the progress bar", testIo.out().contains(
                "]"));
    }

    @Test
    public void testResultBarWorks() {
        String string = TmcCliProgressObserver.getPassedTestsBar(1, 2,
                Color.AnsiColor.ANSI_NONE, Color.AnsiColor.ANSI_NONE);
        assertTrue("Prints the start of the progress bar", string.contains(
                " 50%["));
        assertTrue("Prints the first part of the progress bar", string.contains(
                "██████████████████████"));
        assertTrue("Prints the second of the progress bar", string.contains(
                "░░░░░░░░░░░░░░░░░░░░░░"));
        assertTrue("Prints the end of the progress bar", string.contains(
                "]"));
    }

    @Test
    public void shortensLongMessages() {
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(testIo);
        progobs.progress(0,
                "fooooooooooooooooooooooooooooooooooooooooooooooooobar");
        assertTrue("Prints what it's supposed to",
                testIo.out().contains("foooooooooooooooooooooooooooooooooooooooooooooo..."));
        assertTrue("Doesn't print what it's not supposed to",
                !testIo.out().contains("bar"));
    }

    @Test
    public void percentagesWork() {
        assertEquals("  6%", TmcCliProgressObserver.percentage(0.06));
        assertEquals(" 20%", TmcCliProgressObserver.percentage(0.2));
        assertEquals("100%", TmcCliProgressObserver.percentage(1.0));
    }
}
