plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.10"
}

gitHooks {
    preCommit {
        tasks("spotlessApply spotlessCheck")
    }
    commitMsg {
        conventionalCommits()
    }
    hook("post-commit") {
        from {
            """
            files="${'$'}(git show --pretty= --name-only | tr '\n' ' ')"
            git add ${'$'}files
            git -c core.hooksPath= commit --amend -C HEAD
            """.trimIndent()
        }
    }
    createHooks(true)
}

rootProject.name = "EpicBanItem"
