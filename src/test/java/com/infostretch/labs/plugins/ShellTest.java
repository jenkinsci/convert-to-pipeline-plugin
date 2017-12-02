package com.infostretch.labs.plugins;

import com.infostretch.labs.transformers.Transformer;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ShellTest {

    private String convertJob(String xmlPath) {
        InputStream is = null;
        try {
            is = getClass().getResource(xmlPath).openStream();
            Transformer t = new Transformer(new HashMap());
            String script = t.transformXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is), "test");
            System.out.println(script);
            return script;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void transformBuild() {
        String expectedConversion = "sh '''#!/bin/bash\n" +
                "shell_var=123\n" +
                "other_var=${shell_var}  # this should not be expanded by groovy! \n" +
                "'''";
        assertThat(convertJob("../../../../xml/shell-script-simple.xml"), containsString(expectedConversion));
    }

    @Test
    public void transformBuildWithUnstable() {
        String expectedConversion = "def shellReturnStatus = sh returnStatus: true, script: '''#!/bin/bash\n" +
                "echo ${var}\n" +
                "'''\n" +
                " if(shellReturnStatus == 2) { currentBuild.result = 'UNSTABLE' } ";
        assertThat(convertJob("../../../../xml/shell-script-unstable-return.xml"), containsString(expectedConversion));
    }
}