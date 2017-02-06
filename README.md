# OpenPOWER Host OS infrastructure as code

This git repository keeps automated scripts to set up an infrastructure capable
of building OpenPOWER Host OS artifacts, i.e. packages and ISO image.

## Infrastructure description

The scripts assume you have an organization/user on GitHub with forks of
four of the OpenPOWER Host OS git repositories:
- infrastructure :: this repository
- builds :: build scripts
- versions :: packages metadata (source URL, version, etc) for creating builds
- open-power-host-os.github.io :: web page with build summary/release notes

They will prepare a Jenkins instance capable of:
- manually start a build
- automatically start a build when there are pull requests to the "builds" and
"versions" git repositories in GitHub
- create weekly and nightly builds, which must have some extra artifacts

The Jenkins instance is composed of at least two nodes: a master one which
centralizes administrative tasks, builds queuing, build execution status and
build results, and a slave one which executes the build itself and validates
build system source code. You may install both nodes in a single system or
even install only the slave node and use it to build via command line.

### GitHub integration

The Jenkins instance polls GitHub for status changes on the corresponding
repositories' pull requests by using the GitHub Pull Request Builder plugin. It
will start a batch of tests whenever a new pull request is open in the "builds"
and "versions" repositories by an authorized user, when those pull requests are
updated with new commits or when an authorized user comments the phrase
"start tests". Currently, tests are: building of all packages, Pylint validation
of Python code, yamllint validation of all package metadata YAML files and rpmlint
validation of all RPM packages specification files. These tests can be triggered
independently with the phrases "start build", "start pylint", "start yamllint"
and "start rpmlint" respectively.

### Periodic builds process

