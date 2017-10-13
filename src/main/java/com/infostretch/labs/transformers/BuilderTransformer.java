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

package com.infostretch.labs.transformers;

import com.infostretch.labs.plugins.Plugins;
import com.infostretch.labs.utils.PluginIgnoredClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;

/**
 * BuilderTransformer handles the conversion of build steps in
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class BuilderTransformer {
    private Transformer transformer;

    /**
     * Initialise local Transformer variable. This constructor is called in main Transformer class.
     *
     * @param transformer Transformer object to be assigned to local Transformer variable.
     */
    protected BuilderTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Converts all build steps in FreeStyle job.
     */
    public void convertBuilders() {

        if (transformer.doc.getElementsByTagName("builders").getLength() > 0) {
            Element builders = (Element) transformer.doc.getElementsByTagName("builders").item(0);
            transformer.buildersList = builders.getChildNodes();
            transformer.buildSteps = new StringBuffer();
            if (transformer.buildersList.getLength() > 0) {
                transformer.buildSteps.append("\n\tstage ('"+transformer.currentJobName+" - Build') {\n \t");
            }
            transformer.jdk = (Element) transformer.doc.getElementsByTagName("jdk").item(0);
            if (transformer.jdk != null && !transformer.jdk.getTextContent().equals("(System)")) {
                transformer.buildSteps.append("\nwithEnv([\"JAVA_HOME=${ tool '\"+JDK+\"' }\", \"PATH=${env.JAVA_HOME}/bin\"]) { \n");
            }
            Element buildWrappers = (Element) transformer.doc.getElementsByTagName("buildWrappers").item(0);
            if (buildWrappers != null) {
                NodeList buildWrappersList = buildWrappers.getChildNodes();
                for (int i = 1; i < buildWrappersList.getLength(); i = i + 2) {
                    Node buildWrapper = buildWrappersList.item(i);
                    try {
                        Class pluginClass = Plugins.getPluginClass(buildWrapper.getNodeName());
                        if(pluginClass != null) {
                            Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, Node.class);
                            Plugins plugin = pluginConstructor.newInstance(transformer, buildWrapper);
                            plugin.transformBuildWrapper();
                        } else {
                            PluginIgnoredClass ignoredPlugin = PluginIgnoredClass.searchByValue(buildWrapper.getNodeName());
                            if(ignoredPlugin == null) {
                                transformer.buildSteps.append("\n// Unable to convert a build step referring to \"" + buildWrapper.getNodeName() + "\". Please verify and convert manually if required.");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = 1; i < transformer.buildersList.getLength(); i = i + 2) {
                Node builder = transformer.buildersList.item(i);
                try {
                    Class pluginClass = Plugins.getPluginClass(builder.getNodeName());
                    if(pluginClass != null) {
                        Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, Node.class);
                        Plugins plugin = pluginConstructor.newInstance(transformer, builder);
                        plugin.transformBuild();
                    } else {
                        PluginIgnoredClass ignoredPlugin = PluginIgnoredClass.searchByValue(builder.getNodeName());
                        if(ignoredPlugin == null) {
                            transformer.buildSteps.append("\n// Unable to convert a build step referring to \"" + builder.getNodeName() + "\". Please verify and convert manually if required.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
