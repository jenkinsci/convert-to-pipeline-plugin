package com.infostretch.labs.actions;

import com.infostretch.labs.transformers.Transformer;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ConvertFolderActionTest {
    private String convertJob(String xmlPath) {
        InputStream is = null;
        try {
            is = getClass().getResource(xmlPath).openStream();
            Transformer t = new Transformer(new HashMap());
            String script = t.transformXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is), "testfolder/test");
            script = script.replaceAll("\\s","");
            System.out.println(script);
            return script;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void transformBuild() {
        String expectedConversion = "// Powered by Infostretch timestamps { node () { stage ('testfolder/test - Build') { // Shell build step sh ''' echo \"hello\" ''' } } }";
        expectedConversion = expectedConversion.replaceAll("\\s","");
        assertThat(convertJob("../../../../xml/folder-freestyle-config.xml"), containsString(expectedConversion));
    }
}
