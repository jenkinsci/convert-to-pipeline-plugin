/*******************************************************************************
 * Copyright 2017 Infostretch Corporation
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 *
 * You should have received a copy of the GNU General Public License along with this program in the name of LICENSE.txt in the root folder of the distribution. If not, see https://opensource.org/licenses/gpl-3.0.html
 *
 *
 * For any inquiry or need additional information, please contact labs_support@infostretch.com
 *******************************************************************************/

package com.infostretch.labs.plugins;

import com.infostretch.labs.transformers.Transformer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

/**
 * Handles transformation of Maven Plugin properties
 *
 * @author Mohit Dharamshi
 */
public class Maven extends Plugins {

    public Maven(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transformBuild() {
        appendBuildSteps("\t\t// Maven build step");
        Element settingsPath = null;
        Element globalSettingsPath = null;
        StringBuffer mvnParams = new StringBuffer();
        StringBuffer mvnTargets = new StringBuffer();
        String mavenParams = "";
        Element targets  = getElementByTag("targets");
        Element mavenName = getElementByTag("mavenName");
        Element jvmOptions = getElementByTag("jvmOptions");
        Element pom = getElementByTag("pom");
        Element properties = getElementByTag("properties");
        Element usePrivateRepository = getElementByTag("usePrivateRepository");
        Element injectBuildVariables = getElementByTag("injectBuildVariables");
        Element settings = getElementByTag("settings");
        Element globalSettings = getElementByTag("globalSettings");

        if (settings != null) {
            settingsPath = getElementByTag(settings, "path");
        }
        if (globalSettings != null) {
            globalSettingsPath = getElementByTag(globalSettings, "path");
        }
        if (transformer.jdk != null && !transformer.jdk.getTextContent().equals("(System)")) {
            mvnParams.append(" jdk: '" + transformer.jdk.getTextContent() + "',");
        }
        if (mavenName != null) {
            mvnParams.append(" maven: '" + mavenName.getTextContent() + "',");
        }
        if (jvmOptions != null) {
            mvnParams.append(" mavenOpts: '" + jvmOptions.getTextContent() + "',");
        }
        if (usePrivateRepository != null && usePrivateRepository.getTextContent().equals("true")) {
            mvnParams.append(" mavenLocalRepo: \"$WORKSPACE/.repository\",");
        }
        if (settingsPath != null) {
            mvnParams.append(" mavenSettingsFilePath: '" + settingsPath.getTextContent() + "',");
        }
        if (globalSettingsPath != null) {
            mvnParams.append(" globalMavenSettingsFilePath: '" + globalSettingsPath.getTextContent() + "',");
        }
        if (injectBuildVariables != null) {
            // s_injectBuildVariables =
            // injectBuildVariables.getTextContent();
        }
        if (pom != null) {
            mvnTargets.append("-f " + pom.getTextContent() + " ");
        }
        if (properties != null) {
            mvnTargets.append(Pattern.compile("\n", Pattern.MULTILINE).matcher(Pattern.compile("^", Pattern.MULTILINE).matcher(Pattern.compile("^#.*", Pattern.MULTILINE).matcher(properties.getTextContent()).replaceAll("").trim()).replaceAll("-D").trim()).replaceAll(" ").trim() + " ");
        }
        if (targets != null) {
            mvnTargets.append(targets.getTextContent() + " ");
        }
        if (mvnParams.length()> 0 && String.valueOf(mvnParams.charAt(mvnParams.length() - 1)).equals(",")) {
            mavenParams = mvnParams.substring(0, mvnParams.length() - 1).trim();
        } else {
            mavenParams = mvnParams.toString().trim();
        }
        if (mavenParams.length() > 0) {
            appendBuildSteps("\n\twithMaven(" + mavenParams + ") { \n \t\t\tif(isUnix()) {\n \t\t\t\tsh \"mvn " + mvnTargets + "\" \n\t\t\t} else { \n \t\t\t\tbat \"mvn " + mvnTargets + "\" \n\t\t\t} \n \t\t}");
        } else {
            appendBuildSteps("\n\twithMaven { \n \t\t\tif(isUnix()) {\n \t\t\t\tsh \"mvn " + mvnTargets + "\" \n\t\t\t} else { \n \t\t\t\tbat \"mvn " + mvnTargets + "\" \n\t\t\t} \n \t\t}");
        }
    }
}
