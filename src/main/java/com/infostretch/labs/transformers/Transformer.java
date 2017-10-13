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
import com.infostretch.labs.utils.SCMUtil;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Transformer class is the main class that handles the conversion of
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class Transformer {
    private static final Logger logger = Logger.getLogger(Transformer.class.getName());

    private InputStream is;
    private Element flowDefinition;
    private String scmURL ="", scmCredentialsId = "", scmType = "", scmBranch = "";

    protected Document doc, dest;
    public Element jdk;
    protected NodeList buildersList;
    public boolean firstJob = true;
    public StringBuffer script, buildSteps, publishSteps;
    public String currentJobName = "", previousUrl = "", previousLabel = "";
    private Map<String, Object> requestParams;
    private List<String> copyConfigs = new ArrayList<>(Arrays.asList("description", "properties", "triggers"));
    private List<String> transformConfigs = new ArrayList<>(Arrays.asList("label", "scm", "builders", "publishers"));

    /**
     * Constructor to initialise variables required to process transformation.
     *
     * @param requestParams Map that contains request parameters.
     */
    public Transformer(Map requestParams) {
        script = new StringBuffer();
        this.requestParams = requestParams;
    }

    /**
     * Initialises transformation process of Freestyle project to Pipeline.
     */
    public void performFreeStyleTransformation() {
        appendToScript("// Powered by Infostretch \n\n");
        appendToScript("timestamps {\n");
        transformJob((FreeStyleProject) requestParams.get("initialProject"), (boolean)requestParams.get("downStream"));
        appendToScript("\n}\n}");
        appendScriptToXML((boolean) requestParams.get("commitJenkinsfile"), requestParams.get("commitMessage").toString());
        writeConfiguration();
        logger.info("Completed conversion of all jobs");
    }

    /**
     * Invokes conversion of given FreeStyle Job.
     * This method is recursively called if downstream jobs are to be converted also.
     *
     * @param item FreeStyle job to convert.
     * @param downStream Boolean to decide if item's downstream jobs are to be converted.
     */
    private void transformJob(FreeStyleProject item, boolean downStream) {
        try {
            currentJobName = item.getFullName();
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(item.getConfigFile().getFile());
            dest = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            flowDefinition = dest.createElement("flow-definition");
            dest.appendChild(flowDefinition);
            doc.getDocumentElement().normalize();
            transformFile();
            if (downStream) {
                for (Item job : item.getDownstreamProjects()) {
                    if (job instanceof FreeStyleProject) {
                        firstJob = false;
                        transformJob((FreeStyleProject) job, true);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Exception occurred in Transformer constructor: " + e.getMessage());
        }
    }

    /**
     * Returns the input stream which consists of the entire XML structure of the new pipeline job.
     *
     * @return InputStream of XML structure of the new pipeline job.
     */
    public InputStream getStream() {
        return is;
    }

    /**
     * Calls 'copy' or 'transformers' methods of various configurations of a FreeStyle job.
     */
    private void transformFile() {
        copyConfigurations(copyConfigs);
        transformConfigurations(transformConfigs);
    }

    /**
     * Calls respective methods that convert configurations.
     *
     * @param configurations List of configuration to convert.
     */
    private void transformConfigurations(List<String> configurations) {
        for (String config: configurations) {
            logger.info("Transforming configuration: " + config.toUpperCase());
            switch (config) {
                case "label":
                    transformLabel();
                    break;
                case "scm":
                    SCMTransformer scmTransformer = new SCMTransformer(this);
                    scmTransformer.convertSCM();
                    break;
                case "builders":
                    BuilderTransformer builderTransformer = new BuilderTransformer(this);
                    builderTransformer.convertBuilders();
                    break;
                case "publishers":
                    PublisherTransformer publisherTransformer = new PublisherTransformer(this);
                    publisherTransformer.convertPublishers();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Copies known configurations that are identical in pipeline jobs.
     * Plugins / configurations that cause job creation / build to fail are explicitely to removed.
     *
     * @param configurations List of configurations to copy.
     */
    private void copyConfigurations(List<String> configurations) {
        for (String configuration: configurations) {
            logger.info("Transforming configuration: " + configuration.toUpperCase());
            if (doc.getElementsByTagName(configuration).getLength() > 0) {
                Node destConfigNode = dest.importNode(doc.getElementsByTagName(configuration).item(0), true);
                if(configuration.equalsIgnoreCase("properties")) {
                    NodeList propertyChildren = destConfigNode.getChildNodes();
                    for(int i=1;i<propertyChildren.getLength();i=i+2) {
                        Node property = propertyChildren.item(i);
                        if(property.getNodeName().contains("DiskUsageProperty")) {
                            logger.info("Disk usage property found and discarded");
                            property.getParentNode().removeChild(property);
                            break;
                        }
                    }
                }
                flowDefinition.appendChild(destConfigNode);
            }
        }
    }

    /**
     * Transforms label to node block.
     */
    private void transformLabel() {
        String label = "";
        if (doc.getElementsByTagName("assignedNode").getLength() > 0) {
            label = doc.getElementsByTagName("assignedNode").item(0).getTextContent();
            if(firstJob) {
                appendToScript("\nnode ('" + label + "') { \n");
            } else {
                if(!label.equalsIgnoreCase(previousLabel)) {
                    appendToScript("\n}\nnode ('" + label + "') { \n");
                }
            }
        } else {
            if(firstJob) {
                appendToScript("\nnode () {\n");
            } else {
                if(!label.equalsIgnoreCase(previousLabel)) {
                    appendToScript("\n}\nnode () { \n");
                }
            }
        }
        previousLabel = label;
    }

    /**
     * General method to append script block with a stage wrap.
     * @param stage Name of stage to create.
     * @param block Script block to add under stage.
     */
    public void appendToScript(String stage, String block) {
        script.append("\n\tstage ('" + stage + "') {\n \t " + block + " \n\t}");
    }

    /**
     * General method to append script block to main script.
     * @param block Script block to add to main script.
     */
    public void appendToScript(String block) {
        script.append(block);
    }

    /**
     * Add Groovy Script to XML strucuture.
     * If commit to SCM is selected, script is written to Jenkinsfile and committed to SCM.
     * @param commitJenkinsfile Boolean to decide if script is to be kept inline or committed as Jenkinsfile to SCM.
     * @param commitMessage Commit message if Jenkinsfile is to be committed to SCM.
     */
    private void appendScriptToXML(boolean commitJenkinsfile, String commitMessage) {
        if(commitJenkinsfile) {
            new SCMUtil().pushJenkinsfile(script.toString(), scmURL, scmBranch, scmCredentialsId, commitMessage, scmType);
            flowDefinition.appendChild(writeCPSFlow());
        } else {
            Element definition = dest.createElement("definition");
            definition.setAttribute("class", "org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition");
            Element scriptDefinition = dest.createElement("script");
            scriptDefinition.setTextContent(script.toString());
            definition.appendChild(scriptDefinition);
            flowDefinition.appendChild(definition);
        }
    }

    /**
     * Gets Element object from Node based on tag name.
     * @param node Node object from which element is to be extracted.
     * @param tag Name of tag to extract.
     * @return Element derived from node by given tag name.
     */
    public Element getElementByTag(Node node, String tag) {
        return (Element) ((Element) node).getElementsByTagName(tag).item(0);
    }

    /**
     * Write complete transformed configuration to input stream object.
     */
    private void writeConfiguration() {
        try {
            javax.xml.transform.Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(dest), new StreamResult(outputStream));
            is = new ByteArrayInputStream(outputStream.toByteArray());
            logger.info("Transformation for job " + currentJobName + " completed successfully");
        } catch (Exception e) {
            logger.severe("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Setter method to set SCM Branch. Useful when committing Jenkinsfile to SCM.
     * @param scmBranch SCM Branch to set for committing Jenkinsfile to SCM.
     */
    public void setScmBranch(String scmBranch) {
        this.scmBranch = scmBranch;
    }

    /**
     * Setter method to set SCM URL. Useful when committing Jenkinsfile to SCM.
     * @param scmURL SCM URL to set for committing Jenkinsfile to SCM.
     */
    public void setScmURL(String scmURL) {
        this.scmURL = scmURL;
    }

    /**
     * Setter method to set SCM Type. Useful when committing Jenkinsfile to SCM.
     * @param scmType SCM Type to set for committing Jenkinsfile to SCM.
     */
    public void setScmType(String scmType) {
        this.scmType = scmType;
    }

    /**
     * Setter method to set SCM Credentials Id. Useful when committing Jenkinsfile to SCM.
     * @param scmCredentialsId SCM Credentials Id to set for committing Jenkinsfile to SCM.
     */
    public void setScmCredentialsId(String scmCredentialsId) {
        this.scmCredentialsId = scmCredentialsId;
    }

    /**
     * Write CPS Flow XML structure for SCM type defined.
     *
     * @return Element object definition with XML
     */
    private Element writeCPSFlow() {
        try {
            Class pluginClass = Plugins.getPluginClass(scmType);
            if(pluginClass != null) {
                Constructor<Plugins> pluginConstructor = pluginClass.getConstructor();
                Plugins plugin = pluginConstructor.newInstance();
                return plugin.writeCPSFlow(dest, scmURL, scmBranch, scmCredentialsId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