A Jenkins job will trigger periodically to check the versions of the packages
available in OpenPOWER Host OS GitHub organization, update the corresponding
packages' versions in the "versions" git repository, create a build with all
packages available and create release notes, by executing the corresponding
commands from the build scripts in the "builds" git repository. Commits will
be created for the local "versions" and "open-power-host-os.github.io" git
repositories and pushed to the designated GitHub user's git repository. Ideally,
this should be a "bot" user. Pull requests for those commits can be created
manually and merged into the main organization/user git repository, if desired.
The default is to execute those periodic builds once a week (every Wednesday
at 11 AM, Jenkins master's timezone).

There's another periodic job similar to the one above, which differs by executing
additional testing during the packages builds and by not pushing commits to remote
git repositories. It allows developers of the packages to have their new commits
tested and to have a build with those commits as soon as possible. The default
is to execute those periodic builds nightly (daily at 22 PM, Jenkins master's timezone).

## Infrastructure setup

If the whole infrastructure setup is desired, you should do the following steps:
- Prepare CentOS system(s)
- Setup SSH credentials used to access upload server and GitHub from Jenkins slaves
- Automatically setup Jenkins master and slave(s) using Ansible playbooks
- Manually setup Jenkins via web UI
 - Create administrative jobs
 - Setup SSH credentials used to access Jenkins slaves from Jenkins master
 - Create credentials used to access GitHub API from Jenkins
 - Create Jenkins slaves in Jenkins web UI
 - Create builds jobs

They are described in detail below.

### Prepare CentOS system(s)

First, you will need to prepare at least one CentOS system. If the Jenkins
instance will be used for testing, one system is enough; production instances
should use more systems. Virtual machines are recommended, but you could
optionally use bare-metal or containers instead. 

If you do not have a direct network connection to the system(s), you may
setup an SSH proxy in ~/.ssh/config in your workstation to make the SSH
proxy transparent. Example:

Host open-power-hostos-builds
    HostName <target_server>
    ProxyCommand ssh <myuser>@<proxy_server> nc <target_server> 22

After the SSH proxy is configured, simply use the following to connect to the system:
`ssh <myuser>@open-power-hostos-builds`

### Setup SSH credentials used to access upload server and GitHub from Jenkins slaves

To automate SSH authentication between 2 hosts, it is necessary to:
 - create an SSH key pair if you still do not have one
 - add the target host to ~/.ssh/known_hosts in the source host. It is also
   possible to do it by executing a simple SSH command from the source host to
   the target host. Alternatively, you could disable "StrictHostKeyChecking"
   for all SSH connections in the source host, but that may be a security hazard.
 - add the SSH private key to ~/.ssh/ in the source host
 - add the SSH public key to ~/.ssh/authorized_keys file in the target host

To be able to upload the results of builds to a server and push commits to
GitHub automatically, you must automate SSH authentication from Jenkins slaves
to those two servers. The private SSH key is already installed in Jenkins nodes
by Jenkins Ansible playbooks, so you do not need to do it manually.

### Automatically setup Jenkins master and slave(s) using Ansible playbooks

There are two Ansible playbooks: one that sets up a Jenkins master node and
another one that sets up a Jenkins slave node. Read the
[Ansible instructions](ansible/README.md) for details on how to execute
the playbooks.

If you wish to have a single system hosting the entire Jenkins instance, both
playbooks can be executed in the same system. Execute first the Jenkins master
playbook, stop Jenkins service (systemctl stop jenkins) and then execute the
Jenkins slave playbook.

Note: The Jenkins playbooks may fail due to network errors. If you see HTTP
request errors, try executing them again.


### Manually setup Jenkins via web UI

If you do not have a direct network connection to the system(s), you may
create an SSH tunnel in your workstation to be able to access the Jenkins
web UI, which is accessible via HTTPS. Example:

`sudo ssh -o "ProxyCommand ssh <user>@<proxy_server> -W %h:%p" -NL 443:localhost:443 root@<jenkins_master>`

<proxy_server> is the hostname or IP address of the server which will be the
intermediate (proxy) between you and the Jenkins server. While this connection
is alive, you can access then Jenkins web UI at https://localhost using a web
browser.

#### Create administrative jobs

The administrative jobs will help you set up your Jenkins instance, providing a
simplified way of creating jobs, credentials and slaves.

Execute the single Jenkins job by accessing `https://<jenkins_server>/job/seed_job/build`.
Update the JOB_DESCRIPTORS_FILES parameter with "jenkins_jobs/create_*.groovy".
This will prevent the creation of jobs which are executed automatically ("trigger"
jobs), since they would fail if executed automatically without properly configured
credentials.

#### Create credentials in Jenkins

The Credentials plugin allows you to store and manage credentials in Jenkins.
In the case of SSH credentials, the SSH key pair must have been created beforehand.
The following credentials must be created in Jenkins:

##### Setup SSH credentials used to access Jenkins slaves from master

Setup the SSH credentials necessary to access the slaves in Jenkins:
`https://<jenkins_server>/job/create_ssh_credentials`

##### Setup credentials used to access GitHub API from Jenkins slave

The GitHub Pull Request Builder plugin needs read access to check out pull
requests for building and validating and write access to update the pull
requests statuses, informing the developers of the job results.

To create the credentials, you can execute the job at
`https://<jenkins_server>/job/create_user_pass_credentials`, passing either the
user's password or its API token as the "password" parameter.

It is recommended you use an API token instead of the user's password.
Access https://github.com/settings/tokens/new to create a token, select
"repo" scope and press the "Generate token" button. Refer to
https://github.com/blog/1509-personal-api-tokens for more information on
how to create those tokens.

Next, go to Jenkins home -> "Manage Jenkins" -> "Configure System" and look
for "GitHub Pull Request Builder". Select the credentials you have created and
set a meaningful description. The other parameters values do not need to be
modified. You can use the "Test Credentials" button to make sure the permissions
to your repository are correct. You will need at least push and pull permissions.

#### Create slaves in Jenkins web UI

The default behavior is for administrative Jenkins jobs to execute only on the
master node, and remaining jobs to execute only on slave nodes labeled
"builds_slave_label" or "validation_slave_label". You will then need to add at
least one slave node to execute the non-administrative jobs. To do this, you can execute
the job at `https://<jenkins-server>/job/create_node`. Set the IP address or hostname
of the slave in IP_ADDRESS job parameter. The other job parameters values do not
need to be modified. You should have already executed the Jenkins slave playbook(s)
on those slaves.

#### Create builds jobs

When the credentials are configured, reexecute the seed job with the default
"JOB_DESCRIPTORS_FILES" parameter. This will create all the other jobs
configured to check out from the GitHub organization/user repositories.
