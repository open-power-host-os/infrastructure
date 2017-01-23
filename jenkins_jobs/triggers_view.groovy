listView('Triggers') {
    description('Automatic triggers')
    jobs {
        name('trigger_host_os_build_from_builds_repo')
        name('trigger_host_os_build_from_versions_repo')
        name('trigger_pylint_from_builds_repo')
        name('trigger_weekly_host_os_build')
        name('trigger_nightly_host_os_build')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }
}
