plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.10"
}

gitHooks {
    preCommit {
        tasks("spotlessApply")
    }
    commitMsg {
        conventionalCommits()
    }
    createHooks()
}

rootProject.name = "EpicBanItem"
