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
import com.infostretch.labs.utils.PluginClass;
import com.infostretch.labs.utils.PluginIgnoredClass;
import org.apache.commons.text.WordUtils;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;

/**
 * SCMTransformer handles the conversion of SCMs in
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class SCMTransformer {
    
    private Transformer transformer;
    private Node scm;

    /**
     * Initialise local Transformer variable. This constructor is called in main Transformer class.
     *
     * @param transformer Transformer object to be assigned to local Transformer variable.
     */
    protected SCMTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Calls respective convert SCM methods to convert FreeStyle Job SCM Configurations.
     */
    protected void convertSCM() {
        if (transformer.doc.getElementsByTagName("scm").getLength() > 0) {
            scm = transformer.doc.getElementsByTagName("scm").item(0);
            String scmType = scm.getAttributes().getNamedItem("class").getTextContent();
            try {
                Class pluginClass = Plugins.getPluginClass(scmType);
                if(pluginClass != null) {
                    Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, Node.class);
                    Plugins plugin = pluginConstructor.newInstance(transformer, scm);
                    plugin.transformSCM();
                } else {
                    PluginIgnoredClass ignoredPlugin = PluginIgnoredClass.searchByValue(scmType);
                    if(ignoredPlugin == null) {
                        transformer.appendToScript(transformer.currentJobName+ " - Checkout", "\n// Unable to convert SCM referring to \"" + scmType + "\". Please verify and convert manually if required.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(transformer.firstJob) {
                transformer.setScmType(scmType);
            }
        }
    }
}
