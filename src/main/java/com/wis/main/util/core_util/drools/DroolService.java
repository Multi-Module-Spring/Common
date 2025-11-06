package com.wis.main.util.core_util.drools;

import com.wis.i18n.TranslateCommon;
import com.wis.i18n.exception.TranslateCommonException;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface DroolService {

    KieContainer getKieContainer();

    default <T> T executeRule(Object o, Class<T> responseType) {
        KieContainer kieContainer = getKieContainer();
        if (kieContainer == null) {
            throw new TranslateCommonException(HttpStatus.BAD_REQUEST, TranslateCommon.DROOL_NOT_GENERATED);
        }

        try {
            T result = responseType.getDeclaredConstructor().newInstance();

            KieSession session = kieContainer.newKieSession();
            session.insert(o);
            session.insert(result);
            session.fireAllRules();
            session.dispose();

            return result;

        } catch (Exception e) {
            throw new TranslateCommonException(HttpStatus.INTERNAL_SERVER_ERROR, TranslateCommon.DROOL_EXECUTION_ERROR, e.getMessage());
        }
    }

    default Path locateDroolsPath(String rulePath) {
        try {
            String prefixedPath = "drools/" + rulePath;
            Path basePath;

            File classesDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classesDir.isDirectory()) {
                basePath = classesDir.toPath().resolve(prefixedPath);
            } else {
                basePath = Paths.get(System.getProperty("user.dir")).resolve(prefixedPath);
            }

            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }

            return basePath;
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve Drools path for: " + rulePath, e);
        }
    }


    void buildKieContainer();

    void buildKieContainer(String id);
}
