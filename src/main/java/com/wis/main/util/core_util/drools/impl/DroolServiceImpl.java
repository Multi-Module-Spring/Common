package com.wis.main.util.core_util.drools.impl;


import com.wis.main.util.core_util.drools.DroolService;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DroolServiceImpl implements DroolService {
    private KieContainer kieContainer;

    @Value("${drools.route.rule-path}")
    private String rulePath;

    @Override
    public KieContainer getKieContainer() {
        return this.kieContainer;
    }


    @Override
    public void buildKieContainer() {
        buildKieContainer(null);
    }

    @Override
    public void buildKieContainer(String id) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        Path ruleDir = locateDroolsPath(rulePath);
        System.out.println("[Drools] Finding rule at: " + ruleDir.toAbsolutePath());

        try {
            if (!Files.exists(ruleDir)) {
                Files.createDirectories(ruleDir);
                System.out.println("[Drools] Created directory: " + ruleDir);
            }

            Stream<Path> rulePaths = Files.walk(ruleDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".drl"))
                    .filter(p -> id == null || p.getFileName().toString().contains(id));

            rulePaths.forEach(path -> {
                try {
                    String content = Files.readString(path);
                    kfs.write("src/main/resources/" + path.getFileName(), content);
                    System.out.println("[Drools] Loaded rule: " + path.getFileName());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read rule: " + path, e);
                }
            });
            rulePaths.close();

            KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
            if (kb.getResults().hasMessages(Message.Level.ERROR)) {
                kb.getResults().getMessages(Message.Level.ERROR)
                        .forEach(msg -> System.err.println("Drools Error: " + msg.getText()));
                throw new RuntimeException("Drools rule compilation failed!");
            }

            this.kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        } catch (IOException e) {
            throw new RuntimeException("Error building KieContainer: " + e.getMessage(), e);
        }
    }

}