package com.infostretch.labs.actions;

import com.infostretch.labs.transformers.Transformer;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ConvertSecretJobActionTest {

    private String convertJob(String xmlPath) {
        InputStream is = null;
        try {
            is = getClass().getResource(xmlPath).openStream();
            Transformer t = new Transformer(new HashMap());
            String script = t.transformXml(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is), "test");
            script = script.replaceAll("\\s","");
            System.out.println(script);
            return script;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void transformBuild() {
        String expectedConversion = "// Powered by Infostretch node () { stage ('test - Build') { // Shell build step sh ''' echo \"hello\" ''' }\n"
                                  + "stage ('test - Postbuild actions') {"
                                  + "\n/*\nPlease note this is a direct conversion of post-build actions. It may not necessarily work/behave in the same way as post-build actions work. A logic review is suggested.\n*/"
                                  + "\n\t\t// ExtendedEmailPublisher notification"
                                  + "\n} }";
        // XXX This is not the right end result...

        expectedConversion = expectedConversion.replaceAll("\\s","");
        String result = convertJob("../../../../xml/secret-freestyle-config.xml");
        System.out.println(result);
        assertThat(convertJob("../../../../xml/secret-freestyle-config.xml"), containsString(expectedConversion));
    }
}
