listView('Pipelines') {
    description('Host OS builds pipelines')
    jobs {
        name('infrastructure')
        name('builds')
        name('versions')
        name('daily')
        name('weekly')

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
