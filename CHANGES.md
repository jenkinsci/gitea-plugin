# Changes

<!-- Each version newest first -->

<!-- Template:

## Version X.Y.Z (yyyy-MM-dd)

* details

-->

## Version 1.1.1 (unreleased)


## Version 1.1.0 (2019-01-17)

* Fix PR and branch links ([JENKINS-54517](https://issues.jenkins-ci.org/browse/JENKINS-54517)) 
* Switch to handy-uri Jenkins API plugin rather than bundle duplicate classes within plugin.


## Version 1.0.8 (2018-04-04)

* Use Jenkins configured proxy settings to connect to Gitea ([JENKINS-50565](https://issues.jenkins-ci.org/browse/JENKINS-50565))

## Version 1.0.7 (2018-03-22)

* Fix NPE during dynamic installation of the plugin ([JENKINS-50349](https://issues.jenkins-ci.org/browse/JENKINS-50349))

## Version 1.0.6 (2018-03-21)

* Fix NPE during dynamic installation of the plugin ([JENKINS-50319](https://issues.jenkins-ci.org/browse/JENKINS-50319))

## Version 1.0.5 (2018-03-14)

* Fix receipt of `pull_request` webhooks.
* Fix parsing of clone URLs when Gitea is publishes scp style clone URLs ([JENKINS-49768](https://issues.jenkins-ci.org/browse/JENKINS-49768))
* Misc fixes in Branch discovery strategies and pull request discovery traits

## Version 1.0.4 (2017-12-18)

* Added support for Webhook notification of repository creation / deletion now that Gitea 1.3 supports those events
* Verified branch deletion events sent by Gitea 1.3 are parsed correctly

## Version 1.0.3 (2017-10-24)

* Update to new Gitea logo

## Version 1.0.2 (2017-08-08)

* Fix Webhook notification of pushes to branches
* Add webhook notification and management of non-`SCMSource` based job types

## Version 1.0.1 (2017-07-28)

* Disable shallow clone when we know a merge will take place ([JENKINS-45771](https://issues.jenkins-ci.org/browse/JENKINS-45771))

## Version 1.0.0 (2017-07-18)

* Initial release
