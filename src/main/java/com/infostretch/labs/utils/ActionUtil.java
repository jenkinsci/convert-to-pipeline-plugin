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

import com.cloudbees.hudson.plugins.folder.Folder;
import com.infostretch.labs.transformers.Transformer;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ActionUtil is the class that provides the common logic for actions to handle the validation and conversion.
 *
 * @author Mohit Dharamshi
 */

public class ActionUtil {

    private FreeStyleProject job;

    /**
     * Constructor to initialise FreeStyleProject.
     *
     * @param job Initial job from which conversion is to start.
     */
    public ActionUtil(FreeStyleProject job) {
        this.job = job;
    }

    /**
     * Validates if desired new name of converted job does not exist at current level.
     *
     * @param newName Desired new name of the pipeline job to be created (optional).
     * @param originalJob Full name of original job that will be converted.
     *
     * @return Returns true if a job with desired new name does not exist.
     */
    public static boolean validateForm(String newName, String originalJob) {
        Item item = null;
        try {
            FreeStyleProject orgJob = (FreeStyleProject) Jenkins.getInstance().getItemByFullName(originalJob);
            if(orgJob != null) {
                newName = defineName(newName, orgJob.getName());
                if(orgJob.getParent().getClass().equals(Folder.class)) {
                    item = ((Folder) orgJob.getParent()).getItem(newName);
                }
                else {
                    item = Jenkins.getInstance().getItem(newName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return item == null;
        }
        return item == null;
    }

    /**
     * Calls Transformer class with request parameters and creates new pipeline job from XML.
     *
     * @param request StaplerRequest object from the form.
     * @param response StaplerResponse object that re-directs to newly created job.
     *
     * @throws ServletException If a servlet encounters difficulty; servlet exception is thrown.
     * @throws IOException If an input or output exception occurred.
     */
    public void doConvert(final StaplerRequest request, final StaplerResponse response) throws ServletException,
            IOException {
        try {
            String newName = request.getParameter("newName");
            boolean convertDownStream = false;
            boolean commitJenkinsfile = false;
            String commitMessage = request.getParameter("commitMessage");
            if (request.getParameter("downStream") != null && request.getParameter("downStream").equalsIgnoreCase("on")) {
                convertDownStream = true;
            }
            if (request.getParameter("commitJFile") != null && request.getParameter("commitJFile").equalsIgnoreCase("on")) {
                commitJenkinsfile = true;
            }
            if(commitJenkinsfile && commitMessage.isEmpty()) {
                commitMessage = "";
            }

            Map<String, Object> params = new HashMap();
            params.put("initialProject", job);
            params.put("downStream", convertDownStream);
            params.put("commitJenkinsfile", commitJenkinsfile);
            params.put("commitMessage", commitMessage);

            TopLevelItem newJob;
            newName = defineName(newName, job.getName());
            if (job.getParent().getClass().equals(Folder.class)) {
                Folder folder = (Folder) job.getParent();
                Transformer transformer = new Transformer(params);
                transformer.performFreeStyleTransformation();
                newJob = folder.createProjectFromXML(newName, transformer.getStream());
            } else {
                Transformer transformer = new Transformer(params);
                transformer.performFreeStyleTransformation();
                newJob = Jenkins.getInstance().createProjectFromXML(newName, transformer.getStream());
            }
            Jenkins.getInstance().reload();
            response.sendRedirect2(newJob.getAbsoluteUrl());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates new name of pipeline job if none is specified.
     *
     * @param name Desired new name of the pipeline job to be created (optional).
     * @param jobName Current job name against which to compare.
     *
     * @return A non-null value for new name of pipeline job.
     */
    public static String defineName(String name, String jobName) {
        if (name.isEmpty()) {
            name = jobName + "-pipeline";
        }
        return name;
    }
}
