package fr.gouv.vitamui.ui.commons;

import fr.gouv.vitamui.commons.api.application.AbstractVitamUIApplication;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class StartTest extends AbstractVitamUIApplication {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(StartTest.class);

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(StartTest.class, args);
        ctx.close();
    }

    @PostConstruct
    private void init() {
        LOGGER.info("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
        try {
            LOGGER.info("Spring Boot - Module: {}.", getModuleName());
            LOGGER.info("Spring Boot - Logger Message preprend: {}.",
                    ServerIdentityConfiguration.getInstance().getLoggerMessagePrepend());
            LOGGER.info("Spring Boot : {}.", ServerIdentityConfiguration.getInstance());
        }
        catch (final InternalServerException | NullPointerException exception) {
            // do nothing
        }
    }
}
