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

/**
 * Handles transformation of Shell properties
 *
 * @author Mohit Dharamshi
 */
public class Shell extends Plugins {

    public Shell(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transformBuild() {
        appendBuildSteps("\t\t// Shell build step");
        Element unstableReturn = getElementByTag("unstableReturn");
        String unstableValue = "";
        if (unstableReturn != null && unstableReturn.getTextContent() != null) {
            unstableValue = unstableReturn.getTextContent();
        }
        String command = getElementByTag("command").getTextContent().trim();
        // If the script has a shebang it needs to be on the first line so that the shell parses it
        if (!command.startsWith("#!")) {
            // Otherwise insert a newline to make the code more readable
            command = "\n" + command;
        }
        if (unstableValue.length() > 0) {
            appendBuildSteps("\n{ \n def shellReturnStatus = sh returnStatus: true, script: '''" + command + "\n'''\n if(shellReturnStatus == " + unstableValue + ") { currentBuild.result = 'UNSTABLE' } \n}");
        } else {
            appendBuildSteps("\nsh '''" + command + " \n'''");
        }
    }
}
