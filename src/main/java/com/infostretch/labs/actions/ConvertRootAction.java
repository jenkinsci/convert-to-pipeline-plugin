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

package com.infostretch.labs.actions;

import com.infostretch.labs.utils.ActionUtil;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Handle job level convert action.
 *
 * @author Mohit Dharamshi
 */

@Extension
public class ConvertRootAction implements RootAction, Describable<ConvertRootAction> {

    @Override
    public String getIconFileName() {
        return Messages.ConvertAction_IconName();
    }

    @Override
    public String getDisplayName() {
        return Messages.ConvertRootAction_DisplayName();
    }

    @Override
    public String getUrlName() { return Messages.ConvertAction_UrlName(); }

    /**
     * Handles submit action of convert button.
     *
     * @param request StaplerRequest object from the form.
     * @param response StaplerResponse object that re-directs to newly created job.
     *
     * @throws ServletException If a servlet encounters difficulty; servlet exception is thrown.
     * @throws IOException If an input or output exception occurred.
     */
    public void doConvert(final StaplerRequest request, final StaplerResponse response) throws ServletException,
            IOException {
        if(ActionUtil.validateForm(request.getParameter("newName"), request.getParameter("sourceJob"))) {
            FreeStyleProject job = (FreeStyleProject) Jenkins.getInstance().getItemByFullName(request.getParameter("sourceJob"));
            ActionUtil actionUtil = new ActionUtil(job);
            actionUtil.doConvert(request, response);
        } else {
            response.forwardToPreviousPage(request);
        }
    }

    /**
     * List FreeStyle Jobs at root level.
     *
     * @return List of FreeStyle jobs.
     */
    public List listFreeStyleJobs() {
        return Jenkins.getInstance().getAllItems(FreeStyleProject.class);
    }

    @Override
    public Descriptor<ConvertRootAction> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Descriptor class to handle form validation.
     *
     * @author Mohit Dharamshi
     */
    @Extension
    public static final class ConvertRootActionDescriptor extends Descriptor<ConvertRootAction>{
        /**
         * Validate new name by checking if any existing job exists with same name at current level.
         *
         * @param newName Desired new name of new pipeline job.
         * @param sourceJob Full name of original FreeStyle job that will be converted.
         *
         * @return Form Validation response with error message if any.
         */
        public FormValidation doCheckNewName(@QueryParameter("newName") String newName,
                                             @QueryParameter("sourceJob") String sourceJob) {
            if(sourceJob != null && sourceJob.length()>0 && !(ActionUtil.validateForm(newName, sourceJob))) {
                return FormValidation.error(Messages.ConvertAction_JobExists()+ " '" + ActionUtil.defineName(newName, sourceJob) + "'");
            }
            return FormValidation.ok();
        }
    }
}