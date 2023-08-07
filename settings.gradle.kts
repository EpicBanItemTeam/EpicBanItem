pluginManagement {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.9"
}

gitHooks {
    preCommit {
        from {
            """
            git diff --cached --name-only --diff-filter=ACMR | while read -r a; do echo ${'$'}(readlink -f ${"$"}a); 
            ./gradlew spotlessApply -q -PspotlessIdeHook="${'$'}(readlink -f ${"$"}a)" </dev/null; done
            """.trimIndent()
        }
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
