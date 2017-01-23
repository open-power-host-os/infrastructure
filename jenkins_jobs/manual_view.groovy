listView('Host OS') {
    description('Jobs that build OpenPOWER Host OS')
    jobs {
        name('build_host_os')
        name('build_host_os_iso')
        name('trigger_weekly_host_os_build')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
