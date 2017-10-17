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

import com.infostretch.labs.transformers.PublisherTransformer;
import com.infostretch.labs.transformers.Transformer;
import org.w3c.dom.Node;

/**
 * Handles transformation of Mailer Plugin properties
 *
 * @author Mohit Dharamshi
 */
public class Mailer extends Plugins {

    public Mailer(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transformPublisher() {
        transformer.setOnlyBuildTrigger(false);
        appendPublishSteps("\n\t\t// Mailer notification");
        appendPublishSteps("\n\t\tstep([$class: 'Mailer', notifyEveryUnstableBuild: " + !Boolean.valueOf(getElementByTag("dontNotifyEveryUnstableBuild").getTextContent()) + ", recipients: '" + getElementByTag("recipients").getTextContent() + "', sendToIndividuals: " + getElementByTag("sendToIndividuals").getTextContent() + "])\n");
    }
}
