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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Strings;
import com.infostretch.labs.plugins.Plugins;
import hudson.model.Item;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

/**
 * SCMUtil provides functionality to handle Jenkinsfile operations for various SCMs.
 *
 * @author Mohit Dharamshi
 */

public class SCMUtil {
    /**
     * Public method to create Jenkinsfile from script and push to given SCM repo.
     *
     * @param script Groovy script to be written in Jenkinsfile
     * @param url URL of SCM repository where Jenkinsfile is to be pushed.
     * @param branchName Branch of SCM where Jenkinsfile is to be pushed.
     * @param credentialsId Credentials to checkout, commit and push to SCM.
     * @param commitMessage Commit message to be included in commit when pushing Jenkinsfile.
     * @param scmType Type of SCM.
     */
    public void pushJenkinsfile(String script, String url, String branchName, String credentialsId, String commitMessage, String scmType) {
        StandardUsernamePasswordCredentials credentials = getCredentials(credentialsId);
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(credentials.getUsername(),credentials.getPassword().getPlainText());
        File workSpace = new File(Jenkins.getInstance().getRootDir(),"plugins/convert-to-pipeline/ws");
        cleanWorkSpace(workSpace);
        try {
            Class pluginClass = Plugins.getPluginClass(scmType);
            if(pluginClass != null) {
                Constructor<Plugins> pluginConstructor = pluginClass.getConstructor();
                Plugins plugin = pluginConstructor.newInstance();
                plugin.pushJenkinsfile(workSpace, script, url, branchName, commitMessage, credentialsProvider);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cleanWorkSpace(workSpace);
    }

    /**
     * Writes script to Jenkinsfile and adds to workspace.
     * This is called once the repo is cloned since a repo cannot be cloned to a non-empty directory.
     *
     * @param workSpace Workspace in which the Jenkinsfile will be added.
     * @param script Groovy script to be written in Jenkinsfile.
     */
    public static void writeJenkinsfile(File workSpace, String script) {
        try {
            File jenkinsFile = new File(workSpace.getAbsolutePath()+"/Jenkinsfile");
            FileUtils.writeStringToFile(jenkinsFile, script);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes temporary workspace if it exists.
     */
    private void cleanWorkSpace(File workSpace) {
        if (workSpace.exists()) {
            try {
                FileUtils.deleteDirectory(workSpace);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get Standard username and password credentials from credentials ID.
     *
     * @param credentialId Credentials ID of repo.
     *
     * @return StandardUsernamePasswordCredentials object of matching credentialsId.
     */
    private StandardUsernamePasswordCredentials getCredentials(String credentialId) {
        if (!Strings.isNullOrEmpty(credentialId)) {
            StandardUsernamePasswordCredentials cred = CredentialsMatchers.firstOrNull(fetchAllCredentials(), CredentialsMatchers.withId(credentialId));
            if (cred != null) {
                return cred;
            }
        }
        return null;
    }

    /**
     * List of all credentials in Jenkins against which credential matching will be performed.
     *
     * @return List of all StandardUsernamePasswordCredentials in Jenkins
     */
    private List<StandardUsernamePasswordCredentials> fetchAllCredentials() {
        return com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class,
                (Item) null,
                ACL.SYSTEM,
                Collections.emptyList()
        );
    }
}
