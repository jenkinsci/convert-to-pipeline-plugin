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
 * Handles transformation of Ant Plugin properties
 *
 * @author Mohit Dharamshi
 */
public class Ant extends Plugins {

    public Ant(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transformBuild() {
        transformer.buildSteps.append("\t\t// Ant build step");
        StringBuffer antTargets = new StringBuffer();
        StringBuffer withEnv = new StringBuffer();
        String ant_Name = "";
        Element targets = getElementByTag("targets");
        Element antName = getElementByTag("antName");
        Element antOpts = getElementByTag("antOpts");
        Element buildFile = getElementByTag("buildFile");
        Element properties = getElementByTag("properties");

        if (antOpts != null && antOpts.getTextContent().length() > 0) {
            antTargets.append(antOpts.getTextContent() + " ");
        }
        if (buildFile != null && buildFile.getTextContent().length() > 0) {
            antTargets.append("-buildfile " + buildFile.getTextContent() + " ");
        }
        if (properties != null && properties.getTextContent().length() > 0) {
            antTargets.append(Pattern.compile("\n", Pattern.MULTILINE).matcher(Pattern.compile("^", Pattern.MULTILINE).matcher(Pattern.compile("^#.*", Pattern.MULTILINE).matcher(properties.getTextContent()).replaceAll("").trim()).replaceAll("-D").trim()).replaceAll(" ").trim() + " ");
        }
        if (targets != null && targets.getTextContent().length() > 0) {
            antTargets.append(targets.getTextContent() + " ");
        }
        if (antName != null && !antName.getTextContent().equals("(Default)")) {
            ant_Name = antName.getTextContent();
        }
        if (ant_Name.length() > 0) {
            withEnv.append("\"PATH+ANT=${tool '" + ant_Name + "'}/bin\"");
        }

        if (withEnv.length() > 0) {
            appendBuildSteps("\n\twithEnv([\"PATH+ANT=${tool '" + ant_Name + "'}/bin\"]) { \n \t\t\tif(isUnix()) {\n \t\t\t\tsh \"ant " + antTargets + "\" \n\t\t\t} else { \n \t\t\t\tbat \"ant " + antTargets + "\" \n\t\t\t} \n \t\t}");
        } else {
            appendBuildSteps("\n \t\t\tif(isUnix()) {\n \t\t\t\tsh \"ant " + antTargets + "\" \n\t\t\t} else { \n \t\t\t\tbat \"ant " + antTargets + "\" \n\t\t\t}");
        }
    }
}
