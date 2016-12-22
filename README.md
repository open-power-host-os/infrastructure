# OpenPOWER Host OS infrastructure as code

This repository keeps automated scripts to set up an infrastructure capable of
building OpenPOWER Host OS artifacts, i.e. packages and ISO image.

## Infrastructure description

The scripts assume you have an organization/user on GitHub with forks of
four of the OpenPOWER Host OS repositories:
- infrastructure :: this repository
- builds :: scripts to build Host OS
- versions :: packages metadata (URL, version, etc) for each Host OS release
- open-power-host-os.github.io :: web page with build summary/release notes

They will prepare a Jenkins instance capable of building Host OS on demand,
automatically building when there are pull requests to the "builds" and
"versions" repositories and generating weekly builds. The single components are
independent, so you could execute just the parts that interest you. For example,
you could execute only the Ansible playbook that prepares a host for building
Host OS or just add a single job to a pre-existing Jenkins instance using its
Groovy script.

### GitHub integration

The Jenkins instance polls GitHub for status changes on the corresponding
repositories' pull requests by using the GitHub Pull Request Builder plugin. It
will start a batch of tests whenever a new pull request is open in the "builds"
and "versions" repositories by an authorized user, when those pull requests are
updated with new commits or when an authorized user comments the phrase
"start tests". Builds and Pylint validation can be triggered independently with
the phrases "start build" and "start pylint", respectively.

### Release process

A Jenkins job will trigger periodically to update the packages' versions, create
release notes and execute a build with all packages available, by executing the
corresponding commands from the "builds" repository scripts. Commits will be
created for the "versions" and "open-power-host-os.github.io" repositories and
pushed to the designated GitHub user's repository. Ideally, this should be a
"bot" user. Pull requests for those commits can be created manually and merged
into the main organization/user repository, if desired.

Note: The default is to run those periodic builds once a week (every Wednesday
at 11 AM).

## Infrastructure setup

If the whole infrastructure setup is desired, you should do the following steps:
- Prepare CentOS system
- Execute Ansible playbooks
- Manual Jenkins steps
 - Create administrative jobs
 - Create slaves credentials
 - Create GitHub credentials
 - Create slave nodes
 - Create Host OS builds jobs
They are described in detail below.

### Prepare CentOS system

First, you'll need to prepare at least one CentOS system. Virtual machines are
recommended, but they could also be bare-metal or containers.

Note: to be able to upload the results of builds to a server and push commits to
GitHub automatically, the slaves must have be able to identify the hosts. Add
them to the "known_hosts" file by executing a simple ssh command from the slaves
to the upload server and to github.com. Alternatively, you could disable
"StrictHostKeyChecking", but that may be a security hazard.

### Execute Ansible playbooks

There are Ansible playbooks that set up the Jenkins master node and two Jenkins
slave nodes, one for Host OS builds and one for validating the code from GitHub
pull requests. There could be multiple slave nodes set up for parallel builds.
The different roles (master, builds slave and validation slave) could even be
set up in the same system, as desired.

Read the [Ansible instructions](ansible/README.md) for details on how to execute
the playbooks.

Note: The "master" playbook may fail due to Jenkins temporary unresponsiveness.
If you see HTTP request errors, try running it again.

### Manual Jenkins steps

#### Create administrative jobs

The administrative jobs will help you set up your Jenkins instance, providing a
simplified way of creating jobs, credentials and slave nodes.

If you've set up a fresh Jenkins instance, it should have a single job,
available at `https://myjenkins.com/job/seed_job`, which creates jobs based on
a git repository. When executing it, make sure the parameters are adequate for
your infrastructure.

If you are on a fresh Jenkins installation or do not yet have GitHub credentials
configured, execute the seed job with a glob that matches only the
administrative jobs (e.g. "jenkins_jobs/create_*.groovy"). This will prevent
"trigger" jobs from being created, since they would fail if executed
automatically without properly configured credentials.

#### Create slaves credentials

Create the credentials necessary to access the slave nodes:
`https://myjenkins.com/job/create_ssh_credentials`

#### Create GitHub credentials

The GitHub Pull Request Builder plugin needs read access to check out pull
requests for building and validating and write access to update the pull
requests statuses, informing the developers of the job results.

To create the credentials, you can run the job at
`https://myjenkins.com/job/create_user_pass_credentials`, passing either the
user's password or it's API token as the "password" parameter.

Refer to https://github.com/blog/1509-personal-api-tokens for information on
how to create those tokens.

Next, go to Jenkins home -> "Manage Jenkins" -> "Configure System" and look
for "GitHub Pull Request Builder". Select the credentials you've created and
set a meaningful description. The other fields do not need to be modified. You
can use the "Test Credentials" button to make sure the permissions to your
repository are correct. You'll need at least push and pull permissions.

#### Create slave nodes

The default behaviour is for administrative jobs (creating jobs, nodes and
credentials) to execute only on the master node, and remaining jobs to execute
only on slave nodes labeled "builds_slave_label" or "validation_slave_label".
You'll then need to add at least one slave node to execute the interesting jobs.
To do this, you can run the job at `https://myjenkins.com/job/create_node`. You
should have already executed the slave playbook(s) on those slaves.

#### Create Host OS builds jobs

When the credentials are configured, reexecute the seed job with the default
"JOB_DESCRIPTORS_FILES" parameter. This will create all the other jobs
configured to check out from the GitHub organization/user repositories. The
default organization is
[open-power-host-os](https://github.com/open-power-host-os/).
