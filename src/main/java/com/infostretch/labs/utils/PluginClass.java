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

package com.infostretch.labs.utils;

/**
 * List of Plugin class names mapped to node tags from XML.
 * 
 * @author Mohit Dharamshi
 */
public enum PluginClass {

    TestNG("hudson.plugins.testng.Publisher"),
    Git("hudson.plugins.git.GitSCM");

    private String nodeTag;

    PluginClass(String nodeTag) {
        this.nodeTag = nodeTag;
    }

    /**
     * Searches all Plugin Classes and returns the one that matches given nodeTag.
     * @param nodeTag Node tag whose Plugin Class needs to be found.
     * @return PluginClass that matches given nodeTag.
     */
    public static PluginClass searchByTag(String nodeTag) {
        for (PluginClass plugin : PluginClass.values()) {
            if (plugin.nodeTag.equals(nodeTag)) {
                return plugin;
            }
        }
        return null;
    }
}
