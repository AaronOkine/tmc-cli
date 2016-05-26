package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class LoginCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);
    private final Io io;
    private final Options options;
    private Application app;

    // TODO: TerminalIo should be injected in constructor
    public LoginCommand(Application app) {
        this.app = app;
        this.io = new TerminalIo();
        this.options = new Options();
        options.addOption("u", "user", true, "TMC username");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("s", "server", true, "Address for TMC server");
    }

    @Override
    public String getDescription() {
        return "Login to TMC server.";
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public void run(String[] args) {
        CommandLine line = parseData(args);
        String username = getUsername(line);
        String password = getPassword(line);
        String serverAddress = getServerAddress(line);

        Settings settings = new Settings(serverAddress, username, password);
        if (loginPossible(settings) && saveLoginSettings(settings)) {
            io.println("Login succesful.");
        }
    }

    private CommandLine parseData(String[] args) {
        GnuParser parser = new GnuParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.warn("Unable to parse username or password.", e);
        }
        return null;
    }

    private String getUsername(CommandLine line) {
        String username = line.getOptionValue("u");
        if (username == null) {
            username = io.readLine("username: ");
        }
        return username;
    }

    private String getPassword(CommandLine line) {
        String password = line.getOptionValue("p");
        if (password == null) {
            password = io.readPassword("password: ");
        }
        return password;
    }

    private String getServerAddress(CommandLine line) {
        String serverAddress = line.getOptionValue("s");
        if (serverAddress == null) {
            // todo: don't hardcode the default value, get it from somewhere
            serverAddress = "https://tmc.mooc.fi/staging";
        }
        return serverAddress;
    }

    private boolean saveLoginSettings(Settings settings) {
        SettingsIo settingsIo = new SettingsIo();
        if (settingsIo.save(settings)) {
            return true;
        } else {
            io.println("Login failed.");
            return false;
        }
    }

    /**
     * Try to contact TMC server. If successful, user exists.
     *
     * @return True if user exist
     */
    private boolean loginPossible(Settings settings) {
        app.createTmcCore(settings);
        TmcCore core = this.app.getTmcCore();
        Callable<List<Course>> callable = core.listCourses(
                ProgressObserver.NULL_OBSERVER);

        try {
            callable.call();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof FailedHttpResponseException) {
                FailedHttpResponseException httpEx
                        = (FailedHttpResponseException) cause;
                if (httpEx.getStatusCode() == 401) {
                    io.println("Incorrect username or password.");
                    return false;
                }
            }
            
            logger.error("Unable to connect to server", e);
            io.println("Unable to connect to server "
                    + settings.getServerAddress());
            return false;
        }

        return true;
    }
}
