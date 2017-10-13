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
 * Handles transformation of TestNG Plugin properties
 *
 * @author Mohit Dharamshi
 */
public class TestNG extends Plugins {
    public TestNG(Transformer transformer, Node node) {
        super(transformer,node);
    }

    @Override
    public void transformPublisher() {
        appendBuildSteps("\n\t\t// TestNG Results");
        String testNG_Params = "";
        StringBuffer testNGParams = new StringBuffer();
        Element reportFilenamePattern = getElementByTag("reportFilenamePattern");
        Element escapeTestDescp = getElementByTag("escapeTestDescp");
        Element escapeExceptionMsg = getElementByTag("escapeExceptionMsg");
        Element failureOnFailedTestConfig = getElementByTag("failureOnFailedTestConfig");
        Element showFailedBuilds = getElementByTag("showFailedBuilds");
        Element unstableSkips = getElementByTag("unstableSkips");
        Element failedSkips = getElementByTag("failedSkips");
        Element unstableFails = getElementByTag("unstableFails");
        Element failedFails = getElementByTag("failedFails");
        Element thresholdMode = getElementByTag("thresholdMode");

        if (unstableSkips != null && unstableSkips.getTextContent().length() > 0) {
            testNGParams.append("unstableSkips: " + unstableSkips.getTextContent() + ", ");
        }
        if (failedSkips != null && failedSkips.getTextContent().length() > 0) {
            testNGParams.append("failedSkips: " + failedSkips.getTextContent() + ", ");
        }
        if (unstableFails != null && unstableFails.getTextContent().length() > 0) {
            testNGParams.append("unstableFails: " + unstableFails.getTextContent() + ", ");
        }
        if (failedFails != null && failedFails.getTextContent().length() > 0) {
            testNGParams.append("failedFails: " + failedFails.getTextContent());
        }

        if (String.valueOf(testNGParams.charAt(testNGParams.length() - 1)).equals(",")) {
            testNG_Params = testNGParams.substring(0, testNGParams.length() - 1).trim();
        } else {
            testNG_Params = testNGParams.toString().trim();
        }
        if (testNG_Params.length() > 0) {
            appendBuildSteps("\n\t\tstep([$class: 'Publisher', escapeExceptionMsg: " + escapeExceptionMsg.getTextContent() + ", escapeTestDescp: " + escapeTestDescp.getTextContent() + ", failureOnFailedTestConfig: " + failureOnFailedTestConfig.getTextContent() + ", reportFilenamePattern: '" + reportFilenamePattern.getTextContent() + "', showFailedBuilds: " + showFailedBuilds.getTextContent() + ", thresholdMode: " + thresholdMode.getTextContent() + ", " + testNG_Params + "])");
        } else {
            appendBuildSteps("\n\t\tstep([$class: 'Publisher', escapeExceptionMsg: " + escapeExceptionMsg.getTextContent() + ", escapeTestDescp: " + escapeTestDescp.getTextContent() + ", failureOnFailedTestConfig: " + failureOnFailedTestConfig.getTextContent() + ", reportFilenamePattern: '" + reportFilenamePattern.getTextContent() + "', showFailedBuilds: " + showFailedBuilds.getTextContent() + ", thresholdMode: " + thresholdMode.getTextContent() + "])");
        }
    }
}
